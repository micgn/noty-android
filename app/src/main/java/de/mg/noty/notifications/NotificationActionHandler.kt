package de.mg.noty.notifications

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import de.mg.noty.db.NotyRepository

class NotificationActionHandler : Activity() {

    enum class Action {
        DELETE, NEXT_DAY, NEXT_WEEK
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()

        when (intent?.action) {
            Action.DELETE.name -> delete()
            Action.NEXT_WEEK.name -> next(7)
            Action.NEXT_DAY.name -> next(1)
        }
        finish()
    }

    private fun delete() {
        val repo = NotyRepository(applicationContext as Application)
        val dueNotes = repo.findAllDueNotes()
        dueNotes.forEach { note -> repo.delete(note) }

        Toast.makeText(this, "deleted ${dueNotes.size}", Toast.LENGTH_SHORT).show()
    }

    private fun next(amount: Int) {
        val repo = NotyRepository(applicationContext as Application)
        val dueNotes = repo.findAllDueNotes()
        dueNotes.forEach { note ->
            note.dueDate = note.dueDate?.plusDays(amount.toLong())
            repo.update(note)
        }

        Toast.makeText(this, "moved ${dueNotes.size} +$amount", Toast.LENGTH_SHORT).show()
    }

}