package com.pss.proxy.image.service;

import static org.assertj.core.api.Assertions.*;
import com.pss.proxy.image.TestUtils;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

@ExtendWith(VertxExtension.class)
public class JwtTokenCheckerTest {

    protected JwtTokenChecker checker = new JwtTokenChecker(new JwtDecoder(),  null, TestUtils.loadDefault());

    @Test
    public void testInvalidToken(VertxTestContext testContext){
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcmdzIjp7InciOjE1MCwidGh1bWJuYWlsIjoxNTAsInByb2ZpbGUiOiJwLXRfbCJ9LCJpYXQiOjE2Nzc4NDk4NTksImV4cCI6MTY3Nzg4NTg1OSwiYXVkIjoiL3AvIn0.obXLqvEkiSOwsJEcTtVnXu7twjpmqAA8S1ORkxj23DM";
        checker.checkTokens(Map.of("p-t_l", token)).onComplete(testContext.failing(h-> {
            testContext.verify(()-> {
                assertThat(h).hasMessageStartingWith("Jwt token errors: ");
                testContext.completeNow();
            });
        }));
    }
}
