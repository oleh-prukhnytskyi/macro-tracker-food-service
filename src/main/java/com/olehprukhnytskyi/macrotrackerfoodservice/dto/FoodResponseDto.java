package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponseDto {
    private String id;
    private String code;
    private String productName;
    private String genericName;
    private String imageUrl;
    private String brands;
    private NutrimentsDto nutriments;
}
