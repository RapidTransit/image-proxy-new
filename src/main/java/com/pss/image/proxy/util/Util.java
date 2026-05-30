package com.pss.image.proxy.util;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.RequestOptions;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class Util {

    private static final Pattern IMAGE_EXTENSIONS =
            Pattern.compile("\\.(png|jpe?g|svgz?|avif|gif|webp|heif|heic|ico|jp2|j2k|jpf|jpx|jpm|mj2)$");

    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String REFERER = "Referer";
    public static final String USER_AGENT = "User-Agent";
    public static final String VERSION_QUERY_PARAM = "v";

    public static final Buffer png;

    static {
        try {
            var bytes =
                    Util.class.getClassLoader().getResourceAsStream("dot.png").readAllBytes();
            png = Buffer.buffer(bytes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Util() {}

    public static boolean isImage(String value) {
        return value != null
                && !value.isEmpty()
                && IMAGE_EXTENSIONS.matcher(value).find();
    }

    public static Handler<Throwable> printAndRethrow() {
        return throwable -> {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        };
    }

    public static void putHeader(RequestOptions sirvRequest, HttpServerRequest clientRequest, String value) {
        var header = clientRequest.getHeader(value);
        if (header != null && !header.isEmpty()) {
            sirvRequest.putHeader(value, header);
        }
    }

    /// Ensure headers do not contain If-Modified-Since and If-None-Match, this will cause a 302 return
    public static void addHeaders(RequestOptions sirvRequest, HttpServerRequest clientRequest) {
        putHeader(sirvRequest, clientRequest, ACCEPT);
        putHeader(sirvRequest, clientRequest, ACCEPT_ENCODING);
        putHeader(sirvRequest, clientRequest, REFERER);
        putHeader(sirvRequest, clientRequest, USER_AGENT);
    }

    /// Parse uri for existing query params, drop any named "v" (cache busting),
    /// then merge in queryParams (also dropping any named "v").
    /// URL-encodes names and values with UTF-8.
    public static String buildUri(String uri, MultiMap queryParams) {
        Verify.isTrue(uri != null && !uri.isEmpty(), "Null or empty string found");
        var params = parseQueryString(uri);
        var queryPath = extractPath(uri);
        var result = new StringBuilder(queryPath);
        var first = true;

        for (var entry : params.entrySet()) {
            if (!VERSION_QUERY_PARAM.equals(entry.getKey())) {
                for (var value : entry.getValue()) {
                    if (first) {
                        result.append("?");
                        first = false;
                    } else {
                        result.append("&");
                    }
                    result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }
        }

        for (var param : queryParams) {
            if (!VERSION_QUERY_PARAM.equals(param.getKey())) {
                if (first) {
                    result.append("?");
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
            }
        }

        return result.toString();
    }

    /// Same as buildUri but only adds a single profile=<profile> param, avoiding object creation.
    public static String buildUriProfile(String uri, String profile) {
        Verify.isTrue(uri != null && !uri.isEmpty(), "Null or empty string found");
        Verify.isTrue(profile != null && !profile.isEmpty(), "Null or empty string found");
        var params = parseQueryString(uri);
        var queryPath = extractPath(uri);
        var result = new StringBuilder(queryPath);
        var first = true;

        for (var entry : params.entrySet()) {
            if (!VERSION_QUERY_PARAM.equals(entry.getKey())) {
                for (var value : entry.getValue()) {
                    if (first) {
                        result.append("?");
                        first = false;
                    } else {
                        result.append("&");
                    }
                    result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                }
            }
        }

        if (first) {
            result.append("?");
        } else {
            result.append("&");
        }
        result.append(URLEncoder.encode("profile", StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(profile, StandardCharsets.UTF_8));

        return result.toString();
    }

    /// Parse query string from URI into a map of parameter names to lists of values.
    private static Map<String, List<String>> parseQueryString(String uri) {
        var result = new LinkedHashMap<String, List<String>>();
        var queryStart = uri.indexOf('?');
        if (queryStart == -1) {
            return result;
        }

        var queryString = uri.substring(queryStart + 1);
        if (queryString.isEmpty()) {
            return result;
        }

        for (var pair : queryString.split("&")) {
            var eqIdx = pair.indexOf('=');
            String name, value;
            if (eqIdx == -1) {
                name = URLDecoder.decode(pair, StandardCharsets.UTF_8);
                value = "";
            } else {
                name = URLDecoder.decode(pair.substring(0, eqIdx), StandardCharsets.UTF_8);
                value = URLDecoder.decode(pair.substring(eqIdx + 1), StandardCharsets.UTF_8);
            }
            result.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        return result;
    }

    /// Extract the path portion of a URI (everything before the query string).
    private static String extractPath(String uri) {
        var queryStart = uri.indexOf('?');
        return queryStart == -1 ? uri : uri.substring(0, queryStart);
    }
}
