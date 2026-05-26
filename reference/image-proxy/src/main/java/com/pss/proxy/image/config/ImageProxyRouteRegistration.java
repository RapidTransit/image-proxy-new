package com.pss.proxy.image.config;

import com.pss.proxy.image.config.annotations.*;
import com.pss.proxy.image.routes.*;
import com.pss.vertx.common.routes.EndHandler;
import com.pss.vertx.common.routes.ErrorHandler;
import com.pss.vertx.common.server.RouteRegistration;
import io.vertx.ext.web.Router;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Singleton
public class ImageProxyRouteRegistration implements RouteRegistration {

    private static final Logger log = LogManager.getLogger(ImageProxyRouteRegistration.class);

    protected final ValidImageHandler validImageHandler;
    protected final CachingNotFoundHandler cachingNotFoundHandler;
    protected final QueryRouteHandler dynamicRoute;
    protected final QueryRouteHandler jwtRoute;
    protected final QueryRouteHandler unprotectedRoute;
    protected final NotFoundHandler notFoundHandler;
    protected final NotFoundHandler noImageHandler;
    protected final EndHandler endHandler;
    protected final ErrorHandler errorHandler;

    @Inject
    public ImageProxyRouteRegistration(ValidImageHandler validImageHandler,
                                       CachingNotFoundHandler cachingNotFoundHandler,
                                       @DynamicRoute QueryRouteHandler dynamicRoute,
                                       @JwtRoute QueryRouteHandler jwtRoute,
                                       @UnprotectedRoute QueryRouteHandler unprotectedRoute,
                                       @NotFound NotFoundHandler notFoundHandler,
                                       @NoImage NotFoundHandler noImageHandler,
                                       EndHandler endHandler,
                                       ErrorHandler errorHandler) {
        this.validImageHandler = validImageHandler;
        this.cachingNotFoundHandler = cachingNotFoundHandler;
        this.dynamicRoute = dynamicRoute;
        this.jwtRoute = jwtRoute;
        this.unprotectedRoute = unprotectedRoute;
        this.notFoundHandler = notFoundHandler;
        this.noImageHandler = noImageHandler;
        this.endHandler = endHandler;
        this.errorHandler = errorHandler;
    }


    @Override
    public void registerRoutes(Router router) {
        log.info("Registering routes");

        router.clear();
        router.get().setName("check-invalid-or-missing-image")
                .failureHandler(errorHandler)
                .handler(validImageHandler)
                .handler(cachingNotFoundHandler);

        router.get("/p/*").setName("protected-route")
                .handler(jwtRoute);

        router.get("/u/*").setName("unprotected-route")
                .handler(unprotectedRoute);

        router.get("/c/*").setName("category-route")
                .handler(jwtRoute);

        router.get("/d/*").setName("dynamic-route")
                .handler(dynamicRoute);

        router.get("/images/no-image.png").setName("store-no-image-route")
                .handler(noImageHandler);

        router.get("/shared/*").setName("shared-no-image-route")
                .handler(noImageHandler);

        router.get().setName("store-not-found-image-route")
                .handler(notFoundHandler)
                .handler(endHandler)
                .failureHandler(errorHandler);

    }

}
