package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.olehprukhnytskyi.macrotrackerfoodservice.validation.AtLeastOneNutrientPresent;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;

@AtLeastOneNutrientPresent
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

    @JsonProperty("per100gAvailable")
    public boolean isPer100gSet() {
        return Stream.of(kcal, fat, proteins, carbohydrates)
                .anyMatch(Objects::nonNull);
    }

    @JsonProperty("perPieceAvailable")
    public boolean isPerPieceSet() {
        return Stream.of(kcalPerPiece, fatPerPiece, proteinsPerPiece, carbohydratesPerPiece)
                .anyMatch(Objects::nonNull);
    }
}
