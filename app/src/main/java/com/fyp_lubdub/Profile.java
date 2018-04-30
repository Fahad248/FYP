package com.fyp_lubdub;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private FloatingActionButton fab;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private ExpandableListView Hist;
    private Hist_Adapter adap;
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
                adap=null;
                Hist.setAdapter(adap);
                finish();
            }
        });
        getHistory();

        /*Intent i = getIntent();
        boolean fetch = i.getBooleanExtra("Fetch",false);
        if(fetch){
            db.child(auth.getUid()+"/New/Normal").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dS) {
                    int result = Integer.valueOf(dS.getValue().toString());
                    if (result == 1)
                        Toast.makeText(Profile.this,"Signal is Normal!!!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(Profile.this,"Signal is AbNormal!!!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }*/

        Hist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(Profile.this, "Score: "+D_Score.get(date.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    ArrayList<String> date;
    ArrayList<String> time,RMSSD,Criterial,Score;
    private Map<String,ArrayList<String>> DT , D_Score;
    String d,d_orig;
    private void getHistory(){


        db.child(auth.getUid()+"/Signals/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DT = new HashMap<>();
                D_Score= new HashMap<>();
                date = new ArrayList<>();
                for (final DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                   d = String.valueOf(childDataSnapshot.getKey());
                   d_orig = String.valueOf(childDataSnapshot.getKey());
                    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date temp = df1.parse(d);
                        df1 = new SimpleDateFormat("dd-MM-yyyy");
                        d = df1.format(temp);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                   date.add(d);
                   db.child(auth.getUid()+"/Signals/"+d_orig).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot DS) {
                           time = new ArrayList<>();
                           Score = new ArrayList<>();
                         //  Toast.makeText(Profile.this, DS.getKey(), Toast.LENGTH_SHORT).show();
                           for (DataSnapshot keyDS : DS.getChildren()) {
                               String t = String.valueOf(keyDS.child("Time").getValue());
                               String score = String.valueOf(keyDS.child("Score").getValue());
                               time.add(t);
                               Score.add(score);
                              // Toast.makeText(Profile.this, t, Toast.LENGTH_SHORT).show();
                           }
                           if(time.size()>0) {
                               DT.put(d, time);
                               D_Score.put(d,Score);
                               adap = new Hist_Adapter(Profile.this, date, DT,D_Score);
                               Hist.setAdapter(adap);
                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {
                           Toast.makeText(Profile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                       }
                   });


                   // Toast.makeText(Profile.this, String.valueOf(DT.get(d)), Toast.LENGTH_SHORT).show();
                   }
             //   Toast.makeText(Profile.this, String.valueOf(date.size()), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Profile.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


}
