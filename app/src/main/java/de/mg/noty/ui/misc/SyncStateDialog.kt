package de.mg.noty.ui.misc

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import de.mg.noty.R
import de.mg.noty.external.CallServerWorkManager
import de.mg.noty.ui.NotyViewModel
import kotlinx.android.synthetic.main.dialog_syncstate.view.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SyncStateDialog {

    fun open(activity: Activity, viewModel: NotyViewModel) {
        val layoutInflater = LayoutInflater.from(activity)
        val promptView = layoutInflater.inflate(R.layout.dialog_syncstate, null)

        val lastRcv = viewModel.getMetaData()?.lastReceivedServerDelta
        val lastSyncTxt = if (lastRcv != null) {
            val dateTime =
                Instant.ofEpochMilli(lastRcv).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"))
            "last server sync:\n$dateTime"
        } else "no server sync ever"

        val queueInfo = CallServerWorkManager().getQueueInfo()


        promptView.textSyncState.text = "${lastSyncTxt}\n${queueInfo}"

        val alertDialogBuilder = AlertDialog.Builder(activity)
        alertDialogBuilder.setView(promptView)

        alertDialogBuilder.setCancelable(true)
            .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
            .setNegativeButton("Cancel Work") { dialog, _ ->
                CallServerWorkManager().cancelAll()
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()
    }
}