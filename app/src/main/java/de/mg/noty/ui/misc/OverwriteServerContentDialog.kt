package de.mg.noty.ui.misc

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Toast
import de.mg.noty.R
import de.mg.noty.ui.NotyViewModel

object OverwriteServerContentDialog {

    fun execute(activity: Activity, viewModel: NotyViewModel) {
        val layoutInflater = LayoutInflater.from(activity)
        val promptView = layoutInflater.inflate(R.layout.dialog_overwrite, null)
        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptView)

        alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                viewModel.overwriteServerContent()
                dialog.cancel()
                Toast.makeText(activity, "overwritten...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }


        val alert = alertDialogBuilder.create()
        alert.show()
    }


}