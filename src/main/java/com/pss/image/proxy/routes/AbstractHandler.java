package com.pss.image.proxy.routes;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext event) {
        handleInternal(event, event.request(), event.response(), event.normalizedPath());
    }

    protected abstract void handleInternal(
            RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path);
}
