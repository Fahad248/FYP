package com.fyp_lubdub;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private RelativeLayout RL;

    private boolean fetchedHist = false, fetchedDetails = false;
    Dialog dialog;
    ProgressBar Progress;
    TextView History,Profile,text;
    String diabetes="No", hyperTension="No", heartProblems="No", cholestrol="No";
    String smoker ="No", Chest_PAIN="No", Nausea="No", Sweating="No";
    String Name;
    View tempView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        History = findViewById(R.id.text_hist);
        Profile = findViewById(R.id.view_profile);
        RL = findViewById(R.id.layout);

        History.setTextColor(Color.parseColor("#ffffff"));
        History.setBackgroundColor(Color.parseColor("#006064"));
        Profile.setBackgroundColor(Color.parseColor("#ffffff"));
        Profile.setTextColor(Color.parseColor("#006064"));

        dialog = new Dialog(Profile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout);

        TextView text = dialog.findViewById(R.id.dia_text);
        text.setText("Getting Data...");
        ProgressBar Progress = dialog.findViewById(R.id.progress);

        Progress.setIndeterminate(true);
        dialog.show();

        LayoutInflater factory = LayoutInflater.from(Profile.this);
        View myView = factory.inflate(R.layout.layout_history, null);
        RL.removeAllViews();
        RL.addView(myView);
        if(!fetchedHist)
            History_View(myView);

        History.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                History.setTextColor(Color.parseColor("#ffffff"));
                History.setBackgroundColor(Color.parseColor("#006064"));
                Profile.setBackgroundColor(Color.parseColor("#ffffff"));
                Profile.setTextColor(Color.parseColor("#006064"));

                dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_layout);

                TextView text = dialog.findViewById(R.id.dia_text);
                text.setText("Getting Data...");
                ProgressBar Progress = dialog.findViewById(R.id.progress);

                Progress.setIndeterminate(true);
                dialog.show();

                LayoutInflater factory = LayoutInflater.from(Profile.this);
                View myView = factory.inflate(R.layout.layout_history, null);
                RL.removeAllViews();
                RL.addView(myView);
                History_View(myView);
            }
        });

        Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile.setTextColor(Color.parseColor("#ffffff"));
                Profile.setBackgroundColor(Color.parseColor("#006064"));
                History.setBackgroundColor(Color.parseColor("#ffffff"));
                History.setTextColor(Color.parseColor("#006064"));

                dialog = new Dialog(Profile.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_layout);

                TextView text = dialog.findViewById(R.id.dia_text);
                text.setText("Getting Data...");
                ProgressBar Progress = dialog.findViewById(R.id.progress);

                Progress.setIndeterminate(true);
                dialog.show();

                LayoutInflater factory = LayoutInflater.from(Profile.this);
                View myView = factory.inflate(R.layout.patient_details, null);
                RL.removeAllViews();
                RL.addView(myView);
                tempView = myView;
                if(!fetchedDetails)
                    Profile_View(myView);
                RL.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onGlobalLayout() {
                        int availableHeight = RL.getMeasuredHeight();
                        if(availableHeight>0) {
                            RL.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                 //           SC(RL);
                            //save height here and do whatever you want with it
                        }
                    }
                });
            }
        });

        db.child(auth.getUid() + "/Profile/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Name = dataSnapshot.child("Name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    Bitmap b;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public  void  SC(View v){

        b =    Bitmap.createBitmap( v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        createPdf();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPdf() {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(b.getWidth(), b.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        Bitmap bitmap = Bitmap.createScaledBitmap(b, b.getWidth(), b.getHeight(), true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
        File filePath = new File(Environment.getExternalStorageDirectory()+"/TEMP/","temp.pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        // close the document
        document.close();

        //  openPdf(path);// You can open pdf after complete
    }

    FloatingActionButton edit_fab;
    public void Profile_View(View V){

        edit_fab = V.findViewById(R.id.edit_details);
        Show_Details(V);

        edit_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Edit_Details();
            }
        });
    }

    public void History_View(View V){
        fab = V.findViewById(R.id.newRec);
        Hist = V.findViewById(R.id.hist);


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

        Hist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Toast.makeText(Profile.this, "Score: "+D_Score.get(date.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

    }

    ArrayList<String> date;
    ArrayList<String> time,Score;
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
                               dialog.dismiss();
                               fetchedHist = true;
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

    private void Edit_Details(){
        dialog = new Dialog(Profile.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.personal_info);

        final Button proceed = dialog.findViewById(R.id.info_proceed);
        final EditText contact, city, weight,blood, age;
        final CheckBox Diabetes, HyperTension, HeartProblems, Cholestrol, smoke, chest, nausea, sweat;
        TextView close = dialog.findViewById(R.id.close_dia);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Diabetes = dialog.findViewById(R.id.diabetes);
        HyperTension = dialog.findViewById(R.id.hyper);
        HeartProblems = dialog.findViewById(R.id.heart_problems);
        Cholestrol = dialog.findViewById(R.id.cholestrol);
        smoke = dialog.findViewById(R.id.smoking);
        chest = dialog.findViewById(R.id.chest_pain);
        nausea = dialog.findViewById(R.id.nausea);
        sweat = dialog.findViewById(R.id.sweating);

        contact = dialog.findViewById(R.id.contact);
        city = dialog.findViewById(R.id.city);
        weight = dialog.findViewById(R.id.weight);
        blood = dialog.findViewById(R.id.bloodGroup);
        age = dialog.findViewById(R.id.age);
        dialog.show();

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String con = contact.getText().toString();
                String cit = city.getText().toString();
                String wei = weight.getText().toString();
                String group = blood.getText().toString();

                if(Diabetes.isChecked())
                    diabetes = "Yes";
                if(HyperTension.isChecked())
                    hyperTension = "Yes";
                if(HeartProblems.isChecked())
                    heartProblems = "Yes";
                if(Cholestrol.isChecked())
                    cholestrol = "Yes";
                if(smoke.isChecked())
                    smoker = "Yes";
                if(chest.isChecked())
                    Chest_PAIN = "Yes";
                if(nausea.isChecked())
                    Nausea = "Yes";
                if(sweat.isChecked())
                    Sweating = "Yes";

                if(!Cholestrol.isChecked()) {
                    HashMap map = new HashMap<String, String>();
                    map.put("Name",Name);
                    map.put("Age", age.getText().toString());
                    map.put("Contact", con);
                    map.put("City", cit);
                    map.put("Weight", wei);
                    map.put("Blood Group", group);
                    map.put("Diabetes", diabetes);
                    map.put("Hyper Tension", hyperTension);
                    map.put("Heart Problems", heartProblems);
                    map.put("High Cholestrol", cholestrol);
                    map.put("Smoker", smoker);
                    map.put("Chest Pain", Chest_PAIN);
                    map.put("Nausea", Nausea);
                    map.put("Sweating", Sweating);

                    db.child(auth.getUid() + "/Profile/").setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Profile.this, "Data Updated!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Profile.this, "Error Updating Profile!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else{
                    Show_Details(tempView);
                    dialog.dismiss();
                }
            }
        });
    }

    private TextView name, city, age, blood;
    private boolean Dia, HT, HP, HC, Smoke, CP, Nau, Sweat;
    private CheckBox dia, ht, hp, hc, smoke, cp, nau, sweat;
    private void Show_Details(View V){
        name = V.findViewById(R.id.textViewName);
        city =  V.findViewById(R.id.textViewCity);
        age = V.findViewById(R.id.textViewAge);
        blood = V.findViewById(R.id.textViewBlood);
        dia = V.findViewById(R.id.chDiabetes);
        ht = V.findViewById(R.id.chHyper);
        hp = V.findViewById(R.id.chHeart_problems);
        hc = V.findViewById(R.id.chCholestrol);
        smoke = V.findViewById(R.id.chSmoking);
        cp = V.findViewById(R.id.chChest_pain);
        nau = V.findViewById(R.id.chnausea);
        sweat = V.findViewById(R.id.chSweating);
        try {
            db.child(auth.getUid() + "/Profile/").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        name.setText(dataSnapshot.child("Name").getValue().toString());
                        city.setText(dataSnapshot.child("City").getValue().toString());
                        age.setText(dataSnapshot.child("Age").getValue().toString());
                        blood.setText(dataSnapshot.child("Blood Group").getValue().toString());
                        Dia = dataSnapshot.child("Diabetes").getValue().toString().equals("Yes");
                        HT = dataSnapshot.child("Hyper Tension").getValue().toString().equals("Yes");
                        HP = dataSnapshot.child("Heart Problems").getValue().toString().equals("Yes");
                        HC = dataSnapshot.child("High Cholestrol").getValue().toString().equals("Yes");
                        Smoke = dataSnapshot.child("Smoker").getValue().toString().equals("Yes");
                        CP = dataSnapshot.child("Chest Pain").getValue().toString().equals("Yes");
                        Nau = dataSnapshot.child("Nausea").getValue().toString().equals("Yes");
                        Sweat = dataSnapshot.child("Sweating").getValue().toString().equals("Yes");

                        dia.setChecked(Dia); ht.setChecked(HT); hp.setChecked(HP);
                        hc.setChecked(HC); smoke.setChecked(Smoke); cp.setChecked(CP);
                        nau.setChecked(Nau); sweat.setChecked(Sweat);
                        dialog.dismiss();
                        fetchedDetails = true;
                    }
                    catch(Exception e){
                       dialog.dismiss();
                        Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        //Log.e("Error", e.getMessage());
                      //  Edit_Details();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch(Exception e){
            Toast.makeText(Profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(dialog.isShowing())
            dialog.dismiss();
    }
}
