package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FoodPatchRequestDto {
    @Size(max = 100)
    private String productName;

    @Size(max = 100)
    private String genericName;

    @Size(max = 100)
    private String brands;

    @Valid
    private NutrimentsPatchDto nutriments;
}
