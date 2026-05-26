package com.pss.proxy.image.service;

import com.pss.proxy.image.utils.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class ClientFailureHandler {

    private static final Logger log = LogManager.getLogger(ClientFailureHandler.class);

    protected final MultiTryService multiTryService;

    public ClientFailureHandler(MultiTryService multiTryService) {
        this.multiTryService = multiTryService;
    }

    /**
     * Handle hard failures on client operations when calling to Sirv backend, these are non http responses, this cropped
     * up do to a DNS issue in my misconfigured `/etc/hosts` file missing a `localhost` entry.
     *
     * @param tryNext when true continue the handler chain
     * @param context routing context
     * @param serverResponse server response
     * @return a handler responsible for handling client errors before sending to Sirv
     */
    public Handler<Throwable> handle(boolean tryNext, RoutingContext context, HttpServerResponse serverResponse) {
        return throwable -> {
            log.error("Client Failure Handler", throwable);
            if (tryNext) {
                context.next();
            } else if(!serverResponse.ended()){
                serverResponse.putHeader("Content-Type", "image/png");
                multiTryService.setHeaders(serverResponse);
                serverResponse.end(Utils.png);
            }
        };
    }
}
