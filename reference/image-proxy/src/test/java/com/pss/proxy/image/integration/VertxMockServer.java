package com.pss.proxy.image.integration;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class VertxMockServer {

    private static final Logger log = LogManager.getLogger(VertxMockServer.class);
    public static final Vertx vertx = Vertx.vertx();
    public static final HttpServer vertxHttp = vertx.createHttpServer(new HttpServerOptions().setPort(9090));
    public static final Router router = Router.router(vertx);


    public static final Buffer image;
    public static final Buffer noImage;
    static {
        try {

            image = Buffer.buffer(VertxMockServer.class.getClassLoader().getResourceAsStream("1080x1920.png").readAllBytes());
            noImage = Buffer.buffer(VertxMockServer.class.getClassLoader().getResourceAsStream("no-image.png").readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        vertxHttp.requestHandler(router).listen(9090 ).onComplete(x-> {
            log.info(x);
        }).onSuccess(x-> {
            log.info("startup complete, {}", x);
        }).onFailure(t-> t.printStackTrace());
    }

}
