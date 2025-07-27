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

@Service
public class ImageProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingService.class);
    
    static {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadShared();
        logger.info("OpenCV loaded successfully");
    }
    
    /**
     * Preprocess image for better OCR results
     */
    public BufferedImage preprocessImage(File imageFile) throws IOException {
        logger.debug("Preprocessing image: {}", imageFile.getName());
        
        // Read image using OpenCV
        Mat image = Imgcodecs.imread(imageFile.getAbsolutePath());
        
        if (image.empty()) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }
        
        // Apply preprocessing steps
        Mat processed = new Mat();
        
        // Convert to grayscale
        Imgproc.cvtColor(image, processed, Imgproc.COLOR_BGR2GRAY);
        
        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(processed, processed, new Size(1, 1), 0);
        
        // Apply threshold to get binary image
        Imgproc.threshold(processed, processed, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        
        // Morphological operations to clean up the image
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.morphologyEx(processed, processed, Imgproc.MORPH_CLOSE, kernel);
        
        // Convert back to BufferedImage
        BufferedImage result = matToBufferedImage(processed);
        
        // Cleanup
        image.release();
        processed.release();
        kernel.release();
        
        logger.debug("Image preprocessing completed for: {}", imageFile.getName());
        return result;
    }
    
    /**
     * Enhance image contrast and brightness
     */
    public BufferedImage enhanceImage(BufferedImage input) {
        logger.debug("Enhancing image contrast and brightness");
        
        Mat image = bufferedImageToMat(input);
        Mat enhanced = new Mat();
        
        // Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
        Imgproc.createCLAHE(2.0, new Size(8, 8)).apply(image, enhanced);
        
        BufferedImage result = matToBufferedImage(enhanced);
        
        // Cleanup
        image.release();
        enhanced.release();
        
        return result;
    }
    
    /**
     * Detect and correct image rotation
     */
    public BufferedImage correctRotation(BufferedImage input) {
        logger.debug("Correcting image rotation");
        
        Mat image = bufferedImageToMat(input);
        Mat rotated = new Mat();
        
        // Simple rotation correction - in a full implementation, 
        // you would use more sophisticated algorithms
        // For now, we'll just return the original image
        image.copyTo(rotated);
        
        BufferedImage result = matToBufferedImage(rotated);
        
        // Cleanup
        image.release();
        rotated.release();
        
        return result;
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
    
    /**
     * Convert OpenCV Mat to BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        
        try {
            return ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            logger.error("Error converting Mat to BufferedImage", e);
            return null;
        }
    }
}