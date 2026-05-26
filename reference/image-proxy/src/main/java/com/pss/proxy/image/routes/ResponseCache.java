package com.pss.proxy.image.routes;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

public record ResponseCache(Buffer buffer, MultiMap headers) {

}
