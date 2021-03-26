package com.example.hamza.noteappp.Activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.example.hamza.noteappp.Adapter.NoteAdapter;
import com.example.hamza.noteappp.Model.Note;
import com.example.hamza.noteappp.R;
import com.example.hamza.noteappp.databinding.ActivityMainBinding;
import com.example.hamza.noteappp.viewmodel.ViewModel;
import java.util.List;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private ViewModel noteViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityMainBinding binding = DataBindingUtil.setContentView(this,R.layout.activity_main);


        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getApplicationContext(),AddNote.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });



        binding.rec.setLayoutManager(new LinearLayoutManager(this));
        binding.rec.setHasFixedSize(true);
        final NoteAdapter adapter = new NoteAdapter();
        binding.rec.setAdapter(adapter);



        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.rec);



        noteViewModel = ViewModelProviders.of(this).get(ViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(@Nullable List<Note> notes) {
                //update RecyclerView
                adapter.setNotes(notes);
            }
        });

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent intent = new Intent(MainActivity.this, AddNote.class);
                intent.putExtra(AddNote.EXTRA_ID, note.getId());
                intent.putExtra(AddNote.EXTRA_TITLE, note.getTitle());
                intent.putExtra(AddNote.EXTRA_DESCRIPTION, note.getContent());
                startActivityForResult(intent, EDIT_NOTE_REQUEST);
            }
        }); }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!isFinishing()){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(" Alert")
                                    .setMessage("Do you really want to delete all notes ?")
                                    .setCancelable(true)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Whatever...

                                            noteViewModel.deleteAllNotes();
                                            Toast.makeText(getApplicationContext(), "All notes deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }).show();
                        }
                    }
                });


                return true;

        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(AddNote.EXTRA_TITLE);
            String description = data.getStringExtra(AddNote.EXTRA_DESCRIPTION);
            Note note = new Note(title, description);
            noteViewModel.insert(note);
            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
        } else if (requestCode==EDIT_NOTE_REQUEST&&resultCode==RESULT_OK){
            int id = data.getIntExtra(AddNote.EXTRA_ID, -1);
            if (id == -1) {
                Toast.makeText(this, "Task can't be updated , Please click update", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = data.getStringExtra(AddNote.EXTRA_TITLE);
            String description = data.getStringExtra(AddNote.EXTRA_DESCRIPTION);
            Note note = new Note(title, description);
            note.setId(id);
            noteViewModel.update(note);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Task not saved", Toast.LENGTH_SHORT).show();
        }
    }}