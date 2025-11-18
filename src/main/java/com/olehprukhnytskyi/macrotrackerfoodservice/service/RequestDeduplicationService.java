package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olehprukhnytskyi.exception.InternalServerException;
import com.olehprukhnytskyi.exception.error.EventErrorCode;
import com.olehprukhnytskyi.macrotrackerfoodservice.util.ProcessedEntityType;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestDeduplicationService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public String buildRequestKey(ProcessedEntityType type, String requestId, Long userId) {
        return String.format("processed:%s:%d:%s", type.name().toLowerCase(), userId, requestId);
    }

    public boolean isProcessed(ProcessedEntityType type, String requestId, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildRequestKey(type, requestId, userId)));
    }

    public <T> void markAsProcessed(ProcessedEntityType type, String requestId,
                                    Long userId, T value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(
                    buildRequestKey(type, requestId, userId),
                    json,
                    1, TimeUnit.HOURS
            );
        } catch (JsonProcessingException e) {
            throw new InternalServerException(EventErrorCode.EVENT_SERIALIZATION_FAILED,
                    "Failed to serialize processed object", e);
        }
    }

    public <T> Optional<T> getProcessed(ProcessedEntityType type, String requestId,
                                        Long userId, Class<T> clazz) {
        String key = buildRequestKey(type, requestId, userId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
