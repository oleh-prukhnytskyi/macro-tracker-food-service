package com.olehprukhnytskyi.macrotrackerfoodservice.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "gemini-api", url = "https://generativelanguage.googleapis.com")
public interface GeminiClient {

    @PostMapping(value = "/v1beta/models/gemini-2.0-flash:generateContent")
    ResponseEntity<String> generateContent(
            @RequestParam("key") String apiKey,
            @RequestBody Map<String, Object> requestBody
    );
}
