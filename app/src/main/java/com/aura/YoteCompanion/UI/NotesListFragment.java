package com.aura.YoteCompanion.UI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aura.YoteCompanion.Helpers.NotesAdapter;
import com.aura.YoteCompanion.Models.Note;
import com.aura.YoteCompanion.NoteActivities.AddNotes;
import com.aura.YoteCompanion.NoteActivities.NotesList;
import com.aura.YoteCompanion.NoteActivities.ViewNote;
import com.aura.YoteCompanion.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class NotesListFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{

    private List<Note> notesList  = new ArrayList<>();
    private DatabaseReference notesRef, adminNoteRef;
    private RecyclerView lstNotes;
    private NotesAdapter nAdapter;
    //private DataSnapshot dataSnapshot;
    //private final static String FRAGMENT_TAG = "FRAGMENTB_TAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.activity_noteslist, container, false);

        onDestroyView();

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddNotes.class);
                startActivity(intent);
            }

        });

        lstNotes = (RecyclerView) v.findViewById(R.id.lst_notes);
        nAdapter = new NotesAdapter(notesList);
        lstNotes.setLayoutManager(new LinearLayoutManager(getActivity()));
        lstNotes.setAdapter(nAdapter);


        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser mFireBaseUser = mFirebaseAuth.getCurrentUser();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        //boolean admin = mFireBaseUser.getUid().equals("RBMyK8QpEohgmqzabXBAm811CBh1");

        notesRef = database.getReference("/Users/" + mFireBaseUser.getUid() + "/");

        notesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getValue() != null) {
                    String title = dataSnapshot.child("title").getValue().toString();
                    String details = (String) dataSnapshot.child("details").getValue();
                    String date = (String) dataSnapshot.child("dateSaved").getValue();
                    Note note = new Note(title, details, date);
                    notesList.add(note);
                    nAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               onDestroyView();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /*onDestroyView();
                String key = dataSnapshot.getKey();
                if (notesList.contains(key)) {
                    int index = notesList.indexOf(key);
                    notesList.remove(index);
                }*/
                onDestroyView();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                onDestroyView();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                onDestroyView();
            }
        });

        lstNotes.addOnItemTouchListener(new NotesList.RecyclerTouchListener(getActivity(), lstNotes, new NotesList.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Note note = notesList.get(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), ViewNote.class);
                intent.putExtra("Note", note);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, final int position) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setMessage("Delete the note? ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                  // DatabaseReference noteDelete.child("Users").child( mFireBaseUser.getUid()+ "/").removeValue();
                                    notesList.remove(position);
                                    nAdapter.notifyDataSetChanged();
                                    deleteUserNote(mFireBaseUser.getUid());
                                    //notesRef.getRef().child( mFireBaseUser.getUid()+ "/").removeValue();
                                    Toast.makeText(v.getContext(), "Deleted Successfully ", Toast.LENGTH_SHORT).show();
                                    /*Snackbar snackbar = Snackbar.make(v.findViewById(R.id.coordinatorLayoutNotesList), "Note Deleted", Snackbar.LENGTH_LONG).
                                            setAction("Undo?", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Snackbar snackbar1 = Snackbar.make(v.findViewById(R.id.coordinatorLayoutNotesList), "Ok", Snackbar.LENGTH_SHORT);
                                                    snackbar1.show();
                                                }
                                            });
                                    snackbar.show();*/
                                } catch (Exception e) {
                                    Toast.makeText(v.getContext(), "Failed to delete....", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel" , null);
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
            }
        }));

        //Return the fragment view
        return  v;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notesList.clear();
    }
    private void deleteUserNote(String userId) {
        notesRef.child("/Users/").child(userId).removeValue();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
