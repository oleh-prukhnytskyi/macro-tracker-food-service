package com.olehprukhnytskyi.macrotrackerfoodservice.job;

import com.olehprukhnytskyi.macrotrackerfoodservice.model.OutboxEvent;
import com.olehprukhnytskyi.macrotrackerfoodservice.repository.jpa.OutboxRepository;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.S3StorageService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxJob {
    private final OutboxRepository outboxRepository;
    private final S3StorageService s3StorageService;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processFoodDeletedEvents() {
        List<OutboxEvent> events = outboxRepository
                .findTop100ByProcessedFalseAndEventTypeOrderByCreatedAtAsc("FOOD_DELETED");
        if (events.isEmpty()) {
            return;
        }

        List<OutboxEvent> processedEvents = new ArrayList<>();
        for (OutboxEvent event : events) {
            try {
                s3StorageService.deleteFolder("images/products/" + event.getAggregateId() + "/");
                event.setProcessed(true);
                event.setProcessedAt(Instant.now());
                processedEvents.add(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
        if (!processedEvents.isEmpty()) {
            outboxRepository.saveAll(processedEvents);
        }
    }
}
