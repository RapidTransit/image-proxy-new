package com.pss.proxy.image.utils;

import com.pss.vertx.common.utils.Verify;
import io.micronaut.core.util.StringUtils;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.RequestOptions;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.regex.Pattern;

public class Utils {


    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile("\\.(png|jpe?g|svgz?|avif|gif|webp|heif|heic|ico|jp2|j2k|jpf|jpx|jpm|mj2)$");

    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_NONE_MATCH = "If-None-Match";
    public static final String REFERER = "Referer";
    public static final String USER_AGENT = "User-Agent";
    public static final String VERSION_QUERY_PARAM = "v";

    public static final Buffer png;
//    private static final MethodHandles.Lookup methodHandles = MethodHandles.lookup();
//    private static final VarHandle nettyValuesMap;

    static {
        try {
//            Field valuesMap = NettyHttpParameters.class.getDeclaredField("valuesMap");
//            valuesMap.setAccessible(true);
//           nettyValuesMap =MethodHandles.privateLookupIn(NettyHttpParameters.class, methodHandles).unreflectVarHandle(valuesMap);

            byte[] bytes = Utils.class.getClassLoader().getResourceAsStream("dot.png").readAllBytes();
            png = Buffer.buffer(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void rethrow(Throwable throwable, Runnable runnable){
        runnable.run();
        throw new RuntimeException(throwable);
    }

    public static boolean isImage(@Nullable String value){
        return StringUtils.isNotEmpty(value) && IMAGE_EXTENSIONS.matcher(value).find();
    }



    public static Handler<Throwable> failFast(@NotNull Vertx vertx){
        return throwable -> {
            throwable.printStackTrace();
            vertx.close();
        };
    }
    public static Handler<Throwable> printAndRethrow(){
        return throwable -> {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        };
    }
    public static Handler<Throwable> printStackTraceAndExit(){
        return throwable -> {
            throwable.printStackTrace();
        };
    }

    public static <T> void putHeader(@NotNull RequestOptions sirvRequest, @NotNull HttpServerRequest clientRequest,
                                     @MagicConstant(valuesFromClass = Utils.class) String value){
        String header = clientRequest.getHeader(value);
        if(StringUtils.isNotEmpty(header)){
            sirvRequest.putHeader(value, header);
        }
    }

    /**
     * Ensure headers do not contain If-Modified-Since and If-None-Match, this will cause a 302 return
     * @param sirvRequest
     * @param clientRequest
     */
    public static void addHeaders(RequestOptions sirvRequest, HttpServerRequest clientRequest){
        putHeader(sirvRequest, clientRequest, ACCEPT);
        putHeader(sirvRequest, clientRequest, ACCEPT_ENCODING);
        putHeader(sirvRequest, clientRequest, REFERER);
        putHeader(sirvRequest, clientRequest, USER_AGENT);
    }


    /**
     * Copied from {@link io.vertx.ext.web.client.impl.HttpRequestImpl#buildUri(String, MultiMap)}
     * Exclude {@link Utils#VERSION_QUERY_PARAM} "v" this is for cache busting
     * @param uri the uri
     * @param queryParams the query params
     * @return a valid uri
     */
    @SuppressWarnings("JavadocReference")
    @NotNull
    public static String buildUri(@NotNull String uri, @NotNull MultiMap queryParams) {
        Verify.isTrue(StringUtils.isNotEmpty(uri), "Null or empty string found");
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        QueryStringEncoder encoder = new QueryStringEncoder(decoder.rawPath());
        decoder.parameters().forEach((name, values) -> {
            if(!VERSION_QUERY_PARAM.equals(name)) {
                for (String value : values) {
                    encoder.addParam(name, value);
                }
            }
        });
        queryParams.forEach(param -> {
            if(!VERSION_QUERY_PARAM.equals(param.getKey())) {
                encoder.addParam(param.getKey(), param.getValue());
            }
        });
        return encoder.toString();
    }

    /**
     * Same as {@link Utils#buildUri(String, MultiMap)} but instead just add profile to the uri, avoid object creation
     * @param uri uri
     * @param profile profile name
     * @return constructed uri
     */
    @NotNull
    public static String buildUriProfile(@NotNull String uri, @NotNull String profile) {
        Verify.isTrue(StringUtils.isNotEmpty(uri), "Null or empty string found");
        Verify.isTrue(StringUtils.isNotEmpty(profile), "Null or empty string found");
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        QueryStringEncoder encoder = new QueryStringEncoder(decoder.rawPath());
        decoder.parameters().forEach((name, values) -> {
            if(!VERSION_QUERY_PARAM.equals(name)) {
                for (String value : values) {
                    encoder.addParam(name, value);
                }
            }
        });
        encoder.addParam("profile", profile);
        return encoder.toString();
    }

    public static void throwAndClose(Logger log, Throwable throwable, HttpServerResponse response, String description){
        if(response.ended()){
            response.end();
        }
        throw new RuntimeException(description, throwable);
    }

    public static void throwAndClose(Throwable throwable, HttpServerResponse response, String description){
        if(!response.ended()){
            response.end();
        }
        if(!response.closed()){
            //response.close();
        }
        throw new RuntimeException(description, throwable);
    }

//    public static LinkedHashMap<CharSequence, List<String>> getParamsMap(MutableHttpRequest<?> request) {
//        return (LinkedHashMap<CharSequence, List<String>>) nettyValuesMap.get(request.getParameters());
//    }


}
