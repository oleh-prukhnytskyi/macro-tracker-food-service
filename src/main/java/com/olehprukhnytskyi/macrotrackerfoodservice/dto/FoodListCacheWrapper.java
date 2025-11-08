package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cached food list wrapper")
public class FoodListCacheWrapper {
    @Schema(description = "List of food products")
    private List<FoodResponseDto> items;
}
