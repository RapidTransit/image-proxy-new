package com.pss.image.proxy.routes;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(EndHandler.class);

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request,
                                  HttpServerResponse response, String path) {
        if (!response.ended()) {
            log.warn("Response did not end");
            response.end();
        }
    }
}
