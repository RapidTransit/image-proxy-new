package com.pss.proxy.image.utils;

public interface ThrowingSupplier<T>  {

    static <T> T supply(ThrowingSupplier<T> supplier){
        try  {
            return supplier.get();
        } catch (Throwable t){
            throw new RuntimeException(t);
        }
    }

    T get() throws Throwable;

}
