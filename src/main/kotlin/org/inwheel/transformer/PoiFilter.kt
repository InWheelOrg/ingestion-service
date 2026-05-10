/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.transformer

import org.inwheel.model.PlaceCategory

sealed class PoiFilterResult {
    data class Included(val category: PlaceCategory) : PoiFilterResult()
    data object Excluded : PoiFilterResult()
}

object PoiFilter {

    private val AMENITY_ALLOWLIST = setOf(
        "restaurant", "cafe", "bar", "pub", "fast_food", "food_court", "ice_cream", "biergarten",
        "hospital", "clinic", "doctors", "dentist", "pharmacy", "veterinary",
        "nursing_home", "social_facility",
        "school", "university", "college", "kindergarten", "childcare", "library",
        "language_school", "music_school", "driving_school", "research_institute",
        "bank", "atm", "bureau_de_change", "money_transfer",
        "cinema", "theatre", "nightclub", "arts_centre", "casino",
        "community_centre", "events_venue", "social_centre",
        "post_office", "townhall", "courthouse", "embassy", "police", "fire_station",
        "bus_station", "ferry_terminal", "taxi", "car_rental", "bicycle_rental", "car_sharing",
        "place_of_worship",
        "shelter",
        "toilets",
        "marketplace", "internet_cafe",
    )

    private val PUBLIC_TRANSPORT_ALLOWLIST = setOf(
        "station",
        "stop_area",
    )

    private val BUILDING_QUALIFYING_KEYS = setOf(
        "amenity",
        "shop",
        "tourism",
        "leisure",
        "healthcare",
        "office",
    )

    private val GLOBALLY_EXCLUDED_KEYS = setOf(
        "highway",
        "barrier",
        "natural",
        "landuse",
        "waterway",
        "boundary",
    )

    fun evaluate(tags: Map<String, String>): PoiFilterResult {
        if (tags.keys.any { it in GLOBALLY_EXCLUDED_KEYS }) return PoiFilterResult.Excluded

        return when {
            tags.containsKey("amenity") -> evaluateAmenity(tags.getValue("amenity"))
            tags.containsKey("shop") -> PoiFilterResult.Included(PlaceCategory.SHOP)
            tags.containsKey("tourism") -> PoiFilterResult.Included(PlaceCategory.TOURISM)
            tags.containsKey("leisure") -> PoiFilterResult.Included(PlaceCategory.LEISURE)
            tags.containsKey("healthcare") -> PoiFilterResult.Included(PlaceCategory.HEALTHCARE)
            tags.containsKey("office") -> PoiFilterResult.Included(PlaceCategory.OFFICE)
            tags.containsKey("public_transport") -> evaluatePublicTransport(tags.getValue("public_transport"))
            tags.containsKey("building") -> evaluateBuilding(tags)
            else -> PoiFilterResult.Excluded
        }
    }

    private fun evaluateAmenity(value: String): PoiFilterResult =
        if (value in AMENITY_ALLOWLIST) {
            PoiFilterResult.Included(PlaceCategory.AMENITY)
        } else PoiFilterResult.Excluded

    private fun evaluatePublicTransport(value: String): PoiFilterResult =
        if (value in PUBLIC_TRANSPORT_ALLOWLIST) {
            PoiFilterResult.Included(PlaceCategory.PUBLIC_TRANSPORT)
        } else PoiFilterResult.Excluded

    private fun evaluateBuilding(tags: Map<String, String>): PoiFilterResult {
        for (key in BUILDING_QUALIFYING_KEYS) {
            if (tags.containsKey(key)) return evaluate(tags - "building")
        }
        return PoiFilterResult.Excluded
    }
}
