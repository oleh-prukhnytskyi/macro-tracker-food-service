package com.olehprukhnytskyi.macrotrackerfoodservice.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "counters")
public class Counter {
    @MongoId
    private String id;
    private Long sequence;
}
