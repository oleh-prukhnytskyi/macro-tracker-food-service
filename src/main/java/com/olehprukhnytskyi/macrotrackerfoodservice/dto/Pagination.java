package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Pagination metadata")
public class Pagination {
    @Schema(
            description = "Number of items to skip",
            example = "0",
            minimum = "0"
    )
    private int offset;

    @Schema(
            description = "Maximum number of items to return",
            example = "20",
            minimum = "1",
            maximum = "100"
    )
    private int limit;

    @Schema(
            description = "Total number of items available",
            example = "150",
            minimum = "0"
    )
    private int total;
}
