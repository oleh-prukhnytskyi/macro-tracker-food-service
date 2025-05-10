package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import java.util.List;

public interface GeminiService {
    List<String> generateKeywords(Food food);
}
