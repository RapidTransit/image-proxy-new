module vertx.admin.vertx.common.main {
    exports com.pss.vertx.common.routes;
    exports com.pss.vertx.common.utils;
    exports com.pss.vertx.common.server;

    requires io.micronaut.micronaut_context;
    requires io.micronaut.micronaut_core;
    requires io.micronaut.micronaut_inject;
    requires io.vertx.core;
    requires io.vertx.web;
    requires jakarta.annotation;
    requires jakarta.inject;

    requires org.apache.logging.log4j;
    requires org.jetbrains.annotations;
    opens com.pss.vertx.common;
    opens com.pss.vertx.common.routes;
    opens com.pss.vertx.common.utils;
    opens com.pss.vertx.common.server;
    opens com.pss.vertx.common.service;
}