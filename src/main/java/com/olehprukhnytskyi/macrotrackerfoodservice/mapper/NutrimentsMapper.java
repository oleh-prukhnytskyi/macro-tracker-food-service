package com.olehprukhnytskyi.macrotrackerfoodservice.mapper;

import com.olehprukhnytskyi.macrotrackerfoodservice.config.MapperConfig;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.NutrimentsDto;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Nutriments;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface NutrimentsMapper {
    Nutriments toModel(NutrimentsDto nutrimentsDto);
}
