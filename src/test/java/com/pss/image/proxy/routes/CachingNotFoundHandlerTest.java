package com.pss.image.proxy.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pss.image.proxy.MutableClock;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CachingNotFoundHandlerTest {

    private CachingNotFoundHandler handler;
    private MutableClock clock;
    private Map<String, ResponseCache> cache;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        cache = new ConcurrentHashMap<>();
        handler = new CachingNotFoundHandler(cache, clock);
    }

    @Test
    void cacheMissCallsNext() {
        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/missing.jpg");

        verify(ctx).next();
    }

    @Test
    void cacheHitWithFutureExpiryReturnsCache() {
        var buffer = Buffer.buffer("fake image data");
        var headers = MultiMap.caseInsensitiveMultiMap().add("Content-Type", "image/png");
        var expiresAt = clock.millis() + 60000;
        cache.put("/p/cached.jpg", new ResponseCache(buffer, headers, expiresAt));

        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(response.closed()).thenReturn(false);
        when(response.headers()).thenReturn(MultiMap.caseInsensitiveMultiMap());

        handler.handleInternal(ctx, request, response, "/p/cached.jpg");

        verify(response).headers();
        verify(response).end(buffer);
        verify(ctx, never()).next();
    }

    @Test
    void expiredCacheEntryRemovesAndCallsNext() {
        var buffer = Buffer.buffer("fake image data");
        var headers = MultiMap.caseInsensitiveMultiMap();
        var expiresAt = clock.millis() - 1;
        cache.put("/p/expired.jpg", new ResponseCache(buffer, headers, expiresAt));

        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/expired.jpg");

        assertThat(cache).doesNotContainKey("/p/expired.jpg");
        verify(ctx).next();
    }

    @Test
    void closedResponseDoesNotWrite() {
        var buffer = Buffer.buffer("fake image data");
        var headers = MultiMap.caseInsensitiveMultiMap();
        var expiresAt = clock.millis() + 60000;
        cache.put("/p/test.jpg", new ResponseCache(buffer, headers, expiresAt));

        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(response.closed()).thenReturn(true);

        handler.handleInternal(ctx, request, response, "/p/test.jpg");

        verify(response, never()).end(buffer);
        verify(response, never()).headers();
    }

    @Test
    void cacheHitAtExactExpiryBoundary() {
        var buffer = Buffer.buffer("test");
        var headers = MultiMap.caseInsensitiveMultiMap();
        var expiresAt = clock.millis();
        cache.put("/p/boundary.jpg", new ResponseCache(buffer, headers, expiresAt));

        var ctx = mock(RoutingContext.class);
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        handler.handleInternal(ctx, request, response, "/p/boundary.jpg");

        assertThat(cache).doesNotContainKey("/p/boundary.jpg");
        verify(ctx).next();
    }
}
