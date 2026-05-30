package com.pss.image.proxy.service;

import com.pss.image.proxy.config.ProxyConfig;
import com.pss.image.proxy.util.Util;
import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryParamService {

    private static final Logger log = LoggerFactory.getLogger(QueryParamService.class);

    protected final ProxyConfig proxyConfig;

    public QueryParamService(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public String extractQueryParam(HttpServerRequest request, String path) {
        String profile = request.getParam(proxyConfig.queryParam());
        if (profile == null || profile.isEmpty()) {
            log.warn(
                    "`{}`requested with no profile using default profile: `{}`, referrer: `{}`",
                    path,
                    proxyConfig.defaultProfile(),
                    request.getHeader(Util.REFERER));
            return proxyConfig.defaultProfile();
        }
        String possibleShim = proxyConfig.shimMappings().getOrDefault(profile, profile);
        if (!profile.equals(possibleShim)) {
            log.warn(
                    "Deprecated profile used: `{}`, shimmed to: `{}`, referrer: `{}`",
                    profile,
                    profile,
                    request.getHeader(Util.REFERER));
        }
        if (proxyConfig.acceptedProfiles().contains(possibleShim)) {
            return possibleShim;
        } else {
            log.warn(
                    "Profile `{}` was not found in accepted profiles using default: `{}`, referrer: `{}`",
                    possibleShim,
                    proxyConfig.defaultProfile(),
                    request.getHeader(Util.REFERER));
            return proxyConfig.defaultProfile();
        }
    }
}
