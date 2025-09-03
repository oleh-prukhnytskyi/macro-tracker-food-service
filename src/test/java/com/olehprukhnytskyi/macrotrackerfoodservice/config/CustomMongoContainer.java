package com.olehprukhnytskyi.macrotrackerfoodservice.config;

import org.testcontainers.containers.MongoDBContainer;

public class CustomMongoContainer extends MongoDBContainer {
    private static final String IMAGE_VERSION = "mongo:6.0.5";
    private static CustomMongoContainer container;

    private CustomMongoContainer() {
        super(IMAGE_VERSION);
    }

    public static synchronized CustomMongoContainer getInstance() {
        if (container == null) {
            container = new CustomMongoContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.data.mongodb.uri", container.getReplicaSetUrl());
    }

    @Override
    public void stop() {
    }
}
