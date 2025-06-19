package com.olehprukhnytskyi.macrotrackerfoodservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Data
public class Nutriments {
    @JsonProperty("energy-kcal")
    @Field(name = "energy-kcal", targetType = FieldType.DOUBLE)
    private BigDecimal kcal;

    @Field(targetType = FieldType.DOUBLE)
    private BigDecimal fat;

    @Field(targetType = FieldType.DOUBLE)
    private BigDecimal proteins;

    @Field(targetType = FieldType.DOUBLE)
    private BigDecimal carbohydrates;

    @JsonProperty("energy-kcal_piece")
    @Field(name = "energy-kcal_piece", targetType = FieldType.DOUBLE)
    @DecimalMin(value = "0.0")
    private BigDecimal kcalPerPiece;

    @JsonProperty("fat_piece")
    @Field(name = "fat_piece", targetType = FieldType.DOUBLE)
    private BigDecimal fatPerPiece;

    @JsonProperty("proteins_piece")
    @Field(name = "proteins_piece", targetType = FieldType.DOUBLE)
    private BigDecimal proteinsPerPiece;

    @JsonProperty("carbohydrates_piece")
    @Field(name = "carbohydrates_piece", targetType = FieldType.DOUBLE)
    private BigDecimal carbohydratesPerPiece;
}
