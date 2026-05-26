package com.pss.proxy.image;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

public class MutableClock extends Clock {

    private final AtomicReference<Instant> instant;
    private final ZoneId zoneId;


    public MutableClock() {
        this.instant = new AtomicReference<>(Instant.now());
        this.zoneId = ZoneOffset.UTC;
    }

    public MutableClock(ZoneId zoneId) {
        this.instant = new AtomicReference<>(Instant.now());
        this.zoneId = zoneId;
    }

    public MutableClock(Instant instant, ZoneId zoneId) {
        this.instant = new AtomicReference<>(instant);
        this.zoneId = zoneId;
    }
    public MutableClock(Instant instant) {
        this.instant = new AtomicReference<>(instant);
        this.zoneId = ZoneOffset.UTC;
    }
    public MutableClock(AtomicReference<Instant> instant, ZoneId zoneId) {
        this.instant = instant;
        this.zoneId = zoneId;
    }

    public void setInstant(Instant instant) {
        this.instant.set(instant);
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
