package com.pss.proxy.image.integration;

import com.pss.proxy.image.config.annotations.NotFound;
import com.pss.proxy.image.routes.NotFoundHandler;
import com.pss.proxy.image.test.MicronautVertxTest;
import com.pss.proxy.image.test.VertxCheckpoint;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import jakarta.inject.Inject;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

import static com.pss.proxy.image.TestUtils.tryCatcher;
import static com.pss.proxy.image.TestUtils.tryThrow;
import static com.pss.proxy.image.integration.VertxMockServer.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;


@MicronautVertxTest(environments = { "jwt", "integration"}, propertySources = "file:config/application-jwt.yaml")
public class IntegrationTest {

    private static final Logger log = LogManager.getLogger(IntegrationTest.class);

    @Inject
    protected Vertx vertx;

//    @Inject
//    protected ServerBootstrap serverBootstrap;

//    @Inject
//    @NotFound
//    protected NotFoundHandler notFound;

    @Inject
    protected Clock clock;

    @VertxCheckpoint
    protected Checkpoint simulate404;

    @VertxCheckpoint
    protected Checkpoint simulateCancelled;

    @VertxCheckpoint
    protected Checkpoint simulateSuccess;

    public static final ZonedDateTime START = LocalDateTime.of(2023, 7,1,12, 0, 0, 0).atZone(ZoneOffset.UTC);
    public static final Instant START_INSTANT = START.toInstant();




//    @Bean
//    @Singleton
//    @NotFound
//    @Primary
//    public NotFoundHandler notFoundHandler(
//            @NotFound URI uri,
//            @SirvClient HttpClient httpClient,
//            QueryParamService queryParamService,
//            MultiTryService multiTryService) {
//        return spy(new NotFoundHandler(uri, httpClient, queryParamService, multiTryService));
//    }

    @Test
    public void testMultiTry(VertxTestContext testContext, @NotFound NotFoundHandler notFound) {



//        serverBootstrap.start();



        Mutable<Future<SimpleHttpResponse>> mutableResponse = new MutableObject<>();

        router.get("/images/no-image.png").handler(h-> h.end(noImage));
        router.get("/u/*").blockingHandler(new Handler<>() {
            int times = 0;

            @Override
            public void handle(RoutingContext event) {
                if (times == 0) {
                    // Simulate a Server Error
                    times++;
                    LockSupport.parkNanos(1000);
                    event.fail(404);
                } else if (times == 1) {
                    // Simulate the client disconnected midway
                    times++;
                    LockSupport.parkNanos(1000);
                    mutableResponse.getValue().cancel(true);

                    event.end(image);
                } else if (times == 2) {
                    // Simulate Success
                    LockSupport.parkNanos(1000);
                    event.end(image);
                }
            }
        });

        CloseableHttpAsyncClient client = HttpAsyncClients.createDefault();
        client.start();
        var simpleRequestBuilder = SimpleRequestBuilder.get()
                .setHttpHost(new HttpHost("localhost", 9091))
                .setPath("/u/1080x1920.png").build();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Future<SimpleHttpResponse> firstResponse = client.execute(simpleRequestBuilder, new Simulate404NotFoundHandler(notFound, testContext, simulate404, countDownLatch));

        mutableResponse.setValue(firstResponse);
        tryThrow(()-> firstResponse.get());
        Future<SimpleHttpResponse> secondResponse = client.execute(simpleRequestBuilder, new SimulateCancelled(testContext, simulateCancelled));
        mutableResponse.setValue(secondResponse);
        tryCatcher(()-> secondResponse.get());

        Future<SimpleHttpResponse> thirdResponse = client.execute(simpleRequestBuilder, new SimulateSuccess(simulateSuccess, testContext));
        tryCatcher(()-> thirdResponse.get());
    }


    private static class Simulate404NotFoundHandler implements FutureCallback<SimpleHttpResponse> {
        private final NotFoundHandler notFoundHandler;
        private final VertxTestContext testContext;
        private final Checkpoint checkpoint;
        private final CountDownLatch countDownLatch;

        public Simulate404NotFoundHandler(NotFoundHandler notFoundHandler, VertxTestContext testContext, Checkpoint checkpoint, CountDownLatch countDownLatch) {
            this.notFoundHandler = notFoundHandler;
            this.testContext = testContext;
            this.checkpoint = checkpoint;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void completed(SimpleHttpResponse result) {
            try {
                verify(notFoundHandler).handle(any(RoutingContext.class));
                assertThat(result.getHeader("cache-control")).isNotNull();
                assertThat(result.getHeader("cache-control").getValue()).isEqualTo("max-age=10");
                assertThat(result.getHeader("expires")).isNotNull();
                assertThat(result.getHeader("expires").getValue()).isEqualTo("Sat, 01 Jul 2023 12:00:10 GMT");
                checkpoint.flag();
            } catch (Throwable t) {
                testContext.failNow(t);
            }
        }

        @Override
        public void failed(Exception ex) {
            testContext.failNow(ex);
        }

        @Override
        public void cancelled() {
            testContext.failNow("1st Call cancelled, when it shouldn't have.");
        }
    }

    private static class SimulateCancelled implements FutureCallback<SimpleHttpResponse> {

        private final VertxTestContext testContext;
        private final Checkpoint checkpoint;

        public SimulateCancelled(VertxTestContext testContext, Checkpoint checkpoint) {
            this.testContext = testContext;
            this.checkpoint = checkpoint;
        }

        @Override
        public void completed(SimpleHttpResponse result) {
            testContext.failNow("This Should have been cancelled");
        }

        @Override
        public void failed(Exception ex) {
            testContext.failNow(ex);
        }

        @Override
        public void cancelled() {
            checkpoint.flag();
        }
    }

    private static class SimulateSuccess implements FutureCallback<SimpleHttpResponse> {
        private final Checkpoint simulateSuccess;
        private final VertxTestContext testContext;

        public SimulateSuccess(Checkpoint simulateSuccess, VertxTestContext testContext) {
            this.simulateSuccess = simulateSuccess;
            this.testContext = testContext;
        }

        @Override
        public void completed(SimpleHttpResponse result) {
            boolean success = true;

            try {
                assertThat(result.getBodyBytes()).isEqualTo(image.getBytes());
                assertThat(result.getHeader("cache-control")).isNull();
                assertThat(result.getHeader("expires")).isNull();
                simulateSuccess.flag();
            } catch (ProtocolException e) {
                testContext.failNow(e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void failed(Exception ex) {
            testContext.failNow(ex);
        }

        @Override
        public void cancelled() {
            testContext.failNow("Third Response Cancelled");
        }
    }
}

