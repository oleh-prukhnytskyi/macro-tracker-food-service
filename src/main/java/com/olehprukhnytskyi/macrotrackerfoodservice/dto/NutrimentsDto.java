package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;

@Data
public class NutrimentsDto {
    @DecimalMin(value = "0.0")
    private BigDecimal kcal;

    @DecimalMin(value = "0.0")
    private BigDecimal fat;

    @DecimalMin(value = "0.0")
    private BigDecimal proteins;

    @DecimalMin(value = "0.0")
    private BigDecimal carbohydrates;

    @DecimalMin(value = "0.0")
    private BigDecimal kcalPerPiece;

    @DecimalMin(value = "0.0")
    private BigDecimal fatPerPiece;

    @DecimalMin(value = "0.0")
    private BigDecimal proteinsPerPiece;

    @DecimalMin(value = "0.0")
    private BigDecimal carbohydratesPerPiece;

    @AssertTrue(message = "At least one nutrient must be filled")
    public boolean isAtLeastOneSet() {
        return Stream.of(kcal, fat, proteins, carbohydrates,
                        kcalPerPiece, fatPerPiece, proteinsPerPiece, carbohydratesPerPiece)
                .anyMatch(Objects::nonNull);
    }
}
