package com.pss.proxy.image.service;

import static org.assertj.core.api.Assertions.*;

import com.pss.proxy.image.data.JwtArgs;
import com.pss.proxy.image.data.JwtPayload;
import com.pss.proxy.image.data.JwtToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class JwtDecoderTest {

    protected JwtDecoder decoder = new JwtDecoder();

    @Test
    public void testNormal(){
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjE1MCwidGh1bWJuYWlsIjoxNTAsInByb2ZpbGUiOiJwLXRfbCJ9LCJpYXQiOjE2Nzc4NDk4NTksImV4cCI6MTY3Nzg4NTg1OSwiYXVkIjoiL3AvIn0.obXLqvEkiSOwsJEcTtVnXu7twjpmqAA8S1ORkxj23DM";
        JwtToken decode = decoder.decode(token);
        JwtPayload payload = decode.payload();
        assertThat(payload).isNotNull();
        Instant iat = payload.iat();
        Instant exp = payload.exp();
        String aud = payload.aud();
        assertThat(iat).isNotNull();
        assertThat(iat.toEpochMilli() / 1000).isEqualTo(1677849859);
        assertThat(exp).isNotNull();
        assertThat(exp.toEpochMilli() / 1000).isEqualTo(1677849859 + 36000);
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
    public void testError(){
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjE1MCwidGh1bWJuYWlsIjoxNTAsInByb2ZpbGUiOiJwLXRfbCJ9LCJpYXQiOjE2Nzc4NDk4NTksImV4cCI6MTY3Nzg4NTg1OSwiYXVkIjoiL3AvIn0obXLqvEkiSOwsJEcTtVnXu7twjpmqAA8S1ORkxj23DM";
        assertThatThrownBy(()-> decoder.decode(token)).hasMessage("Split length invalid");
    }
}
