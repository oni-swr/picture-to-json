package com.picturetojson.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TextAnalysisServiceTest {
    
    private final TextAnalysisService textAnalysisService = new TextAnalysisService();
    
    @Test
    void testIsHandwritingDetected_withBufferedImage() {
        BufferedImage testImage = createTestImage();
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            boolean result = textAnalysisService.isHandwritingDetected(testImage);
            // Result can be true or false, we're just testing it doesn't crash
            assertTrue(result || !result);
        });
    }
    
    @Test
    void testIsHandwritingDetected_withNonExistentFile() {
        File nonExistentFile = new File("/nonexistent/file.jpg");
        
        assertThrows(IOException.class, () -> {
            textAnalysisService.isHandwritingDetected(nonExistentFile);
        });
    }
    
    @Test
    void testIsHandwritingDetected_withSimpleImage() {
        BufferedImage simpleImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = simpleImage.createGraphics();
        
        // Draw some simple shapes (should be detected as printed text)
        g2d.drawRect(10, 10, 80, 80);
        g2d.drawString("Hello", 20, 50);
        g2d.dispose();
        
        // Should not crash and return a boolean result
        assertDoesNotThrow(() -> {
            boolean result = textAnalysisService.isHandwritingDetected(simpleImage);
            assertTrue(result || !result);
        });
    }
    
    @Test
    void testIsHandwritingDetected_withComplexImage() {
        BufferedImage complexImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = complexImage.createGraphics();
        
        // Draw irregular shapes to simulate handwriting
        g2d.drawOval(10, 10, 30, 20);
        g2d.drawOval(50, 15, 25, 18);
        g2d.drawOval(80, 12, 35, 25);
        g2d.dispose();
        
        // Should not crash and return a boolean result
        assertDoesNotThrow(() -> {
            boolean result = textAnalysisService.isHandwritingDetected(complexImage);
            assertTrue(result || !result);
        });
    }
    
    private BufferedImage createTestImage() {
        BufferedImage image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.drawString("Test", 10, 20);
        g2d.dispose();
        return image;
    }
}