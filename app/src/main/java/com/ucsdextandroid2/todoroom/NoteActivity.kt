package com.ucsdextandroid2.todoroom

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class NoteActivity: AppCompatActivity() {

    companion object {
        private const val NOTE_EXTRA = "note_extra"

        fun createIntent(context: Context, note: Note? = null): Intent =
                Intent(context, NoteActivity::class.java).apply { putExtra(NOTE_EXTRA, note) }
    }

    private lateinit var toolbar: Toolbar
    private lateinit var titleView: EditText
    private lateinit var textView: EditText
    private lateinit var cameraIcon: ImageView
    private lateinit var bottomMenuIcon: ImageView

    private val originalNote: Note? by lazy(LazyThreadSafetyMode.NONE) {
        intent.getParcelableExtra<Note>(NOTE_EXTRA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        toolbar = findViewById(R.id.toolbar)
        titleView = findViewById(R.id.an_title)
        textView = findViewById(R.id.an_text)
        cameraIcon = findViewById(R.id.an_camera)
        bottomMenuIcon = findViewById(R.id.an_bottom_menu)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        if(originalNote != null) {
            titleView.setText(originalNote?.title)
            textView.setText(originalNote?.text)
        }
    }

    private fun saveNote(){
        if(titleView.text.isNotEmpty() || textView.text.isNotEmpty()){
            val note = Note(titleView.text.toString(),
                    textView.text.toString(),
                    originalNote?.datetime ?: System.currentTimeMillis(),
                    originalNote?.imageUri)
            AppDatabase.get(this).noteDao().insertNote(note)

            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
    }

    override fun onBackPressed() {
        saveNote()
        super.onBackPressed()
    }


}
