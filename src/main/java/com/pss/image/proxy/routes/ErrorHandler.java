package com.pss.image.proxy.routes;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    @Override
    protected void handleInternal(
            RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        log.error("Error", event.failure());
        if (!response.ended()) {
            if (event.statusCode() != -1) {
                response.setStatusCode(event.statusCode());
            }
            response.end();
        }
    }
}
