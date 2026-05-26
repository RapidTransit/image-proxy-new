/// Image proxy module.
///
/// JPMS module powering the Vert.x reverse proxy. The graph is intentionally
/// minimal: required modules are declared explicitly, reflective access is
/// granted only to the framework modules that need it (Vert.x launcher for
/// verticle loading, Jackson for record deserialization).
///
/// Module names for transitive auto-modules (Netty, logback) are folded into
/// the Beryx-produced `mergedModule` at jlink time, so they do not appear
/// here.
module pss.image.proxy {
    requires io.vertx.core;
    requires io.vertx.web;
    requires io.vertx.web.client;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires org.slf4j;

    requires java.naming;
    requires jdk.unsupported;

    /// Jackson reads record components reflectively.
    opens com.pss.image.proxy.config to com.fasterxml.jackson.databind;
    opens com.pss.image.proxy.data to com.fasterxml.jackson.databind;
}
