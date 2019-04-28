package de.mg.noty.external.dto.req

data class NoteDeltaDto(
    val noteId: String, val text: String, val dueDate: String? = null,
    val updated: Long = System.currentTimeMillis()
) {
    constructor() : this("", "")
}
