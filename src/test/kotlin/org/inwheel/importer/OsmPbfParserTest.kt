/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.importer

import com.google.protobuf.ByteString
import crosby.binary.Osmformat
import org.inwheel.model.OsmMember
import org.inwheel.model.OsmMemberType
import org.inwheel.model.OsmNode
import org.inwheel.model.OsmRelation
import org.inwheel.model.OsmWay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OsmPbfParserTest {

    private val collected = mutableListOf<org.inwheel.model.OsmElement>()
    private val parser = OsmPbfParser { collected.add(it) }

    @BeforeEach
    fun setUp() = collected.clear()

    // Builds a PrimitiveBlock with the given strings in the string table and
    // lets the caller add PrimitiveGroups. Granularity is fixed at 100 so
    // lat/lon math is predictable: stored_value = degrees / (100 * 1e-9).
    private fun parseBlock(
        strings: List<String> = emptyList(),
        setup: Osmformat.PrimitiveBlock.Builder.() -> Unit,
    ) {
        val strTable = Osmformat.StringTable.newBuilder().apply {
            addS(ByteString.copyFromUtf8(""))
            strings.forEach { addS(ByteString.copyFromUtf8(it)) }
        }.build()

        val block = Osmformat.PrimitiveBlock.newBuilder().apply {
            setStringtable(strTable)
            setGranularity(100)
            setup()
        }.build()

        parser.parse(block)
    }

    @Test
    fun `dense node with tags`() {
        parseBlock(strings = listOf("amenity", "hospital")) {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().setDense(
                    Osmformat.DenseNodes.newBuilder()
                        .addId(42L)
                        .addLat(470_000_000L)
                        .addLon(80_000_000L)
                        .addKeysVals(1)
                        .addKeysVals(2)
                        .addKeysVals(0),
                ),
            )
        }

        val node = collected.single() as OsmNode
        assertEquals(42L, node.id)
        assertEquals(47.0, node.latitude, 1e-6)
        assertEquals(8.0, node.longitude, 1e-6)
        assertEquals(mapOf("amenity" to "hospital"), node.tags)
    }

    @Test
    fun `dense nodes delta-encoded ids and coordinates`() {
        parseBlock {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().setDense(
                    Osmformat.DenseNodes.newBuilder()
                        .addId(100L).addId(50L)
                        .addLat(470_000_000L).addLat(10_000_000L)
                        .addLon(80_000_000L).addLon(20_000_000L)
                        .addKeysVals(0)
                        .addKeysVals(0),
                ),
            )
        }

        val nodes = collected.map { it as OsmNode }
        assertEquals(2, nodes.size)
        assertEquals(100L, nodes[0].id)
        assertEquals(47.0, nodes[0].latitude, 1e-6)
        assertEquals(8.0, nodes[0].longitude, 1e-6)
        assertEquals(150L, nodes[1].id)
        assertEquals(48.0, nodes[1].latitude, 1e-6)
        assertEquals(10.0, nodes[1].longitude, 1e-6)
    }

    @Test
    fun `dense node with no tags and empty keysVals`() {
        parseBlock {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().setDense(
                    Osmformat.DenseNodes.newBuilder()
                        .addId(1L)
                        .addLat(0L)
                        .addLon(0L),
                ),
            )
        }

        val node = collected.single() as OsmNode
        assertTrue(node.tags.isEmpty())
    }

    @Test
    fun `dense nodes first with tags second without`() {
        parseBlock(strings = listOf("amenity", "cafe")) {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().setDense(
                    Osmformat.DenseNodes.newBuilder()
                        .addId(1L).addId(1L)
                        .addLat(0L).addLat(0L)
                        .addLon(0L).addLon(0L)
                        .addKeysVals(1)
                        .addKeysVals(2)
                        .addKeysVals(0)
                        .addKeysVals(0),
                ),
            )
        }

        assertEquals(2, collected.size)
        assertEquals(mapOf("amenity" to "cafe"), (collected[0] as OsmNode).tags)
        assertTrue((collected[1] as OsmNode).tags.isEmpty())
    }

    @Test
    fun `dense nodes negative coordinate delta`() {
        parseBlock {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().setDense(
                    Osmformat.DenseNodes.newBuilder()
                        .addId(1L).addId(1L)
                        .addLat(480_000_000L).addLat(-10_000_000L)
                        .addLon(100_000_000L).addLon(0L)
                        .addKeysVals(0)
                        .addKeysVals(0),
                ),
            )
        }

        val nodes = collected.map { it as OsmNode }
        assertEquals(48.0, nodes[0].latitude, 1e-6)
        assertEquals(47.0, nodes[1].latitude, 1e-6)
    }

    @Test
    fun `way with delta-encoded refs including negative delta`() {
        parseBlock(strings = listOf("highway", "residential")) {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().addWays(
                    Osmformat.Way.newBuilder()
                        .setId(10L)
                        .addKeys(1).addVals(2)
                        .addRefs(100L)
                        .addRefs(50L)
                        .addRefs(-30L),
                ),
            )
        }

        val way = collected.single() as OsmWay
        assertEquals(10L, way.id)
        assertEquals(listOf(100L, 150L, 120L), way.nodeIds)
        assertEquals(mapOf("highway" to "residential"), way.tags)
    }

    @Test
    fun `relation with all member types and delta-encoded member ids`() {
        parseBlock(strings = listOf("outer", "inner")) {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().addRelations(
                    Osmformat.Relation.newBuilder()
                        .setId(99L)
                        .addMemids(10L)
                        .addMemids(5L)
                        .addMemids(-5L)
                        .addRolesSid(1)
                        .addRolesSid(2)
                        .addRolesSid(1)
                        .addTypes(Osmformat.Relation.MemberType.NODE)
                        .addTypes(Osmformat.Relation.MemberType.WAY)
                        .addTypes(Osmformat.Relation.MemberType.RELATION),
                ),
            )
        }

        val rel = collected.single() as OsmRelation
        assertEquals(99L, rel.id)
        assertEquals(OsmMember(OsmMemberType.NODE, 10L, "outer"), rel.members[0])
        assertEquals(OsmMember(OsmMemberType.WAY, 15L, "inner"), rel.members[1])
        assertEquals(OsmMember(OsmMemberType.RELATION, 10L, "outer"), rel.members[2])
    }

    @Test
    fun `non-dense node`() {
        parseBlock(strings = listOf("shop", "bakery")) {
            addPrimitivegroup(
                Osmformat.PrimitiveGroup.newBuilder().addNodes(
                    Osmformat.Node.newBuilder()
                        .setId(77L)
                        .setLat(510_000_000L)
                        .setLon(130_000_000L)
                        .addKeys(1)
                        .addVals(2),
                ),
            )
        }

        val node = collected.single() as OsmNode
        assertEquals(77L, node.id)
        assertEquals(51.0, node.latitude, 1e-6)
        assertEquals(13.0, node.longitude, 1e-6)
        assertEquals(mapOf("shop" to "bakery"), node.tags)
    }
}
