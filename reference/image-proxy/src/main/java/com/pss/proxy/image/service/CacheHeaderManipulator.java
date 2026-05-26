package com.pss.proxy.image.service;

import io.vertx.core.http.HttpServerResponse;

public interface CacheHeaderManipulator {

    CacheHeaderManipulator NO_OP = (url, response) -> {

    };

    void manipulateResponseCache(String url, HttpServerResponse response);

}
