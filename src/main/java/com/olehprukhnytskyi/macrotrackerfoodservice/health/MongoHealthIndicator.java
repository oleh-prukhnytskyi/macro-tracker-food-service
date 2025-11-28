package com.olehprukhnytskyi.macrotrackerfoodservice.health;

import org.bson.Document;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MongoHealthIndicator extends AbstractHealthIndicator {
    private final MongoTemplate mongoTemplate;

    public MongoHealthIndicator(MongoTemplate mongoTemplate) {
        super("MongoDB health check failed");
        Assert.notNull(mongoTemplate, "'mongoTemplate' must not be null");
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            Document result = this.mongoTemplate.executeCommand("{ buildInfo: 1 }");
            builder.up()
                    .withDetail("version", result.getString("version"))
                    .withDetail("ok", result.get("ok"));
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
