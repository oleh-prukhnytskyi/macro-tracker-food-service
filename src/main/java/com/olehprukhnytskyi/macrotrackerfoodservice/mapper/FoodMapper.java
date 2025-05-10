package com.olehprukhnytskyi.macrotrackerfoodservice.mapper;

import com.olehprukhnytskyi.macrotrackerfoodservice.config.MapperConfig;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodPatchRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodRequestDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.FoodResponseDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = MapperConfig.class)
public interface FoodMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "keywords", ignore = true),
            @Mapping(target = "dataHash", ignore = true)
    })
    Food toModel(FoodRequestDto requestDto);

    FoodResponseDto toDto(Food food);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "keywords", ignore = true),
            @Mapping(target = "dataHash", ignore = true),
            @Mapping(target = "code", ignore = true)
    })
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFoodFromPatchDto(FoodPatchRequestDto dto, @MappingTarget Food entity);
}
