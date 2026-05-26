package com.pss.proxy.image.service;

import io.vertx.core.http.HttpServerResponse;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.mockito.Mockito.*;


public class MultiTryServiceTest {

    private static final String TEST_URL = "/u/a/test/url.png";


    private static Cache<String, MultiTryService.CounterValue> defaultCache() {
        return Cache2kBuilder.of(String.class, MultiTryService.CounterValue.class)
                .weigher((k,v)-> k.length())
                .maximumWeight(50_000)
                .storeByReference(true)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Test
    public void testMultiTry() {
        HttpServerResponse response = mock(HttpServerResponse.class);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        MultiTryService multiTryService = new MultiTryService(defaultCache(), Clock.systemUTC(), 10);
        multiTryService.manipulateResponseCache(TEST_URL, response);
        verify(response).putHeader("cache-control", "max-age=10");
        reset(response);
        LockSupport.parkNanos(Duration.ofSeconds(11).toNanos());
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        multiTryService.manipulateResponseCache(TEST_URL, response);
        verify(response).putHeader("cache-control", "max-age=10");
        reset(response);
        LockSupport.parkNanos(Duration.ofSeconds(11).toNanos());
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        multiTryService.manipulateResponseCache(TEST_URL, response);
        verify(response).putHeader("cache-control", "max-age=10");
        reset(response);
        LockSupport.parkNanos(Duration.ofSeconds(11).toNanos());
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
        multiTryService.manipulateResponseCache(TEST_URL, response);
        verify(response).putHeader("Content-Type", "image/png");
        verify(response).putHeader("cache-control", "max-age=10");
    }
}
