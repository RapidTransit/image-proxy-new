package com.pss.image.proxy.service;

import static org.assertj.core.api.Assertions.*;


import com.pss.image.proxy.data.JwtArgs;
import com.pss.image.proxy.data.JwtPayload;
import com.pss.image.proxy.data.JwtToken;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

public class JwtDecoderTest {

    private static JwtDecoder decoder;

    @BeforeAll
    public static void setUp() {
        var mapper = JsonMapper.builder().build();
        decoder = new JwtDecoder(mapper);
    }

    @Test
    public void testNormal() {
        String token =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjE1MCwidGh1bWJuYWlsIjoxNTAsInByb2ZpbGUiOiJwLXRfbCJ9LCJpYXQiOjE3NjkwMDkwNzYsImV4cCI6MTc4NDU2MTA3NiwiYXVkIjoiL3AvIn0.Ldo-qhAe1EMHmqHCVm6ZRX6x10Sbs-u9TXy3b0-wQG0";
        JwtToken decode = decoder.decode(token);
        JwtPayload payload = decode.payload();
        assertThat(payload).isNotNull();
        Instant iat = payload.iat();
        Instant exp = payload.exp();
        String aud = payload.aud();
        assertThat(iat).isNotNull();
        assertThat(iat.toEpochMilli() / 1000).isEqualTo(1769009076);
        assertThat(exp).isNotNull();
        assertThat(exp.toEpochMilli() / 1000).isEqualTo(1784561076);
        assertThat(aud).isNotNull();

        JwtArgs args = payload.args();
        assertThat(args).isNotNull();
        String profile = args.profile();
        assertThat(profile).isEqualTo("p-t_l");
        int w = args.w();
        assertThat(w).isEqualTo(150);

        int thumbnail = args.thumbnail();
        assertThat(thumbnail).isEqualTo(150);
    }

    @Test
    public void testError() {
        String token =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjE1MCwidGh1bWJuYWlsIjoxNTAsInByb2ZpbGUiOiJwLXRfbCJ9LCJpYXQiOjE2Nzc4NDk4NTksImV4cCI6MTY3Nzg4NTg1OSwiYXVkIjoiL3AvIn0obXLqvEkiSOwsJEcTtVnXu7twjpmqAA8S1ORkxj23DM";
        assertThatThrownBy(() -> decoder.decode(token)).hasMessage("Split length invalid");
    }

    @Test
    public void testNullToken() {
        assertThatThrownBy(() -> decoder.decode(null)).hasMessage("JWT Value was empty");
    }

    @Test
    public void testEmptyToken() {
        assertThatThrownBy(() -> decoder.decode("")).hasMessage("JWT Value was empty");
    }
}
