package com.pss.image.proxy.service.routes;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public sealed interface RouteService permits DynamicRouteService, JwtRouteService, UnprotectedRouteService {

    void prepareRequest(MultiMap httpRequest, HttpServerRequest request,
                        HttpServerResponse response, String path);

}
