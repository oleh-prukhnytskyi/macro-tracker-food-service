package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Food product partial update request")
public class FoodPatchRequestDto {
    @Schema(description = "Product name", example = "Updated Chicken Breast", maxLength = 100)
    @Size(max = 100)
    private String productName;

    @Schema(description = "Generic product name", example = "Updated Poultry", maxLength = 100)
    @Size(max = 100)
    private String genericName;

    @Schema(description = "Product brands", example = "Updated Brands Inc.", maxLength = 100)
    @Size(max = 100)
    private String brands;

    @Schema(description = "Nutrition information for update")
    @Valid
    private NutrimentsPatchDto nutriments;
}
