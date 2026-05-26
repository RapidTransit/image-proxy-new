package com.pss.image.proxy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pss.image.proxy.config.ProxyConfig;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class QueryParamServiceTest {

    private ProxyConfig cfg;
    private QueryParamService service;

    @BeforeEach
    void setUp() {
        cfg = cfg();
        service = new QueryParamService(cfg);
    }

    @Test
    void noParamReturnsDefault() {
        var request = mock(HttpServerRequest.class);
        when(request.getParam("tr")).thenReturn(null);
        when(request.getHeader("Referer")).thenReturn(null);

        var result = service.extractQueryParam(request, "/p/foo.jpg");
        assertThat(result).isEqualTo("p");
    }

    @Test
    void emptyParamReturnsDefault() {
        var request = mock(HttpServerRequest.class);
        when(request.getParam("tr")).thenReturn("");
        when(request.getHeader("Referer")).thenReturn(null);

        var result = service.extractQueryParam(request, "/p/foo.jpg");
        assertThat(result).isEqualTo("p");
    }

    @Test
    void acceptedProfileReturnsAsIs() {
        var request = mock(HttpServerRequest.class);
        when(request.getParam("tr")).thenReturn("p-m_m");
        when(request.getHeader("Referer")).thenReturn(null);

        var result = service.extractQueryParam(request, "/p/foo.jpg");
        assertThat(result).isEqualTo("p-m_m");
    }

    @Test
    void shimMappingAppliedAndReturned() {
        var request = mock(HttpServerRequest.class);
        when(request.getParam("tr")).thenReturn("legacy");
        when(request.getHeader("Referer")).thenReturn(null);

        var result = service.extractQueryParam(request, "/p/foo.jpg");
        assertThat(result).isEqualTo("p-m_m");
    }

    @Test
    void unknownProfileReturnsDefault() {
        var request = mock(HttpServerRequest.class);
        when(request.getParam("tr")).thenReturn("unknown-profile");
        when(request.getHeader("Referer")).thenReturn(null);

        var result = service.extractQueryParam(request, "/p/foo.jpg");
        assertThat(result).isEqualTo("p");
    }

    private static ProxyConfig cfg() {
        return new ProxyConfig(
                Set.of("p", "p-m_m"),
                "p",
                "tr",
                "/images/no-image.png",
                "/p/test-image.png",
                Map.of("p", "JWT_P", "p-m_m", "JWT_M"),
                Map.of(),
                Map.of("legacy", "p-m_m"),
                false,
                10);
    }
}
