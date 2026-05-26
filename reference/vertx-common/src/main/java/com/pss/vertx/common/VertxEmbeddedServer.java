package com.pss.vertx.common;

import com.pss.vertx.common.server.RouteRegistration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class VertxEmbeddedServer implements EmbeddedServer {

    private static final Logger log = LogManager.getLogger(VertxEmbeddedServer.class);

    protected final CountDownLatch latch = new CountDownLatch(1);
    protected final AtomicBoolean started = new AtomicBoolean(false);
    protected final ApplicationConfiguration configuration;
    protected final ApplicationContext applicationContext;
    protected final RouteRegistration routeRegistration;
    protected final Router router;
    protected final Vertx vertx;
    protected final HttpServer server;
    protected final HttpServerOptions serverOptions;

    @Inject
    public VertxEmbeddedServer(HttpServer server,
                               RouteRegistration routeRegistration,
                               ApplicationConfiguration configuration,
                               ApplicationContext applicationContext, Router router,
                               Vertx vertx,
                               HttpServerOptions serverOptions) {
        this.server = server;
        this.configuration = configuration;
        this.applicationContext = applicationContext;
        this.router = router;
        this.vertx = vertx;
        this.serverOptions = serverOptions;
        this.routeRegistration = routeRegistration;
    }

    @Override
    public int getPort() {
        return server.actualPort();
    }

    @Override
    public String getHost() {
        return serverOptions.getHost();
    }

    @Override
    public String getScheme() {
        return serverOptions.isSsl() ? "https" : "http";
    }

    @Override
    public URL getURL() {
        try {
            return getURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI getURI() {
        return URI.create(getScheme() + "://" + getHost() + ":" + getPort());
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return configuration;
    }

    @Override
    public boolean isRunning() {
        return started.get();
    }

    @Override
    public synchronized @NonNull EmbeddedServer start() {
        if(!isRunning()) {
            routeRegistration.registerRoutes(router);
             server.requestHandler(router)
                    .listen()
                    .onSuccess(s-> {
                        started.set(true);
                        log.info("Server started, listening on port: {}", s.actualPort());
                    }).andThen(x-> {
                        latch.countDown();
                    });

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public @NonNull EmbeddedServer stop() {
        vertx.close();
        return EmbeddedServer.super.stop();
    }
}
