package com.picturetojson.service.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * OCR Engine implementation using Tesseract for printed text recognition
 */
@Component
public class TesseractOcrEngine implements OcrEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrEngine.class);
    
    private final ITesseract tesseract;
    private int lastConfidence = 85; // Default confidence
    
    public TesseractOcrEngine(@Value("${app.ocr.tesseract.data-path}") String tessDataPath,
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
        
        logger.info("Tesseract OCR Engine initialized with language: {} and data path: {}", language, tessDataPath);
    }
    
    @Override
    public String extractTextFromImage(File imageFile) throws Exception {
        try {
            logger.debug("Extracting text from image using Tesseract: {}", imageFile.getName());
            String result = tesseract.doOCR(imageFile);
            logger.debug("Tesseract OCR extraction completed for: {}", imageFile.getName());
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            logger.error("Tesseract OCR failed for file: {}", imageFile.getName(), e);
            throw e;
        }
    }
    
    @Override
    public String extractTextFromImage(BufferedImage image) throws Exception {
        try {
            logger.debug("Extracting text from BufferedImage using Tesseract");
            String result = tesseract.doOCR(image);
            logger.debug("Tesseract OCR extraction completed from BufferedImage");
            return result != null ? result.trim() : "";
        } catch (TesseractException e) {
            logger.error("Tesseract OCR failed for BufferedImage", e);
            throw e;
        }
    }
    
    @Override
    public int getConfidence() {
        return lastConfidence;
    }
    
    @Override
    public String getEngineType() {
        return "TESSERACT";
    }
    
    @Override
    public boolean isAvailable() {
        return tesseract != null;
    }
}