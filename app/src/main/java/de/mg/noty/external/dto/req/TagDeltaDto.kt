package de.mg.noty.external.dto.req

class TagDeltaDto(
    val tagId: String, val name: String,
    val updated: Long = System.currentTimeMillis()
) {
    constructor() : this("", "")
}
