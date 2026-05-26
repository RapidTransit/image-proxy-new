package com.pss.image.proxy.routes;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.time.Clock;
import java.util.Map;

/// Short-circuit cached "no-image" responses keyed by the full request path.
///
/// On a hit whose [ResponseCache#expiresAt] is still in the future, write
/// the cached bytes and headers directly. On miss or expired entry, fall
/// through with `event.next()`.
public final class CachingNotFoundHandler extends AbstractHandler {

    private final Map<String, ResponseCache> cache;
    private final Clock clock;

    public CachingNotFoundHandler(Map<String, ResponseCache> cache, Clock clock) {
        this.cache = cache;
        this.clock = clock;
    }

    @Override
    protected void handleInternal(
            RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        var entry = cache.get(path);
        if (entry != null && entry.expiresAt() > clock.millis()) {
            if (!response.closed()) {
                response.headers().addAll(entry.headers());
                response.end(entry.buffer());
            }
        } else {
            if (entry != null) {
                cache.remove(path, entry);
            }
            event.next();
        }
    }
}
