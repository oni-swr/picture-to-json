package com.picturetojson.service.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class GoogleVisionOcrEngineTest {
    
    private GoogleVisionOcrEngine googleVisionEngine;
    
    @BeforeEach
    void setUp() {
        // Initialize with disabled configuration for testing
        googleVisionEngine = new GoogleVisionOcrEngine(false, "", List.of("en"));
    }
    
    @Test
    void testGetEngineType() {
        assertEquals("GOOGLE_VISION", googleVisionEngine.getEngineType());
    }
    
    @Test
    void testIsAvailable_whenDisabled() {
        assertFalse(googleVisionEngine.isAvailable());
    }
    
    @Test
    void testGetConfidence() {
        int confidence = googleVisionEngine.getConfidence();
        assertTrue(confidence > 0 && confidence <= 100);
    }
    
    @Test
    void testExtractTextFromImage_whenNotAvailable() {
        assertThrows(RuntimeException.class, () -> {
            googleVisionEngine.extractTextFromImage(new java.io.File("test.jpg"));
        });
    }
    
    @Test
    void testIsAvailable_withCredentialsPath() {
        GoogleVisionOcrEngine engineWithPath = new GoogleVisionOcrEngine(true, "/nonexistent/path", List.of("en"));
        assertFalse(engineWithPath.isAvailable());
    }
    
    @Test
    void testGetLanguageHints() {
        List<String> hints = googleVisionEngine.getLanguageHints();
        assertEquals(List.of("en"), hints);
    }
    
    @Test
    void testSetLanguageHints_German() {
        googleVisionEngine.setLanguageHints(List.of("de"));
        assertEquals(List.of("de"), googleVisionEngine.getLanguageHints());
    }
    
    @Test
    void testSetLanguageHints_MultipleLanguages() {
        googleVisionEngine.setLanguageHints(List.of("de", "en", "fr"));
        assertEquals(List.of("de", "en", "fr"), googleVisionEngine.getLanguageHints());
    }
}