package de.mg.noty.ui.noteslist

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.mg.noty.R
import de.mg.noty.model.Note
import de.mg.noty.ui.NotyViewModel
import de.mg.noty.ui.tagslist.OnTagListItemListener


class NotesListFragment : Fragment() {

    private lateinit var viewModel: NotyViewModel
    private var listener: OnNoteListItemListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        viewModel = activity!!.run { ViewModelProviders.of(this).get(NotyViewModel::class.java) }
        val viewAdapter = NotesListViewAdapter(listener, viewModel)

        with(view as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = viewAdapter
        }

        viewModel.filteredNotes.observe(this, Observer<List<Note>> { values ->
            viewAdapter.setValues(values)
        })

        return view
    }

    fun deleteNote(note: Note) {
        val layoutInflater = LayoutInflater.from(activity)
        val promptView = layoutInflater.inflate(R.layout.dialog_deletenote, null)
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptView)

        alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                viewModel.delete(note)
                viewModel.selectedNote = null
                dialog.cancel()
                Toast.makeText(activity, "deleted...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                viewModel.selectedNote = null
                dialog.cancel()
            }


        val alert = alertDialogBuilder.create()
        alert.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTagListItemListener)
            listener = context as OnNoteListItemListener
        else
            throw RuntimeException("$context must implement OnTagListItemListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}
