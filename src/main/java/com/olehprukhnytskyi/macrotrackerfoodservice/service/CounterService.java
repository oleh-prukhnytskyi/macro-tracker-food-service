package com.olehprukhnytskyi.macrotrackerfoodservice.service;

import com.olehprukhnytskyi.exception.InternalServerException;
import com.olehprukhnytskyi.exception.error.CommonErrorCode;
import com.olehprukhnytskyi.macrotrackerfoodservice.model.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounterService {
    private final MongoTemplate mongoTemplate;

    public Long getNextSequence(String sequenceName) {
        log.debug("Generating next sequence for '{}'", sequenceName);
        try {
            Query query = new Query(Criteria.where("_id").is(sequenceName));
            Update update = new Update().inc("sequence", 1);
            FindAndModifyOptions options = FindAndModifyOptions.options()
                    .returnNew(true)
                    .upsert(true);
            Counter counter = mongoTemplate.findAndModify(query, update, options, Counter.class);
            Long next = (counter != null && counter.getSequence() != null)
                    ? counter.getSequence()
                    : 1L;
            log.trace("Next sequence for '{}' = {}", sequenceName, next);
            return next;
        } catch (Exception e) {
            log.error("Failed to get next sequence for '{}'", sequenceName, e);
            throw new InternalServerException(CommonErrorCode.INTERNAL_ERROR,
                    "Failed to get next sequence for: " + sequenceName, e);
        }
    }
}
