package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoodCodeGenerator {
    private final CounterService counterService;

    public String resolveCode(FoodRequestDto request) {
        return (request.getCode() != null && isValidCode(request.getCode()))
                ? request.getCode()
                : generateInternalCode();
    }

    public boolean isValidCode(String code) {
        return code != null && code.matches("\\d{8}|\\d{12}|\\d{13}|\\d{24}");
    }

    private String generateInternalCode() {
        Long sequence = counterService.getNextSequence("food_code");
        return "200" + String.format("%010d", sequence);
    }
}
