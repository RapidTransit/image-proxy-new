package com.pss.proxy.image.service;

import com.pss.proxy.image.config.properties.ProxyConfig;
import com.pss.proxy.image.utils.Utils;
import io.micronaut.core.util.StringUtils;
import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class QueryParamService {

    private static final Logger log = LogManager.getLogger(QueryParamService.class);

    protected final ProxyConfig proxyConfig;

    @Inject
    public QueryParamService(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }


    public String extractQueryParam(HttpServerRequest request, String path) {
        String profile = request.getParam(proxyConfig.queryParam());
        if(StringUtils.isEmpty(profile)) {
            log.warn("`{}`requested with no profile using default profile: `{}`, referrer: `{}`", path, proxyConfig.defaultProfile(), request.getHeader(Utils.REFERER));
            return proxyConfig.defaultProfile();
        }
        String possibleShim = proxyConfig.shimMappings().getOrDefault(profile, profile);
        if(!profile.equals(possibleShim)) {
            log.warn("Deprecated profile used: `{}`, shimmed to: `{}`, referrer: `{}`", profile, profile, request.getHeader(Utils.REFERER));
        }
        if(proxyConfig.acceptedProfiles().contains(possibleShim)) {
            return possibleShim;
        } else {
            log.warn("Profile `{}` was not found in accepted profiles using default: `{}`, referrer: `{}`", possibleShim, proxyConfig.defaultProfile(), request.getHeader(Utils.REFERER));
            return proxyConfig.defaultProfile();
        }
    }


}
