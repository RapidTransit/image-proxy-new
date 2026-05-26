package com.pss.proxy.image.service.routes;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Singleton;

@Singleton
public class DynamicRouteService implements RouteService {

    @Override
    public void prepareRequest(MultiMap httpRequest, HttpServerRequest request, HttpServerResponse response, String path) {
        httpRequest.addAll(request.params());
    }

}
