/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "full-import" -> TODO("full-import not yet implemented")
        "diff-sync" -> TODO("diff-sync not yet implemented")
        else -> {
            System.err.println("Usage: ingestion-service <mode>")
            System.err.println("Modes:")
            System.err.println("  full-import   Process a PBF file from scratch (initial setup or disaster recovery)")
            System.err.println("  diff-sync     Apply OSM replication diffs since last run")
            exitProcess(1)
        }
    }
}
