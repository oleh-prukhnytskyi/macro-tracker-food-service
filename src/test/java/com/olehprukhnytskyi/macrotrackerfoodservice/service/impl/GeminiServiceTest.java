package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.olehprukhnytskyi.exception.ExternalServiceException;
import com.olehprukhnytskyi.macrotrackerfoodservice.client.GeminiClient;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Nutriments;
import com.olehprukhnytskyi.macrotrackerfoodservice.properties.GeminiProperties;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.GeminiService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {
    @Mock
    private GeminiClient geminiClient;
    @Mock
    private GeminiProperties geminiProperties;

    @InjectMocks
    private GeminiService geminiService;

    private Food food;

    @BeforeEach
    void setup() {
        Nutriments nutriments = new Nutriments();
        nutriments.setKcal(BigDecimal.valueOf(200.0));
        nutriments.setFat(BigDecimal.valueOf(8.0));
        nutriments.setProteins(BigDecimal.valueOf(10.0));
        nutriments.setCarbohydrates(BigDecimal.valueOf(15.0));

        food = new Food();
        food.setProductName("Protein Bar");
        food.setGenericName("bar");
        food.setBrands("TestBrand");
        food.setNutriments(nutriments);
    }

    @Test
    @DisplayName("When response is valid, should return a list")
    void generateKeywords_whenResponseIsValid_shouldReturnList() {
        // Given
        String json = """
              {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "protein bar, chocolate, peanuts"
                        }]
                    }
                }]
              }
               \s""";

        when(geminiClient.generateContent(any(), any()))
                .thenReturn(ResponseEntity.ok(json));

        // When
        List<String> result = geminiService.generateKeywords(food);

        // Then
        assertEquals(List.of("protein bar", "chocolate", "peanuts"), result);
    }

    @Test
    @DisplayName("When content is unknown, should return an empty list")
    void generateKeywords_whenContentIsUnknown_shouldReturnEmptyList() {
        // Given
        String json = """
              {
                "candidates": [{
                    "content": {
                        "parts": [{
                            "text": "unknown"
                        }]
                    }
                }]
              }
               \s""";

        when(geminiClient.generateContent(any(), any()))
                .thenReturn(ResponseEntity.ok(json));

        // When
        List<String> result = geminiService.generateKeywords(food);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("When response is not 2xx, should return an empty list")
    void generateKeywords_whenResponseIsNot2xx_shouldReturnEmptyList() {
        // Given
        when(geminiClient.generateContent(any(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(""));

        // When
        List<String> result = geminiService.generateKeywords(food);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("When client fails, should throw KeywordGenerationException")
    void generateKeywords_whenClientFails_shouldThrowKeywordGenerationException() {
        // Given
        when(geminiClient.generateContent(any(), any()))
                .thenThrow(new RuntimeException("Failure"));

        // When
        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class, () -> geminiService.generateKeywords(food)
        );

        // Then
        String expected = "Failed to generate keywords from Gemini";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    @DisplayName("When food is null, should return an empty list")
    void generateKeywords_whenFoodIsNull_shouldReturnEmptyList() {
        assertTrue(geminiService.generateKeywords(null).isEmpty());
    }
}
