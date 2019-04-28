package de.mg.noty.db

import org.apache.commons.text.similarity.CosineDistance

class SimilarityService {

    fun get(input: String, allNotes: List<String>?): List<String> {

        if (allNotes == null || allNotes.size <= 3 || input.length < 3)
            return emptyList()

        class NoteDistance(val note: String, val distance: Double)

        return allNotes.map {
            // TODO maybe other algorithm?
            NoteDistance(it, CosineDistance().apply(it.toLowerCase(), input.toLowerCase()))
        }.filter { it.distance < 1.0 }.sortedBy { it.distance }.take(3).map { "${it.note}" }
    }
}