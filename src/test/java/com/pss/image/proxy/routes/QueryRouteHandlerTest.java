package com.pss.image.proxy.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.pss.image.proxy.service.routes.DynamicRouteService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class QueryRouteHandlerTest {

    private QueryRouteHandler handler;
    private Logger handlerLogger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        handler = new QueryRouteHandler(
                mock(HttpClient.class), new DynamicRouteService(), new StubClientFailureHandler());

        handlerLogger = (Logger) LoggerFactory.getLogger(QueryRouteHandler.class);
        appender = new ListAppender<>();
        appender.start();
        handlerLogger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(appender);
        appender.stop();
    }

    @Test
    void unsupportedMediaTypeLogsWithDedicatedMarker() {
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        var clientResponse = mock(HttpClientResponse.class);
        when(clientResponse.statusCode()).thenReturn(415);

        handler.handleResponse(ctx, request, response, "/p/big.jpg", clientResponse);

        verify(ctx).next();
        assertThat(appender.list)
                .anyMatch(event ->
                        event.getLevel() == Level.ERROR && markerNames(event).contains("415_UNSUPPORTED_MEDIA"));
    }

    private static java.util.List<String> markerNames(ILoggingEvent event) {
        var markers = event.getMarkerList();
        if (markers == null) {
            return java.util.List.of();
        }
        return markers.stream().map(Marker::getName).toList();
    }

    private static class StubClientFailureHandler extends com.pss.image.proxy.service.ClientFailureHandler {
        StubClientFailureHandler() {
            super(null);
        }

        @Override
        public Handler<Throwable> handle(boolean tryNext, RoutingContext context, HttpServerResponse serverResponse) {
            return throwable -> {};
        }
    }
}
