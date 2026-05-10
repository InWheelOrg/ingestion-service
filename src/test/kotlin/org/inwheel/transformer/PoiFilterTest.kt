/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.transformer

import org.inwheel.model.PlaceCategory
import org.inwheel.transformer.PoiFilterResult.Excluded
import org.inwheel.transformer.PoiFilterResult.Included
import kotlin.test.Test
import kotlin.test.assertEquals

class PoiFilterTest {

    @Test
    fun `restaurant is included as amenity`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "restaurant"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `hospital is included as amenity`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "hospital"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `atm is included as amenity`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "atm"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `dancing_school is excluded as it is not a standard amenity value`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "dancing_school"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `toilets is included as amenity`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "toilets"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `childcare is included as amenity`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "childcare"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `waste_basket is excluded`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "waste_basket"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `bench is excluded`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "bench"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `parking is excluded`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "parking"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `unknown amenity value is excluded`() {
        val result = PoiFilter.evaluate(mapOf("amenity" to "some_future_tag"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `shop is included`() {
        val result = PoiFilter.evaluate(mapOf("shop" to "supermarket"))
        assertEquals(Included(PlaceCategory.SHOP), result)
    }

    @Test
    fun `any shop value is included`() {
        val result = PoiFilter.evaluate(mapOf("shop" to "anything"))
        assertEquals(Included(PlaceCategory.SHOP), result)
    }

    @Test
    fun `tourism hotel is included`() {
        val result = PoiFilter.evaluate(mapOf("tourism" to "hotel"))
        assertEquals(Included(PlaceCategory.TOURISM), result)
    }

    @Test
    fun `leisure park is included`() {
        val result = PoiFilter.evaluate(mapOf("leisure" to "park"))
        assertEquals(Included(PlaceCategory.LEISURE), result)
    }

    @Test
    fun `healthcare doctor is included`() {
        val result = PoiFilter.evaluate(mapOf("healthcare" to "doctor"))
        assertEquals(Included(PlaceCategory.HEALTHCARE), result)
    }

    @Test
    fun `office is included`() {
        val result = PoiFilter.evaluate(mapOf("office" to "government"))
        assertEquals(Included(PlaceCategory.OFFICE), result)
    }

    @Test
    fun `public_transport station is included`() {
        val result = PoiFilter.evaluate(mapOf("public_transport" to "station"))
        assertEquals(Included(PlaceCategory.PUBLIC_TRANSPORT), result)
    }

    @Test
    fun `public_transport stop_area is included`() {
        val result = PoiFilter.evaluate(mapOf("public_transport" to "stop_area"))
        assertEquals(Included(PlaceCategory.PUBLIC_TRANSPORT), result)
    }

    @Test
    fun `public_transport stop_position is excluded`() {
        val result = PoiFilter.evaluate(mapOf("public_transport" to "stop_position"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `public_transport platform is excluded`() {
        val result = PoiFilter.evaluate(mapOf("public_transport" to "platform"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `building alone is excluded`() {
        val result = PoiFilter.evaluate(mapOf("building" to "yes"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `building with amenity is included via amenity rule`() {
        val result = PoiFilter.evaluate(mapOf("building" to "yes", "amenity" to "hospital"))
        assertEquals(Included(PlaceCategory.AMENITY), result)
    }

    @Test
    fun `building with shop is included as shop`() {
        val result = PoiFilter.evaluate(mapOf("building" to "retail", "shop" to "mall"))
        assertEquals(Included(PlaceCategory.SHOP), result)
    }

    @Test
    fun `building with unknown amenity is excluded`() {
        val result = PoiFilter.evaluate(mapOf("building" to "yes", "amenity" to "waste_basket"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `highway tag is globally excluded`() {
        val result = PoiFilter.evaluate(mapOf("highway" to "residential"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `natural tag is globally excluded`() {
        val result = PoiFilter.evaluate(mapOf("natural" to "tree"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `landuse tag is globally excluded`() {
        val result = PoiFilter.evaluate(mapOf("landuse" to "residential"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `barrier tag is globally excluded`() {
        val result = PoiFilter.evaluate(mapOf("barrier" to "fence"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `waterway tag is globally excluded`() {
        val result = PoiFilter.evaluate(mapOf("waterway" to "river"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `empty tags are excluded`() {
        val result = PoiFilter.evaluate(emptyMap())
        assertEquals(Excluded, result)
    }

    @Test
    fun `element with name only and no place tag is excluded`() {
        val result = PoiFilter.evaluate(mapOf("name" to "Some Place"))
        assertEquals(Excluded, result)
    }

    @Test
    fun `shop with extra metadata tags is still included`() {
        val result = PoiFilter.evaluate(
            mapOf("shop" to "clothes", "name" to "H&M", "addr:city" to "Helsinki"),
        )
        assertEquals(Included(PlaceCategory.SHOP), result)
    }

    @Test
    fun `highway tag globally excludes even when amenity also present`() {
        val result = PoiFilter.evaluate(mapOf("highway" to "services", "amenity" to "fuel"))
        assertEquals(Excluded, result)
    }
}
