package com.fyp_lubdub;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private FloatingActionButton fab;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private RecyclerView Hist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        fab = findViewById(R.id.newRec);
        Hist = findViewById(R.id.hist);

        db = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Profile.this,MainActivity.class);
                startActivity(i);
            }
        });
        getHistory();


    }

    private ArrayList<String> date,time;
    private Map<String,ArrayList<String>> DT ;
    private void getHistory(){
        DT = new HashMap<>();
        date = new ArrayList<>();
        db.child(auth.getUid()+"/Signals/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String d = String.valueOf(childDataSnapshot.getKey());
                   date.add(d);
                   db.child(auth.getUid()+"/Signals/"+d).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot DS) {
                           time = new ArrayList<>();
                           for (DataSnapshot timeDS : DS.getChildren()) {
                               String t = String.valueOf(timeDS);
                               time.add(t);
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {
                           Toast.makeText(Profile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   });
                   DT.put(date.get(date.size()-1),time);
                   }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Profile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
