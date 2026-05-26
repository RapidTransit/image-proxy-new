package com.pss.proxy.image.service.routes;

import com.pss.proxy.image.service.QueryParamService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class UnprotectedRouteService implements RouteService {

    private final QueryParamService queryParamService;

    @Inject
    public UnprotectedRouteService(QueryParamService queryParamService) {
        this.queryParamService = queryParamService;
    }

    @Override
    public void prepareRequest(MultiMap httpRequest, HttpServerRequest request, HttpServerResponse response, String path) {
        String param = queryParamService.extractQueryParam(request, path);
        httpRequest.add("profile", param);
    }

}
