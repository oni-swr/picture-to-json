package com.picturetojson.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "field_mappings")
public class FieldMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;
    
    @Column(nullable = false)
    private String sourceField;
    
    @Column(nullable = false)
    private String targetField;
    
    @Column(nullable = false)
    private String fieldType;
    
    @Column
    private String extractedValue;
    
    @Column
    private String correctedValue;
    
    @Column
    private String validationRule;
    
    @Column
    private Integer confidence;
    
    // Constructors
    public FieldMapping() {}
    
    public FieldMapping(Document document, String sourceField, String targetField, String fieldType) {
        this.document = document;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.fieldType = fieldType;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }
    
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    
    public String getTargetField() { return targetField; }
    public void setTargetField(String targetField) { this.targetField = targetField; }
    
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    
    public String getExtractedValue() { return extractedValue; }
    public void setExtractedValue(String extractedValue) { this.extractedValue = extractedValue; }
    
    public String getCorrectedValue() { return correctedValue; }
    public void setCorrectedValue(String correctedValue) { this.correctedValue = correctedValue; }
    
    public String getValidationRule() { return validationRule; }
    public void setValidationRule(String validationRule) { this.validationRule = validationRule; }
    
    public Integer getConfidence() { return confidence; }
    public void setConfidence(Integer confidence) { this.confidence = confidence; }
}