package com.pss.proxy.image.service;

import com.pss.proxy.image.config.properties.ProxyConfig;
import com.pss.proxy.image.data.JwtToken;
import com.pss.proxy.image.utils.Utils;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.RequestOptions;


import java.time.Instant;
import java.util.*;


public class JwtTokenChecker {

    protected final JwtDecoder decoder;
    private final HttpClient httpClient;
    protected final ProxyConfig config;

    public JwtTokenChecker(JwtDecoder decoder, HttpClient httpClient, ProxyConfig config) {
        this.decoder = decoder;
        this.httpClient = httpClient;
        this.config = config;
    }

    public Future<Void> checkTokens(Map<String, String> stringTokens) {
        Map<String, List<String>> errors = Collections.synchronizedMap(new HashMap<>());
        List<Future<?>> futures = new ArrayList<>();
        Instant now = Instant.now();
        for (Map.Entry<String, String> stringToken : stringTokens.entrySet()) {
            var key = stringToken.getKey();
            JwtToken decoded = decoder.decode(stringToken.getValue());
            if(now.isAfter(decoded.payload().exp())){
                errors.computeIfAbsent(key, k -> new ArrayList<>()).add("Token expired at: " + decoded.payload().exp());
            } else if(config.testJwt()) {
                var encoder = new QueryStringEncoder(config.protectedTestImage());
                encoder.addParam("jwt", stringToken.getValue());
                futures.add(httpClient.request(new RequestOptions().setURI(encoder.toString()))
                        .flatMap(HttpClientRequest::send)
                        .onSuccess(h-> {
                            if(h.statusCode() == 404) {
                                throw new RuntimeException("Protected Test Image not found");
                            } else if(h.statusCode() == 403) {
                                errors.computeIfAbsent(key, k -> new ArrayList<>()).add("Token is invalid");
                            }
                        }).onFailure(Utils.printAndRethrow()));


            }
        }
        return Future.all(futures)
                .map(h-> {
                    if(!errors.isEmpty()){
                        throw new RuntimeException("Jwt token errors: " + errors);
                    }
                    return null;
                });
    }
}
