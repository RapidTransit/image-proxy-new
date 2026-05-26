package com.pss.image.proxy.data;

public record JwtToken(JwtHeader header, JwtPayload payload, String signature) {}
