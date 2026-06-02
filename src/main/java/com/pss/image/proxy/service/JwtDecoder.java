package com.pss.image.proxy.service;

import com.pss.image.proxy.data.JwtHeader;
import com.pss.image.proxy.data.JwtPayload;
import com.pss.image.proxy.data.JwtToken;
import com.pss.image.proxy.util.Verify;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import tools.jackson.databind.ObjectMapper;

/// Decodes a JWT compact serialization into a [JwtToken] record.
///
/// Only header + payload are JSON-deserialized; the signature is kept as
/// the raw URL-safe base64 string (segment 3 verbatim). We never verify
/// signatures locally — that is delegated to Sirv via
/// [JwtTokenChecker] when `proxy.test-jwt` is enabled.
public final class JwtDecoder {

    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper mapper;

    public JwtDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JwtToken decode(String value) {
        Verify.isTrue(value != null && !value.isEmpty(), "JWT Value was empty");
        var split = value.split("\\.");
        Verify.isTrue(split.length == 3, "Split length invalid");
        try {
            var header = mapper.readValue(decodeSegment(split[0]), JwtHeader.class);
            var payload = mapper.readValue(decodeSegment(split[1]), JwtPayload.class);
            return new JwtToken(header, payload, split[2]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode JWT", e);
        }
    }

    private static String decodeSegment(String value) {
        return new String(DECODER.decode(value.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
