package com.pss.image.proxy.data;

import java.time.Instant;

/// Decoded JWT payload.
///
/// Shape matches Sirv's pre-signed tokens, e.g.:
/// ```
/// { "args": { "w": 150, "thumbnail": 150, "profile": "p-t_l" },
///   "iat": 1677849859, "exp": 1677885859, "aud": "/p/" }
/// ```
public record JwtPayload(JwtArgs args, Instant iat, Instant exp, String aud) {}
