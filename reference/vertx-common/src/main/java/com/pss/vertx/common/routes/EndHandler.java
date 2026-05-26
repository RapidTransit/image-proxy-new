package com.pss.vertx.common.routes;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class EndHandler extends AbstractHandler {

    private static final Logger log = LogManager.getLogger(EndHandler.class);

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request,
                                  HttpServerResponse response, String path) {
        if(!response.ended()){
            log.warn("Response did not end");
            response.end();
        }
    }
}
