/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.transformer

import org.inwheel.model.InWheelPlace
import org.inwheel.model.OsmElement
import org.inwheel.model.OsmNode
import org.inwheel.model.OsmRelation
import org.inwheel.model.OsmType
import org.inwheel.model.OsmWay
import org.inwheel.model.PlaceCategory
import org.inwheel.model.PlaceRank

class PlaceTransformer {

    fun transform(element: OsmElement, category: PlaceCategory): InWheelPlace? = when (element) {
        is OsmNode -> transformNode(element, category)
        is OsmWay -> null
        is OsmRelation -> null
    }

    private fun transformNode(node: OsmNode, category: PlaceCategory): InWheelPlace = InWheelPlace(
        osmId = node.id,
        osmType = OsmType.NODE,
        name = node.tags["name"],
        latitude = node.latitude,
        longitude = node.longitude,
        category = category,
        rank = deriveRank(category, node.tags),
        tags = node.tags,
        externalId = "node/${node.id}",
        source = "osm",
        parentOsmId = null,
        parentOsmType = null,
    )

    private fun deriveRank(category: PlaceCategory, tags: Map<String, String>): PlaceRank {
        val amenity = tags["amenity"]
        val publicTransport = tags["public_transport"]
        return when {
            isLandmark(category, amenity, publicTransport) -> PlaceRank.LANDMARK
            isFeature(category, amenity) -> PlaceRank.FEATURE
            else -> PlaceRank.ESTABLISHMENT
        }
    }

    private fun isLandmark(category: PlaceCategory, amenity: String?, publicTransport: String?): Boolean =
        when (category) {
            PlaceCategory.HEALTHCARE -> amenity == "hospital"
            PlaceCategory.EDUCATION -> amenity == "university"
            PlaceCategory.TRANSPORT ->
                amenity in LANDMARK_TRANSPORT_AMENITY || publicTransport in LANDMARK_TRANSPORT_PUBLIC
            else -> false
        }

    private fun isFeature(category: PlaceCategory, amenity: String?): Boolean =
        when (category) {
            PlaceCategory.OTHER -> amenity == "toilets"
            PlaceCategory.SOCIAL -> amenity == "shelter"
            PlaceCategory.FINANCE -> amenity == "atm"
            else -> false
        }

    private companion object {
        val LANDMARK_TRANSPORT_AMENITY = setOf("bus_station", "ferry_terminal")
        val LANDMARK_TRANSPORT_PUBLIC = setOf("station")
    }
}
