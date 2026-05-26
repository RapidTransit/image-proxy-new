package com.pss.proxy.image;

import com.pss.proxy.image.config.annotations.*;
import com.pss.proxy.image.routes.NotFoundHandler;
import com.pss.proxy.image.routes.QueryRouteHandler;
import com.pss.proxy.image.routes.ResponseCache;
import com.pss.proxy.image.service.CacheHeaderManipulator;
import com.pss.proxy.image.service.ClientFailureHandler;
import com.pss.proxy.image.service.MultiTryService;
import com.pss.proxy.image.service.QueryParamService;
import com.pss.proxy.image.service.routes.DynamicRouteService;
import com.pss.proxy.image.service.routes.JwtRouteService;
import com.pss.proxy.image.service.routes.UnprotectedRouteService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.net.URI;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Factory
public class ImageProxy {

    private static final Logger log = LogManager.getLogger(ImageProxy.class);


    /**
     * The clock used to time responses, needed for testing
     * @return a clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    @Singleton
    public HttpClientOptions httpClientOptions(
            @Property(name = "vertx.client")
            @MapFormat(keyFormat = StringConvention.RAW) Map<String, Object> client) {
        return new HttpClientOptions(new JsonObject(client)).setLogActivity(true);
    }



    @Bean
    @Singleton
    @ResponseCacheQualifier
    public Cache<String, ResponseCache> responseCache() {
        return Cache2kBuilder.of(String.class, ResponseCache.class)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .entryCapacity(16)
                .storeByReference(true)
                .build();
    }

    @Bean
    @MultiTryCache
    @Singleton
    public Cache<String, MultiTryService.CounterValue> multiTryCache() {
        return Cache2kBuilder.of(String.class, MultiTryService.CounterValue.class)
                .weigher((k,v)-> k.length())
                .maximumWeight(50_000)
                .storeByReference(true)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Bean
    @Singleton
    @NotFound
    public NotFoundHandler notFoundHandler(
            @NotFound URI uri,
            @SirvClient HttpClient httpClient,
            QueryParamService queryParamService,
            MultiTryService multiTryService,
            ClientFailureHandler clientFailureHandler) {
        return new NotFoundHandler(uri, httpClient, queryParamService, multiTryService, clientFailureHandler);
    }

    @Bean
    @Singleton
    @NoImage
    public NotFoundHandler noImageHandler(
            @NotFound URI uri,
            @SirvClient HttpClient httpClient,
            QueryParamService queryParamService, ClientFailureHandler clientFailureHandler) {
        return new NotFoundHandler(uri, httpClient, queryParamService, CacheHeaderManipulator.NO_OP, clientFailureHandler);
    }

    @Bean
    @Singleton
    @DynamicRoute
    public QueryRouteHandler dynamicRoute(HttpClient client, DynamicRouteService dynamicRouteService, ClientFailureHandler clientFailureHandler) {
        return new QueryRouteHandler(client, dynamicRouteService, clientFailureHandler);
    }

    @Bean
    @Singleton
    @JwtRoute
    public QueryRouteHandler jwtRoute(HttpClient client, JwtRouteService jwtRouteService, ClientFailureHandler clientFailureHandler) {
        return new QueryRouteHandler(client, jwtRouteService, clientFailureHandler);
    }

    @Bean
    @Singleton
    @UnprotectedRoute
    public QueryRouteHandler unprotectedRoute(HttpClient client, UnprotectedRouteService unprotectedRouteService, ClientFailureHandler clientFailureHandler) {
        return new QueryRouteHandler(client, unprotectedRouteService, clientFailureHandler);
    }

    @Bean
    @Singleton
    @NotFound
    public URI notFoundUri(@Value("${client.host:https://pleasant-smoke.sirv.com}") URI host,
                           @Value("${proxy.not-found:/images/no-image.png}") URI notFound) {
        return host.resolve(notFound);
    }


    @Bean
    @Singleton
    @SirvClient
    public HttpClient httpClient(Vertx vertx,  HttpClientOptions options){
        return vertx.createHttpClient(options);
    }


}
