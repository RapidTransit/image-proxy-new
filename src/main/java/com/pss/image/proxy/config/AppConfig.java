package com.pss.image.proxy.config;

import java.util.Map;

/// Top-level application configuration.
///
/// The `vertx`, `server`, and `client` blocks are deserialized as raw maps;
/// [com.pss.image.proxy.MainVerticle] wraps them with `new JsonObject(map)`
/// before handing to Vert.x options constructors. This keeps record shape
/// independent of Vert.x's internal JSON type.
public record AppConfig(ProxyConfig proxy, VertxBlock vertx, ClientBlock client) {

    public record VertxBlock(Map<String, Object> instance, Map<String, Object> server, Map<String, Object> client) {}

    public record ClientBlock(String host) {}
}
