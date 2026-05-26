package com.pss.proxy.image;

import static org.assertj.core.api.Assertions.*;

import com.pss.proxy.image.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.abc.com/image.png",
            "/image.png",
            "image.png",
            "https://www.abc.com/image.jpg",
            "/../paths/another/image.jpeg",
    })
    public void testValidImageExtensions(String value){
        assertThat(Utils.isImage(value)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "",
            "image.jpng",
            "image.jpeeg",
            "https://www.abc.com/image.sql",
            "/../paths/another/image.he",
    })
    public void testInvalidImageExtensions(String value){
        assertThat(Utils.isImage(value)).isFalse();
    }

    @Test
    public void testNullImageExtensions(){
        assertThat(Utils.isImage(null)).isFalse();
    }


}
