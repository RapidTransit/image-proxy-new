package com.pss.vertx.common.routes;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public abstract class AbstractHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext event) {
        handleInternal(event, event.request(), event.response(), event.normalizedPath());
    }

    /**
     * This method makes it easier to test handlers
     * @param event
     * @param request
     * @param response
     * @param path
     */
    protected abstract void handleInternal(RoutingContext event, HttpServerRequest request,
                                                   HttpServerResponse response, String path);
}
