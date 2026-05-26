package com.pss.proxy.image.integration;

import com.pss.proxy.image.MutableClock;
import com.pss.proxy.image.config.annotations.NotFound;
import com.pss.proxy.image.config.annotations.SirvClient;
import com.pss.proxy.image.routes.NotFoundHandler;
import com.pss.proxy.image.service.ClientFailureHandler;
import com.pss.proxy.image.service.MultiTryService;
import com.pss.proxy.image.service.QueryParamService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.vertx.core.http.HttpClient;
import jakarta.inject.Singleton;

import java.net.URI;
import java.time.Clock;

import static com.pss.proxy.image.integration.IntegrationTest.START_INSTANT;
import static org.mockito.Mockito.spy;

@Factory
public class IntegrationTestConfig {
    @Bean
    @Singleton
    @NotFound
    @Primary
    public NotFoundHandler notFoundHandler(
            @NotFound URI uri,
            @SirvClient HttpClient httpClient,
            QueryParamService queryParamService,
            MultiTryService multiTryService, ClientFailureHandler clientFailureHandler) {
        return spy(new NotFoundHandler(uri, httpClient, queryParamService, multiTryService, clientFailureHandler));
    }

    @Bean
    @Primary
    public Clock clock() {
        return new MutableClock(START_INSTANT);
    }
}
