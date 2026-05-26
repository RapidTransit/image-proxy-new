package com.pss.image.proxy.service;

import com.pss.image.proxy.util.Util;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientFailureHandler.class);

    protected final MultiTryService multiTryService;

    public ClientFailureHandler(MultiTryService multiTryService) {
        this.multiTryService = multiTryService;
    }

    /// Handle hard failures on client operations when calling to Sirv backend.
    /// These are non-HTTP responses that can arise from DNS issues or misconfigured hosts.
    public Handler<Throwable> handle(boolean tryNext, RoutingContext context, HttpServerResponse serverResponse) {
        return throwable -> {
            log.error("Client Failure Handler", throwable);
            if (tryNext) {
                context.next();
            } else if (!serverResponse.ended()) {
                serverResponse.putHeader("Content-Type", "image/png");
                multiTryService.setHeaders(serverResponse);
                serverResponse.end(Util.png);
            }
        };
    }
}
