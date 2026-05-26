package com.pss.vertx.common.utils;

import org.jetbrains.annotations.NotNull;

public class Verify {
    public static void isTrue(boolean check, @NotNull String message){
        if(!check){
            throw new RuntimeException(message);
        }
    }

    public static void isFalse(boolean check, @NotNull String message){
        if(check){
            throw new RuntimeException(message);
        }
    }
}
