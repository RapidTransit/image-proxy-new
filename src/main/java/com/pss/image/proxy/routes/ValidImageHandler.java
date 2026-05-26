package com.pss.image.proxy.routes;

import com.pss.image.proxy.util.Util;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ValidImageHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(ValidImageHandler.class);

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        if (Util.isImage(path)) {
            event.next();
        } else {
            log(request, path);
            event.fail(400);
        }
    }

    private static void log(HttpServerRequest request, String route) {
        if (log.isDebugEnabled()) {
            String ip = request.connection().remoteAddress().host();
            var sb = new StringBuilder(512);
            for (Map.Entry<String, String> header : request.headers()) {
                if (!"Cookie".equalsIgnoreCase(header.getKey())) {
                    sb.append("\n    ").append(header.getKey()).append(": ").append(header.getValue());
                }
            }
            log.debug("Incoming request: Route: {}, IP: {}, Path: {} Params: {} Headers: {}", route, ip, request.path(), request.params(), sb);
        }
    }
}
