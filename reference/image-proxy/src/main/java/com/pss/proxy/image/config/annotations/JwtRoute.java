package com.pss.proxy.image.config.annotations;

import com.pss.proxy.image.config.ImageProxyRouteRegistration;
import com.pss.proxy.image.service.routes.JwtRouteService;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for {@link com.pss.proxy.image.routes.QueryRouteHandler} handle's images that are prefixed with a `/p/**`,
 * looks up a profile and passes in the correct JWT token https://sirv.com/help/json-web-token-protected-signed-url/
 * link:config/application-jwt.yaml
 *
 * @see com.pss.proxy.image.routes.QueryRouteHandler
 * @see ImageProxyRouteRegistration
 * @see ImageProxy#jwtRoute(HttpClient, JwtRouteService)
 * @see DynamicRoute
 * @see UnprotectedRoute
 */
@Documented
@Qualifier
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface JwtRoute {
}
