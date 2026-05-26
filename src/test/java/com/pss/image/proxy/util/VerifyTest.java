package com.pss.image.proxy.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class VerifyTest {

    @Test
    public void testIsTrue() {
        assertThatThrownBy(() -> Verify.isTrue(false, "It Failed")).hasMessage("It Failed");
        assertThatCode(() -> Verify.isTrue(true, "It Failed")).doesNotThrowAnyException();
    }

    @Test
    public void testIsFalse() {
        assertThatThrownBy(() -> Verify.isFalse(true, "It Failed")).hasMessage("It Failed");
        assertThatCode(() -> Verify.isFalse(false, "It Failed")).doesNotThrowAnyException();
    }
}
