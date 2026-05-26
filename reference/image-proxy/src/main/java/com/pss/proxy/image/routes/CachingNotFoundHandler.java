package com.pss.proxy.image.routes;


import com.pss.proxy.image.config.annotations.ResponseCacheQualifier;
import com.pss.vertx.common.routes.AbstractHandler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.cache2k.Cache;

@Singleton
public class CachingNotFoundHandler extends AbstractHandler {


    private final Cache<String, ResponseCache> cache;

    @Inject
    public CachingNotFoundHandler(@ResponseCacheQualifier Cache<String, ResponseCache> cache) {
        this.cache = cache;

    }

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request, HttpServerResponse response, String path) {
        ResponseCache responseCache = cache.get(path);
        if(responseCache != null) {
            if(!response.closed()){
                response.headers().addAll(responseCache.headers());
                response.end(responseCache.buffer());
            }
        } else {
            event.next();
        }
    }

}
