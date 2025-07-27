package com.picturetojson.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JsonGenerationServiceTest {
    
    private final JsonGenerationService jsonGenerationService = new JsonGenerationService(new com.fasterxml.jackson.databind.ObjectMapper());
    
    @Test
    void testGenerateJsonFromText_withBasicFormData() {
        String extractedText = "First Name: John\nLast Name: Doe\nEmail: john.doe@example.com\nPhone: 123-456-7890";
        
        String result = jsonGenerationService.generateJsonFromText(extractedText);
        
        assertNotNull(result);
        assertTrue(jsonGenerationService.isValidJson(result));
        assertTrue(result.contains("john.doe@example.com"));
    }
    
    @Test
    void testIsValidJson() {
        assertTrue(jsonGenerationService.isValidJson("{\"name\":\"John\"}"));
        assertFalse(jsonGenerationService.isValidJson("{invalid json}"));
        assertFalse(jsonGenerationService.isValidJson("not json at all"));
    }
    
    @Test
    void testGenerateJsonFromText_emptyText() {
        String result = jsonGenerationService.generateJsonFromText("");
        
        assertNotNull(result);
        assertTrue(jsonGenerationService.isValidJson(result));
        assertEquals("{}", result);
    }
}