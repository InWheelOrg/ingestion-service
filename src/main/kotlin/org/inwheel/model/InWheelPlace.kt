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
    val rank: PlaceRank,
    val tags: Map<String, String>,
    val externalId: String,
    val source: String,
    val parentOsmId: Long? = null,
    val parentOsmType: OsmType? = null,
)

enum class OsmType { NODE, WAY, RELATION }

@Suppress("MagicNumber")
enum class PlaceRank(val value: Int) {
    LANDMARK(1),
    ESTABLISHMENT(2),
    FEATURE(3),
}

enum class PlaceCategory {
    RESTAURANT,
    CAFE,
    BAR,
    HEALTHCARE,
    EDUCATION,
    SHOP,
    TOURISM,
    LEISURE,
    TRANSPORT,
    FINANCE,
    GOVERNMENT,
    ENTERTAINMENT,
    WORSHIP,
    OFFICE,
    SOCIAL,
    OTHER,
}
