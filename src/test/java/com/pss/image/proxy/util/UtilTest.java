package com.pss.image.proxy.util;

import static org.assertj.core.api.Assertions.*;

import io.vertx.core.MultiMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UtilTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://www.abc.com/image.png",
                "/image.png",
                "image.png",
                "https://www.abc.com/image.jpg",
                "/../paths/another/image.jpeg",
            })
    public void testValidImageExtensions(String value) {
        assertThat(Util.isImage(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/",
                "",
                "image.jpng",
                "image.jpeeg",
                "https://www.abc.com/image.sql",
                "/../paths/another/image.he",
            })
    public void testInvalidImageExtensions(String value) {
        assertThat(Util.isImage(value)).isFalse();
    }

    @Test
    public void testNullImageExtensions() {
        assertThat(Util.isImage(null)).isFalse();
    }

    @Test
    public void testBuildUriWithQueryParamFiltering() {
        var multiMap = MultiMap.caseInsensitiveMultiMap().add("a", "1").add("v", "drop");
        var result = Util.buildUri("/p/foo.jpg", multiMap);
        assertThat(result).contains("a=1");
        assertThat(result).doesNotContain("v=drop");
    }

    @Test
    public void testBuildUriWithExistingQueryString() {
        var multiMap = MultiMap.caseInsensitiveMultiMap();
        var result = Util.buildUri("/p/foo.jpg?x=1&v=2", multiMap);
        assertThat(result).contains("x=1");
        assertThat(result).doesNotContain("v=2");
    }

    @Test
    public void testBuildUriProfile() {
        var result = Util.buildUriProfile("/images/no-image.png", "p");
        assertThat(result).isEqualTo("/images/no-image.png?profile=p");
    }

    @Test
    public void testBuildUriProfileWithExistingQueryString() {
        var multiMap = MultiMap.caseInsensitiveMultiMap();
        var result = Util.buildUriProfile("/images/no-image.png?other=1&v=drop", "p");
        assertThat(result).contains("other=1");
        assertThat(result).contains("profile=p");
        assertThat(result).doesNotContain("v=drop");
    }

    @Test
    public void buildUriRejectsEmptyInput() {
        var mm = MultiMap.caseInsensitiveMultiMap();
        assertThatThrownBy(() -> Util.buildUri("", mm)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Util.buildUri(null, mm)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void buildUriProfileRejectsEmptyInput() {
        assertThatThrownBy(() -> Util.buildUriProfile("", "p")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Util.buildUriProfile("/x.png", "")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Util.buildUriProfile("/x.png", null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void buildUriHandlesParamWithoutEqualsSign() {
        var result = Util.buildUri("/x.png?flag", MultiMap.caseInsensitiveMultiMap());
        assertThat(result).contains("flag=");
    }

    @Test
    public void buildUriHandlesMultiValueParams() {
        var result = Util.buildUri("/x.png?a=1&a=2", MultiMap.caseInsensitiveMultiMap());
        assertThat(result).contains("a=1").contains("a=2");
    }
}
