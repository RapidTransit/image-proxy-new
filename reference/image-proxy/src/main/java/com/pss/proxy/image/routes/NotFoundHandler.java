package com.pss.proxy.image.routes;

import com.pss.proxy.image.service.CacheHeaderManipulator;
import com.pss.proxy.image.service.ClientFailureHandler;
import com.pss.proxy.image.service.QueryParamService;
import com.pss.proxy.image.utils.Utils;
import com.pss.vertx.common.routes.AbstractHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.pss.proxy.image.utils.Utils.ACCEPT;
import static com.pss.proxy.image.utils.Utils.buildUriProfile;


public class NotFoundHandler extends AbstractHandler  {

    private static final Logger log = LogManager.getLogger(NotFoundHandler.class);

    static final Cache<String, ResponseCache> responseCache = Cache2kBuilder.of(String.class, ResponseCache.class)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .entryCapacity(16)
            .storeByReference(true)
            .build();


    protected final URI noImage;
    protected final HttpClient httpClient;
    protected final QueryParamService queryParamService;
    protected final CacheHeaderManipulator cacheHeaderManipulator;
    protected final ClientFailureHandler clientFailureHandler;


    public NotFoundHandler(URI noImage, HttpClient httpClient,
                           QueryParamService queryParamService,
                           CacheHeaderManipulator cacheHeaderManipulator, ClientFailureHandler clientFailureHandler) {
        this.noImage = noImage;
        this.httpClient = httpClient;
        this.queryParamService = queryParamService;
        this.cacheHeaderManipulator = cacheHeaderManipulator;

        this.clientFailureHandler = clientFailureHandler;
    }

    @Override
    protected void handleInternal(RoutingContext event, HttpServerRequest request,
                                          HttpServerResponse serverResponse, String path) {
        String profile = queryParamService.extractQueryParam(request, path);
        String handle = Optional.ofNullable(request.getHeader(ACCEPT)).orElse("").contains("image/webp") ? "webp" : "jpeg";

        String key = profile + ':' + handle;
        ResponseCache cachedResponse = responseCache.get(key);
        if(cachedResponse == null) {
            RequestOptions options = new RequestOptions();
            Utils.addHeaders(options, request);
            String uri = buildUriProfile(noImage.toString(), profile);
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
                            Buffer copy = body.copy();
                            responseCache.put(key, new ResponseCache(copy, result.headers()));
                            if(!serverResponse.closed()) {
                                return serverResponse.end(body);
                            } else {
                               return event.end();
                            }
                        });
                    });
        } else {
            serverResponse.headers().addAll(cachedResponse.headers());
            cacheHeaderManipulator.manipulateResponseCache(path, serverResponse);
            serverResponse.end(cachedResponse.buffer());
        }

    }

}
