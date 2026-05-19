/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfigTest {

    @Test
    fun `fromEnv reads every variable when all are provided`() {
        val env = mapOf(
            "DB_HOST" to "db.example.com",
            "DB_PORT" to "6543",
            "DB_USER" to "inwheel_user",
            "DB_PASSWORD" to "s3cret",
            "DB_NAME" to "inwheel_prod",
            "DB_SSLMODE" to "require",
            "OSM_PBF_URL" to "https://example.com/finland.osm.pbf",
        )

        val config = Config.fromEnv(env)

        assertEquals("db.example.com", config.dbHost)
        assertEquals(6543, config.dbPort)
        assertEquals("inwheel_user", config.dbUser)
        assertEquals("s3cret", config.dbPassword)
        assertEquals("inwheel_prod", config.dbName)
        assertEquals("require", config.dbSslMode)
        assertEquals("https://example.com/finland.osm.pbf", config.osmPbfUrl)
    }

    @Test
    fun `fromEnv applies inwheel-api defaults when only OSM_PBF_URL is provided`() {
        val env = mapOf("OSM_PBF_URL" to "https://example.com/extract.osm.pbf")

        val config = Config.fromEnv(env)

        assertEquals("localhost", config.dbHost)
        assertEquals(5432, config.dbPort)
        assertEquals("postgres", config.dbUser)
        assertEquals("postgres", config.dbPassword)
        assertEquals("inwheel", config.dbName)
        assertEquals("disable", config.dbSslMode)
        assertEquals("https://example.com/extract.osm.pbf", config.osmPbfUrl)
    }

    @Test
    fun `fromEnv throws ConfigException listing OSM_PBF_URL when it is missing`() {
        val exception = assertFailsWith<ConfigException> {
            Config.fromEnv(emptyMap())
        }

        assertTrue(
            exception.errors.any { "OSM_PBF_URL" in it },
            "Expected errors to mention OSM_PBF_URL, got: ${exception.errors}",
        )
    }

    @Test
    fun `fromEnv throws ConfigException when DB_PORT is not an integer`() {
        val env = mapOf(
            "OSM_PBF_URL" to "https://example.com/extract.osm.pbf",
            "DB_PORT" to "not-a-number",
        )

        val exception = assertFailsWith<ConfigException> {
            Config.fromEnv(env)
        }

        assertTrue(
            exception.errors.any { "DB_PORT" in it },
            "Expected errors to mention DB_PORT, got: ${exception.errors}",
        )
    }

    @Test
    fun `fromEnv accumulates multiple errors instead of failing on the first one`() {
        val env = mapOf("DB_PORT" to "not-a-number")

        val exception = assertFailsWith<ConfigException> {
            Config.fromEnv(env)
        }

        assertTrue(
            exception.errors.any { "OSM_PBF_URL" in it },
            "Expected OSM_PBF_URL in errors, got: ${exception.errors}",
        )
        assertTrue(
            exception.errors.any { "DB_PORT" in it },
            "Expected DB_PORT in errors, got: ${exception.errors}",
        )
        assertEquals(2, exception.errors.size, "Expected exactly 2 errors, got: ${exception.errors}")
    }
}
