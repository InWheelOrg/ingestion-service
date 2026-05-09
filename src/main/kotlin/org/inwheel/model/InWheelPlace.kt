/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.model

data class InWheelPlace(
    val osmId: Long,
    val osmType: OsmType,
    val name: String?,
    val latitude: Double,
    val longitude: Double,
    val category: PlaceCategory,
    val tags: Map<String, String>,
    val parentOsmId: Long? = null,
    val parentOsmType: OsmType? = null,
)

enum class OsmType { NODE, WAY, RELATION }

enum class PlaceCategory {
    AMENITY,
    SHOP,
    TOURISM,
    LEISURE,
    HEALTHCARE,
    OFFICE,
    PUBLIC_TRANSPORT,
}
