package com.pss.image.proxy.routes;

import static com.pss.image.proxy.util.Util.buildUri;

import com.pss.image.proxy.service.ClientFailureHandler;
import com.pss.image.proxy.service.routes.RouteService;
import com.pss.image.proxy.util.Util;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class QueryRouteHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(QueryRouteHandler.class);
    private static final Marker NOT_FOUND = MarkerFactory.getMarker("404_NOT_FOUND");
    private static final Marker UNSUPPORTED_MEDIA = MarkerFactory.getMarker("415_UNSUPPORTED_MEDIA");

    private final HttpClient client;
    private final RouteService routeService;
    private final ClientFailureHandler clientFailureHandler;

    public QueryRouteHandler(HttpClient client, RouteService routeService, ClientFailureHandler clientFailureHandler) {
        this.client = client;
        this.routeService = routeService;
        this.clientFailureHandler = clientFailureHandler;
    }

    @Override
    protected void handleInternal(
            RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        var query = MultiMap.caseInsensitiveMultiMap();
        routeService.prepareRequest(query, request, response, path);
        var options = new RequestOptions();
        Util.addHeaders(options, request);
        var uri = buildUri(path, query);
        log.debug("uri: {}", uri);
        options.setURI(uri);
        client.request(options)
                .compose(HttpClientRequest::send)
                .onFailure(clientFailureHandler.handle(true, event, response))
                .onSuccess(httpClientResponse -> handleResponse(event, request, response, path, httpClientResponse));
    }

    protected Future<Void> handleResponse(
            RoutingContext event,
            HttpServerRequest request,
            HttpServerResponse response,
            String path,
            HttpClientResponse clientResponse) {
        if (clientResponse.statusCode() != 200) {
            if (clientResponse.statusCode() == 403) {
                log.error("JWT token expired: {}, status: {}", path, clientResponse.statusCode());
            } else if (clientResponse.statusCode() == 404) {
                log.error(NOT_FOUND, "Image not found: {}", path);
            } else if (clientResponse.statusCode() == 415) {
                log.error(UNSUPPORTED_MEDIA, "Image rejected as unsupported (likely oversized): {}", path);
            } else {
                log.error(
                        "Error encountered: {}, status: {}, message: {}",
                        path,
                        clientResponse.statusCode(),
                        clientResponse.statusMessage());
            }
            event.next();
        } else {
            if (!response.ended()) {
                response.headers().addAll(clientResponse.headers());
                return clientResponse.pipeTo(response);
            }
        }
        return Future.succeededFuture();
    }
}
