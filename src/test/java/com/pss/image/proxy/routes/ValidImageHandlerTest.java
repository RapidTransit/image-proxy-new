package com.pss.image.proxy.routes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

public class ValidImageHandlerTest {

    @Test
    void validImagePathCallsNext() {
        var handler = new ValidImageHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/image.png");

        verify(ctx).next();
        verify(ctx, never()).fail(400);
    }

    @Test
    void invalidImagePathCallsFail() {
        var handler = new ValidImageHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/notanimage.txt");

        verify(ctx).fail(400);
        verify(ctx, never()).next();
    }

    @Test
    void jpegFilePathCallsNext() {
        var handler = new ValidImageHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/image.jpg");

        verify(ctx).next();
        verify(ctx, never()).fail(400);
    }

    @Test
    void webpFilePathCallsNext() {
        var handler = new ValidImageHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/image.webp");

        verify(ctx).next();
        verify(ctx, never()).fail(400);
    }
}
