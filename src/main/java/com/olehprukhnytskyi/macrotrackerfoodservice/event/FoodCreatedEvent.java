package com.olehprukhnytskyi.macrotrackerfoodservice.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FoodCreatedEvent {
    private final String foodId;
    private final Long userId;
}
