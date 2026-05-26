package com.pss.proxy.image.routes;

import com.pss.proxy.image.service.ClientFailureHandler;
import com.pss.proxy.image.service.routes.RouteService;
import com.pss.proxy.image.utils.Utils;
import com.pss.vertx.common.routes.AbstractHandler;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.http.*;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.VisibleForTesting;

import static com.pss.proxy.image.utils.Utils.buildUri;


public class QueryRouteHandler extends AbstractHandler {


    private static final Logger log = LogManager.getLogger(QueryRouteHandler.class);
    private static final Marker NOT_FOUND = MarkerManager.getMarker("404_NOT_FOUND");

    private final HttpClient client;
    private final RouteService routeService;
    private final ClientFailureHandler clientFailureHandler;




    public QueryRouteHandler(HttpClient client, RouteService routeService, ClientFailureHandler clientFailureHandler) {
        this.client = client;
        this.routeService = routeService;

        this.clientFailureHandler = clientFailureHandler;
    }

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        MultiMap query = MultiMap.caseInsensitiveMultiMap();
        routeService.prepareRequest(query, request, response, path);
        RequestOptions options = new RequestOptions();
        Utils.addHeaders(options, request);
        String uri = buildUri(path, query);
        log.debug("uri: {}", uri);
        options.setURI(uri);
        client.request(options)
                .compose(HttpClientRequest::send)
                .onFailure(clientFailureHandler.handle(true, event, response))
                .onSuccess(httpClientResponse -> this.handleResponse(event, request, response, path, httpClientResponse));
    }

    @VisibleForTesting
    protected Future<Void> handleResponse(RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path, HttpClientResponse clientResponse) {

        if(clientResponse.statusCode() != 200){
            if(clientResponse.statusCode() == 403) {
                log.error("JWT token expired: {}, status: {}", path, clientResponse.statusCode());
            } else if(clientResponse.statusCode() == 404) {
                log.error(NOT_FOUND, "Image not found: {}", path);
            } else {
                log.error("Error encountered: {}, status: {}, message: {}", path, clientResponse.statusCode(), clientResponse.statusMessage());
            }
            event.next();
        } else {
            if(!response.ended()) {
                response.headers().addAll(clientResponse.headers());
                return clientResponse.pipeTo(response);
            }
        }
        return Future.succeededFuture();
    }


}
