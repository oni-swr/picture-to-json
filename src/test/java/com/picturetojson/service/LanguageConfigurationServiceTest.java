package com.picturetojson.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.picturetojson.service.ocr.GoogleVisionOcrEngine;
import com.picturetojson.service.ocr.TesseractOcrEngine;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class LanguageConfigurationServiceTest {
    
    @Mock
    private TesseractOcrEngine tesseractEngine;
    
    @Mock 
    private GoogleVisionOcrEngine googleVisionEngine;
    
    private LanguageConfigurationService languageConfigurationService;
    
    @BeforeEach
    void setUp() {
        languageConfigurationService = new LanguageConfigurationService(
            tesseractEngine, googleVisionEngine, "en");
    }
    
    @Test
    void testSetLanguage_English() {
        // Test setting English language
        languageConfigurationService.setLanguage("en");
        
        verify(tesseractEngine).setLanguage("eng", List.of());
        verify(googleVisionEngine).setLanguageHints(List.of("en"));
    }
    
    @Test
    void testSetLanguage_German() {
        // Test setting German language
        languageConfigurationService.setLanguage("de");
        
        verify(tesseractEngine).setLanguage("deu", List.of());
        verify(googleVisionEngine).setLanguageHints(List.of("de"));
    }
    
    @Test
    void testSetLanguage_MultipleLanguages() {
        // Test setting multiple languages
        languageConfigurationService.setLanguage("de", List.of("en", "fr"));
        
        verify(tesseractEngine).setLanguage("deu", List.of("eng", "fra"));
        verify(googleVisionEngine).setLanguageHints(List.of("de", "en", "fr"));
    }
    
    @Test
    void testGetSupportedLanguages() {
        Map<String, String> supportedLanguages = languageConfigurationService.getSupportedLanguages();
        
        assertNotNull(supportedLanguages);
        assertTrue(supportedLanguages.containsKey("en"));
        assertTrue(supportedLanguages.containsKey("de"));
        assertEquals("English", supportedLanguages.get("en"));
        assertEquals("German (Deutsch)", supportedLanguages.get("de"));
    }
    
    @Test
    void testIsLanguageSupported() {
        assertTrue(languageConfigurationService.isLanguageSupported("en"));
        assertTrue(languageConfigurationService.isLanguageSupported("de"));
        assertTrue(languageConfigurationService.isLanguageSupported("fr"));
        assertFalse(languageConfigurationService.isLanguageSupported("xyz"));
    }
    
    @Test
    void testGetCurrentLanguageInfo() {
        // Mock return values
        when(tesseractEngine.getCurrentLanguages()).thenReturn("deu");
        when(googleVisionEngine.getLanguageHints()).thenReturn(List.of("de"));
        
        LanguageConfigurationService.LanguageInfo info = languageConfigurationService.getCurrentLanguageInfo();
        
        assertNotNull(info);
        assertEquals("deu", info.getTesseractLanguages());
        assertEquals(List.of("de"), info.getGoogleVisionLanguageHints());
        assertEquals("en", info.getDefaultLanguage());
    }
}