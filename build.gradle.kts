plugins {
    java
    application
    jacoco
    alias(libs.plugins.spotless)
    alias(libs.plugins.beryx.jlink)
    alias(libs.plugins.pitest)
}

group = "com.pss"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}
val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
    implementation(platform(libs.vertx.bom))
    implementation(libs.vertx.web)
    implementation(libs.vertx.web.client)
    runtimeOnly(variantOf(libs.netty.epoll) { classifier("linux-x86_64") })

    implementation(libs.jackson.databind)
    //implementation(libs.jackson.jsr310)

    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.vertx.junit5)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.logback.classic)
    mockitoAgent(libs.mockito) { isTransitive = false }
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

application {
    mainModule.set("pss.image.proxy")
    mainClass.set("com.pss.image.proxy.Main")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(listOf("--enable-preview"))
    options.release.set(26)
}

tasks.withType<Test>().configureEach {
   // jvmArgs.add("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
    jvmArgs("--enable-preview", "-javaagent:${mockitoAgent.asPath}")
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("-enable-preview", true)
        addStringOption("source", "26")
    }
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat(libs.versions.palantir.get())
    }
}

jlink {
    options.set(
        listOf(
            "--strip-debug",
            "--no-header-files",
            "--no-man-pages",
            "--compress", "zip-6",
            "--vm", "server",
        )
    )

    launcher {
        name = "image-proxy"
        jvmArgs = listOf(
            "--enable-preview",
            "--enable-native-access=com.pss.merged.module",
            "-Djava.awt.headless=true",
            "-Dfile.encoding=UTF-8",
        )
    }

    forceMerge("netty", "logback", "log4j", "slf4j", "jackson")

    mergedModule {
        requires("java.naming")
        requires("java.management")
        requires("java.logging")
        requires("java.xml")
        requires("java.sql")
        requires("jdk.unsupported")
        uses("org.slf4j.spi.SLF4JServiceProvider")
        uses("ch.qos.logback.classic.spi.Configurator")
        provides("org.slf4j.spi.SLF4JServiceProvider")
                .with("ch.qos.logback.classic.spi.LogbackServiceProvider")
    }
}

jacoco {
    toolVersion = "0.8.14"
}

tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestReport"))

}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

dependencies {
    testImplementation(libs.mockito)

}

pitest {
    junit5PluginVersion.set(libs.versions.pitestJunit5)
    targetClasses.set(listOf("com.pss.image.proxy.*"))
    targetTests.set(listOf("com.pss.image.proxy.*"))
    threads.set(2)
    outputFormats.set(listOf("HTML", "XML"))
    timestampedReports.set(false)
    jvmArgs.set(listOf("--enable-preview"))
    mainProcessJvmArgs.set(listOf("--enable-preview"))
    failWhenNoMutations.set(false)
}
