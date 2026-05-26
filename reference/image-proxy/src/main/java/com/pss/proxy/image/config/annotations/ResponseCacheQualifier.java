package com.pss.proxy.image.config.annotations;

import com.pss.proxy.image.config.ImageProxyRouteRegistration;
import com.pss.proxy.image.service.routes.DynamicRouteService;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for {@link com.pss.proxy.image.routes.QueryRouteHandler} handle's dynamic, query params, literally passes
 * https://sirv.com/help/articles/dynamic-imaging/ params to Sirv
 *
 * @see com.pss.proxy.image.routes.QueryRouteHandler
 * @see ImageProxyRouteRegistration
 * @see ImageProxy#dynamicRoute(HttpClient, DynamicRouteService)
 * @see JwtRoute
 * @see UnprotectedRoute
 */
@Documented
@Qualifier
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface ResponseCacheQualifier {
}
