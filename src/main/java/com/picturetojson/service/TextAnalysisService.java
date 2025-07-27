package com.picturetojson.service;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for analyzing text regions in images to determine if they contain
 * handwritten or printed text
 */
@Service
public class TextAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(TextAnalysisService.class);
    
    static {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadShared();
    }
    
    /**
     * Analyze image to determine if it contains primarily handwritten or printed text
     * @param imageFile the image file to analyze
     * @return true if handwriting is detected, false if printed text
     */
    public boolean isHandwritingDetected(File imageFile) throws IOException {
        logger.debug("Analyzing text type in image: {}", imageFile.getName());
        
        Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
        if (image.empty()) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }
        
        boolean result = analyzeTextCharacteristics(image);
        image.release();
        
        logger.debug("Handwriting detection result for {}: {}", imageFile.getName(), result);
        return result;
    }
    
    /**
     * Analyze BufferedImage to determine if it contains primarily handwritten or printed text
     */
    public boolean isHandwritingDetected(BufferedImage bufferedImage) {
        logger.debug("Analyzing text type in BufferedImage");
        
        Mat image = bufferedImageToMat(bufferedImage);
        boolean result = analyzeTextCharacteristics(image);
        image.release();
        
        logger.debug("Handwriting detection result for BufferedImage: {}", result);
        return result;
    }
    
    /**
     * Analyze text characteristics to determine if it's handwritten or printed
     * This is a simplified implementation based on stroke width variation,
     * irregularity, and other visual features
     */
    private boolean analyzeTextCharacteristics(Mat image) {
        try {
            Mat gray = new Mat();
            Mat binary = new Mat();
            Mat contours = new Mat();
            
            // Convert to grayscale
            if (image.channels() > 1) {
                Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            } else {
                image.copyTo(gray);
            }
            
            // Apply threshold to get binary image
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
            
            // Find contours
            List<MatOfPoint> contoursList = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binary, contoursList, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            // Analyze contour characteristics
            double totalIrregularity = 0;
            double totalAspectRatio = 0;
            int validContours = 0;
            
            for (MatOfPoint contour : contoursList) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                
                // Filter out very small or very large contours
                if (boundingRect.area() < 100 || boundingRect.area() > image.rows() * image.cols() * 0.1) {
                    continue;
                }
                
                // Calculate aspect ratio
                double aspectRatio = (double) boundingRect.width / boundingRect.height;
                totalAspectRatio += aspectRatio;
                
                // Calculate contour irregularity (approximation of how "rough" the edges are)
                double contourArea = Imgproc.contourArea(contour);
                double arcLength = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
                double circularity = 4 * Math.PI * contourArea / (arcLength * arcLength);
                
                totalIrregularity += (1 - circularity); // Higher value means more irregular
                validContours++;
            }
            
            // Cleanup
            gray.release();
            binary.release();
            contours.release();
            hierarchy.release();
            
            if (validContours == 0) {
                return false; // Default to printed text if no valid contours found
            }
            
            double avgIrregularity = totalIrregularity / validContours;
            double avgAspectRatio = totalAspectRatio / validContours;
            
            // Heuristic rules for handwriting detection
            // Handwritten text tends to have:
            // - Higher irregularity (less perfect shapes)
            // - More varied aspect ratios
            // - Less uniform spacing
            
            boolean isHandwritten = avgIrregularity > 0.8 || avgAspectRatio > 3.0 || avgAspectRatio < 0.3;
            
            logger.debug("Text analysis - Irregularity: {}, Aspect Ratio: {}, Handwritten: {}", 
                avgIrregularity, avgAspectRatio, isHandwritten);
            
            return isHandwritten;
            
        } catch (Exception e) {
            logger.warn("Error analyzing text characteristics, defaulting to printed text", e);
            return false; // Default to printed text on error
        }
    }
    
    /**
     * Convert BufferedImage to OpenCV Mat
     */
    private Mat bufferedImageToMat(BufferedImage img) {
        if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage converted = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            converted.getGraphics().drawImage(img, 0, 0, null);
            img = converted;
        }
        
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        mat.put(0, 0, pixels);
        return mat;
    }
}