package com.pss.image.proxy.service.routes;

import com.pss.image.proxy.service.QueryParamService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public final class UnprotectedRouteService implements RouteService {

    private final QueryParamService queryParamService;

    public UnprotectedRouteService(QueryParamService queryParamService) {
        this.queryParamService = queryParamService;
    }

    @Override
    public void prepareRequest(MultiMap httpRequest, HttpServerRequest request, HttpServerResponse response, String path) {
        String param = queryParamService.extractQueryParam(request, path);
        httpRequest.add("profile", param);
    }

}
