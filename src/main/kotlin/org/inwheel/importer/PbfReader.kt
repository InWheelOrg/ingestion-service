/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.importer

import crosby.binary.file.BlockInputStream
import org.inwheel.model.OsmElement
import org.inwheel.transformer.PoiFilter
import org.inwheel.transformer.PoiFilterResult
import java.io.File

class PbfReader(private val filter: (Map<String, String>) -> PoiFilterResult = PoiFilter::evaluate) {

    fun read(file: File, sink: (OsmElement) -> Unit) {
        require(file.exists()) { "PBF file not found: ${file.absolutePath}" }
        require(file.extension == "pbf") { "Expected a .pbf file, got: ${file.name}" }

        try {
            val parser = OsmPbfParser { element ->
                if (filter(element.tags) is PoiFilterResult.Included) {
                    sink(element)
                }
            }
            file.inputStream().buffered().use { stream ->
                BlockInputStream(stream, parser).process()
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw PbfReadException("Failed to read PBF file: ${file.absolutePath}", e)
        }
    }
}
