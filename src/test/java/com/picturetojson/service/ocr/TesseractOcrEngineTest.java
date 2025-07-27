package com.picturetojson.service.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TesseractOcrEngineTest {
    
    private TesseractOcrEngine tesseractEngine;
    
    @BeforeEach
    void setUp() {
        tesseractEngine = new TesseractOcrEngine("/tmp/tessdata", "eng", List.of());
    }
    
    @Test
    void testGetEngineType() {
        assertEquals("TESSERACT", tesseractEngine.getEngineType());
    }
    
    @Test
    void testIsAvailable() {
        assertTrue(tesseractEngine.isAvailable());
    }
    
    @Test
    void testGetConfidence() {
        int confidence = tesseractEngine.getConfidence();
        assertTrue(confidence > 0 && confidence <= 100);
    }
    
    @Test
    void testExtractTextFromImage_withNullImage() {
        assertThrows(Exception.class, () -> {
            tesseractEngine.extractTextFromImage((BufferedImage) null);
        });
    }
    
    @Test
    void testGetCurrentLanguages() {
        assertEquals("eng", tesseractEngine.getCurrentLanguages());
    }
    
    @Test 
    void testSetLanguage_German() {
        tesseractEngine.setLanguage("deu", List.of());
        assertEquals("deu", tesseractEngine.getCurrentLanguages());
    }
    
    @Test
    void testSetLanguage_MultipleLanguages() {
        tesseractEngine.setLanguage("eng", List.of("deu", "fra"));
        assertEquals("eng+deu+fra", tesseractEngine.getCurrentLanguages());
    }
}