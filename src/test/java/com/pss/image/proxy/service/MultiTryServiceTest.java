package com.pss.image.proxy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pss.image.proxy.MutableClock;
import io.vertx.core.http.HttpServerResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiTryServiceTest {

    private static final String TEST_URL = "/u/a/test/url.png";

    private MutableClock clock;
    private Map<String, MultiTryService.CounterValue> cache;
    private MultiTryService service;
    private HttpServerResponse response;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        cache = new ConcurrentHashMap<>();
        service = new MultiTryService(cache, clock, 10);
        response = mock(HttpServerResponse.class);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);
    }

    @Test
    void firstHitCreatesEntryAndSkipsHeaders() {
        service.manipulateResponseCache(TEST_URL, response);
        verify(response, never()).putHeader(anyString(), anyString());
        assertThat(cache).containsKey(TEST_URL);
        assertThat(cache.get(TEST_URL).counter).isZero();
    }

    @Test
    void hitWithinBackoffWindowSetsHeadersWithoutIncrement() {
        service.manipulateResponseCache(TEST_URL, response);
        reset(response);
        when(response.putHeader(anyString(), anyString())).thenReturn(response);

        clock.advance(Duration.ofSeconds(5));
        service.manipulateResponseCache(TEST_URL, response);

        verify(response).putHeader("cache-control", "max-age=10");
        assertThat(cache.get(TEST_URL).counter).isZero();
    }

    @Test
    void fourthCrossingRemovesEntry() {
        // One creation + four crossings (counter goes 1,2,3,4; removed when count > 3).
        for (int i = 0; i < 5; i++) {
            service.manipulateResponseCache(TEST_URL, response);
            clock.advance(Duration.ofSeconds(11));
        }
        assertThat(cache).doesNotContainKey(TEST_URL);
    }

    @Test
    void setHeadersFormatsExpires() {
        service.setHeaders(response);
        verify(response).putHeader("cache-control", "max-age=10");
        verify(response)
                .putHeader(org.mockito.ArgumentMatchers.eq("expires"), org.mockito.ArgumentMatchers.contains("2026"));
    }

    @Test
    void sweepRemovesEntriesOlderThanOneHour() {
        service.manipulateResponseCache(TEST_URL, response);
        assertThat(cache).containsKey(TEST_URL);
        clock.advance(Duration.ofMinutes(61));
        service.sweep();
        assertThat(cache).doesNotContainKey(TEST_URL);
    }

    @Test
    void sweepKeepsFreshEntries() {
        service.manipulateResponseCache(TEST_URL, response);
        clock.advance(Duration.ofMinutes(30));
        service.sweep();
        assertThat(cache).containsKey(TEST_URL);
    }

    @Test
    void constructorRejectsNonPositiveSeconds() {
        assertThatThrownBy(() -> new MultiTryService(new ConcurrentHashMap<>(), clock, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("greater than 0");
        assertThatThrownBy(() -> new MultiTryService(new ConcurrentHashMap<>(), clock, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void constructorAcceptsSecondsOfOne() {
        var s = new MultiTryService(new ConcurrentHashMap<>(), clock, 1);
        s.setHeaders(response);
        verify(response).putHeader("cache-control", "max-age=1");
    }

    @Test
    void crossingExtendsBackoffWindow() {
        service.manipulateResponseCache(TEST_URL, response);
        var initial = cache.get(TEST_URL).time;
        clock.advance(Duration.ofSeconds(11));
        service.manipulateResponseCache(TEST_URL, response);
        var afterCrossing = cache.get(TEST_URL).time;
        assertThat(afterCrossing).isGreaterThan(initial);
        assertThat(cache.get(TEST_URL).counter).isEqualTo(1);
    }
}
