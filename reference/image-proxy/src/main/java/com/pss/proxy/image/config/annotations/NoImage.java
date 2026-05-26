package com.pss.proxy.image.config.annotations;

import com.pss.proxy.image.config.ImageProxyRouteRegistration;
import com.pss.proxy.image.config.properties.ProxyConfig;
import com.pss.proxy.image.service.QueryParamService;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier for {@link com.pss.proxy.image.routes.NotFoundHandler}, this is different than {@link NotFound}, because
 * with this handler, we want to cache the results immediately because we literally looked up: `/store/no-image.png`
 *
 * @see com.pss.proxy.image.routes.NotFoundHandler
 * @see ImageProxyRouteRegistration
 * @see ImageProxy#noImageHandler(ProxyConfig, HttpClient, QueryParamService)
 * @see NotFound
 */
@Documented
@Qualifier
@Target({FIELD, PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface NoImage {
}
