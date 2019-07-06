package com.ucsdextandroid2.todoroom

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.persistableBundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.*

class MainActivity: AppCompatActivity() {

    companion object{
        const val REQUET_CODE = 7

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        val recyclerView: RecyclerView = findViewById(R.id.am_recycler_view)
        val adapter = NotesAdapter()
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        recyclerView.adapter = adapter

        adapter.onNoteClickListener = {
            note -> startActivity(NoteActivity.createIntent(this, note))
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int){
                super.onItemRangeInserted(positionStart, itemCount)

                if(positionStart == 0){
                    recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })

        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.START or ItemTouchHelper.END){
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val note = adapter.removeItem(viewHolder.adapterPosition)
                if (note != null){
                    AppDatabase.get(this@MainActivity).noteDao().deleteNote(note)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)

        val addNoteView: View = findViewById(R.id.am_add_note)
        addNoteView.setOnClickListener{
            startActivityForResult(NoteActivity.createIntent(this), REQUET_CODE)
        }

        //onDataChanged()

        AppDatabase.get(this).noteDao().getAllNotesLiveData().observe(this, Observer<List<Note>>{ notes ->
            onDataChanged(notes)

            adapter.submitList(notes)
        })
    }

/*    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUET_CODE && resultCode == Activity.RESULT_OK){
            onDataChanged()
        }
    }*/

    private fun onDataChanged(notes: List<Note>){
        notes.forEach {
            Log.d("Main Activity", it.title)
        }

        Toast.makeText(this, "Total Number of Notes: " + notes.size, Toast.LENGTH_SHORT).show()
    }

}

private class NotesAdapter : ListAdapter<Note, NoteCardViewHolder>(listDiffer) {

    var onNoteClickListener: ((Note) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteCardViewHolder {
        val viewHolder = NoteCardViewHolder.inflate(parent)
        viewHolder.itemView.setOnClickListener{
            val item = getItem(viewHolder.adapterPosition)
            if (item != null){
                onNoteClickListener?.invoke(item)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: NoteCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun removeItem(position: Int): Note? {
        val note = getItem(position)
        notifyItemRemoved(position)

        return note
    }

    companion object {
        val listDiffer: DiffUtil.ItemCallback<Note> = object: DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem.datetime == newItem.datetime
            }

            override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
                return oldItem == newItem
            }
        }
    }
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

}

private class NoteCardViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {

    val image: ImageView = itemView.findViewById(R.id.vnc_image)
    val titleView: TextView = itemView.findViewById(R.id.vnc_title)
    val textView: TextView = itemView.findViewById(R.id.vnc_text)

    companion object {
        fun inflate(parent: ViewGroup): NoteCardViewHolder = NoteCardViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_holder_note_card, parent, false)
        )
    }

    fun bind(note: Note?) {
        if (note != null){
            titleView.setText(note.title)
            textView.setText(note.text)

            if(note.imageUri != null){
                image.isVisible = true
                image.setImageURI(note.imageUri)
            } else{
                image.isVisible = false
            }
        } else {
            titleView.setText("")
            textView.setText("")
            image.isVisible = false
        }
    }

}