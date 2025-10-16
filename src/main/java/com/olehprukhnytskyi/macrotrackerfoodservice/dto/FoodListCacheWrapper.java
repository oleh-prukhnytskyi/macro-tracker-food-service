package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodListCacheWrapper {
    private List<FoodResponseDto> items;
}
