# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run unit tests
./gradlew test

# Lint (detekt + ktlint)
./gradlew detekt

# Lint with auto-correct
./gradlew detektMain --auto-correct
./gradlew detektTest --auto-correct

# Build fat JAR
./gradlew shadowJar
```

## Architecture

Batch job that reads OpenStreetMap data and upserts places into the InWheel PostgreSQL database.
Two modes dispatched from `Main.kt`:

| Mode | Role |
|---|---|
| `full-import` | Initial ingest from a `.osm.pbf` file |
| `diff-sync` | Apply OSM replication diffs since last run |

**Data flow (full-import):**
1. `PbfReader` streams elements from the PBF file via `osmpbf`
2. `PoiFilter` decides which elements to import (allowlist-based — unknown tags excluded by default)
3. Transformer converts `OsmElement` → `InWheelPlace`
4. DB layer bulk-upserts places, skipping rows where `user_verified = true`

**Write path separation:**
- User submissions go directly through inwheel-api (real-time, `user_verified = true`)
- OSM data comes through this service (batch, never touches user-verified rows)

## Key Packages

**`model/`** — Domain types:
- `OsmElement`: sealed class — `OsmNode`, `OsmWay`, `OsmRelation` from the PBF parser
- `InWheelPlace`: output of the transformer, ready for DB upsert
- `PlaceCategory`: typed classification (AMENITY, SHOP, TOURISM, etc.)

**`importer/`** — PBF reading:
- `OsmPbfParser`: extends `BinaryParser`, translates protobuf callbacks into `OsmElement` domain model. Dense node delta-decoding lives here.
- `PbfReader`: public facade — opens file, wires parser, applies `PoiFilter`, calls sink per included element

**`transformer/`** — OSM → InWheel conversion:
- `PoiFilter`: allowlist-based filter. Returns `PoiFilterResult` (sealed: `Included(category)` or `Excluded`)

## Patterns

- **No DI framework** — dependencies wired manually in `Main.kt`; the graph is shallow
- **`PoiFilter` is an `object`** — stateless, pure function over a fixed allowlist. Pass as `PoiFilter::evaluate`
- **`osmpbf` is internal** — `OsmPbfParser` is `internal`, `PbfReader` is the only public surface
- **Allowlist over denylist** — `PoiFilter` only imports explicitly approved tags; unknown tags are excluded by default
- **Direct DB access** — no REST calls to inwheel-api; schema is the shared contract
- **`parent_osm_id` + `parent_osm_type`** as two separate nullable fields — OSM IDs are not unique across element types

## License Headers

Every source file must start with:
```
/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */
```

## Testing Notes

- Unit tests use JUnit 5 + MockK
- Parser tests build `Osmformat.PrimitiveBlock` directly — do not mock `BinaryParser`
- Testcontainers used for DB integration tests (requires Docker)
- `@Suppress("TooGenericExceptionCaught")` is acceptable only when wrapping library code that can throw multiple exception types
