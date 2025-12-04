package com.olehprukhnytskyi.macrotrackerfoodservice.validation.validator;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.validation.AtLeastOneNutrientPresent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.stream.Stream;

public class NutrimentsValidator implements
        ConstraintValidator<AtLeastOneNutrientPresent, NutrimentsDto> {
    @Override
    public boolean isValid(NutrimentsDto dto, ConstraintValidatorContext context) {
        return Stream.of(
                dto.getCalories(), dto.getFat(), dto.getProtein(), dto.getCarbohydrates(),
                dto.getCaloriesPerPiece(), dto.getFatPerPiece(),
                dto.getProteinPerPiece(), dto.getCarbohydratesPerPiece()
        ).anyMatch(Objects::nonNull);
    }
}
