package com.pss.proxy.image.config.properties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pss.proxy.image.utils.Utils;
import com.pss.vertx.common.utils.Verify;
import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.convert.format.MapFormat;
import io.micronaut.core.naming.conventions.StringConvention;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@ConfigurationProperties("proxy")
public class ProxyConfig {


    private final Set<String> acceptedProfiles;

    private final String defaultProfile;

    private final String queryParam;

    private final String noImage;

    private final String protectedTestImage;

    private final Map<String, String> jwtMappings;

    private final Map<String, String> secureMappings;

    private final Map<String, String> shimMappings;

    private final boolean testJwt;

    private final int multiTryDelay;

    @JsonCreator
    @ConfigurationInject
    public ProxyConfig(@JsonProperty("accepted-profiles") Set<String> acceptedProfiles,
                       @JsonProperty("default-profile") String defaultProfile,
                       @JsonProperty("query-param") String queryParam,
                       @JsonProperty("no-image") String noImage,
                       @JsonProperty("protected-test-image") String protectedTestImage,
                       @MapFormat(keyFormat = StringConvention.RAW)
                       @JsonProperty("jwt-mappings") Map<String, String> jwtMappings,
                       @MapFormat(keyFormat = StringConvention.RAW)
                       @JsonProperty("secure-mappings") Map<String, String> secureMappings,
                       @MapFormat(keyFormat = StringConvention.RAW)
                       @JsonProperty("shim-mappings") Map<String, String> shimMappings,
                       @JsonProperty("test-jwt") boolean testJwt,
                       @JsonProperty("multi-try-delay") int multiTryDelay) {
        this.acceptedProfiles = acceptedProfiles;
        this.defaultProfile = defaultProfile;
        this.queryParam = queryParam;
        this.noImage = noImage;
        this.protectedTestImage = protectedTestImage;
        this.secureMappings = secureMappings;
        this.jwtMappings = jwtMappings;
        this.shimMappings = shimMappings;
        this.testJwt = testJwt;
        this.multiTryDelay = multiTryDelay;
        check();
    }


    public Set<String> acceptedProfiles() {
        return acceptedProfiles;
    }

    public String defaultProfile() {
        return defaultProfile;
    }

    public String queryParam() {
        return queryParam;
    }

    public String noImage() {
        return noImage;
    }

    public String protectedTestImage() {
        return protectedTestImage;
    }

    public Map<String, String> jwtMappings() {
        return jwtMappings;
    }

    public Map<String, String> secureMappings() {
        return secureMappings;
    }

    public Map<String, String> shimMappings() {
        return shimMappings;
    }

    public boolean testJwt() {
        return testJwt;
    }

    public int multiTryDelay() {
        return multiTryDelay;
    }

    protected void check(){
        Verify.isFalse(defaultProfile().isEmpty(), "defaultProfile is empty");
        Verify.isFalse(acceptedProfiles().isEmpty(), "Accepted profiles is empty");
        Verify.isFalse(jwtMappings().isEmpty(), "jwtMappings is empty");
        Verify.isFalse(queryParam().isEmpty(), "query params is empty");
        Verify.isFalse(noImage().isEmpty(), "no image is empty");
        Verify.isFalse(protectedTestImage().isEmpty(), "protected test image is empty");
        Verify.isTrue(jwtMappings().containsKey(defaultProfile()), "Default profile not found");
        Verify.isTrue(jwtMappings().keySet().containsAll(acceptedProfiles()), "Missing Profiles");
        Verify.isTrue(acceptedProfiles().containsAll(shimMappings().values()), "Shim mappings contains a non accepeted  Profiles");
    }
}
