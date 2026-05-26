package com.pss.proxy.image.config.annotations;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * We will most likely have other clients, this connects to Sirv's backend.
 *
 * @see ImageProxy#httpClientOptions()
 * @see ImageProxy#httpClient(Vertx, HttpClientOptions)
 */
@Documented
@Qualifier
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface SirvClient {
}
