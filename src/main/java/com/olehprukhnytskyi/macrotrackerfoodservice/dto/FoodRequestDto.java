package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class FoodRequestDto {
    @Size(min = 8, max = 24, message = "The barcode length must be between 8 and 24 characters")
    @Pattern(regexp = "\\d+", message = "The barcode must contain digits only")
    private String code;

    @NotNull
    @Size(max = 100)
    private String productName;

    @Size(max = 100)
    private String genericName;

    @URL
    @Size(max = 300)
    private String imageUrl;

    @Size(max = 100)
    private String brands;

    @NotNull
    private NutrimentsDto nutriments;
}
