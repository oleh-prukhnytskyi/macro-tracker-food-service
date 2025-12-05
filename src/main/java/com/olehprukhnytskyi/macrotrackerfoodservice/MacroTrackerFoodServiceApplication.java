package com.olehprukhnytskyi.macrotrackerfoodservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@OpenAPIDefinition(
        info = @Info(
                title = "Food Service API",
                version = "1.0",
                description = "Microservice for food product management, "
                        + "search and nutrition information"
        )
)
@EnableJpaRepositories(basePackages = {
        "com.olehprukhnytskyi.repository.jpa"
})
@EnableMongoRepositories(basePackages = {
        "com.olehprukhnytskyi.macrotrackerfoodservice.repository.mongo"
})
@EntityScan(basePackages = {
        "com.olehprukhnytskyi.macrotrackerfoodservice.model",
        "com.olehprukhnytskyi.model"
})
@EnableCaching
@EnableFeignClients
@SpringBootApplication
@ConfigurationPropertiesScan
public class MacroTrackerFoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerFoodServiceApplication.class, args);
    }

}
