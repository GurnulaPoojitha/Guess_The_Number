package com.guessword;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock tests can move forward, e.g. to check that the daily limit resets the next day. */
public class MutableClock extends Clock {

    public static final Instant START = Instant.parse("2026-10-14T10:00:00Z");

    private Instant instant;
    private final ZoneId zone;

    public MutableClock() {
        this(START, ZoneOffset.UTC);
    }

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public void advance(Duration duration) {
        instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
