package de.mg.noty.ui.datepicker

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.time.LocalDate


class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

    var callback: DateSetCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val now = LocalDate.now()
        return DatePickerDialog(context!!, this, now.year, now.monthValue - 1, now.dayOfMonth)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {

        callback?.onDateSet(year, month + 1, day)
    }


}
