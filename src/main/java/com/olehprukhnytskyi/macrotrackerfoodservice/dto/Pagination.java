package com.olehprukhnytskyi.macrotrackerfoodservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagination {
    private int offset;
    private int limit;
    private int total;
}
