package com.picturetojson.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JsonGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonGenerationService.class);
    private final ObjectMapper objectMapper;
    
    public JsonGenerationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Convert extracted text to structured JSON
     */
    public String generateJsonFromText(String extractedText) {
        logger.debug("Generating JSON from extracted text");
        
        Map<String, Object> extractedData = new HashMap<>();
        
        // Common field patterns for signup forms
        extractedData.putAll(extractNameFields(extractedText));
        extractedData.putAll(extractContactFields(extractedText));
        extractedData.putAll(extractAddressFields(extractedText));
        extractedData.putAll(extractDateFields(extractedText));
        extractedData.putAll(extractOtherFields(extractedText));
        
        try {
            String json = objectMapper.writeValueAsString(extractedData);
            logger.debug("JSON generation completed");
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Error generating JSON from extracted text", e);
            return "{}";
        }
    }
    
    /**
     * Extract name-related fields
     */
    private Map<String, Object> extractNameFields(String text) {
        Map<String, Object> nameFields = new HashMap<>();
        
        // First Name pattern
        Pattern firstNamePattern = Pattern.compile("(?i)(?:first\\s*name|given\\s*name)[:\\s]*([A-Za-z]+)", Pattern.CASE_INSENSITIVE);
        Matcher firstNameMatcher = firstNamePattern.matcher(text);
        if (firstNameMatcher.find()) {
            nameFields.put("firstName", firstNameMatcher.group(1).trim());
        }
        
        // Last Name pattern
        Pattern lastNamePattern = Pattern.compile("(?i)(?:last\\s*name|family\\s*name|surname)[:\\s]*([A-Za-z]+)", Pattern.CASE_INSENSITIVE);
        Matcher lastNameMatcher = lastNamePattern.matcher(text);
        if (lastNameMatcher.find()) {
            nameFields.put("lastName", lastNameMatcher.group(1).trim());
        }
        
        // Full Name pattern (fallback)
        if (nameFields.isEmpty()) {
            Pattern fullNamePattern = Pattern.compile("(?i)(?:name|full\\s*name)[:\\s]*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
            Matcher fullNameMatcher = fullNamePattern.matcher(text);
            if (fullNameMatcher.find()) {
                String fullName = fullNameMatcher.group(1).trim();
                String[] nameParts = fullName.split("\\s+");
                if (nameParts.length >= 2) {
                    nameFields.put("firstName", nameParts[0]);
                    nameFields.put("lastName", nameParts[nameParts.length - 1]);
                } else {
                    nameFields.put("fullName", fullName);
                }
            }
        }
        
        return nameFields;
    }
    
    /**
     * Extract contact-related fields
     */
    private Map<String, Object> extractContactFields(String text) {
        Map<String, Object> contactFields = new HashMap<>();
        
        // Email pattern
        Pattern emailPattern = Pattern.compile("(?i)(?:email|e-mail)[:\\s]*([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})", Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(text);
        if (emailMatcher.find()) {
            contactFields.put("email", emailMatcher.group(1).trim());
        }
        
        // Phone pattern
        Pattern phonePattern = Pattern.compile("(?i)(?:phone|telephone|mobile|cell)[:\\s]*([+]?[1-9]?[0-9]{7,15})", Pattern.CASE_INSENSITIVE);
        Matcher phoneMatcher = phonePattern.matcher(text);
        if (phoneMatcher.find()) {
            contactFields.put("phone", phoneMatcher.group(1).trim());
        }
        
        return contactFields;
    }
    
    /**
     * Extract address-related fields
     */
    private Map<String, Object> extractAddressFields(String text) {
        Map<String, Object> addressFields = new HashMap<>();
        
        // Address pattern
        Pattern addressPattern = Pattern.compile("(?i)(?:address|street)[:\\s]*([A-Za-z0-9\\s,.-]+)", Pattern.CASE_INSENSITIVE);
        Matcher addressMatcher = addressPattern.matcher(text);
        if (addressMatcher.find()) {
            addressFields.put("address", addressMatcher.group(1).trim());
        }
        
        // City pattern
        Pattern cityPattern = Pattern.compile("(?i)(?:city)[:\\s]*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher cityMatcher = cityPattern.matcher(text);
        if (cityMatcher.find()) {
            addressFields.put("city", cityMatcher.group(1).trim());
        }
        
        // ZIP/Postal Code pattern
        Pattern zipPattern = Pattern.compile("(?i)(?:zip|postal\\s*code|post\\s*code)[:\\s]*([A-Za-z0-9\\s-]+)", Pattern.CASE_INSENSITIVE);
        Matcher zipMatcher = zipPattern.matcher(text);
        if (zipMatcher.find()) {
            addressFields.put("zipCode", zipMatcher.group(1).trim());
        }
        
        return addressFields;
    }
    
    /**
     * Extract date-related fields
     */
    private Map<String, Object> extractDateFields(String text) {
        Map<String, Object> dateFields = new HashMap<>();
        
        // Date of Birth pattern
        Pattern dobPattern = Pattern.compile("(?i)(?:date\\s*of\\s*birth|birth\\s*date|dob)[:\\s]*([0-9]{1,2}[/-][0-9]{1,2}[/-][0-9]{2,4})", Pattern.CASE_INSENSITIVE);
        Matcher dobMatcher = dobPattern.matcher(text);
        if (dobMatcher.find()) {
            dateFields.put("dateOfBirth", dobMatcher.group(1).trim());
        }
        
        return dateFields;
    }
    
    /**
     * Extract other common fields
     */
    private Map<String, Object> extractOtherFields(String text) {
        Map<String, Object> otherFields = new HashMap<>();
        
        // Gender pattern
        Pattern genderPattern = Pattern.compile("(?i)(?:gender|sex)[:\\s]*(male|female|m|f|other)", Pattern.CASE_INSENSITIVE);
        Matcher genderMatcher = genderPattern.matcher(text);
        if (genderMatcher.find()) {
            otherFields.put("gender", genderMatcher.group(1).trim());
        }
        
        // Age pattern
        Pattern agePattern = Pattern.compile("(?i)(?:age)[:\\s]*([0-9]{1,3})", Pattern.CASE_INSENSITIVE);
        Matcher ageMatcher = agePattern.matcher(text);
        if (ageMatcher.find()) {
            otherFields.put("age", Integer.parseInt(ageMatcher.group(1).trim()));
        }
        
        return otherFields;
    }
    
    /**
     * Validate and format JSON
     */
    public boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
    
    /**
     * Merge extracted JSON with corrected JSON
     */
    public String mergeJsonData(String extractedJson, String correctedJson) throws JsonProcessingException {
        JsonNode extractedNode = objectMapper.readTree(extractedJson);
        JsonNode correctedNode = objectMapper.readTree(correctedJson);
        
        ObjectNode merged = objectMapper.createObjectNode();
        
        // Start with extracted data
        merged.setAll((ObjectNode) extractedNode);
        
        // Override with corrected data
        merged.setAll((ObjectNode) correctedNode);
        
        return objectMapper.writeValueAsString(merged);
    }
}