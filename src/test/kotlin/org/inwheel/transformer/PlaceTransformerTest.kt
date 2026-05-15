/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.transformer

import org.inwheel.model.OsmNode
import org.inwheel.model.OsmRelation
import org.inwheel.model.OsmType
import org.inwheel.model.OsmWay
import org.inwheel.model.PlaceCategory
import org.inwheel.model.PlaceRank
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlaceTransformerTest {

    private val transformer = PlaceTransformer()

    private fun node(
        id: Long = 123L,
        tags: Map<String, String> = emptyMap(),
        lat: Double = 60.1699,
        lon: Double = 24.9384,
    ) = OsmNode(id = id, version = 1, tags = tags, latitude = lat, longitude = lon)

    @Test
    fun `restaurant node is transformed with correct fields`() {
        val place = transformer.transform(
            node(id = 123L, tags = mapOf("amenity" to "restaurant", "name" to "Ravintola Tor")),
            PlaceCategory.RESTAURANT,
        )
        assertNotNull(place)
        assertEquals(123L, place.osmId)
        assertEquals(OsmType.NODE, place.osmType)
        assertEquals("Ravintola Tor", place.name)
        assertEquals(60.1699, place.latitude)
        assertEquals(24.9384, place.longitude)
        assertEquals(PlaceCategory.RESTAURANT, place.category)
        assertEquals(PlaceRank.ESTABLISHMENT, place.rank)
        assertEquals("node/123", place.externalId)
        assertEquals("osm", place.source)
    }

    @Test
    fun `node with no name tag produces null name`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "restaurant")),
            PlaceCategory.RESTAURANT,
        )
        assertNotNull(place)
        assertNull(place.name)
    }

    @Test
    fun `hospital node has landmark rank`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "hospital", "name" to "City Hospital")),
            PlaceCategory.HEALTHCARE,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.LANDMARK, place.rank)
    }

    @Test
    fun `university node has landmark rank`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "university", "name" to "Aalto University")),
            PlaceCategory.EDUCATION,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.LANDMARK, place.rank)
    }

    @Test
    fun `public_transport station node has landmark rank`() {
        val place = transformer.transform(
            node(tags = mapOf("public_transport" to "station", "name" to "Central Station")),
            PlaceCategory.TRANSPORT,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.LANDMARK, place.rank)
    }

    @Test
    fun `toilets node has feature rank`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "toilets")),
            PlaceCategory.OTHER,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.FEATURE, place.rank)
    }

    @Test
    fun `atm node has feature rank`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "atm")),
            PlaceCategory.FINANCE,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.FEATURE, place.rank)
    }

    @Test
    fun `shelter node has feature rank`() {
        val place = transformer.transform(
            node(tags = mapOf("amenity" to "shelter")),
            PlaceCategory.SOCIAL,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.FEATURE, place.rank)
    }

    @Test
    fun `supermarket shop node defaults to establishment rank`() {
        val place = transformer.transform(
            node(tags = mapOf("shop" to "supermarket", "name" to "K-Market")),
            PlaceCategory.SHOP,
        )
        assertNotNull(place)
        assertEquals(PlaceRank.ESTABLISHMENT, place.rank)
    }

    @Test
    fun `all original tags are preserved in result`() {
        val tags = mapOf("amenity" to "restaurant", "name" to "Burger Place", "addr:city" to "Helsinki")
        val place = transformer.transform(node(tags = tags), PlaceCategory.RESTAURANT)
        assertNotNull(place)
        assertEquals(tags, place.tags)
    }

    @Test
    fun `external id is formatted as node slash id`() {
        val place = transformer.transform(
            node(id = 42L, tags = mapOf("amenity" to "cafe")),
            PlaceCategory.CAFE,
        )
        assertNotNull(place)
        assertEquals("node/42", place.externalId)
    }

    @Test
    fun `OsmWay returns null`() {
        val way = OsmWay(id = 1L, version = 1, tags = mapOf("amenity" to "restaurant"), nodeIds = listOf(10L, 11L))
        assertNull(transformer.transform(way, PlaceCategory.RESTAURANT))
    }

    @Test
    fun `OsmRelation returns null`() {
        val relation = OsmRelation(id = 1L, version = 1, tags = mapOf("amenity" to "hospital"), members = emptyList())
        assertNull(transformer.transform(relation, PlaceCategory.HEALTHCARE))
    }
}
