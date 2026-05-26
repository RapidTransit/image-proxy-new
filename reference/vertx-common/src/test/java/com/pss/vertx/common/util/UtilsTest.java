package com.pss.vertx.common.util;

import com.pss.vertx.common.utils.Verify;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.catchThrowable;

public class UtilsTest {

    @Test
    public void testIsTrue(){
        assertThatThrownBy(()-> Verify.isTrue(false, "It Failed")).hasMessage("It Failed");
        assertThat(catchThrowable(()-> Verify.isTrue(true, "It Failed"))).isNull();
    }

    @Test
    public void testIsFalse(){
        assertThatThrownBy(()-> Verify.isFalse(true, "It Failed")).hasMessage("It Failed");
        assertThat(catchThrowable(()-> Verify.isFalse(false, "It Failed"))).isNull();
    }
}
