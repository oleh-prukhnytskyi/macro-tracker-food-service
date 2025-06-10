package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface FoodService {
    FoodResponseDto createFoodWithImages(FoodRequestDto dto, MultipartFile image, Long userId);

    List<FoodResponseDto> findByQuery(String query, int offset, int limit);

    FoodResponseDto findById(String id);

    List<String> getSearchSuggestions(String query);

    FoodResponseDto patch(String id, FoodPatchRequestDto dto);

    void deleteByIdAndUserId(String id, Long userId);
}
