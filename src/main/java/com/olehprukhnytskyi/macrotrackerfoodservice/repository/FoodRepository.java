package com.olehprukhnytskyi.macrotrackerfoodservice.repository;

import com.olehprukhnytskyi.macrotrackerfoodservice.model.Food;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
    Optional<Food> findByDataHash(String dataHash);

    void deleteByIdAndUserId(String id, Long userId);
}
