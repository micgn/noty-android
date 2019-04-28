package de.mg.noty.external.dto

import de.mg.noty.external.dto.req.NoteDeltaDto
import de.mg.noty.external.dto.req.NoteTagDeltaDto
import de.mg.noty.external.dto.req.TagDeltaDto
import de.mg.noty.model.Note
import de.mg.noty.model.NotesTagsJoin
import de.mg.noty.model.Tag
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DtoMapper {

    fun map(dto: NoteDeltaDto): Note =
        Note(id = dto.noteId, text = dto.text, dueDate = map(dto.dueDate), lastEdit = dto.updated)

    fun map(dto: TagDeltaDto, selected: Boolean): Tag =
        Tag(id = dto.tagId, name = dto.name, selected = selected)

    private fun map(date: String?): LocalDate? =
        if (date == null) null else LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun map(note: Note): NoteDeltaDto =
        NoteDeltaDto(noteId = note.id, text = note.text, dueDate = map(note.dueDate), updated = note.lastEdit)

    fun map(tag: Tag): TagDeltaDto =
        TagDeltaDto(tagId = tag.id, name = tag.name, updated = tag.lastEdit)

    fun map(noteTag: NotesTagsJoin): NoteTagDeltaDto =
        NoteTagDeltaDto(noteId = noteTag.noteId, tagId = noteTag.tagId, updated = noteTag.lastEdit)

    private fun map(date: LocalDate?): String? =
        if (date == null) null else DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)

}