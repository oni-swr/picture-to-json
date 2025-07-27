package com.picturetojson.dto;

import com.picturetojson.entity.Document;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentResponseDto {
    
    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Document.ProcessingStatus status;
    private String extractedText;
    private String extractedJson;
    private String correctedJson;
    private String errorMessage;
    private Integer processingProgress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public DocumentResponseDto() {}
    
    public DocumentResponseDto(Document document) {
        this.id = document.getId();
        this.filename = document.getFilename();
        this.originalFilename = document.getOriginalFilename();
        this.contentType = document.getContentType();
        this.fileSize = document.getFileSize();
        this.status = document.getStatus();
        this.extractedText = document.getExtractedText();
        this.extractedJson = document.getExtractedJson();
        this.correctedJson = document.getCorrectedJson();
        this.errorMessage = document.getErrorMessage();
        this.processingProgress = document.getProcessingProgress();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public Document.ProcessingStatus getStatus() { return status; }
    public void setStatus(Document.ProcessingStatus status) { this.status = status; }
    
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    
    public String getExtractedJson() { return extractedJson; }
    public void setExtractedJson(String extractedJson) { this.extractedJson = extractedJson; }
    
    public String getCorrectedJson() { return correctedJson; }
    public void setCorrectedJson(String correctedJson) { this.correctedJson = correctedJson; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Integer getProcessingProgress() { return processingProgress; }
    public void setProcessingProgress(Integer processingProgress) { this.processingProgress = processingProgress; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}