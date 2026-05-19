/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.config

private const val DEFAULT_DB_PORT = 5432

data class Config(
    val dbHost: String,
    val dbPort: Int,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String,
    val dbSslMode: String,
    val osmPbfUrl: String,
) {
    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): Config {
            val errors = mutableListOf<String>()

            val osmPbfUrl = env["OSM_PBF_URL"]
            if (osmPbfUrl.isNullOrBlank()) {
                errors += "OSM_PBF_URL is required but was not set"
            }

            val dbPort = parsePort(env["DB_PORT"], errors)

            if (errors.isNotEmpty()) {
                throw ConfigException(errors)
            }

            return Config(
                dbHost = env["DB_HOST"] ?: "localhost",
                dbPort = dbPort!!,
                dbUser = env["DB_USER"] ?: "postgres",
                dbPassword = env["DB_PASSWORD"] ?: "postgres",
                dbName = env["DB_NAME"] ?: "inwheel",
                dbSslMode = env["DB_SSLMODE"] ?: "disable",
                osmPbfUrl = osmPbfUrl!!,
            )
        }

        private fun parsePort(raw: String?, errors: MutableList<String>): Int? {
            if (raw == null) return DEFAULT_DB_PORT
            val parsed = raw.toIntOrNull()
            if (parsed == null) {
                errors += "DB_PORT must be an integer, got: \"$raw\""
            }
            return parsed
        }
    }
}

class ConfigException(val errors: List<String>) : RuntimeException(
    "Configuration is invalid:\n" + errors.joinToString("\n") { "  - $it" },
)
