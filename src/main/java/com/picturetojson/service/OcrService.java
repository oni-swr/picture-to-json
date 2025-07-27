package com.picturetojson.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;

@Service
public class OcrService {
    
    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);
    
    private final ITesseract tesseract;
    
    public OcrService(@Value("${app.ocr.tesseract.data-path}") String tessDataPath,
                     @Value("${app.ocr.tesseract.language}") String language) {
        this.tesseract = new Tesseract();
        
        // Configure Tesseract
        if (new File(tessDataPath).exists()) {
            tesseract.setDatapath(tessDataPath);
        }
        tesseract.setLanguage(language);
        
        // OCR configuration for better form recognition
        tesseract.setOcrEngineMode(1); // Use LSTM OCR Engine Mode
        tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
        
        logger.info("OCR Service initialized with language: {} and data path: {}", language, tessDataPath);
    }
    
    /**
     * Extract text from image file
     */
    public String extractTextFromImage(File imageFile) throws TesseractException {
        try {
            logger.debug("Extracting text from image: {}", imageFile.getName());
            String result = tesseract.doOCR(imageFile);
            logger.debug("OCR extraction completed for: {}", imageFile.getName());
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            logger.error("OCR failed for file: {}", imageFile.getName(), e);
            throw e;
        }
    }
    
    /**
     * Extract text from BufferedImage
     */
    public String extractTextFromImage(BufferedImage image) throws TesseractException {
        try {
            logger.debug("Extracting text from BufferedImage");
            String result = tesseract.doOCR(image);
            logger.debug("OCR extraction completed from BufferedImage");
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            logger.error("OCR failed for BufferedImage", e);
            throw e;
        }
    }
    
    /**
     * Get basic confidence estimate (simplified implementation)
     */
    public int getBasicConfidence() {
        // Return a default confidence value for now
        // In a full implementation, this would analyze the OCR results
        return 85;
    }
}