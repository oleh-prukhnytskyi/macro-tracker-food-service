package com.olehprukhnytskyi.macrotrackerfoodservice.service.impl;

import com.olehprukhnytskyi.macrotrackerfoodservice.exception.InternalServerErrorException;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Counter;
import com.olehprukhnytskyi.macrotrackerfoodservice.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CounterServiceImpl implements CounterService {
    private final MongoTemplate mongoTemplate;

    @Override
    public Long getNextSequence(String sequenceName) {
        try {
            Query query = new Query(Criteria.where("_id").is(sequenceName));
            Update update = new Update().inc("sequence", 1);
            FindAndModifyOptions options = FindAndModifyOptions.options()
                    .returnNew(true)
                    .upsert(true);
            Counter counter = mongoTemplate
                    .findAndModify(query, update, options, Counter.class);
            return (counter != null && counter.getSequence() != null)
                    ? counter.getSequence() : 1L;
        } catch (Exception e) {
            throw new InternalServerErrorException(
                    "Failed to get next sequence for: " + sequenceName, e);
        }
    }
}
