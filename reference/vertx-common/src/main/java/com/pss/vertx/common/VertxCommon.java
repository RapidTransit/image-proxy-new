package com.pss.vertx.common;

import com.pss.vertx.common.service.ExceptionHandler;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Factory
public class VertxCommon {

    private static final Logger log = LogManager.getLogger(VertxCommon.class);

    @Bean
    @Singleton
    public VertxOptions vertxOptions(
            @Property(name = "vertx.instance")
            @MapFormat(keyFormat = StringConvention.RAW) Map<String, Object> instance,
            @Property(name = "vertx.default.instance")
            @MapFormat(keyFormat = StringConvention.RAW) Map<String, Object> defaultInstance) {

        return new VertxOptions(new JsonObject(instance));
    }

    @Bean
    @Singleton
    public HttpServerOptions httpServerOptions(
            @Property(name = "vertx.server")
            @MapFormat(keyFormat = StringConvention.RAW) Map<String, Object> server,
            @Property(name = "vertx.default.server")
            @MapFormat(keyFormat = StringConvention.RAW) Map<String, Object> serverDefault) {

        return new HttpServerOptions(new JsonObject(server));
    }



    @Bean
    @Singleton
    public Vertx vertx(VertxOptions options, ExceptionHandler exceptionHandler) {
        return Vertx.vertx(options).exceptionHandler(exceptionHandler);
    }
    @Bean
    @Singleton
    public HttpServer httpServer(Vertx vertx, HttpServerOptions options){
        return vertx.createHttpServer(options);
    }

    @Bean
    @Singleton
    public Router router(Vertx vertx){
        return Router.router(vertx);
    }
}
