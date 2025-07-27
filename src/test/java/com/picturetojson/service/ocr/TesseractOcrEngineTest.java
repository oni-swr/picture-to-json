package com.picturetojson.service.ocr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TesseractOcrEngineTest {
    
    private TesseractOcrEngine tesseractEngine;
    
    @BeforeEach
    void setUp() {
        tesseractEngine = new TesseractOcrEngine("/tmp/tessdata", "eng");
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
}