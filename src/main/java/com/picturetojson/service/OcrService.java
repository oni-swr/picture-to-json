package com.picturetojson.service;

import com.picturetojson.service.ocr.GoogleVisionOcrEngine;
import com.picturetojson.service.ocr.OcrEngine;
import com.picturetojson.service.ocr.TesseractOcrEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service
public class OcrService {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    
    private final TesseractOcrEngine tesseractEngine;
    private final GoogleVisionOcrEngine googleVisionEngine;
    private final TextAnalysisService textAnalysisService;
    private final boolean autoDetectHandwriting;
    private final String defaultEngine;
    
    public OcrService(TesseractOcrEngine tesseractEngine,
                     GoogleVisionOcrEngine googleVisionEngine,
                     TextAnalysisService textAnalysisService,
                     @Value("${app.ocr.handwriting.auto-detect:true}") boolean autoDetectHandwriting,
                     @Value("${app.ocr.default-engine:TESSERACT}") String defaultEngine) {
        this.tesseractEngine = tesseractEngine;
        this.googleVisionEngine = googleVisionEngine;
        this.textAnalysisService = textAnalysisService;
        this.autoDetectHandwriting = autoDetectHandwriting;
        this.defaultEngine = defaultEngine;
        
        logger.info("OCR Service initialized - Auto-detect handwriting: {}, Default engine: {}", 
            autoDetectHandwriting, defaultEngine);
        logger.info("Available OCR engines - Tesseract: {}, Google Vision: {}", 
            tesseractEngine.isAvailable(), googleVisionEngine.isAvailable());
    }
    
    /**
     * Extract text from image file using appropriate OCR engine
     */
    public String extractTextFromImage(File imageFile) throws Exception {
        try {
            logger.debug("Extracting text from image: {}", imageFile.getName());
            
            OcrEngine engine = selectOcrEngine(imageFile);
            String result = engine.extractTextFromImage(imageFile);
            
            logger.debug("OCR extraction completed for: {} using engine: {}", 
                imageFile.getName(), engine.getEngineType());
            
            return result != null ? result.trim() : "";
        } catch (Exception e) {
            logger.error("OCR failed for file: {}", imageFile.getName(), e);
            throw e;
        }
    }
    
    /**
     * Extract text from BufferedImage using appropriate OCR engine
     */
    public String extractTextFromImage(BufferedImage image) throws Exception {
        try {
            logger.debug("Extracting text from BufferedImage");
            
            OcrEngine engine = selectOcrEngine(image);
            String result = engine.extractTextFromImage(image);
            
            logger.debug("OCR extraction completed from BufferedImage using engine: {}", 
                engine.getEngineType());
            
            return result != null ? result.trim() : "";
        } catch (Exception e) {
            logger.error("OCR failed for BufferedImage", e);
            throw e;
        }
    }
    
    /**
     * Extract text using specific OCR engine
     */
    public String extractTextFromImage(File imageFile, String engineType) throws Exception {
        OcrEngine engine = getEngineByType(engineType);
        return engine.extractTextFromImage(imageFile);
    }
    
    /**
     * Extract text using specific OCR engine
     */
    public String extractTextFromImage(BufferedImage image, String engineType) throws Exception {
        OcrEngine engine = getEngineByType(engineType);
        return engine.extractTextFromImage(image);
    }
    
    /**
     * Get basic confidence estimate from the last used engine
     */
    public int getBasicConfidence() {
        // Return confidence from the default engine
        return getEngineByType(defaultEngine).getConfidence();
    }
    
    /**
     * Select the appropriate OCR engine based on image analysis or configuration
     */
    private OcrEngine selectOcrEngine(File imageFile) {
        if (autoDetectHandwriting) {
            try {
                boolean isHandwriting = textAnalysisService.isHandwritingDetected(imageFile);
                if (isHandwriting && googleVisionEngine.isAvailable()) {
                    logger.debug("Handwriting detected, using Google Vision OCR for: {}", imageFile.getName());
                    return googleVisionEngine;
                }
            } catch (Exception e) {
                logger.warn("Failed to analyze text type for {}, using default engine", imageFile.getName(), e);
            }
        }
        
        // Fall back to configured default engine or Tesseract
        return getEngineByType(defaultEngine);
    }
    
    /**
     * Select the appropriate OCR engine based on image analysis or configuration
     */
    private OcrEngine selectOcrEngine(BufferedImage image) {
        if (autoDetectHandwriting) {
            try {
                boolean isHandwriting = textAnalysisService.isHandwritingDetected(image);
                if (isHandwriting && googleVisionEngine.isAvailable()) {
                    logger.debug("Handwriting detected, using Google Vision OCR for BufferedImage");
                    return googleVisionEngine;
                }
            } catch (Exception e) {
                logger.warn("Failed to analyze text type for BufferedImage, using default engine", e);
            }
        }
        
        // Fall back to configured default engine or Tesseract
        return getEngineByType(defaultEngine);
    }
    
    /**
     * Get OCR engine by type
     */
    private OcrEngine getEngineByType(String engineType) {
        switch (engineType.toUpperCase()) {
            case "GOOGLE_VISION":
                if (googleVisionEngine.isAvailable()) {
                    return googleVisionEngine;
                }
                logger.warn("Google Vision OCR not available, falling back to Tesseract");
                return tesseractEngine;
            case "TESSERACT":
            default:
                return tesseractEngine;
        }
    }
    
    /**
     * Check if handwriting recognition is available
     */
    public boolean isHandwritingRecognitionAvailable() {
        return googleVisionEngine.isAvailable();
    }
    
    /**
     * Get available OCR engines
     */
    public String[] getAvailableEngines() {
        if (googleVisionEngine.isAvailable()) {
            return new String[]{"TESSERACT", "GOOGLE_VISION"};
        }
        return new String[]{"TESSERACT"};
    }
}