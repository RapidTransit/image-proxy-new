package com.pss.image.proxy.routes;

import com.pss.image.proxy.service.CacheHeaderManipulator;
import com.pss.image.proxy.service.ClientFailureHandler;
import com.pss.image.proxy.service.QueryParamService;
import com.pss.image.proxy.util.Util;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Clock;
import java.util.Map;
import java.util.Optional;

import static com.pss.image.proxy.util.Util.ACCEPT;
import static com.pss.image.proxy.util.Util.buildUriProfile;

/// Fetches and serves the no-image fallback when no upstream image was found.
///
/// Wired twice with two different [CacheHeaderManipulator]s: once with the
/// [com.pss.image.proxy.service.MultiTryService] for the 404 fall-through
/// path (so clients get cache-control + expires headers that throttle
/// retries), and once with [CacheHeaderManipulator#NO_OP] for the static
/// no-image endpoints. The instance cache is keyed by `profile:handle` so
/// both routes can share the same backing [Map] from `Wiring`.
public final class NotFoundHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(NotFoundHandler.class);

    private static final long ENTRY_TTL_MS = 3_600_000L;

    private final URI noImage;
    private final HttpClient httpClient;
    private final QueryParamService queryParamService;
    private final CacheHeaderManipulator cacheHeaderManipulator;
    private final ClientFailureHandler clientFailureHandler;
    private final Map<String, ResponseCache> responseCache;
    private final Clock clock;

    public NotFoundHandler(
            URI noImage,
            HttpClient httpClient,
            QueryParamService queryParamService,
            CacheHeaderManipulator cacheHeaderManipulator,
            ClientFailureHandler clientFailureHandler,
            Map<String, ResponseCache> responseCache,
            Clock clock) {
        this.noImage = noImage;
        this.httpClient = httpClient;
        this.queryParamService = queryParamService;
        this.cacheHeaderManipulator = cacheHeaderManipulator;
        this.clientFailureHandler = clientFailureHandler;
        this.responseCache = responseCache;
        this.clock = clock;
    }

    @Override
    protected void handleInternal(
            RoutingContext event, HttpServerRequest request, HttpServerResponse serverResponse, String path) {
        var profile = queryParamService.extractQueryParam(request, path);
        var handle = Optional.ofNullable(request.getHeader(ACCEPT)).orElse("").contains("image/webp") ? "webp" : "jpeg";

        var key = profile + ':' + handle;
        var cached = responseCache.get(key);
        if (cached != null && cached.expiresAt() > clock.millis()) {
            serverResponse.headers().addAll(cached.headers());
            cacheHeaderManipulator.manipulateResponseCache(path, serverResponse);
            serverResponse.end(cached.buffer());
            return;
        }
        if (cached != null) {
            responseCache.remove(key, cached);
        }

        var options = new RequestOptions();
        Util.addHeaders(options, request);
        var uri = buildUriProfile(noImage.getRawPath(), profile);
        log.debug("uri: {}", uri);
        options.setURI(uri);

        httpClient.request(options)
                .compose(HttpClientRequest::send)
                .onFailure(clientFailureHandler.handle(false, event, serverResponse))
                .onSuccess(result -> {
                    if (result.statusCode() != 200) {
                        throw new RuntimeException("Not found image not found");
                    }
                    serverResponse.headers().addAll(result.headers());
                    cacheHeaderManipulator.manipulateResponseCache(path, serverResponse);
                    result.body().compose(body -> {
                        var copy = body.copy();
                        responseCache.put(
                                key, new ResponseCache(copy, result.headers(), clock.millis() + ENTRY_TTL_MS));
                        if (!serverResponse.closed()) {
                            return serverResponse.end(body);
                        }
                        return event.end();
                    });
                });
    }
}
