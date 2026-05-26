package com.pss.proxy.image.service;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.pss.proxy.image.data.ImmutableJwtToken;
import com.pss.proxy.image.data.JwtHeader;
import com.pss.proxy.image.data.JwtPayload;
import com.pss.proxy.image.data.JwtToken;
import com.pss.proxy.image.utils.Utils;
import com.pss.vertx.common.utils.Verify;
import io.micronaut.core.util.StringUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.jackson.DatabindCodec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class JwtDecoder {

    private static final Logger log = LogManager.getLogger(JwtDecoder.class);

    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    static {
        DatabindCodec.mapper().registerModule(new JavaTimeModule());
    }

    public JwtToken decode(String value){
        Verify.isTrue(StringUtils.isNotEmpty(value), "JWT Value was empty");
        String[] split = value.split("\\.");
        Verify.isTrue(split.length == 3, "Split length invalid");
        var header = Json.decodeValue(getDecoded(split[0]), JwtHeader.class);
        var body = Json.decodeValue(getDecoded(split[1]), JwtPayload.class);
        String signature = getDecoded(split[2]);
        return null;
//        return ImmutableJwtToken.builder()
//                .header(header)
//                .payload(body)
//                .signature(signature)
//                .build();

    }

    private static String getDecoded(String value){
        return new String(decoder.decode(value.getBytes(StandardCharsets.UTF_8)));
    }
}
