package com.pss.image.proxy;

import com.pss.image.proxy.config.AppConfig;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

/// Main verticle.
///
/// Holds the verticle-scoped lifecycle: builds the [Wiring], schedules the
/// [com.pss.image.proxy.service.MultiTryService] sweep, optionally runs the
/// startup JWT validation, then opens the HTTP server. Receives its config
/// via constructor — config loading itself happens in [Main] so the same
/// `AppConfig` instance can drive `VertxOptions` before this verticle is
/// even deployed.
public final class MainVerticle extends VerticleBase {

    private static final Logger log = LoggerFactory.getLogger(MainVerticle.class);

    private final AppConfig cfg;
    private final ObjectMapper mapper;

    public MainVerticle(AppConfig cfg, ObjectMapper mapper) {
        this.cfg = cfg;
        this.mapper = mapper;
    }

    @Override
    public Future<?> start() {
        Wiring wiring = new Wiring(vertx, mapper, cfg);

        long sweepIntervalMs = cfg.proxy().sweepIntervalSeconds() * 1000L;
        vertx.setPeriodic(sweepIntervalMs, id -> wiring.multiTryService().sweep());

        Future<Void> jwtCheck = cfg.proxy().testJwt()
                ? wiring.jwtTokenChecker().checkTokens(cfg.proxy().jwtMappings())
                : Future.succeededFuture();

        HttpServerOptions serverOptions =
                new HttpServerOptions(new JsonObject(cfg.vertx().server()));

        return jwtCheck.compose(v -> vertx.createHttpServer(serverOptions)
                .requestHandler(wiring.router())
                .listen()
                .onSuccess(s -> log.info("Listening on port {}", s.actualPort())));
    }
}
