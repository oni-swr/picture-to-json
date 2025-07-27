package com.picturetojson.service.ocr;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Interface for OCR engines that can extract text from images
 */
public interface OcrEngine {
    
    /**
     * Extract text from image file
     */
    String extractTextFromImage(File imageFile) throws Exception;
    
    /**
     * Extract text from BufferedImage
     */
    String extractTextFromImage(BufferedImage image) throws Exception;
    
    /**
     * Get confidence estimate for the last OCR operation
     */
    int getConfidence();
    
    /**
     * Get the engine type identifier
     */
    String getEngineType();
    
    /**
     * Check if the engine is properly configured and available
     */
    boolean isAvailable();
}