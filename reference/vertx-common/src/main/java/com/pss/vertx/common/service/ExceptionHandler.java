package com.pss.vertx.common.service;

import io.vertx.core.Handler;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class ExceptionHandler implements Handler<Throwable> {

    private static final Logger log = LogManager.getLogger(ExceptionHandler.class);

    @Override
    public void handle(Throwable event) {
        log.error("Error Thrown", event);
    }
}
