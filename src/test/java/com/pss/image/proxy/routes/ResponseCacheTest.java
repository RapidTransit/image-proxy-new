package com.pss.image.proxy.routes;

import static org.assertj.core.api.Assertions.*;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.Test;

public class ResponseCacheTest {

    @Test
    public void testResponseCacheAccessors() {
        var buffer = Buffer.buffer("test");
        var headers = MultiMap.caseInsensitiveMultiMap();
        var expiresAt = 12345L;

        var cache = new ResponseCache(buffer, headers, expiresAt);
        assertThat(cache.buffer()).isEqualTo(buffer);
        assertThat(cache.headers()).isEqualTo(headers);
        assertThat(cache.expiresAt()).isEqualTo(12345L);
    }
}
