package com.olehprukhnytskyi.macrotrackerfoodservice.repository;

import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
    void deleteByIdAndUserId(String id, Long userId);
}
