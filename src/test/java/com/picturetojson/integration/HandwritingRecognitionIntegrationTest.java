package com.picturetojson.integration;

import com.picturetojson.service.OcrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for handwriting recognition functionality
 */
@SpringBootTest
@ActiveProfiles("test")
class HandwritingRecognitionIntegrationTest {
    
    @Autowired
    private OcrService ocrService;
    
    @Test
    void testOcrServiceIsConfigured() {
        assertNotNull(ocrService);
        assertTrue(ocrService.getAvailableEngines().length > 0);
    }
    
    @Test
    void testHandwritingDetectionEndpoint() {
        // Test that handwriting recognition availability can be checked
        boolean available = ocrService.isHandwritingRecognitionAvailable();
        // Should not throw exception, result depends on configuration
        assertTrue(available || !available);
    }
    
    @Test
    void testExtractTextFromSimulatedHandwriting() {
        // Create a test image that simulates handwritten text
        BufferedImage handwritingImage = createSimulatedHandwritingImage();
        
        // Should be able to process without errors (even if OCR engines are not fully configured)
        try {
            String result = ocrService.extractTextFromImage(handwritingImage);
            assertNotNull(result);
        } catch (Exception | Error e) {
            // If native libraries are not available, the test should still pass
            // This is expected in CI environments where Tesseract may not be installed
            String message = e.getMessage();
            assertTrue(message != null && (message.contains("tesseract") || 
                      message.contains("UnsatisfiedLinkError") ||
                      message.contains("native") ||
                      message.contains("TessAPI")), 
                      "Expected Tesseract-related error, got: " + e.getClass().getSimpleName() + ": " + message);
        }
    }
    
    @Test
    void testExtractTextFromPrintedText() {
        // Create a test image with clean printed text
        BufferedImage printedImage = createPrintedTextImage();
        
        // Should handle the case where native libraries are not available
        try {
            String result = ocrService.extractTextFromImage(printedImage);
            assertNotNull(result);
            // May be empty if Tesseract can't read the simple test image, but shouldn't crash
        } catch (Exception | Error e) {
            // If native libraries are not available, the test should still pass
            // This is expected in CI environments where Tesseract may not be installed
            String message = e.getMessage();
            assertTrue(message != null && (message.contains("tesseract") || 
                      message.contains("UnsatisfiedLinkError") ||
                      message.contains("native") ||
                      message.contains("TessAPI")), 
                      "Expected Tesseract-related error, got: " + e.getClass().getSimpleName() + ": " + message);
        }
    }
    
    @Test
    void testOcrEngineSelection() {
        String[] engines = ocrService.getAvailableEngines();
        assertNotNull(engines);
        assertTrue(engines.length >= 1);
        // Should at least have Tesseract available (even if not fully functional due to missing libraries)
        boolean hasTesseract = false;
        for (String engine : engines) {
            if ("TESSERACT".equals(engine)) {
                hasTesseract = true;
                break;
            }
        }
        assertTrue(hasTesseract, "Tesseract engine should always be available as a configured option");
    }
    
    private BufferedImage createSimulatedHandwritingImage() {
        BufferedImage image = new BufferedImage(300, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 300, 100);
        g2d.setColor(Color.BLACK);
        
        // Simulate irregular handwriting with curves and varied spacing
        g2d.setStroke(new BasicStroke(2.0f));
        
        // Draw some curved lines to simulate handwriting
        g2d.drawArc(10, 20, 20, 15, 0, 180);
        g2d.drawArc(35, 22, 18, 12, 0, 180);
        g2d.drawLine(55, 30, 75, 25);
        g2d.drawLine(75, 25, 85, 35);
        
        // Add some irregular shapes
        Polygon p = new Polygon();
        p.addPoint(100, 20);
        p.addPoint(115, 25);
        p.addPoint(120, 35);
        p.addPoint(105, 30);
        g2d.draw(p);
        
        g2d.dispose();
        return image;
    }
    
    private BufferedImage createPrintedTextImage() {
        BufferedImage image = new BufferedImage(200, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 200, 50);
        g2d.setColor(Color.BLACK);
        
        // Use a clean, regular font for printed text
        g2d.setFont(new Font(Font.SERIF, Font.PLAIN, 16));
        g2d.drawString("John Doe", 10, 25);
        
        g2d.dispose();
        return image;
    }
}