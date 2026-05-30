package com.pss.image.proxy.service.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pss.image.proxy.config.ProxyConfig;
import com.pss.image.proxy.service.QueryParamService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JwtRouteServiceTest {

    private JwtRouteService service;
    private ProxyConfig cfg;

    @BeforeEach
    void setUp() {
        cfg = new ProxyConfig(
                Set.of("p", "p-m_m"),
                "p",
                "tr",
                "/images/no-image.png",
                "/p/test-image.png",
                Map.of("p", "TOKEN_P", "p-m_m", "TOKEN_M"),
                Map.of(),
                Map.of(),
                false,
                10);
        service = new JwtRouteService(new QueryParamService(cfg), cfg);
    }

    @Test
    void prepareRequestAddsJwtForMappedProfile() {
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(request.getParam("tr")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);

        var httpRequest = MultiMap.caseInsensitiveMultiMap();
        service.prepareRequest(httpRequest, request, response, "/p/test.jpg");

        assertThat(httpRequest.get("jwt")).isEqualTo("TOKEN_P");
    }

    @Test
    void prepareRequestAddsJwtForExplicitProfile() {
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(request.getParam("tr")).thenReturn("p-m_m");
        when(request.getHeader("Referer")).thenReturn(null);

        var httpRequest = MultiMap.caseInsensitiveMultiMap();
        service.prepareRequest(httpRequest, request, response, "/p/test.jpg");

        assertThat(httpRequest.get("jwt")).isEqualTo("TOKEN_M");
    }

    @Test
    void prepareRequestFallsBackWhenProfileNotInMappings() {
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(request.getParam("tr")).thenReturn("unknown");
        when(request.getHeader("Referer")).thenReturn(null);

        var httpRequest = MultiMap.caseInsensitiveMultiMap();
        service.prepareRequest(httpRequest, request, response, "/p/test.jpg");

        assertThat(httpRequest.get("jwt")).isEqualTo("TOKEN_P");
    }
}
