package com.picturetojson.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        logger.error("Runtime exception occurred", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        response.put("type", "RUNTIME_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        logger.error("Validation exception occurred", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "Validation failed");
        response.put("type", "VALIDATION_ERROR");
        
        e.getBindingResult().getFieldErrors().forEach(error -> 
            response.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeException(MaxUploadSizeExceededException e) {
        logger.error("File size exceeded", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "File size exceeds maximum allowed size");
        response.put("type", "FILE_SIZE_ERROR");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        logger.error("Unexpected exception occurred", e);
        
        Map<String, String> response = new HashMap<>();
        response.put("error", "An unexpected error occurred");
        response.put("type", "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}