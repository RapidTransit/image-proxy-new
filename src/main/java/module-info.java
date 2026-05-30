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
    requires tools.jackson.databind;
    requires org.slf4j;
    requires java.naming;
    requires jdk.unsupported;

    /// Jackson reads record components reflectively.
    opens com.pss.image.proxy.config to
            tools.jackson.databind;
    opens com.pss.image.proxy.data to
            tools.jackson.databind;
}
