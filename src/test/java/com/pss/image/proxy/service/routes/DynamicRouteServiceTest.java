package com.pss.image.proxy.service.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.junit.jupiter.api.Test;

public class DynamicRouteServiceTest {

    @Test
    void prepareRequestAddsAllParams() {
        var service = new DynamicRouteService();
        var request = mock(HttpServerRequest.class);
        var response = mock(HttpServerResponse.class);

        var requestParams = MultiMap.caseInsensitiveMultiMap()
                .add("key1", "value1")
                .add("key2", "value2");
        when(request.params()).thenReturn(requestParams);

        var httpRequest = MultiMap.caseInsensitiveMultiMap();
        service.prepareRequest(httpRequest, request, response, "/d/test.jpg");

        assertThat(httpRequest.get("key1")).isEqualTo("value1");
        assertThat(httpRequest.get("key2")).isEqualTo("value2");
    }
}
