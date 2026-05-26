package com.pss.image.proxy.service;

import com.pss.image.proxy.config.ProxyConfig;
import com.pss.image.proxy.util.Util;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.RequestOptions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Validates the configured JWT tokens at startup when `proxy.test-jwt` is on.
///
/// For each token: decode it and check the `exp` claim hasn't passed; then
/// make a real upstream request with the token and assert the CDN does not
/// reject it (`403`). Any failure rolls up into a single exception so the
/// verticle aborts loudly rather than running with bad config.
public final class JwtTokenChecker {

    private final JwtDecoder decoder;
    private final HttpClient httpClient;
    private final ProxyConfig config;

    public JwtTokenChecker(JwtDecoder decoder, HttpClient httpClient, ProxyConfig config) {
        this.decoder = decoder;
        this.httpClient = httpClient;
        this.config = config;
    }

    public Future<Void> checkTokens(Map<String, String> stringTokens) {
        Map<String, List<String>> errors = Collections.synchronizedMap(new HashMap<>());
        List<Future<?>> futures = new ArrayList<>();
        var now = Instant.now();
        for (var entry : stringTokens.entrySet()) {
            var key = entry.getKey();
            var decoded = decoder.decode(entry.getValue());
            if (now.isAfter(decoded.payload().exp())) {
                errors.computeIfAbsent(key, k -> new ArrayList<>()).add("Token expired at: " + decoded.payload().exp());
                continue;
            }
            if (config.testJwt()) {
                var encoded = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                var uri = config.protectedTestImage() + "?jwt=" + encoded;
                futures.add(httpClient.request(new RequestOptions().setURI(uri))
                        .flatMap(HttpClientRequest::send)
                        .onSuccess(response -> {
                            if (response.statusCode() == 404) {
                                throw new RuntimeException("Protected Test Image not found");
                            } else if (response.statusCode() == 403) {
                                errors.computeIfAbsent(key, k -> new ArrayList<>()).add("Token is invalid");
                            }
                        })
                        .onFailure(Util.printAndRethrow()));
            }
        }
        return Future.all(futures).map(h -> {
            if (!errors.isEmpty()) {
                throw new RuntimeException("Jwt token errors: " + errors);
            }
            return null;
        });
    }
}
