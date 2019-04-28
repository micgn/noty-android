package de.mg.noty.external.dto.req

class NoteTagDeltaDto(
    val noteId: String, val tagId: String,
    val updated: Long = System.currentTimeMillis()
) {
    constructor() : this("", "")
}
