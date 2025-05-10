package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class NutrimentsDto {
    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal kcal;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal fat;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal proteins;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal carbohydrates;
}
