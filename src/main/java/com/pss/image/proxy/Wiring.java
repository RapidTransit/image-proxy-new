package com.pss.image.proxy;

import com.pss.image.proxy.config.AppConfig;
import com.pss.image.proxy.config.ProxyConfig;
import com.pss.image.proxy.routes.AbstractHandler;
import com.pss.image.proxy.routes.CachingNotFoundHandler;
import com.pss.image.proxy.routes.EndHandler;
import com.pss.image.proxy.routes.ErrorHandler;
import com.pss.image.proxy.routes.NotFoundHandler;
import com.pss.image.proxy.routes.QueryRouteHandler;
import com.pss.image.proxy.routes.ResponseCache;
import com.pss.image.proxy.routes.ValidImageHandler;
import com.pss.image.proxy.service.CacheHeaderManipulator;
import com.pss.image.proxy.service.ClientFailureHandler;
import com.pss.image.proxy.service.JwtDecoder;
import com.pss.image.proxy.service.JwtTokenChecker;
import com.pss.image.proxy.service.MultiTryService;
import com.pss.image.proxy.service.QueryParamService;
import com.pss.image.proxy.service.routes.DynamicRouteService;
import com.pss.image.proxy.service.routes.JwtRouteService;
import com.pss.image.proxy.service.routes.UnprotectedRouteService;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/// Bootstraps the singleton object graph and returns a configured [Router].
///
/// Lifecycle: instantiated once per [MainVerticle] start. No DI container —
/// every dependency is wired by direct constructor call here. The whole
/// graph is ~20 nodes and has zero cycles.
///
/// Thread-safety: built on the Vert.x event-loop context. Both caches are
/// [ConcurrentHashMap] because Vert.x's default event-loop pool size is
/// `2 × processors` and connections can land on either loop.
public final class Wiring {

    private final Router router;
    private final MultiTryService multiTryService;
    private final JwtTokenChecker jwtTokenChecker;

    public Wiring(Vertx vertx, ObjectMapper mapper, AppConfig cfg) {
        ProxyConfig proxy = cfg.proxy();
        Clock clock = Clock.systemUTC();

        HttpClientOptions clientOptions =
                new HttpClientOptions(new JsonObject(cfg.vertx().client())).setLogActivity(true);
        HttpClient sirvClient = vertx.createHttpClient(clientOptions);

        Map<String, ResponseCache> pathResponseCache = new ConcurrentHashMap<>();
        Map<String, ResponseCache> noImageCache = new ConcurrentHashMap<>();
        Map<String, MultiTryService.CounterValue> retryCache = new ConcurrentHashMap<>();

        QueryParamService query = new QueryParamService(proxy);
        this.multiTryService = new MultiTryService(retryCache, clock, proxy.multiTryDelay());
        ClientFailureHandler failure = new ClientFailureHandler(multiTryService);

        URI noImageUri = URI.create(cfg.client().host()).resolve(proxy.noImage());

        NotFoundHandler notFound =
                new NotFoundHandler(noImageUri, sirvClient, query, multiTryService, failure, noImageCache, clock);
        NotFoundHandler noImage = new NotFoundHandler(
                noImageUri, sirvClient, query, CacheHeaderManipulator.NO_OP, failure, noImageCache, clock);

        QueryRouteHandler dynamicRoute = new QueryRouteHandler(sirvClient, new DynamicRouteService(), failure);
        QueryRouteHandler jwtRoute = new QueryRouteHandler(sirvClient, new JwtRouteService(query, proxy), failure);
        QueryRouteHandler unprotectedRoute =
                new QueryRouteHandler(sirvClient, new UnprotectedRouteService(query), failure);

        AbstractHandler validImage = new ValidImageHandler();
        AbstractHandler cachingNotFound = new CachingNotFoundHandler(pathResponseCache, clock);
        AbstractHandler endHandler = new EndHandler();
        AbstractHandler errorHandler = new ErrorHandler();

        this.jwtTokenChecker = new JwtTokenChecker(new JwtDecoder(mapper), sirvClient, proxy);

        Router r = Router.router(vertx);
        r.get()
                .setName("check-invalid-or-missing-image")
                .failureHandler(errorHandler)
                .handler(validImage)
                .handler(cachingNotFound);

        r.get("/p/*").setName("protected-route").handler(jwtRoute);
        r.get("/u/*").setName("unprotected-route").handler(unprotectedRoute);
        r.get("/c/*").setName("category-route").handler(jwtRoute);
        r.get("/d/*").setName("dynamic-route").handler(dynamicRoute);
        r.get("/images/no-image.png").setName("store-no-image-route").handler(noImage);
        r.get("/shared/*").setName("shared-no-image-route").handler(noImage);
        r.get()
                .setName("store-not-found-image-route")
                .handler(notFound)
                .handler(endHandler)
                .failureHandler(errorHandler);

        this.router = r;
    }

    public Router router() {
        return router;
    }

    public MultiTryService multiTryService() {
        return multiTryService;
    }

    public JwtTokenChecker jwtTokenChecker() {
        return jwtTokenChecker;
    }
}
