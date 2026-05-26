package com.pss.image.proxy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.image.proxy.config.AppConfig;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/// Process entry point.
///
/// Loads the JSON config, builds a [Vertx] instance with the requested
/// [VertxOptions], then deploys [MainVerticle] with the same config. The
/// path to the config file is the system property `config.file`, defaulting
/// to `config/application.json` (resolved against the working directory)
/// with a classpath fallback for local runs and tests.
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_CONFIG_PATH = "config/application.json";

    private Main() {}

    public static void main(String[] args) throws Exception {
        var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        var cfg = loadConfig(mapper);
        var vertxOptions = new VertxOptions(new JsonObject(cfg.vertx().instance()));
        var vertx = Vertx.vertx(vertxOptions);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> vertx.close().toCompletionStage().toCompletableFuture().join()));
        vertx.deployVerticle(new MainVerticle(cfg, mapper))
                .onFailure(t -> {
                    log.error("Verticle deployment failed", t);
                    vertx.close();
                });
    }

    private static AppConfig loadConfig(ObjectMapper mapper) throws Exception {
        String configFile = System.getProperty("config.file", DEFAULT_CONFIG_PATH);
        Path path = Path.of(configFile);
        String json;
        if (Files.exists(path)) {
            json = Files.readString(path);
            log.info("Loaded config from filesystem: {}", path.toAbsolutePath());
        } else {
            try (var in = Main.class.getResourceAsStream("/application.json")) {
                if (in == null) {
                    throw new IllegalStateException(
                            "No config file at " + path.toAbsolutePath() + " and no classpath fallback");
                }
                json = new String(in.readAllBytes());
                log.info("Loaded config from classpath: /application.json");
            }
        }
        return mapper.readValue(json, AppConfig.class);
    }
}
