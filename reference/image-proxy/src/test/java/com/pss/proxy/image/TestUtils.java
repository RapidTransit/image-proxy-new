package com.pss.proxy.image;

import com.pss.proxy.image.config.properties.ProxyConfig;
import io.vertx.core.json.JsonObject;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

public class TestUtils {

    public static ProxyConfig loadDefault(){
        try {
            final Yaml yamlMapper = new Yaml(new SafeConstructor(new LoaderOptions()));
            String value = Files.readString(Path.of("config/application.yaml"));
            String jwt = Files.readString(Path.of("config/application-jwt.yaml"));
            JsonObject jobject = jsonify(yamlMapper.load(value));
            JsonObject jobject1 = jsonify(yamlMapper.load(jwt));
            JsonObject entries = jobject.mergeIn(jobject1, true);
            return entries.getJsonObject("proxy").mapTo(ProxyConfig.class);
            //return jsonify(doc).mapTo(ProxyConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void tryCatcher(ThrowingRunnable tr) {
        try {
            tr.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void tryThrow(ThrowingRunnable tr) {
        try {
            tr.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonObject jsonify(Map<Object, Object> yaml) {
        if (yaml == null) {
            return null;
        }

        final JsonObject json = new JsonObject();
        for (Map.Entry<Object, Object> kv : yaml.entrySet()) {
            Object value = kv.getValue();
            if (value instanceof Map) {
                value = jsonify((Map<Object, Object>) value);
            }
            // snake yaml handles dates as java.util.Date, and JSON does Instant
            if (value instanceof Date) {
                value = ((Date) value).toInstant();
            }
            json.put(kv.getKey().toString(), value);
        }

        return json;
    }

    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
