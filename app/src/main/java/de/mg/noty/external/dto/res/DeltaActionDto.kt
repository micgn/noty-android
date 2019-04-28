package de.mg.noty.external.dto

import de.mg.noty.external.dto.req.NoteDeltaDto
import de.mg.noty.external.dto.req.NoteTagDeltaDto
import de.mg.noty.external.dto.req.TagDeltaDto

class DeltaActionDto {
    val action: ActionEnum? = null

    // one of the following will be set:
    val note: NoteDeltaDto? = null
    val tag: TagDeltaDto? = null
    val noteTag: NoteTagDeltaDto? = null

    fun getUpdated() =
        if (note != null) note.updated
        else if (tag != null) tag.updated
        else if (noteTag != null) noteTag.updated
        else 0
}

enum class ActionEnum {
    CREATE, UPDATE, DELETE
}


