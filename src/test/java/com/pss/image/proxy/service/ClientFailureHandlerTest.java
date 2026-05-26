package com.pss.image.proxy.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pss.image.proxy.MutableClock;
import com.pss.image.proxy.util.Util;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

class ClientFailureHandlerTest {

    private MultiTryService multiTry;
    private ClientFailureHandler handler;

    @BeforeEach
    void setUp() {
        var clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        multiTry = new MultiTryService(new ConcurrentHashMap<>(), clock, 10);
        handler = new ClientFailureHandler(multiTry);
    }

    @Test
    void tryNextDelegatesToContext() {
        var ctx = mock(RoutingContext.class);
        var response = mock(HttpServerResponse.class);

        handler.handle(true, ctx, response).handle(new RuntimeException("boom"));

        verify(ctx).next();
        verify(response, never()).end(Util.png);
    }

    @Test
    void terminalFailureEndsWithStaticPng() {
        var ctx = mock(RoutingContext.class);
        var response = mock(HttpServerResponse.class);
        when(response.ended()).thenReturn(false);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);

        handler.handle(false, ctx, response).handle(new RuntimeException("boom"));

        verify(response).putHeader("Content-Type", "image/png");
        verify(response).putHeader(eq("cache-control"), anyString());
        verify(response).end(Util.png);
        verify(ctx, never()).next();
    }

    @Test
    void terminalFailureSkipsAlreadyEndedResponse() {
        var ctx = mock(RoutingContext.class);
        var response = mock(HttpServerResponse.class);
        when(response.ended()).thenReturn(true);

        handler.handle(false, ctx, response).handle(new RuntimeException("boom"));

        verify(response, never()).end(Util.png);
    }
}
