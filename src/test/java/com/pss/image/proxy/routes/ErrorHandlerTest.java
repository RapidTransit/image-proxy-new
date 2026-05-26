package com.pss.image.proxy.routes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

class ErrorHandlerTest {

    @Test
    void endsResponseWhenNotEnded() {
        var handler = new ErrorHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(ctx.failure()).thenReturn(new RuntimeException("boom"));
        when(ctx.statusCode()).thenReturn(-1);
        when(response.ended()).thenReturn(false);

        handler.handleInternal(ctx, request, response, "/x");

        verify(response).end();
        verify(response, never()).setStatusCode(org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void propagatesContextStatusCode() {
        var handler = new ErrorHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(ctx.failure()).thenReturn(new RuntimeException("boom"));
        when(ctx.statusCode()).thenReturn(400);
        when(response.ended()).thenReturn(false);
        when(response.setStatusCode(400)).thenReturn(response);

        handler.handleInternal(ctx, request, response, "/x");

        verify(response).setStatusCode(400);
        verify(response).end();
    }

    @Test
    void leavesAlreadyEndedResponse() {
        var handler = new ErrorHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(ctx.failure()).thenReturn(new RuntimeException("boom"));
        when(response.ended()).thenReturn(true);

        handler.handleInternal(ctx, request, response, "/x");

        verify(response, never()).end();
        verify(response, never()).setStatusCode(org.mockito.ArgumentMatchers.anyInt());
    }
}
