package com.pss.image.proxy.routes;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

/// Cached upstream response.
///
/// `expiresAt` is an absolute epoch-millis timestamp; readers must check it
/// against a [java.time.Clock] before returning the entry. We do not rely
/// on a background TTL sweeper for this cache because it is small (~16
/// entries) and rarely contended.
public record ResponseCache(Buffer buffer, MultiMap headers, long expiresAt) {}
