package com.olehprukhnytskyi.macrotrackerfoodservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
}
