package de.mg.noty.external.dto.req;


class AllContentDto(
    val noteCreateDeltas: List<NoteDeltaDto>,
    val tagCreateDeltas: List<TagDeltaDto>,
    val noteTagCreateDeltas: List<NoteTagDeltaDto>
)
