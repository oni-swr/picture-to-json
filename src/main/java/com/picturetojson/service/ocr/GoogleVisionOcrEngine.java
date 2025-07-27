package com.picturetojson.service.ocr;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * OCR Engine implementation using Google Cloud Vision API for handwriting recognition
 */
@Component
public class GoogleVisionOcrEngine implements OcrEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(GoogleVisionOcrEngine.class);
    
    private final boolean enabled;
    private final String credentialsPath;
    private final List<String> languageHints;
    private int lastConfidence = 85; // Default confidence
    
    public GoogleVisionOcrEngine(@Value("${app.ocr.google-vision.enabled:false}") boolean enabled,
                                @Value("${app.ocr.google-vision.credentials-path:}") String credentialsPath,
                                @Value("${app.ocr.google-vision.language-hints:#{{'en'}}}") List<String> languageHints) {
        this.enabled = enabled;
        this.credentialsPath = credentialsPath;
        this.languageHints = new ArrayList<>(languageHints != null ? languageHints : List.of("en"));
        
        if (enabled) {
            logger.info("Google Vision OCR Engine initialized with credentials path: {} and language hints: {}", 
                credentialsPath, this.languageHints);
        } else {
            logger.info("Google Vision OCR Engine disabled in configuration");
        }
    }
    
    @Override
    public String extractTextFromImage(File imageFile) throws Exception {
        if (!isAvailable()) {
            throw new RuntimeException("Google Vision OCR Engine is not available");
        }
        
        try {
            logger.debug("Extracting text from image using Google Vision: {}", imageFile.getName());
            
            // Read image file
            byte[] data = Files.readAllBytes(imageFile.toPath());
            ByteString imgBytes = ByteString.copyFrom(data);
            
            String result = performOcr(imgBytes);
            logger.debug("Google Vision OCR extraction completed for: {}", imageFile.getName());
            
            return result;
        } catch (Exception e) {
            logger.error("Google Vision OCR failed for file: {}", imageFile.getName(), e);
            throw e;
        }
    }
    
    @Override
    public String extractTextFromImage(BufferedImage image) throws Exception {
        if (!isAvailable()) {
            throw new RuntimeException("Google Vision OCR Engine is not available");
        }
        
        try {
            logger.debug("Extracting text from BufferedImage using Google Vision");
            
            // Convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            ByteString imgBytes = ByteString.copyFrom(baos.toByteArray());
            
            String result = performOcr(imgBytes);
            logger.debug("Google Vision OCR extraction completed from BufferedImage");
            
            return result;
        } catch (Exception e) {
            logger.error("Google Vision OCR failed for BufferedImage", e);
            throw e;
        }
    }
    
    private String performOcr(ByteString imgBytes) throws IOException {
        // Initialize client
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
            // Build the image
            Image img = Image.newBuilder().setContent(imgBytes).build();
            
            // Create the feature for handwriting detection
            Feature feature = Feature.newBuilder()
                .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                .build();
            
            // Create image context with language hints for better recognition
            ImageContext.Builder contextBuilder = ImageContext.newBuilder();
            if (!languageHints.isEmpty()) {
                contextBuilder.addAllLanguageHints(languageHints);
                logger.debug("Using language hints for Google Vision: {}", languageHints);
            }
            
            // Create the request
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(img)
                .setImageContext(contextBuilder.build())
                .build();
            
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            
            // Perform the request
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            
            StringBuilder text = new StringBuilder();
            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    logger.error("Google Vision API error: {}", res.getError().getMessage());
                    throw new RuntimeException("Google Vision API error: " + res.getError().getMessage());
                }
                
                // Extract text
                if (res.hasFullTextAnnotation()) {
                    text.append(res.getFullTextAnnotation().getText());
                }
            }
            
            return text.toString().trim();
        }
    }
    
    @Override
    public int getConfidence() {
        return lastConfidence;
    }
    
    @Override
    public String getEngineType() {
        return "GOOGLE_VISION";
    }
    
    @Override
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }
        
        // Check if credentials are configured
        if (credentialsPath != null && !credentialsPath.isEmpty()) {
            return new File(credentialsPath).exists();
        }
        
        // Check if default credentials are available (environment variable)
        String googleCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        return googleCredentials != null && !googleCredentials.isEmpty();
    }
    
    /**
     * Set language hints for Google Vision OCR
     * @param languageHints List of language codes (e.g., ["en", "de", "fr"])
     */
    public void setLanguageHints(List<String> languageHints) {
        if (languageHints != null && !languageHints.isEmpty()) {
            this.languageHints.clear();
            this.languageHints.addAll(languageHints);
            logger.debug("Updated Google Vision language hints to: {}", this.languageHints);
        }
    }
    
    /**
     * Get current language hints
     * @return Current language hints
     */
    public List<String> getLanguageHints() {
        return new ArrayList<>(languageHints);
    }
}