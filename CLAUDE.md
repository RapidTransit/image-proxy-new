# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This repo is **mid-rewrite**. Two things coexist:

- `src/` — fresh Vert.x 5 starter (just a Hello-World `MainVerticle`). This is what we are building into.
- `reference/` — the live, in-production Micronaut + Vert.x implementation we are replacing. Treat it as read-only documentation of behavior, not a place to edit.

Goal: a lighter-weight Vert.x 5 reverse proxy on JDK 26, shipped as a jlink image, without Micronaut, Immutables, log4j2, cache2k, or `module-info` machinery in multiple modules.

## What the app does

A reverse proxy in front of the Sirv image CDN. All work is on event-loop threads.

1. `ValidImageHandler` — rejects non-image URLs with 400.
2. `CachingNotFoundHandler` — short-circuits with a cached "no-image" response if this exact path missed recently.
3. Dispatch by path prefix:
   - `/p/*`, `/c/*` → JWT route (attaches pre-signed token from `proxy.jwt-mappings`)
   - `/u/*` → Unprotected (forwards `profile` query param)
   - `/d/*` → Dynamic (forwards all params)
4. All three normalize the profile via `accepted-profiles` + `shim-mappings`, build the upstream URI, copy a small curated header set, pipe the upstream response back.
5. On upstream 4xx/5xx/network failure, fall through to `NotFoundHandler`, which fetches `/images/no-image.png`, caches the bytes per `profile+webp|jpeg`, and uses `MultiTryService` to back off retries via `cache-control` / `expires` headers.
6. `/images/no-image.png` and `/shared/*` → serve no-image directly.

## Build & dev commands

```bash
./gradlew build                                # compile + test + assemble
./gradlew test                                 # run all tests
./gradlew test --tests "com.pss.*.SomeTest"    # single test class
./gradlew test --tests "*.someMethod"          # single method by pattern
./gradlew spotlessApply                        # apply Palantir Java Format
./gradlew run                                  # run locally
./gradlew jlink                                # produce build/image/ runtime
./gradlew jlinkZip                             # distributable zip
```

Spotless runs as part of `check`/`build`; format proactively to avoid CI churn.

## Locked-in design decisions

These were chosen deliberately. Do not reintroduce alternatives without discussion.

- **Manual wiring** in a single `Wiring.java` — no DI container, no Avaje, no Micronaut. Object graph is ~20 nodes with one wiring path.
- **Single Gradle + JPMS module** (`pss.image.proxy`). `reference/vertx-common` is folded in.
- **jlink** via `org.beryx.jlink`. Auto-modules (Netty, logback, slf4j-classic) go into `mergedModule`. No shadowJar.
- **JSON config** parsed once at startup with Jackson. No YAML, no `vertx-config`.
- **`ConcurrentHashMap` + per-value `expiresAt`** for both caches; a periodic event-loop tick sweeps the multi-try cache. No cache2k.
- **JDK 26 with `--enable-preview`** on compile, test, and the jlink launcher.
- **Jackson 2.x** (group `com.fasterxml.jackson.*`), `KEBAB_CASE` naming strategy + `JavaTimeModule`.
- **Vert.x 5.1** + `netty-transport-native-epoll` (`preferNativeTransport: true`).
- **Logback** with slf4j facade; not log4j2.
- **JUnit 6.1.0** — fall back to latest 5.x if resolution or `vertx-junit5` compatibility fails.
- **Records** replace Immutables everywhere (`JwtToken`/`JwtPayload`/`JwtHeader`/`JwtArgs`/`ResponseCache`/`ProxyConfig`/`AppConfig`).

## Coding conventions

- Java markdown doc comments (`///`) instead of `/** */` where a doc comment is warranted. Reserve them for public-ish boundaries (`Wiring`, `MainVerticle`, `ProxyConfig`); skip on internal handlers/services unless the WHY is non-obvious.
- Text blocks for multi-line literals (JSON in tests, log patterns, etc.).
- Use simple class names with imports — no fully-qualified class names in code.
- Palantir Java Format is authoritative; do not hand-format against it.

## Concurrency notes (read before touching caches or handlers)

- Default Vert.x event-loop pool is `2 × processors`. On the target single-vCPU host that's **2 event loops**, not 1. Multiple connections will hit both loops, so caches are accessed from multiple threads — they must be thread-safe.
- All request work runs on the event loop. Never block; use `Future` and `vertx.setPeriodic` for housekeeping.
- `NotFoundHandler` is constructed twice with different `CacheHeaderManipulator` instances (`MultiTryService` vs `CacheHeaderManipulator.NO_OP`). `QueryRouteHandler` is constructed three times, one per `RouteService`. The duplicate construction *is* how the route mode is selected — no qualifiers needed because there is no DI container.

## Boot sequence

`MainVerticle.start()`:
1. Read JSON config from `System.getProperty("config.file", "config/application.json")`.
2. Deserialize to `AppConfig` (record holding `ProxyConfig` + raw `JsonObject` blocks for Vert.x/server/client options).
3. Build `Vertx` from options.
4. `new Wiring(vertx, cfg).build()` returns a configured `Router`.
5. If `proxy.test-jwt` is true, run `JwtTokenChecker.checkTokens(...).await()` once.
6. `vertx.createHttpServer(serverOpts).requestHandler(router).listen()`.

## Working with `reference/`

The reference implementation under `reference/image-proxy/` and `reference/vertx-common/` is the source of truth for behavior. When porting, **mine** the following:

- Route logic: `reference/image-proxy/src/main/java/com/pss/proxy/image/routes/`
- Route dispatching by mode: `service/routes/` (`DynamicRouteService`, `JwtRouteService`, `UnprotectedRouteService`)
- Profile + shim mapping and validation: `config/properties/ProxyConfig.java` + `config/application.yaml`
- Atomic retry-counter design: `service/MultiTryService.java`
- No-image fallback flow: `routes/NotFoundHandler.java`

**Do not** port:

- Any Micronaut annotation (`@Factory`, `@Bean`, `@Singleton`, `@Inject`, `@ConfigurationProperties`, `@Value`, `@MapFormat`, `@Qualifier` annotations).
- Immutables (`@Value.Immutable`) — use records.
- `module-info.java` from `vertx-common` — write one fresh for the new single module.
- `FutureUtil.await` — Vert.x 5 has `Future.await()` natively on virtual threads.
- log4j2 config — translate to `logback.xml`.
- `JwtDecoder` returns `null` in the reference (an in-progress bug from the Immutables migration). Finish it correctly with records when porting.

## Delegation

Menial, mechanical work (XML translation, near-1:1 class ports without semantic changes) is fair game for **haiku** sub-agents, ideally in parallel. Reasoning-heavy work (the `Wiring` graph, test rewrites, JWT decoder completion, integration test design) should be done directly or via sonnet.

## README

`README.adoc` is the unmodified Vert.x starter README; it does not yet reflect this rewrite.
