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

    private val AMENITY_RESTAURANT = setOf("restaurant", "fast_food", "food_court")
    private val AMENITY_CAFE = setOf("cafe", "ice_cream", "biergarten")
    private val AMENITY_BAR = setOf("bar", "pub", "nightclub")
    private val AMENITY_HEALTHCARE = setOf(
        "hospital",
        "clinic",
        "doctors",
        "dentist",
        "pharmacy",
        "veterinary",
        "nursing_home",
    )
    private val AMENITY_EDUCATION = setOf(
        "school",
        "university",
        "college",
        "kindergarten",
        "childcare",
        "library",
        "language_school",
        "music_school",
        "driving_school",
        "research_institute",
    )
    private val AMENITY_FINANCE = setOf("bank", "atm", "bureau_de_change", "money_transfer")
    private val AMENITY_ENTERTAINMENT = setOf(
        "cinema",
        "theatre",
        "arts_centre",
        "casino",
        "community_centre",
        "events_venue",
        "social_centre",
    )
    private val AMENITY_GOVERNMENT = setOf(
        "townhall",
        "courthouse",
        "embassy",
        "police",
        "fire_station",
        "post_office",
    )
    private val AMENITY_TRANSPORT = setOf(
        "bus_station",
        "ferry_terminal",
        "taxi",
        "car_rental",
        "bicycle_rental",
        "car_sharing",
    )
    private val AMENITY_SOCIAL = setOf("social_facility", "shelter")
    private val AMENITY_OTHER = setOf("toilets", "marketplace", "internet_cafe")

    private val PUBLIC_TRANSPORT_ALLOWLIST = setOf("station", "stop_area")

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

    private fun evaluateAmenity(value: String): PoiFilterResult = when {
        value in AMENITY_RESTAURANT -> PoiFilterResult.Included(PlaceCategory.RESTAURANT)
        value in AMENITY_CAFE -> PoiFilterResult.Included(PlaceCategory.CAFE)
        value in AMENITY_BAR -> PoiFilterResult.Included(PlaceCategory.BAR)
        value in AMENITY_HEALTHCARE -> PoiFilterResult.Included(PlaceCategory.HEALTHCARE)
        value in AMENITY_EDUCATION -> PoiFilterResult.Included(PlaceCategory.EDUCATION)
        value in AMENITY_FINANCE -> PoiFilterResult.Included(PlaceCategory.FINANCE)
        value in AMENITY_ENTERTAINMENT -> PoiFilterResult.Included(PlaceCategory.ENTERTAINMENT)
        value in AMENITY_GOVERNMENT -> PoiFilterResult.Included(PlaceCategory.GOVERNMENT)
        value in AMENITY_TRANSPORT -> PoiFilterResult.Included(PlaceCategory.TRANSPORT)
        value == "place_of_worship" -> PoiFilterResult.Included(PlaceCategory.WORSHIP)
        value in AMENITY_SOCIAL -> PoiFilterResult.Included(PlaceCategory.SOCIAL)
        value in AMENITY_OTHER -> PoiFilterResult.Included(PlaceCategory.OTHER)
        else -> PoiFilterResult.Excluded
    }

    private fun evaluatePublicTransport(value: String): PoiFilterResult =
        if (value in PUBLIC_TRANSPORT_ALLOWLIST) {
            PoiFilterResult.Included(PlaceCategory.TRANSPORT)
        } else PoiFilterResult.Excluded

    private fun evaluateBuilding(tags: Map<String, String>): PoiFilterResult {
        for (key in BUILDING_QUALIFYING_KEYS) {
            if (tags.containsKey(key)) return evaluate(tags - "building")
        }
        return PoiFilterResult.Excluded
    }
}
