package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class NutrimentsPatchDto {
    @DecimalMin(value = "0.0")
    private BigDecimal kcal;

    @DecimalMin(value = "0.0")
    private BigDecimal fat;

    @DecimalMin(value = "0.0")
    private BigDecimal proteins;

    @DecimalMin(value = "0.0")
    private BigDecimal carbohydrates;
}
