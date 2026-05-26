package com.pss.proxy.image.config.annotations;

import com.pss.proxy.image.config.ImageProxyRouteRegistration;
import com.pss.proxy.image.service.routes.UnprotectedRouteService;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for {@link com.pss.proxy.image.routes.QueryRouteHandler} handle's images that are prefixed with a `/u/**`,
 * looks up a profile in the incoming request and passes in the correct profile name to Sirv's backend, currently shims
 * the requests until we are sure that all profile requests match correctly
 *
 * @see com.pss.proxy.image.routes.QueryRouteHandler
 * @see ImageProxyRouteRegistration
 * @see ImageProxy#unprotectedRoute(HttpClient, UnprotectedRouteService)
 * @see DynamicRoute
 * @see JwtRoute
 */
@Documented
@Qualifier
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface UnprotectedRoute {
}
