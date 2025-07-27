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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OCR Engine implementation using Tesseract for printed text recognition
 */
@Component
public class TesseractOcrEngine implements OcrEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(TesseractOcrEngine.class);
    
    private final ITesseract tesseract;
    private String primaryLanguage;
    private List<String> additionalLanguages;
    private int lastConfidence = 85; // Default confidence
    
    public TesseractOcrEngine(@Value("${app.ocr.tesseract.data-path}") String tessDataPath,
                             @Value("${app.ocr.tesseract.language}") String language,
                             @Value("${app.ocr.tesseract.additional-languages:#{null}}") List<String> additionalLanguages) {
        this.tesseract = new Tesseract();
        this.primaryLanguage = language;
        this.additionalLanguages = new ArrayList<>(additionalLanguages != null ? additionalLanguages : List.of());
        
        // Configure Tesseract
        if (new File(tessDataPath).exists()) {
            tesseract.setDatapath(tessDataPath);
        }
        
        // Set up language configuration
        String languages = buildLanguageString();
        tesseract.setLanguage(languages);
        
        // OCR configuration for better form recognition
        tesseract.setOcrEngineMode(1); // Use LSTM OCR Engine Mode
        tesseract.setPageSegMode(1); // Automatic page segmentation with OSD
        
        logger.info("Tesseract OCR Engine initialized with languages: {} and data path: {}", languages, tessDataPath);
    }
    
    /**
     * Build language string for Tesseract configuration
     * Combines primary language with additional languages
     * @return Language string (e.g., "eng" or "eng+deu+fra")
     */
    private String buildLanguageString() {
        if (additionalLanguages.isEmpty()) {
            return primaryLanguage;
        }
        
        // Combine primary and additional languages
        String combined = primaryLanguage + "+" + 
            additionalLanguages.stream()
                .filter(lang -> !lang.equals(primaryLanguage)) // Avoid duplicates
                .collect(Collectors.joining("+"));
        
        return combined;
    }
    
    /**
     * Set language for OCR processing
     * @param language Primary language code (e.g., "eng", "deu")
     * @param additionalLanguages Additional languages for multi-language support
     */
    public void setLanguage(String language, List<String> additionalLanguages) {
        List<String> languages = additionalLanguages != null ? additionalLanguages : List.of();
        String languageString = language;
        
        if (!languages.isEmpty()) {
            languageString = language + "+" + 
                languages.stream()
                    .filter(lang -> !lang.equals(language))
                    .collect(Collectors.joining("+"));
        }
        
        // Update internal tracking
        this.primaryLanguage = language;
        this.additionalLanguages.clear();
        this.additionalLanguages.addAll(languages);
        
        tesseract.setLanguage(languageString);
        logger.debug("Updated Tesseract language to: {}", languageString);
    }
    
    /**
     * Get current language configuration
     * @return Current language string
     */
    public String getCurrentLanguages() {
        return buildLanguageString();
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