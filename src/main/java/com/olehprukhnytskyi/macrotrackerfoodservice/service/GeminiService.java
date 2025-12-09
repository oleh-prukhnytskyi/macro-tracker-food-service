package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.client.GeminiClient;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.GeminiRequest;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.GeminiResponse;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.properties.GeminiProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {
    private final GeminiClient geminiClient;
    private final GeminiProperties geminiProperties;

    public List<String> generateKeywords(Food food) {
        if (isFoodInvalid(food)) {
            log.warn("Skipping keyword generation: incomplete food data");
            return Collections.emptyList();
        }
        GeminiRequest request = createRequest(food);
        try {
            log.debug("Requesting Gemini keyword generation for food='{}'", food.getProductName());
            GeminiResponse response = geminiClient.generateContent(
                    geminiProperties.getApiKey(),
                    request
            );
            return extractKeywords(response, food.getProductName());
        } catch (Exception e) {
            log.error("Failed to generate keywords for '{}'", food.getProductName(), e);
        }
        return List.of();
    }

    private List<String> extractKeywords(GeminiResponse response, String productName) {
        if (response == null
                || response.getCandidates() == null
                || response.getCandidates().isEmpty()) {
            return Collections.emptyList();
        }
        String content = response.getCandidates().getFirst()
                .getContent().getParts().getFirst()
                .getText().trim();
        if ("unknown".equalsIgnoreCase(content)) {
            log.debug("Gemini returned 'unknown' for '{}'", productName);
            return Collections.emptyList();
        }
        List<String> keywords = Arrays.stream(content.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
        log.info("Generated {} keywords for '{}'", keywords.size(), productName);
        return keywords;
    }

    private GeminiRequest createRequest(Food food) {
        String promptText = buildPromptText(food);
        return new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part(promptText)))
        ));
    }

    private boolean isFoodInvalid(Food food) {
        return food == null || food.getProductName() == null || food.getNutriments() == null;
    }

    private static String buildPromptText(Food food) {
        return ("You are given information about a food product.\n"
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
                        food.getNutriments().getCalories(),
                        food.getNutriments().getFat(),
                        food.getNutriments().getProtein(),
                        food.getNutriments().getCarbohydrates()
                );
    }
}
