/*
 * Copyright (C) 2026 InWheel Contributors
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.inwheel.model

sealed class OsmElement {
    abstract val id: Long
    abstract val version: Int
    abstract val tags: Map<String, String>
    abstract val action: OsmAction?
}

data class OsmNode(
    override val id: Long,
    override val version: Int,
    override val tags: Map<String, String>,
    override val action: OsmAction? = null,
    val latitude: Double,
    val longitude: Double,
) : OsmElement()

data class OsmWay(
    override val id: Long,
    override val version: Int,
    override val tags: Map<String, String>,
    override val action: OsmAction? = null,
    val nodeIds: List<Long>,
) : OsmElement()

data class OsmRelation(
    override val id: Long,
    override val version: Int,
    override val tags: Map<String, String>,
    override val action: OsmAction? = null,
    val members: List<OsmMember>,
) : OsmElement()

data class OsmMember(
    val type: OsmMemberType,
    val ref: Long,
    val role: String,
)

enum class OsmMemberType { NODE, WAY, RELATION }

enum class OsmAction { CREATE, MODIFY, DELETE }
