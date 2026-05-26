package com.pss.image.proxy.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

public class ProxyConfigTest {

    private static ProxyConfig defaults() {
        return new ProxyConfig(
                Set.of("p1", "p2"),
                "p1",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1", "p2", "token2"),
                Map.of(),
                Map.of(),
                false,
                5000
        );
    }

    @Test
    public void testValidConfig() {
        var cfg = defaults();
        assertThat(cfg).isNotNull();
        assertThat(cfg.defaultProfile()).isEqualTo("p1");
        assertThat(cfg.acceptedProfiles()).containsExactlyInAnyOrder("p1", "p2");
    }

    @Test
    public void testEmptyDefaultProfile() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("defaultProfile is empty");
    }

    @Test
    public void testNullDefaultProfile() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                null,
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("defaultProfile is empty");
    }

    @Test
    public void testEmptyAcceptedProfiles() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of(),
                "p1",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("acceptedProfiles is empty");
    }

    @Test
    public void testEmptyJwtMappings() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p1",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of(),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("jwtMappings is empty");
    }

    @Test
    public void testEmptyQueryParam() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p1",
                "",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("queryParam is empty");
    }

    @Test
    public void testEmptyNoImage() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p1",
                "profile",
                "",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("noImage is empty");
    }

    @Test
    public void testEmptyProtectedTestImage() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p1",
                "profile",
                "/images/no-image.png",
                "",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("protectedTestImage is empty");
    }

    @Test
    public void testDefaultProfileNotInJwtMappings() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p2",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("Default profile not found in jwtMappings");
    }

    @Test
    public void testAcceptedProfileNotInJwtMappings() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1", "p2"),
                "p1",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of(),
                false,
                5000
        )).hasMessage("Missing Profiles in jwtMappings");
    }

    @Test
    public void testShimMappingNonAcceptedProfile() {
        assertThatThrownBy(() -> new ProxyConfig(
                Set.of("p1"),
                "p1",
                "profile",
                "/images/no-image.png",
                "/shared/test.png",
                Map.of("p1", "token1"),
                Map.of(),
                Map.of("source", "non-existent"),
                false,
                5000
        )).hasMessage("shimMappings contains a non-accepted profile");
    }
}
