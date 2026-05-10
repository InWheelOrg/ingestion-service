# Copilot Instructions

## Commands

```bash
# Build
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "org.inwheel.importer.OsmPbfParserTest"

# Run a single test method
./gradlew test --tests "org.inwheel.importer.OsmPbfParserTest.methodName"

# Lint (detekt + ktlint)
./gradlew detekt

# Lint with auto-correct
./gradlew detektMain --auto-correct
./gradlew detektTest --auto-correct

# Build fat JAR
./gradlew shadowJar
```

## Architecture

Batch job that reads OpenStreetMap data and upserts places into the InWheel PostgreSQL database. Two modes dispatched from `Main.kt`:

- **`full-import`** — Initial ingest from a `.osm.pbf` file
- **`diff-sync`** — Apply OSM replication diffs since last run

**Data flow (full-import):**
`PbfReader` → `PoiFilter` (allowlist-based) → Transformer (`OsmElement` → `InWheelPlace`) → DB bulk-upsert

**Write path separation:** User submissions go through `inwheel-api` (real-time, `user_verified = true`). OSM data comes through this service (batch, never touches user-verified rows). The DB schema is the shared contract — no REST calls between services.

### Key packages

- **`model/`** — `OsmElement` (sealed: `OsmNode`, `OsmWay`, `OsmRelation`), `InWheelPlace`, `PlaceCategory`
- **`importer/`** — `PbfReader` (public facade) wraps `OsmPbfParser` (internal, extends `BinaryParser`). Dense node delta-decoding lives in the parser.
- **`transformer/`** — `PoiFilter` (stateless `object`, allowlist-based). Returns sealed `PoiFilterResult` (`Included(category)` or `Excluded`)

## Conventions

### License headers

Every source file must start with:

```
/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */
```

### Patterns

- **No DI framework** — dependencies wired manually in `Main.kt`; the graph is shallow
- **Allowlist over denylist** — `PoiFilter` only imports explicitly approved tags; unknown tags are excluded by default
- **`osmpbf` is internal** — `OsmPbfParser` is `internal`, `PbfReader` is the only public surface
- **`parent_osm_id` + `parent_osm_type`** as two separate nullable fields — OSM IDs are not unique across element types

### Testing

- JUnit 5 + MockK for unit tests
- Parser tests build `Osmformat.PrimitiveBlock` directly — do not mock `BinaryParser`
- Testcontainers for DB integration tests (requires Docker)
- `@Suppress("TooGenericExceptionCaught")` is acceptable only when wrapping library code that can throw multiple exception types
