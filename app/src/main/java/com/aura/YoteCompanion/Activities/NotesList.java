package com.aura.YoteCompanion.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.aura.YoteCompanion.Authentication.LogoutActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.aura.YoteCompanion.adapters.NotesAdapter;
import com.aura.YoteCompanion.helpers.DividerItemDecoration;
import com.aura.YoteCompanion.models.Note;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.aura.YoteCompanion.R;

public class NotesList extends AppCompatActivity {
    private List<Note> notesList  = new ArrayList<>();
    public static final String PREFS_NAME = "UsernameFile";
    private RecyclerView lst_notes;
    private NotesAdapter nAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //
        //This is the back button on the action bar
        // See method  onOptionsItemSelected
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("Username","");
        //
        if(username.matches("")) {
            username = UUID.randomUUID().toString();
            //
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("Username", username);
            editor.commit();
        }
        //
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNotes.class);
                startActivity(intent);
            }
        });
        //
        lst_notes = (RecyclerView) findViewById(R.id.lst_notes);
        //
        nAdapter = new NotesAdapter(notesList);
        //
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        lst_notes.setLayoutManager(layoutManager);
        lst_notes.setItemAnimator(new DefaultItemAnimator());
        lst_notes.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        lst_notes.setAdapter(nAdapter);
        //
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference usersRef = database.getReference("Users");
        //
        final DatabaseReference userRef = usersRef.child(username);
        //
        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null) {
                    String title = dataSnapshot.child("title").getValue().toString();
                    String details = (String) dataSnapshot.child("details").getValue();
                    String date = (String) dataSnapshot.child("savedAt").getValue();
                    boolean isSaved = (boolean) dataSnapshot.child("isStarred").getValue();
                    //
                    Note note = new Note(title, details, date, isSaved);
                    note.setNoteId(dataSnapshot.getKey());
                    notesList.add(note);
                    nAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //
        lst_notes.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), lst_notes, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Note note = notesList.get(position);
                //
                Intent intent = new Intent(getApplicationContext(), ViewNote.class);
                intent.putExtra("Note", note);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                AlertDialog.Builder alert = new AlertDialog.Builder(NotesList.this);
                alert.setMessage("Delete the note? ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userRef.getRef().removeValue();
                            }
                        })
                        .setNegativeButton("Cancel" , null);
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
                //Intent intent = new Intent(getApplicationContext(), ViewNote.class);
               // startActivity(intent);
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Intent refresh = new Intent(getApplicationContext(), NotesList.class);
                startActivity(refresh);
                break;
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
                break;
            /*case R.id.action_delete_all_notes:
                Intent intent4 = new Intent(getApplicationContext(), DeleteAllNotes.class);
                startActivity(intent4);
                break;
             */
            case R.id.action_log_out:
                Intent log_out = new Intent(getApplicationContext(), LogoutActivity.class);
                startActivity(log_out);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return false;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private NotesList.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final NotesList.ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}