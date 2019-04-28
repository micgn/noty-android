package de.mg.noty.ui.tagslist

import de.mg.noty.model.Tag

interface OnTagListItemListener {

    fun onItemClick(tag: Tag, longClick: Boolean = false)
}