package com.olehprukhnytskyi.macrotrackerfoodservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
        info = @Info(
                title = "Food Service API",
                version = "1.0",
                description = "Microservice for food product management, "
                        + "search and nutrition information"
        )
)
@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class MacroTrackerFoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerFoodServiceApplication.class, args);
    }

}
