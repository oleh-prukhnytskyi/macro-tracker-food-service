package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import lombok.Data;

@Data
public class FoodResponseDto {
    private String id;
    private String code;
    private String productName;
    private String genericName;
    private String imageUrl;
    private String brands;
    private NutrimentsDto nutriments;
}
