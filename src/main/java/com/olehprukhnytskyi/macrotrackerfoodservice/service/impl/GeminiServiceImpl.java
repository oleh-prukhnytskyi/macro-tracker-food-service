package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.macrotrackerfoodservice.client.GeminiClient;
import com.olehprukhnytskyi.macrotrackerfoodservice.exception.KeywordGenerationException;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.GeminiService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {
    @Value("${gemini.api-key}")
    private String apiKey;
    private final GeminiClient geminiClient;

    @Override
    public List<String> generateKeywords(Food food) {
        if (food == null
                || food.getProductName() == null
                || food.getNutriments() == null) {
            return Collections.emptyList();
        }
        Map<String, Object> prompt = getKeywordsPrompt(food);
        try {
            ResponseEntity<String> response = geminiClient.generateContent(apiKey, prompt);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {
                JsonNode root = new ObjectMapper().readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    String content = candidates.get(0)
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText()
                            .trim();
                    if (content.equalsIgnoreCase("unknown")) {
                        return Collections.emptyList();
                    }
                    return Arrays.stream(content.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            throw new KeywordGenerationException("Failed to generate keywords from Gemini", e);
        }
        return Collections.emptyList();
    }

    private static Map<String, Object> getKeywordsPrompt(Food food) {
        String prompt = ("You are given information about a food product.\n"
                         + "\n"
                         + "Your task:\n"
                         + "1. Detect the language of the product data.\n"
                         + "2. If confident what the product is, generate 5–10\n"
                         + " relevant keywords in that language.\n"
                         + "3. Only use specific nouns or phrases directly related\n"
                         + " to ingredients, type of product, form, preparation method, or brand.\n"
                         + "4. Do NOT include vague adjectives like \"tasty\", \"nutritious\",\n"
                         + " \"healthy\", \"caloric\", or any emotional or evaluative terms.\n"
                         + "5. Format: return ONLY a single comma-separated list of\n"
                         + " lowercase keywords. No labels, no line breaks, no explanations.\n"
                         + "6. If not confident what the product is — return exactly: unknown\n"
                         + "\n"
                         + "Product name: %s\n"
                         + "Generic name: %s\n"
                         + "Brand: %s\n"
                         + "Nutritional values: kcal=%s, fat=%s, proteins=%s, carbohydrates=%s\n")
                .formatted(
                        food.getProductName(),
                        food.getGenericName(),
                        food.getBrands(),
                        food.getNutriments().getKcal(),
                        food.getNutriments().getFat(),
                        food.getNutriments().getProteins(),
                        food.getNutriments().getCarbohydrates()
                );
        return Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );
    }
}
