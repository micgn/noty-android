package de.mg.noty.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import de.mg.noty.BuildConfig
import de.mg.noty.R
import de.mg.noty.R.id.*
import de.mg.noty.external.CallServerWorkManager
import de.mg.noty.model.Note
import de.mg.noty.model.Tag
import de.mg.noty.notifications.NotificationScheduler
import de.mg.noty.ui.NotyViewModel.SelectedViewEnum.NoteEdit
import de.mg.noty.ui.NotyViewModel.SelectedViewEnum.Overview
import de.mg.noty.ui.edit.EditNoteFragment
import de.mg.noty.ui.misc.OverwriteServerContentDialog
import de.mg.noty.ui.misc.SyncStateDialog
import de.mg.noty.ui.noteslist.NotesListFragment
import de.mg.noty.ui.noteslist.OnNoteListItemListener
import de.mg.noty.ui.tagslist.OnTagListItemListener
import de.mg.noty.ui.tagslist.TagsListFragment
import de.mg.noty.ui.widget.AddNewWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), OnTagListItemListener, OnNoteListItemListener {

    lateinit var tagsListFragment: TagsListFragment
    lateinit var notesListFragment: NotesListFragment

    private lateinit var viewModel: NotyViewModel

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        println(BuildConfig.SERVER_URL)

        viewModel = ViewModelProviders.of(this).get(NotyViewModel::class.java)

        notesListFragment = NotesListFragment()
        tagsListFragment = TagsListFragment()
        supportFragmentManager.beginTransaction().add(R.id.top_container, tagsListFragment).commit()

        // widget click
        if (intent.getStringExtra(AddNewWidgetProvider.EXTRA_NAME) == AddNewWidgetProvider.EXTRA_VALUE) {
            viewModel.createNewNote()
            openEditView()
        } else
            openOverview()

        noteAddBtn.setOnLongClickListener { tagsListFragment.openTagInput(); true }
        noteAddBtn.setOnClickListener {
            viewModel.createNewNote()
            openEditView()
        }

        if (isConnected())
            CallServerWorkManager().getDeltas()

        NotificationScheduler().schedule(this)


        Thread {
            while (true) {
                Thread.sleep(3000)
                if (CallServerWorkManager().hasFailures())
                    runOnUiThread {
                        toolbar.setBackgroundColor(
                            ContextCompat.getColor(this, R.color.warn)
                        )
                    }
                else
                    runOnUiThread {
                        toolbar.setBackgroundColor(
                            ContextCompat.getColor(this, R.color.colorPrimary)
                        )
                    }
            }
        }.start()
    }

    private fun isConnected(): Boolean {
        val cm =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }


    fun openOverview() {
        viewModel.currentView = Overview
        noteAddBtn.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction().replace(R.id.bottom_container, notesListFragment)
            .commit()
        tagsListFragment.notifyDataSetChanged()

    }

    private fun openEditView() {
        viewModel.currentView = NoteEdit
        noteAddBtn.visibility = View.GONE
        supportFragmentManager.beginTransaction().replace(R.id.bottom_container, EditNoteFragment())
            .commit()
        tagsListFragment.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            action_about -> true
            action_syncstate -> {
                SyncStateDialog.open(this, viewModel)
                true
            }
            action_overwrite -> {
                OverwriteServerContentDialog.execute(this, viewModel)
                true
            }
            action_about -> {
                true
            }
            /*app_bar_notDueSwitch -> {

                switch_hideNotDue.setOnCheckedChangeListener { buttonView, chhecked ->
                    ;
                }
                true
            }*/
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun switchHideNotDue(view: View) {
        viewModel.setHideNotDue((view as Switch).isChecked)
    }


    override fun onItemClick(note: Note, longClick: Boolean) {
        if (longClick)
            notesListFragment.deleteNote(note)
        else
            openEditView()
    }

    override fun onItemClick(tag: Tag, longClick: Boolean) {
        if (longClick)
            tagsListFragment.openTagInput()
    }
}
