package com.pss.image.proxy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/// Mutable [Clock] for unit tests — advance time deterministically without sleeping.
public final class MutableClock extends Clock {

    private final AtomicReference<Instant> instant;
    private final ZoneId zoneId;

    public MutableClock() {
        this(Instant.now(), ZoneOffset.UTC);
    }

    public MutableClock(Instant instant) {
        this(instant, ZoneOffset.UTC);
    }

    public MutableClock(Instant instant, ZoneId zoneId) {
        this.instant = new AtomicReference<>(instant);
        this.zoneId = zoneId;
    }

    private MutableClock(AtomicReference<Instant> instant, ZoneId zoneId) {
        this.instant = instant;
        this.zoneId = zoneId;
    }

    public void setInstant(Instant instant) {
        this.instant.set(instant);
    }

    public void advance(Duration duration) {
        instant.updateAndGet(i -> i.plus(duration));
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Clock withZone(ZoneId zoneId) {
        return zoneId.equals(this.zoneId) ? this : new MutableClock(instant, zoneId);
    }

    @Override
    public Instant instant() {
        return instant.get();
    }
}
