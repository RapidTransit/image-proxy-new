package com.pss.proxy.image.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableJwtHeader.class)
public interface JwtHeader {
    String alg();

    String typ();
}
