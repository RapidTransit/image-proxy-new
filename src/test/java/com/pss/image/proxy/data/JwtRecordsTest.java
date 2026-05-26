package com.pss.image.proxy.data;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;

public class JwtRecordsTest {

    @Test
    public void testJwtRecordAccessors() {
        var instant = Instant.parse("2026-01-01T00:00:00Z");

        var args = new JwtArgs(150, 200, "test-profile");
        assertThat(args.w()).isEqualTo(150);
        assertThat(args.thumbnail()).isEqualTo(200);
        assertThat(args.profile()).isEqualTo("test-profile");

        var header = new JwtHeader("HS256", "JWT");
        assertThat(header.alg()).isEqualTo("HS256");
        assertThat(header.typ()).isEqualTo("JWT");

        var payload = new JwtPayload(args, instant, instant, "/p/");
        assertThat(payload.args()).isEqualTo(args);
        assertThat(payload.iat()).isEqualTo(instant);
        assertThat(payload.exp()).isEqualTo(instant);
        assertThat(payload.aud()).isEqualTo("/p/");

        var token = new JwtToken(header, payload, "signature-value");
        assertThat(token.header()).isEqualTo(header);
        assertThat(token.payload()).isEqualTo(payload);
        assertThat(token.signature()).isEqualTo("signature-value");
    }
}
