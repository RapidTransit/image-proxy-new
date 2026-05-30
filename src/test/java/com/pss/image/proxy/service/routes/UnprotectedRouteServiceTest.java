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

public class UnprotectedRouteServiceTest {

    private UnprotectedRouteService service;

    @BeforeEach
    void setUp() {
        var cfg = new ProxyConfig(
                Set.of("p", "p-m_m"),
                "p",
                "tr",
                "/images/no-image.png",
                "/p/test-image.png",
                Map.of("p", "JWT_P", "p-m_m", "JWT_M"),
                Map.of(),
                Map.of(),
                false,
                10);
        service = new UnprotectedRouteService(new QueryParamService(cfg));
    }

    @Test
    void prepareRequestAddsProfile() {
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);
        when(request.getParam("tr")).thenReturn("p-m_m");
        when(request.getHeader("Referer")).thenReturn(null);

        var httpRequest = MultiMap.caseInsensitiveMultiMap();
        service.prepareRequest(httpRequest, request, response, "/u/test.jpg");

        assertThat(httpRequest.get("profile")).isEqualTo("p-m_m");
    }
}
