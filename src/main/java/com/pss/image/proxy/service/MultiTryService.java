package com.pss.image.proxy.service;

import com.pss.image.proxy.util.Verify;
import io.vertx.core.http.HttpServerResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/// Per-URL retry backoff with [HttpServerResponse] cache-control fixup.
///
/// We keep getting transient failures from the upstream CDN. To avoid
/// stampedes we mark the response with a short cache window after a miss
/// so clients back off. After three consecutive miss-and-extend cycles we
/// drop the entry so the URL can be retried fresh.
///
/// Uses [java.util.concurrent.ConcurrentHashMap.compute] for atomic
/// per-key updates, replacing the cache2k `mutate` of the original.
/// Entries that go a long time without touch are removed by [#sweep],
/// which the verticle schedules periodically.
public final class MultiTryService implements CacheHeaderManipulator {

    public static final DateTimeFormatter RFC_5322_DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm:ss");

    private static final AtomicIntegerFieldUpdater<CounterValue> UPDATER_COUNTER =
            AtomicIntegerFieldUpdater.newUpdater(CounterValue.class, "counter");
    private static final AtomicLongFieldUpdater<CounterValue> UPDATER_TIME =
            AtomicLongFieldUpdater.newUpdater(CounterValue.class, "time");

    /// Soft TTL for idle entries — matches the reference's 1-hour cache2k expiry.
    private static final long ENTRY_TTL_MS = 3_600_000L;

    private final Map<String, CounterValue> cache;
    private final Clock clock;
    private final int seconds;
    private final int millis;

    public MultiTryService(Map<String, CounterValue> cache, Clock clock, int seconds) {
        Verify.isTrue(seconds >= 1, "Passed in seconds must be greater than 0, got: " + seconds);
        this.cache = cache;
        this.clock = clock;
        this.seconds = seconds;
        this.millis = seconds * 1000;
    }

    @Override
    public void manipulateResponseCache(String url, HttpServerResponse response) {
        long now = clock.millis();
        long forwardTime = now + millis;
        cache.compute(url, (k, existing) -> {
            if (existing == null) {
                return new CounterValue(forwardTime);
            }
            if (existing.time < now) {
                int count = UPDATER_COUNTER.incrementAndGet(existing);
                if (count > 3) {
                    return null;
                }
                UPDATER_TIME.set(existing, forwardTime);
                setHeaders(response);
                return existing;
            }
            setHeaders(response);
            return existing;
        });
    }

    public void setHeaders(HttpServerResponse response) {
        var localDateTime = LocalDateTime.now(clock).plusSeconds(seconds);
        response.putHeader("cache-control", "max-age=" + seconds)
                .putHeader("expires", RFC_5322_DATE_TIME.format(localDateTime) + " GMT");
    }

    /// Remove entries whose backoff window ended more than [#ENTRY_TTL_MS] ago.
    /// Intended to be called periodically from the event loop.
    public void sweep() {
        long cutoff = clock.millis() - ENTRY_TTL_MS;
        cache.entrySet().removeIf(e -> e.getValue().time < cutoff);
    }

    public static final class CounterValue {
        volatile int counter = 0;
        volatile long time;

        CounterValue(long time) {
            this.time = time;
        }
    }
}
