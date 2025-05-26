package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import java.util.List;

public interface FoodService {
    FoodResponseDto save(FoodRequestDto dto);

    List<FoodResponseDto> findByQuery(String query, int offset, int limit);

    FoodResponseDto findById(String id);

    List<String> getSearchSuggestions(String query);

    FoodResponseDto patch(String id, FoodPatchRequestDto dto);
}
