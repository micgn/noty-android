package de.mg.noty.ui.tagslist

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import de.mg.noty.R
import de.mg.noty.model.Tag
import de.mg.noty.ui.NotyViewModel


class TagsListFragment : Fragment() {

    private lateinit var viewModel: NotyViewModel
    private var listener: OnTagListItemListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        viewModel = activity!!.run { ViewModelProviders.of(this).get(NotyViewModel::class.java) }

        val viewAdapter = TagsListViewAdapter(listener, viewModel)
        val flowLayoutManager = FlowLayoutManager()
        flowLayoutManager.isAutoMeasureEnabled = true

        with(view as RecyclerView) {
            layoutManager = flowLayoutManager
            adapter = viewAdapter
        }

        viewModel.registerAnyChangeObserver { viewAdapter.notifyChange() }

        return view
    }

    fun notifyDataSetChanged() {
        if (view != null) (view as RecyclerView).adapter?.notifyDataSetChanged()
    }

    fun openTagInput() {
        val layoutInflater = LayoutInflater.from(activity)
        val promptView = layoutInflater.inflate(R.layout.dialog_edittag, null)
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptView)

        val inputField = promptView.findViewById(R.id.editText) as EditText
        if (viewModel.selectedTag != null)
            inputField.setText(viewModel.selectedTag?.name)

        alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK") { _, _ ->

                val value = inputField.text.toString().trim()
                if (viewModel.selectedTag == null) {
                    val tag = Tag(name = value)
                    viewModel.insert(tag)
                } else {
                    val tag = viewModel.selectedTag!!
                    tag.name = value
                    viewModel.update(tag)
                }
                viewModel.selectedTag = null
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                viewModel.selectedTag = null
                dialog.cancel()
            }

        if (viewModel.selectedTag != null)
            alertDialogBuilder.setNeutralButton("Delete Tag") { dialog, _ ->
                viewModel.delete(viewModel.selectedTag!!)
                viewModel.selectedTag = null
                dialog.cancel()
                Toast.makeText(activity, "deleted...", Toast.LENGTH_SHORT).show()
            }

        val alert = alertDialogBuilder.create()
        alert.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTagListItemListener)
            listener = context
        else
            throw RuntimeException("$context must implement OnTagListItemListener")

    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}
