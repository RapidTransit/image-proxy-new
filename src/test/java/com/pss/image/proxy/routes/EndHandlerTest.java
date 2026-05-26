package com.pss.image.proxy.routes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;

class EndHandlerTest {

    @Test
    void endsResponseWhenNotEnded() {
        var handler = new EndHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(response.ended()).thenReturn(false);

        handler.handleInternal(ctx, request, response, "/x");

        verify(response).end();
    }

    @Test
    void leavesAlreadyEndedResponse() {
        var handler = new EndHandler();
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(response.ended()).thenReturn(true);

        handler.handleInternal(ctx, request, response, "/x");

        verify(response, never()).end();
    }
}
