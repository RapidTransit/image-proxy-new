package com.pss.proxy.image.config.annotations;

import com.pss.proxy.image.config.ImageProxyRouteRegistration;
import com.pss.proxy.image.config.properties.ProxyConfig;
import com.pss.proxy.image.service.MultiTryService;
import com.pss.proxy.image.service.QueryParamService;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for {@link com.pss.proxy.image.routes.NotFoundHandler}, this is different than {@link NoImage}, because
 * with this handler, we want to cache the results after a few retries because sometimes the backend may throw an error
 * or the client ends the request suddenly for an image that may or may not exist with help with {@link MultiTryService}
 *
 * @see com.pss.proxy.image.routes.NotFoundHandler
 * @see ImageProxyRouteRegistration
 * @see ImageProxy#notFoundHandler(ProxyConfig, HttpClient, QueryParamService, MultiTryService)
 * @see MultiTryService
 * @see NoImage
 */
@Documented
@Qualifier
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface NotFound {
}
