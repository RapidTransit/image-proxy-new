package com.pss.proxy.image.service.routes;

import com.pss.proxy.image.config.properties.ProxyConfig;
import com.pss.proxy.image.service.QueryParamService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JwtRouteService implements RouteService {

    private final QueryParamService queryParamService;
    private final ProxyConfig proxyConfig;

    @Inject
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
