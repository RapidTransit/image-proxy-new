package com.pss.proxy.image.data;

import org.immutables.value.Value;

@Value.Immutable
public interface JwtToken {
    JwtHeader header();

    JwtPayload payload();

    String signature();
}
