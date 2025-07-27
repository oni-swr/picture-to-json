package com.picturetojson.service;

import com.picturetojson.dto.DocumentResponseDto;
import com.picturetojson.entity.Document;
import com.picturetojson.repository.DocumentRepository;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class DocumentProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    
    private final DocumentRepository documentRepository;
    private final OcrService ocrService;
    private final ImageProcessingService imageProcessingService;
    private final JsonGenerationService jsonGenerationService;
    private final String uploadDirectory;
    
    public DocumentProcessingService(DocumentRepository documentRepository,
                                   OcrService ocrService,
                                   ImageProcessingService imageProcessingService,
                                   JsonGenerationService jsonGenerationService,
                                   @Value("${app.upload.directory}") String uploadDirectory) {
        this.documentRepository = documentRepository;
        this.ocrService = ocrService;
        this.imageProcessingService = imageProcessingService;
        this.jsonGenerationService = jsonGenerationService;
        this.uploadDirectory = uploadDirectory;
        
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(uploadDirectory));
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", uploadDirectory, e);
        }
    }
    
    /**
     * Upload and save document for processing
     */
    public DocumentResponseDto uploadDocument(MultipartFile file) throws IOException {
        logger.info("Uploading document: {}", file.getOriginalFilename());
        
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDirectory, filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create document entity
        Document document = new Document(
            filename,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            filePath.toString()
        );
        
        document = documentRepository.save(document);
        logger.info("Document uploaded and saved with ID: {}", document.getId());
        
        return new DocumentResponseDto(document);
    }
    
    /**
     * Process single document asynchronously
     */
    @Async
    public CompletableFuture<DocumentResponseDto> processDocumentAsync(Long documentId) {
        logger.info("Starting async processing for document ID: {}", documentId);
        
        try {
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
            
            document.setStatus(Document.ProcessingStatus.PROCESSING);
            document.setProcessingProgress(10);
            document = documentRepository.save(document);
            
            // Process the document
            processDocument(document);
            
            return CompletableFuture.completedFuture(new DocumentResponseDto(document));
        } catch (Exception e) {
            logger.error("Error processing document ID: {}", documentId, e);
            
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document != null) {
                document.setStatus(Document.ProcessingStatus.FAILED);
                document.setErrorMessage(e.getMessage());
                documentRepository.save(document);
            }
            
            throw new RuntimeException("Document processing failed", e);
        }
    }
    
    /**
     * Process multiple documents in batch
     */
    @Async
    public CompletableFuture<List<DocumentResponseDto>> processBatchAsync(List<Long> documentIds) {
        logger.info("Starting batch processing for {} documents", documentIds.size());
        
        List<DocumentResponseDto> results = documentIds.stream()
            .map(this::processDocumentSync)
            .collect(Collectors.toList());
        
        logger.info("Batch processing completed for {} documents", documentIds.size());
        return CompletableFuture.completedFuture(results);
    }
    
    /**
     * Process document synchronously
     */
    private DocumentResponseDto processDocumentSync(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
            
            processDocument(document);
            return new DocumentResponseDto(document);
        } catch (Exception e) {
            logger.error("Error processing document ID: {}", documentId, e);
            throw new RuntimeException("Document processing failed", e);
        }
    }
    
    /**
     * Core document processing logic
     */
    private void processDocument(Document document) throws IOException, TesseractException {
        logger.debug("Processing document: {}", document.getFilename());
        
        File file = new File(document.getFilePath());
        if (!file.exists()) {
            throw new IOException("File not found: " + document.getFilePath());
        }
        
        // Update progress
        document.setProcessingProgress(20);
        documentRepository.save(document);
        
        String extractedText;
        
        if (document.getContentType().equals("application/pdf")) {
            extractedText = processPdfDocument(file, document);
        } else {
            extractedText = processImageDocument(file, document);
        }
        
        // Update progress
        document.setProcessingProgress(70);
        documentRepository.save(document);
        
        // Generate JSON from extracted text
        String extractedJson = jsonGenerationService.generateJsonFromText(extractedText);
        
        // Update document with results
        document.setExtractedText(extractedText);
        document.setExtractedJson(extractedJson);
        document.setStatus(Document.ProcessingStatus.COMPLETED);
        document.setProcessingProgress(100);
        
        documentRepository.save(document);
        logger.info("Document processing completed: {}", document.getFilename());
    }
    
    /**
     * Process PDF document
     */
    private String processPdfDocument(File file, Document document) throws IOException, TesseractException {
        logger.debug("Processing PDF document: {}", file.getName());
        
        StringBuilder extractedText = new StringBuilder();
        
        try (PDDocument pdDocument = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            
            for (int page = 0; page < pdDocument.getNumberOfPages(); page++) {
                // Update progress
                int progress = 30 + (page * 30 / pdDocument.getNumberOfPages());
                document.setProcessingProgress(progress);
                documentRepository.save(document);
                
                // Render page as image
                BufferedImage image = renderer.renderImageWithDPI(page, 300);
                
                // Preprocess image
                BufferedImage processedImage = imageProcessingService.preprocessImage(
                    saveBufferedImageToTempFile(image)
                );
                
                // Extract text using OCR
                String pageText = ocrService.extractTextFromImage(processedImage);
                extractedText.append(pageText).append("\n");
            }
        }
        
        return extractedText.toString().trim();
    }
    
    /**
     * Process image document
     */
    private String processImageDocument(File file, Document document) throws IOException, TesseractException {
        logger.debug("Processing image document: {}", file.getName());
        
        // Update progress
        document.setProcessingProgress(30);
        documentRepository.save(document);
        
        // Preprocess image
        BufferedImage processedImage = imageProcessingService.preprocessImage(file);
        
        // Update progress
        document.setProcessingProgress(50);
        documentRepository.save(document);
        
        // Extract text using OCR
        return ocrService.extractTextFromImage(processedImage);
    }
    
    /**
     * Apply manual corrections to document
     */
    public DocumentResponseDto applyCorrections(Long documentId, String correctedJson) {
        logger.info("Applying corrections to document ID: {}", documentId);
        
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
        
        // Validate JSON
        if (!jsonGenerationService.isValidJson(correctedJson)) {
            throw new RuntimeException("Invalid JSON format");
        }
        
        document.setCorrectedJson(correctedJson);
        document.setStatus(Document.ProcessingStatus.CORRECTED);
        document = documentRepository.save(document);
        
        logger.info("Corrections applied to document ID: {}", documentId);
        return new DocumentResponseDto(document);
    }
    
    /**
     * Get all documents with pagination
     */
    public Page<DocumentResponseDto> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
            .map(DocumentResponseDto::new);
    }
    
    /**
     * Get document by ID
     */
    public DocumentResponseDto getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        return new DocumentResponseDto(document);
    }
    
    /**
     * Get documents by status
     */
    public List<DocumentResponseDto> getDocumentsByStatus(Document.ProcessingStatus status) {
        return documentRepository.findByStatus(status).stream()
            .map(DocumentResponseDto::new)
            .collect(Collectors.toList());
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isValidContentType(contentType)) {
            throw new RuntimeException("Unsupported file type: " + contentType);
        }
        
        // Check file size (50MB limit)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 50MB limit");
        }
    }
    
    /**
     * Check if content type is supported
     */
    private boolean isValidContentType(String contentType) {
        return contentType.equals("image/png") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("application/pdf");
    }
    
    /**
     * Save BufferedImage to temporary file
     */
    private File saveBufferedImageToTempFile(BufferedImage image) throws IOException {
        File tempFile = File.createTempFile("temp_image_", ".png");
        javax.imageio.ImageIO.write(image, "png", tempFile);
        return tempFile;
    }
}