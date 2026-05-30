package com.pss.image.proxy.config;

import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class AppConfigTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void deserializesBundledApplicationJson() throws Exception {
        try (var in = AppConfigTest.class.getResourceAsStream("/application.json")) {
            assertThat(in)
                    .as("bundled application.json must be on the test classpath")
                    .isNotNull();
            var cfg = mapper.readValue(in, AppConfig.class);

            assertThat(cfg.proxy()).isNotNull();
            assertThat(cfg.proxy().defaultProfile()).isEqualTo("p");
            assertThat(cfg.proxy().queryParam()).isEqualTo("tr");
            assertThat(cfg.proxy().acceptedProfiles()).contains("p", "p-m_m");
            assertThat(cfg.proxy().multiTryDelay()).isPositive();
            assertThat(cfg.proxy().jwtMappings()).containsKey("p");

            assertThat(cfg.vertx()).isNotNull();
            assertThat(cfg.vertx().server()).containsKey("port");
            assertThat(cfg.vertx().client()).containsKey("defaultHost");
            assertThat(cfg.vertx().instance()).containsKey("preferNativeTransport");

            assertThat(cfg.client().host()).startsWith("https://");
        }
    }
}
