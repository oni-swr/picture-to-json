package com.picturetojson.controller;

import com.picturetojson.dto.CorrectionRequestDto;
import com.picturetojson.dto.DocumentResponseDto;
import com.picturetojson.entity.Document;
import com.picturetojson.service.DocumentProcessingService;
import com.picturetojson.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/documents")
@Tag(name = "Document Processing", description = "Document upload and processing endpoints")
public class DocumentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    private final DocumentProcessingService documentProcessingService;
    private final OcrService ocrService;
    
    public DocumentController(DocumentProcessingService documentProcessingService, OcrService ocrService) {
        this.documentProcessingService = documentProcessingService;
        this.ocrService = ocrService;
    }
    
    @PostMapping("/upload")
    @Operation(summary = "Upload a document for processing")
    public ResponseEntity<DocumentResponseDto> uploadDocument(
            @Parameter(description = "Document file (PNG, JPG, JPEG, PDF)")
            @RequestParam("file") MultipartFile file) {
        
        try {
            logger.info("Received file upload request: {}", file.getOriginalFilename());
            DocumentResponseDto response = documentProcessingService.uploadDocument(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error uploading document", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }
    
    @PostMapping("/{id}/process")
    @Operation(summary = "Start processing a uploaded document")
    public ResponseEntity<String> processDocument(
            @Parameter(description = "Document ID")
            @PathVariable Long id) {
        
        try {
            logger.info("Starting document processing for ID: {}", id);
            documentProcessingService.processDocumentAsync(id);
            return ResponseEntity.ok("Document processing started");
        } catch (Exception e) {
            logger.error("Error starting document processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error starting document processing: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch/upload")
    @Operation(summary = "Upload multiple documents for batch processing")
    public ResponseEntity<List<DocumentResponseDto>> uploadBatch(
            @Parameter(description = "Multiple document files")
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            logger.info("Received batch upload request for {} files", files.length);
            
            List<DocumentResponseDto> responses = new java.util.ArrayList<>();
            for (MultipartFile file : files) {
                DocumentResponseDto response = documentProcessingService.uploadDocument(file);
                responses.add(response);
            }
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            logger.error("Error uploading batch documents", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }
    
    @PostMapping("/batch/process")
    @Operation(summary = "Process multiple documents in batch")
    public ResponseEntity<String> processBatch(
            @Parameter(description = "List of document IDs to process")
            @RequestBody List<Long> documentIds) {
        
        try {
            logger.info("Starting batch processing for {} documents", documentIds.size());
            CompletableFuture<List<DocumentResponseDto>> future = 
                documentProcessingService.processBatchAsync(documentIds);
            
            return ResponseEntity.ok("Batch processing started for " + documentIds.size() + " documents");
        } catch (Exception e) {
            logger.error("Error starting batch processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error starting batch processing: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<DocumentResponseDto> getDocument(
            @Parameter(description = "Document ID")
            @PathVariable Long id) {
        
        try {
            DocumentResponseDto document = documentProcessingService.getDocumentById(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            logger.error("Error retrieving document", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        }
    }
    
    @GetMapping
    @Operation(summary = "Get all documents with pagination")
    public ResponseEntity<Page<DocumentResponseDto>> getAllDocuments(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<DocumentResponseDto> documents = documentProcessingService.getAllDocuments(pageable);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("Error retrieving documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by processing status")
    public ResponseEntity<List<DocumentResponseDto>> getDocumentsByStatus(
            @Parameter(description = "Processing status")
            @PathVariable Document.ProcessingStatus status) {
        
        try {
            List<DocumentResponseDto> documents = documentProcessingService.getDocumentsByStatus(status);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            logger.error("Error retrieving documents by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    @PutMapping("/{id}/correct")
    @Operation(summary = "Apply manual corrections to extracted JSON")
    public ResponseEntity<DocumentResponseDto> correctDocument(
            @Parameter(description = "Document ID")
            @PathVariable Long id,
            @Valid @RequestBody CorrectionRequestDto correctionRequest) {
        
        try {
            logger.info("Applying corrections to document ID: {}", id);
            DocumentResponseDto document = documentProcessingService.applyCorrections(
                id, correctionRequest.getCorrectedJson());
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            logger.error("Error applying corrections", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null);
        }
    }
    
    @GetMapping("/ocr/engines")
    @Operation(summary = "Get available OCR engines")
    public ResponseEntity<String[]> getAvailableOcrEngines() {
        try {
            String[] engines = ocrService.getAvailableEngines();
            return ResponseEntity.ok(engines);
        } catch (Exception e) {
            logger.error("Error retrieving available OCR engines", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    
    @GetMapping("/ocr/handwriting/available")
    @Operation(summary = "Check if handwriting recognition is available")
    public ResponseEntity<Boolean> isHandwritingRecognitionAvailable() {
        try {
            boolean available = ocrService.isHandwritingRecognitionAvailable();
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            logger.error("Error checking handwriting recognition availability", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(false);
        }
    }
}