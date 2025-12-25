package com.olehprukhnytskyi.macrotrackerfoodservice.listener;

import com.olehprukhnytskyi.macrotrackerfoodservice.event.FoodCreatedEvent;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import com.olehprukhnytskyi.macrotrackerfoodservice.repository.mongo.FoodRepository;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.GeminiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoodListener {
    private final FoodRepository foodRepository;
    private final GeminiService geminiService;

    @Async
    @EventListener
    @Transactional
    public void handleFoodCreated(FoodCreatedEvent event) {
        log.info("Starting background enrichment for foodId={}", event.getFoodId());
        try {
            Food food = foodRepository.findById(event.getFoodId()).orElse(null);
            if (food != null) {
                List<String> keywords = geminiService.generateKeywords(food);
                food.setKeywords(keywords);
                foodRepository.save(food);
                log.info("Successfully enriched foodId={} with {} keywords",
                        event.getFoodId(), keywords.size());
            }
        } catch (Exception e) {
            log.error("Failed to generate keywords for foodId={}", event.getFoodId(), e);
        }
    }
}
