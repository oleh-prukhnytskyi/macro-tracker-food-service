package com.olehprukhnytskyi.macrotrackerfoodservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class MacroTrackerFoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroTrackerFoodServiceApplication.class, args);
    }

}
