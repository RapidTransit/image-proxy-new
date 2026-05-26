package com.pss.proxy.image.service;

import com.pss.proxy.image.config.annotations.MultiTryCache;
import io.micronaut.context.annotation.Value;
import io.vertx.core.http.HttpServerResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.cache2k.Cache;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * We keep getting failures on the backend, this allows a retry every 10 seconds
 */
@Singleton
public class MultiTryService implements CacheHeaderManipulator {

    public static final DateTimeFormatter RFC_5322_DATE_TIME = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm:ss");

    private static final AtomicIntegerFieldUpdater<CounterValue> UPDATER_COUNTER = AtomicIntegerFieldUpdater.newUpdater(CounterValue.class, "counter");
    private static final AtomicLongFieldUpdater<CounterValue> UPDATER_TIME = AtomicLongFieldUpdater.newUpdater(CounterValue.class, "time");


    private final Cache<String, CounterValue> cache;

    private final Clock clock;


    private final int seconds;
    private final int millis;


    @Inject
    public MultiTryService(@MultiTryCache Cache<String, CounterValue> cache, Clock clock,
                           @Value("${proxy.multi-try-delay:10}") int seconds) {
        if(seconds < 1) throw new RuntimeException("Passed in seconds must be greater than 0, got :" + seconds);
        this.cache = cache;
        this.clock = clock;
        this.seconds = seconds;
        this.millis = seconds * 1000;
    }


    @Override
    public void manipulateResponseCache(String url, HttpServerResponse response){
        long time = clock.millis();
        long forwardTime = time + millis;
        cache.computeIfAbsent(url, (k)-> new CounterValue(forwardTime));
        // THis an atomic operation
        cache.mutate(url, entry -> {
            if(entry.exists()){
                CounterValue value = entry.getValue();
                if(value != null) {
                    if(value.time < time){
                        int count = UPDATER_COUNTER.incrementAndGet(value); // Increment first because we start at zero
                        if(count > 3){
                            entry.remove();
                        } else {
                           UPDATER_TIME.set(value, forwardTime);
                           setHeaders(response);
                        }
                    } else {
                        setHeaders(response);
                    }
                }
            }
        });
    }



    public void setHeaders(HttpServerResponse response) {
        LocalDateTime localDateTime = LocalDateTime.now(clock).plusSeconds(seconds);
        response.putHeader("cache-control", "max-age=" + seconds)
                .putHeader("expires", RFC_5322_DATE_TIME.format(localDateTime) + " GMT");
    }



    public static class CounterValue {
        volatile int counter = 0;

        /**
         * Prevent concurrent counting when multiple requests are made before the cdn responds
         */
        volatile long time;

        CounterValue(long time) {
            this.time = time;
        }
    }

}
