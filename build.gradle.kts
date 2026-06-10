plugins {
    java
    application
    jacoco
    alias(libs.plugins.spotless)
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

val jdkTag: String = "eclipse-temurin:${libs.versions.temurinDockerTagVersion.get()}-jdk"

tasks.register("listDeps") {
    group = "build"
    description = "Prints a jdeps report for each runtime dependency"

    val launcher = javaToolchains.launcherFor(java.toolchain)
    val jdeps = launcher.map { it.metadata.installationPath.file("bin/jdeps").asFile.absolutePath }
    val release = launcher.map { it.metadata.languageVersion.asInt().toString() }
    val runtimeJars = configurations.runtimeClasspath.map { it.files }

    doLast {
        val jars = runtimeJars.get()
        val classPath = jars.joinToString(File.pathSeparator) { it.absolutePath }
        jars.forEach { jar ->
            println("Report for: $jar")
            val result = providers.exec {
                isIgnoreExitValue = true
                commandLine(jdeps.get(), "--multi-release", release.get(), "--class-path", classPath, jar.absolutePath)
            }
            result.standardError.asText.orNull?.takeIf(String::isNotBlank)?.let(::println)
            result.standardOutput.asText.orNull?.let(::println)
        }
    }
}
