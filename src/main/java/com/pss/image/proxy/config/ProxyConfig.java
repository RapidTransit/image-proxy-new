package com.pss.image.proxy.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.pss.image.proxy.util.Verify;

import java.util.Map;
import java.util.Set;

/// Static proxy configuration loaded once at startup.
///
/// JSON field names are kebab-case (e.g. `default-profile`), matching the
/// original YAML layout. The compact constructor validates required fields
/// — same invariants as the reference [ProxyConfig] but expressed as a
/// record.
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public record ProxyConfig(
        Set<String> acceptedProfiles,
        String defaultProfile,
        String queryParam,
        String noImage,
        String protectedTestImage,
        Map<String, String> jwtMappings,
        Map<String, String> secureMappings,
        Map<String, String> shimMappings,
        boolean testJwt,
        int multiTryDelay) {

    public ProxyConfig {
        Verify.isFalse(defaultProfile == null || defaultProfile.isEmpty(), "defaultProfile is empty");
        Verify.isFalse(acceptedProfiles == null || acceptedProfiles.isEmpty(), "acceptedProfiles is empty");
        Verify.isFalse(jwtMappings == null || jwtMappings.isEmpty(), "jwtMappings is empty");
        Verify.isFalse(queryParam == null || queryParam.isEmpty(), "queryParam is empty");
        Verify.isFalse(noImage == null || noImage.isEmpty(), "noImage is empty");
        Verify.isFalse(protectedTestImage == null || protectedTestImage.isEmpty(), "protectedTestImage is empty");
        Verify.isTrue(jwtMappings.containsKey(defaultProfile), "Default profile not found in jwtMappings");
        Verify.isTrue(jwtMappings.keySet().containsAll(acceptedProfiles), "Missing Profiles in jwtMappings");
        if (shimMappings != null) {
            Verify.isTrue(acceptedProfiles.containsAll(shimMappings.values()), "shimMappings contains a non-accepted profile");
        }
    }
}
