package com.picturetojson.dto;

import jakarta.validation.constraints.NotBlank;

public class CorrectionRequestDto {
    
    @NotBlank(message = "Corrected JSON is required")
    private String correctedJson;
    
    // Constructors
    public CorrectionRequestDto() {}
    
    public CorrectionRequestDto(String correctedJson) {
        this.correctedJson = correctedJson;
    }
    
    // Getters and Setters
    public String getCorrectedJson() { return correctedJson; }
    public void setCorrectedJson(String correctedJson) { this.correctedJson = correctedJson; }
}