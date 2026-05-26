package com.pss.proxy.image.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(as = ImmutableJwtArgs.class)
public interface JwtArgs {

    int w();

    int thumbnail();

    String profile();
}
