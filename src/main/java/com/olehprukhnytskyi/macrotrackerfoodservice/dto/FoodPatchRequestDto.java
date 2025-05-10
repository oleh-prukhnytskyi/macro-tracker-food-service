package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class FoodPatchRequestDto {
    @Size(max = 100)
    private String productName;

    @Size(max = 100)
    private String genericName;

    @URL
    @Size(max = 300)
    private String imageUrl;

    @Size(max = 100)
    private String brands;

    @Valid
    private NutrimentsPatchDto nutriments;
}
