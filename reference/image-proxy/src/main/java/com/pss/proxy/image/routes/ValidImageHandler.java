package com.pss.proxy.image.routes;


import com.pss.proxy.image.utils.Utils;
import com.pss.vertx.common.routes.AbstractHandler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Singleton
public class ValidImageHandler extends AbstractHandler {

    private static final Logger log = LogManager.getLogger(ValidImageHandler.class);

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        if(Utils.isImage(path)){
            event.next();
        } else {
            log(request, path);
            event.fail(400);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private static void log(HttpServerRequest request, String route) {
        if(log.isDebugEnabled()){
            String ip = request.connection().remoteAddress().host();
            StringBuilder sb = new StringBuilder(512);
            for (Map.Entry<String, String> header : request.headers()) {
                if(!"Cookie".equalsIgnoreCase(header.getKey())) {
                    sb.append("\n    ").append(header.getKey()).append(": ").append(header.getValue());
                }
            }
            log.debug("Incoming request: Route: {}, IP: {}, Path: {} Params: {} Headers: {}", route, ip, request.path(), request.params(), sb);
        }
    }




}
