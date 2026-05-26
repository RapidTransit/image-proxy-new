package com.pss.proxy.image.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.Instant;

/**
 * Based on:
 * ```json
 * {
 *   "alg": "HS256",
 *   "typ": "JWT"
 * }
 * ```
 * ```json
 * {
 *   "args": {
 *     "w": 150,
 *     "thumbnail": 150,
 *     "profile": "p-t_l"
 *   },
 *   "iat": 1677849859,
 *   "exp": 1677885859,
 *   "aud": "/p/"
 * }
 * ```
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableJwtPayload.class)
public interface JwtPayload {

    JwtArgs args();

    @JsonFormat
    Instant iat();

    Instant exp();

    String aud();
}
