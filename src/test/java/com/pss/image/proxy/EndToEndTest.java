package com.pss.image.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pss.image.proxy.config.AppConfig;
import com.pss.image.proxy.config.ProxyConfig;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/// End-to-end test exercising the full route table against a stub upstream.
///
/// Covers the bootstrap path that unit tests can't reach: [Wiring],
/// [MainVerticle], and the live handler chain (`ValidImageHandler` ->
/// `QueryRouteHandler` -> `NotFoundHandler`). The upstream is a small
/// in-process [HttpServer] standing in for Sirv.
@ExtendWith(VertxExtension.class)
class EndToEndTest {

    private static final ObjectMapper MAPPER =
            JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private Vertx vertx;
    private HttpServer upstream;
    private WebClient client;
    private int proxyPort;

    @BeforeEach
    void setUp(VertxTestContext ctx) throws IOException {
        vertx = Vertx.vertx();
        proxyPort = freePort();
        var upstreamPort = freePort();

        upstream = vertx.createHttpServer().requestHandler(this::handleUpstream);
        upstream.listen(upstreamPort, "127.0.0.1")
                .compose(s -> {
                    var cfg = buildCfg(proxyPort, upstreamPort);
                    return vertx.deployVerticle(new MainVerticle(cfg, MAPPER));
                })
                .onComplete(ctx.succeedingThenComplete());

        client = WebClient.create(vertx);
    }

    @AfterEach
    void tearDown(VertxTestContext ctx) {
        vertx.close().onComplete(ctx.succeedingThenComplete());
    }

    private void handleUpstream(HttpServerRequest req) {
        var path = req.path();
        if (path.endsWith("real-image.jpg")) {
            req.response().putHeader("content-type", "image/jpeg").end(Buffer.buffer("REAL"));
        } else if (path.contains("no-image.png")) {
            req.response().putHeader("content-type", "image/png").end(Buffer.buffer("NOIMG"));
        } else {
            req.response().setStatusCode(404).end("not found");
        }
    }

    @Test
    void rejectsNonImagePath(VertxTestContext ctx) {
        client.getAbs("http://127.0.0.1:" + proxyPort + "/p/notanimage.txt")
                .send()
                .onComplete(ctx.succeeding(resp -> {
                    ctx.verify(() -> assertThat(resp.statusCode()).isEqualTo(400));
                    ctx.completeNow();
                }));
    }

    @Test
    void servesImageThroughJwtRoute(VertxTestContext ctx) {
        client.getAbs("http://127.0.0.1:" + proxyPort + "/p/real-image.jpg?tr=p")
                .send()
                .onComplete(ctx.succeeding(resp -> {
                    ctx.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body().toString()).isEqualTo("REAL");
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void fallsBackToNoImageWhenUpstreamReturns404(VertxTestContext ctx) {
        client.getAbs("http://127.0.0.1:" + proxyPort + "/p/missing.jpg?tr=p")
                .send()
                .onComplete(ctx.succeeding(resp -> {
                    ctx.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body().toString()).isEqualTo("NOIMG");
                    });
                    ctx.completeNow();
                }));
    }

    @Test
    void servesNoImageDirectlyAtStaticPath(VertxTestContext ctx) {
        client.getAbs("http://127.0.0.1:" + proxyPort + "/images/no-image.png?tr=p")
                .send()
                .onComplete(ctx.succeeding(resp -> {
                    ctx.verify(() -> {
                        assertThat(resp.statusCode()).isEqualTo(200);
                        assertThat(resp.body().toString()).isEqualTo("NOIMG");
                    });
                    ctx.completeNow();
                }));
    }

    private static int freePort() throws IOException {
        try (var s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static AppConfig buildCfg(int proxyPort, int upstreamPort) {
        return new AppConfig(
                new ProxyConfig(
                        Set.of("p"),
                        "p",
                        "tr",
                        "/images/no-image.png",
                        "/p/test-image.png",
                        Map.of("p", "fake-jwt-token"),
                        Map.of(),
                        Map.of(),
                        false,
                        10),
                new AppConfig.VertxBlock(
                        Map.of("preferNativeTransport", false),
                        Map.of("port", proxyPort, "host", "127.0.0.1"),
                        Map.of("defaultHost", "127.0.0.1", "defaultPort", upstreamPort, "ssl", false)),
                new AppConfig.ClientBlock("http://127.0.0.1:" + upstreamPort));
    }
}
