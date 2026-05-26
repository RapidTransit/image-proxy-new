package com.pss.vertx.common.server;

import io.vertx.ext.web.Router;

public interface RouteRegistration {

    void registerRoutes(Router router);
}
