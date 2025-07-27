package com.picturetojson.service;

import com.picturetojson.service.ocr.GoogleVisionOcrEngine;
import com.picturetojson.service.ocr.TesseractOcrEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class OcrServiceTest {
    
    @Mock
    private TesseractOcrEngine tesseractEngine;
    
    @Mock
    private GoogleVisionOcrEngine googleVisionEngine;
    
    @Mock
    private TextAnalysisService textAnalysisService;
    
    private OcrService ocrService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(tesseractEngine.isAvailable()).thenReturn(true);
        when(tesseractEngine.getEngineType()).thenReturn("TESSERACT");
        when(tesseractEngine.getConfidence()).thenReturn(85);
        
        when(googleVisionEngine.isAvailable()).thenReturn(false);
        when(googleVisionEngine.getEngineType()).thenReturn("GOOGLE_VISION");
        when(googleVisionEngine.getConfidence()).thenReturn(90);
        
        ocrService = new OcrService(
            tesseractEngine, 
            googleVisionEngine, 
            textAnalysisService, 
            true, 
            "TESSERACT"
        );
    }
    
    @Test
    void testGetBasicConfidence() {
        int confidence = ocrService.getBasicConfidence();
        assertEquals(85, confidence);
    }
    
    @Test
    void testIsHandwritingRecognitionAvailable_whenGoogleVisionDisabled() {
        assertFalse(ocrService.isHandwritingRecognitionAvailable());
    }
    
    @Test
    void testGetAvailableEngines_onlyTesseract() {
        String[] engines = ocrService.getAvailableEngines();
        assertArrayEquals(new String[]{"TESSERACT"}, engines);
    }
    
    @Test
    void testGetAvailableEngines_withGoogleVision() {
        when(googleVisionEngine.isAvailable()).thenReturn(true);
        
        OcrService ocrServiceWithGoogleVision = new OcrService(
            tesseractEngine, 
            googleVisionEngine, 
            textAnalysisService, 
            true, 
            "TESSERACT"
        );
        
        String[] engines = ocrServiceWithGoogleVision.getAvailableEngines();
        assertArrayEquals(new String[]{"TESSERACT", "GOOGLE_VISION"}, engines);
    }
    
    @Test
    void testExtractTextFromImage_usesGoogleVisionForHandwriting() throws Exception {
        when(googleVisionEngine.isAvailable()).thenReturn(true);
        when(textAnalysisService.isHandwritingDetected(any(BufferedImage.class))).thenReturn(true);
        when(googleVisionEngine.extractTextFromImage(any(BufferedImage.class))).thenReturn("Handwritten text");
        
        OcrService ocrServiceWithGoogleVision = new OcrService(
            tesseractEngine, 
            googleVisionEngine, 
            textAnalysisService, 
            true, 
            "TESSERACT"
        );
        
        BufferedImage testImage = createTestImage();
        String result = ocrServiceWithGoogleVision.extractTextFromImage(testImage);
        assertEquals("Handwritten text", result);
    }
    
    @Test
    void testExtractTextFromImage_usesTesseractForPrintedText() throws Exception {
        when(textAnalysisService.isHandwritingDetected(any(BufferedImage.class))).thenReturn(false);
        when(tesseractEngine.extractTextFromImage(any(BufferedImage.class))).thenReturn("Printed text");
        
        BufferedImage testImage = createTestImage();
        String result = ocrService.extractTextFromImage(testImage);
        assertEquals("Printed text", result);
    }
    
    @Test
    void testExtractTextFromImage_withSpecificEngine() throws Exception {
        when(tesseractEngine.extractTextFromImage(any(BufferedImage.class))).thenReturn("Test text");
        
        BufferedImage testImage = createTestImage();
        String result = ocrService.extractTextFromImage(testImage, "TESSERACT");
        assertEquals("Test text", result);
    }
    
    private BufferedImage createTestImage() {
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawString("Test", 10, 20);
        g2d.dispose();
        return image;
    }
}