package com.olehprukhnytskyi.macrotrackerfoodservice.controller;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.PagedResponse;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.Pagination;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.FoodService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/foods")
public class FoodController {
    private final FoodService foodService;

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponseDto> findById(
            @PathVariable String id) {
        FoodResponseDto food = foodService.findById(id);
        return ResponseEntity.ok(food);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FoodResponseDto>> findByQuery(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "25") @Min(1) int limit) {
        List<FoodResponseDto> foods = foodService.findByQuery(query, offset, limit);
        Pagination pagination = new Pagination(offset, limit, foods.size());
        return ResponseEntity
                .status(foods.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK)
                .body(new PagedResponse<>(foods, pagination));
    }

    @GetMapping("/search-suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @RequestParam String query) {
        List<String> suggestions = foodService.getSearchSuggestions(query);
        return suggestions.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(suggestions);
    }

    @PostMapping
    public ResponseEntity<FoodResponseDto> save(
            @RequestBody @Valid FoodRequestDto requestDto) {
        FoodResponseDto saved = foodService.save(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FoodResponseDto> patch(
            @PathVariable String id,
            @RequestBody @Valid FoodPatchRequestDto dto) {
        FoodResponseDto updated = foodService.patch(id, dto);
        return ResponseEntity.ok(updated);
    }
}
