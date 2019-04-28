package de.mg.noty.ui.edit

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import de.mg.noty.R
import de.mg.noty.ui.MainActivity
import de.mg.noty.ui.NotyViewModel
import de.mg.noty.ui.datepicker.DatePickerFragment
import de.mg.noty.ui.datepicker.DateSetCallback
import kotlinx.android.synthetic.main.fragment_edit.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EditNoteFragment : Fragment(), DateSetCallback {

    private lateinit var viewModel: NotyViewModel
    private lateinit var textField: EditText
    private lateinit var dueDateField: EditText

    private var newDueDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        viewModel = activity!!.run { ViewModelProviders.of(this).get(NotyViewModel::class.java) }
        textField = view.noteTextInput
        dueDateField = view.dueDate

        view.dueDatePicker.setOnClickListener { pickDate() }
        view.clearDueDate.setOnClickListener { clearDate() }
        view.cancelBtn.setOnClickListener { close() }
        view.okBtn.setOnClickListener { ok() }
        view.deleteBtn.setOnClickListener { delete() }
        view.deleteBtn.isEnabled = viewModel.selectedNote != null

        textField.setText(viewModel.selectedNote?.text)
        updateDueDateField()

        textField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showSimiliarNotes(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        return view
    }

    var similiarToast: Toast? = null

    private fun showSimiliarNotes(input: CharSequence?) {
        val similars = viewModel.getSimilars(input.toString())
        if (similars.isNotEmpty()) {
            if (similiarToast != null) similiarToast!!.cancel()
            val toastText = similars.joinToString(separator = "\n")
            similiarToast = Toast.makeText(activity, toastText, Toast.LENGTH_LONG)
            similiarToast!!.setGravity(Gravity.TOP or Gravity.START, 0, 0)
            similiarToast!!.show()
        }
    }

    private fun pickDate() {

        val datePickerFragment = DatePickerFragment()
        datePickerFragment.callback = this
        datePickerFragment.show(activity?.supportFragmentManager, "datePicker")
    }

    private fun clearDate() {
        newDueDate = null
        viewModel.selectedNote?.dueDate = null
        updateDueDateField()
    }

    override fun onDateSet(year: Int, month: Int, day: Int) {
        newDueDate = LocalDate.of(year, month, day)
        updateDueDateField()
    }

    private fun updateDueDateField() {
        val dueDate = if (newDueDate != null) newDueDate else viewModel.selectedNote?.dueDate
        val dueDateStr = dueDate?.format(
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        )
        dueDateField.setText(dueDateStr ?: "- due date -")
    }

    private fun close() {
        viewModel.selectedNote = null
        viewModel.newNote = null
        (activity as MainActivity).openOverview()
    }

    private fun ok() {
        val textValue = textField.text.toString().trim()
        if (textValue.isNotEmpty()) {
            if (viewModel.selectedNote != null) {
                val note = viewModel.selectedNote!!
                note.text = textValue
                if (newDueDate != null) note.dueDate = newDueDate
                viewModel.update(note)
            } else if (viewModel.newNote != null) {
                val note = viewModel.newNote!!
                note.text = textValue
                if (newDueDate != null) note.dueDate = newDueDate
                viewModel.insert(note)
            }
        }
        close()
    }

    private fun delete() {
        viewModel.selectedNote?.let {
            viewModel.delete(it)
            Toast.makeText(activity, "deleted...", Toast.LENGTH_SHORT).show()
        }
        close()
    }

}
