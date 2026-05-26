package com.pss.proxy.image.service.routes;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public interface RouteService {


     void prepareRequest(MultiMap httpRequest, HttpServerRequest request,
                        HttpServerResponse response, String path);


}
