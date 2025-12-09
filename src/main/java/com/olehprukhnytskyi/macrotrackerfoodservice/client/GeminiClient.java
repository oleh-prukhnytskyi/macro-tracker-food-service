package com.olehprukhnytskyi.macrotrackerfoodservice.client;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.GeminiRequest;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "gemini-api", url = "https://generativelanguage.googleapis.com")
public interface GeminiClient {
    @PostMapping(value = "/v1beta/models/gemini-2.5-flash:generateContent")
    GeminiResponse generateContent(
            @RequestHeader("x-goog-api-key") String apiKey,
            @RequestBody GeminiRequest requestBody
    );
}
