package com.pss.image.proxy.service.routes;

import com.pss.image.proxy.config.ProxyConfig;
import com.pss.image.proxy.service.QueryParamService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public final class JwtRouteService implements RouteService {

    private final QueryParamService queryParamService;
    private final ProxyConfig proxyConfig;

    public JwtRouteService(QueryParamService queryParamService, ProxyConfig proxyConfig) {
        this.queryParamService = queryParamService;
        this.proxyConfig = proxyConfig;
    }

    @Override
    public void prepareRequest(MultiMap httpRequest, HttpServerRequest request, HttpServerResponse response, String path) {
        String param = queryParamService.extractQueryParam(request, path);
        String jwt = proxyConfig.jwtMappings().getOrDefault(param, proxyConfig.defaultProfile());
        httpRequest.add("jwt", jwt);
    }

}
