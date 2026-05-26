package com.pss.vertx.common.server;

import io.micronaut.context.annotation.Context;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//@Singleton
//@Context
public class ServerBootstrap {

    private static final Logger log = LogManager.getLogger(ServerBootstrap.class);

    protected final RouteRegistration routeRegistration;
    protected final Router router;
    protected final HttpServer httpServer;

//    @Inject
    public ServerBootstrap(RouteRegistration routeRegistration, Router router, HttpServer httpServer) {
        this.routeRegistration = routeRegistration;
        this.router = router;
        this.httpServer = httpServer;
    }


    /**
     * Startup the server, return a future so error handling can be dealt with in the calling class
     * @return the Future HttpServer or an Error
     */
    //@PostConstruct
    public Future<HttpServer> start() {
        routeRegistration.registerRoutes(router);
        return httpServer.requestHandler(router)
                .listen()
                .onSuccess(s-> log.info("Server started, listening on port: {}", s.actualPort()));

    }
}
