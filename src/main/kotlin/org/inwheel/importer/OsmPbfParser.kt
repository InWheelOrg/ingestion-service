/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.importer

import crosby.binary.BinaryParser
import crosby.binary.Osmformat
import org.inwheel.model.OsmElement
import org.inwheel.model.OsmMember
import org.inwheel.model.OsmMemberType
import org.inwheel.model.OsmNode
import org.inwheel.model.OsmRelation
import org.inwheel.model.OsmWay

internal class OsmPbfParser(private val sink: (OsmElement) -> Unit) : BinaryParser() {

    override fun parse(header: Osmformat.HeaderBlock) = Unit

    override fun complete() = Unit

    override fun parseDense(nodes: Osmformat.DenseNodes) {
        var id = 0L
        var lat = 0L
        var lon = 0L
        var tagIndex = 0
        val keysVals = nodes.keysValsList
        val hasDenseInfo = nodes.hasDenseinfo()

        for (i in 0 until nodes.idCount) {
            id += nodes.getId(i)
            lat += nodes.getLat(i)
            lon += nodes.getLon(i)

            val tags = mutableMapOf<String, String>()
            while (tagIndex < keysVals.size && keysVals[tagIndex] != 0) {
                val key = getStringById(keysVals[tagIndex++])
                val value = getStringById(keysVals[tagIndex++])
                tags[key] = value
            }
            tagIndex++

            val version = if (hasDenseInfo && i < nodes.denseinfo.versionCount) {
                nodes.denseinfo.getVersion(i)
            } else 0

            sink(
                OsmNode(
                    id = id,
                    version = version,
                    tags = tags,
                    latitude = parseLat(lat),
                    longitude = parseLon(lon),
                ),
            )
        }
    }

    override fun parseNodes(nodes: List<Osmformat.Node>) {
        for (node in nodes) {
            sink(
                OsmNode(
                    id = node.id,
                    version = if (node.hasInfo()) node.info.version else 0,
                    tags = decodeTags(node.keysList, node.valsList),
                    latitude = parseLat(node.lat),
                    longitude = parseLon(node.lon),
                ),
            )
        }
    }

    override fun parseWays(ways: List<Osmformat.Way>) {
        for (way in ways) {
            var ref = 0L
            val nodeIds = buildList {
                for (delta in way.refsList) {
                    ref += delta
                    add(ref)
                }
            }
            sink(
                OsmWay(
                    id = way.id,
                    version = if (way.hasInfo()) way.info.version else 0,
                    tags = decodeTags(way.keysList, way.valsList),
                    nodeIds = nodeIds,
                ),
            )
        }
    }

    override fun parseRelations(rels: List<Osmformat.Relation>) {
        for (rel in rels) {
            var memberId = 0L
            val members = buildList {
                for (i in 0 until rel.memidsCount) {
                    memberId += rel.getMemids(i)
                    add(
                        OsmMember(
                            type = when (rel.getTypes(i)) {
                                Osmformat.Relation.MemberType.NODE -> OsmMemberType.NODE
                                Osmformat.Relation.MemberType.WAY -> OsmMemberType.WAY
                                Osmformat.Relation.MemberType.RELATION -> OsmMemberType.RELATION
                                else -> {
                                    System.err.println(
                                        "Warning: unknown member type in relation ${rel.id}, defaulting to NODE",
                                    )
                                    OsmMemberType.NODE
                                }
                            },
                            ref = memberId,
                            role = getStringById(rel.getRolesSid(i)),
                        ),
                    )
                }
            }
            sink(
                OsmRelation(
                    id = rel.id,
                    version = if (rel.hasInfo()) rel.info.version else 0,
                    tags = decodeTags(rel.keysList, rel.valsList),
                    members = members,
                ),
            )
        }
    }

    private fun decodeTags(keys: List<Int>, vals: List<Int>): Map<String, String> =
        keys.zip(vals).associate { (k, v) -> getStringById(k) to getStringById(v) }
}
