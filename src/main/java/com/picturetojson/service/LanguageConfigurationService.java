package com.picturetojson.service;

import com.picturetojson.service.ocr.GoogleVisionOcrEngine;
import com.picturetojson.service.ocr.TesseractOcrEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for managing OCR language configuration
 */
@Service
public class LanguageConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageConfigurationService.class);
    
    private final TesseractOcrEngine tesseractEngine;
    private final GoogleVisionOcrEngine googleVisionEngine;
    private final String defaultPreferredLanguage;
    
    // Language mapping between ISO codes and Tesseract/Google Vision codes
    private static final Map<String, String> TESSERACT_LANGUAGE_MAP = Map.of(
        "en", "eng",
        "de", "deu", 
        "fr", "fra",
        "es", "spa",
        "it", "ita",
        "pt", "por",
        "nl", "nld",
        "ru", "rus"
    );
    
    private static final Map<String, String> GOOGLE_VISION_LANGUAGE_MAP = Map.of(
        "en", "en",
        "de", "de",
        "fr", "fr", 
        "es", "es",
        "it", "it",
        "pt", "pt",
        "nl", "nl",
        "ru", "ru"
    );
    
    @Autowired
    public LanguageConfigurationService(TesseractOcrEngine tesseractEngine,
                                      GoogleVisionOcrEngine googleVisionEngine,
                                      @Value("${app.ocr.preferred-language:en}") String defaultPreferredLanguage) {
        this.tesseractEngine = tesseractEngine;
        this.googleVisionEngine = googleVisionEngine;
        this.defaultPreferredLanguage = defaultPreferredLanguage;
        
        logger.info("Language Configuration Service initialized with default language: {}", defaultPreferredLanguage);
    }
    
    /**
     * Set language for both OCR engines
     * @param language Primary language code (ISO format: en, de, fr, etc.)
     * @param additionalLanguages Additional languages for multi-language support
     */
    public void setLanguage(String language, List<String> additionalLanguages) {
        logger.info("Setting OCR language to: {} with additional languages: {}", language, additionalLanguages);
        
        // Configure Tesseract
        String tesseractLang = TESSERACT_LANGUAGE_MAP.getOrDefault(language, "eng");
        List<String> tesseractAdditionalLangs = additionalLanguages.stream()
            .map(lang -> TESSERACT_LANGUAGE_MAP.getOrDefault(lang, lang))
            .toList();
        
        tesseractEngine.setLanguage(tesseractLang, tesseractAdditionalLangs);
        
        // Configure Google Vision
        List<String> googleVisionLangs = new ArrayList<>();
        googleVisionLangs.add(GOOGLE_VISION_LANGUAGE_MAP.getOrDefault(language, "en"));
        
        if (!additionalLanguages.isEmpty()) {
            List<String> additionalGoogleLangs = additionalLanguages.stream()
                .map(lang -> GOOGLE_VISION_LANGUAGE_MAP.getOrDefault(lang, lang))
                .toList();
            googleVisionLangs.addAll(additionalGoogleLangs);
        }
        
        googleVisionEngine.setLanguageHints(googleVisionLangs);
        
        logger.debug("OCR engines configured - Tesseract: {}, Google Vision: {}", 
            tesseractLang + (tesseractAdditionalLangs.isEmpty() ? "" : "+" + String.join("+", tesseractAdditionalLangs)),
            googleVisionLangs);
    }
    
    /**
     * Set language for both OCR engines (single language)
     * @param language Language code (ISO format: en, de, fr, etc.)
     */
    public void setLanguage(String language) {
        setLanguage(language, List.of());
    }
    
    /**
     * Get list of supported languages
     * @return Map of language codes to language names
     */
    public Map<String, String> getSupportedLanguages() {
        return Map.of(
            "en", "English",
            "de", "German (Deutsch)",
            "fr", "French (Français)",
            "es", "Spanish (Español)",
            "it", "Italian (Italiano)",
            "pt", "Portuguese (Português)",
            "nl", "Dutch (Nederlands)",
            "ru", "Russian (Русский)"
        );
    }
    
    /**
     * Get current language configuration
     * @return Current language information
     */
    public LanguageInfo getCurrentLanguageInfo() {
        return new LanguageInfo(
            tesseractEngine.getCurrentLanguages(),
            googleVisionEngine.getLanguageHints(),
            defaultPreferredLanguage
        );
    }
    
    /**
     * Check if a language is supported
     * @param language Language code to check
     * @return true if supported
     */
    public boolean isLanguageSupported(String language) {
        return TESSERACT_LANGUAGE_MAP.containsKey(language) || 
               GOOGLE_VISION_LANGUAGE_MAP.containsKey(language);
    }
    
    /**
     * Data class for language information
     */
    public static class LanguageInfo {
        private final String tesseractLanguages;
        private final List<String> googleVisionLanguageHints;
        private final String defaultLanguage;
        
        public LanguageInfo(String tesseractLanguages, List<String> googleVisionLanguageHints, String defaultLanguage) {
            this.tesseractLanguages = tesseractLanguages;
            this.googleVisionLanguageHints = googleVisionLanguageHints;
            this.defaultLanguage = defaultLanguage;
        }
        
        public String getTesseractLanguages() { return tesseractLanguages; }
        public List<String> getGoogleVisionLanguageHints() { return googleVisionLanguageHints; }
        public String getDefaultLanguage() { return defaultLanguage; }
    }
}