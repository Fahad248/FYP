 package com.fyp_lubdub;

 import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.developerpaul123.filepickerlibrary.FilePickerActivity;
import com.github.developerpaul123.filepickerlibrary.enums.Request;
import com.github.developerpaul123.filepickerlibrary.enums.ThemeType;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.github.developerpaul123.filepickerlibrary.FilePickerActivity.REQUEST_FILE;

public class MainActivity extends AppCompatActivity implements Interface_MainActivity {

    private Button strt,stop,pause,open,back;
    private TextView file_name;
    private LineChart graph;
    LineData data = null;

    private WavAudioRecorder WavRecorder = null;
    private Thread thrd ;
    private Handler H;

    private String strDate,File_Path;

    private FirebaseAuth auth;
    private  DatabaseReference db;

    Dialog dialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        strt = findViewById(R.id.start);
        open = findViewById(R.id.OE);
        file_name = findViewById(R.id.file_name);
        graph = findViewById(R.id.bar);
        back = findViewById(R.id.background);

      //  graph.setVisibility(INVISIBLE);
        GraphAxis();

        auth = FirebaseAuth.getInstance();

        H = new Handler();
        thrd = new Thread(THREAD);

        strt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if (WavRecorder == null) {
                GraphAxis();
                Calendar c = Calendar.getInstance();
                DateFormat df = new SimpleDateFormat("h:mm a");
                String time = df.format(Calendar.getInstance().getTime());

                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                String date = df2.format(Calendar.getInstance().getTime());

                strDate = date+"_"+time;
                file_name.setText(strDate);
                File_Path = Environment.getExternalStorageDirectory() + "/PCG/"+strDate+".wav";
               // File_Path = Environment.getExternalStorageDirectory() + "/test.wav";
                //WavRecorder = new WavAudioRecorder(MediaRecorder.AudioSource.MIC, 4000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                WavRecorder = WavAudioRecorder.getInstance();
                WavRecorder.setOutputFile(File_Path);
                WavRecorder.setGraph(graph);
                WavRecorder.prepare();
                WavRecorder.start();
                //Toast.makeText(MainActivity.this, "Recording Started \n", Toast.LENGTH_LONG).show();
               // strt.setText("Stop");
                back.setBackground(getResources().getDrawable(R.drawable.round_red));
                open.setEnabled(false);
            } else {
                WavRecorder.stop();
                WavRecorder.release();
                WavRecorder = null;
                //strt.setText("Record");
                back.setBackground(getResources().getDrawable(R.drawable.round_green));
                open.setEnabled(true);


                /*progressDialog.setMessage("Uploading File...");
                progressDialog.show();*/
                //H.postDelayed(thrd,500);
                Upload();
            }

            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pick_File();
            }
        });


        db = FirebaseDatabase.getInstance().getReference();

        try {
            db.child("Admin/" + auth.getUid() + "/Signal").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        String analyzed = dataSnapshot.child("Analyzed").getValue().toString();
                        if (analyzed.equals("1")) {
                            String score = dataSnapshot.child("Score").getValue().toString();
                            if(!score.equals("None")) {
                                if (score.equals("0")) {
                                    deleteFile(dataSnapshot.child("Path").getValue().toString());
                                    db.child("Admin/" + auth.getUid()).removeValue();
                                    dialog.setContentView(R.layout.response_dialog);
                                    TextView OK = dialog.findViewById(R.id.textOK);
                                    OK.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                else if (score.equals("1")){
                                    dialog.dismiss();
                                    db.child("Admin/" + auth.getUid()).removeValue();
                                    Toast.makeText(MainActivity.this, "Signal OK !", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    catch (Exception e){
                     //   Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch(Exception e){
          //  Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    private float[] DataBuffer;
    long temp = 0,temp2 = 0;
    Runnable THREAD = new Runnable() {
        @Override
        public void run() {
            WavFile wavFile = null;
           /// graph.setVisibility(INVISIBLE);
            GraphAxis();
            try {
               // String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testwave.wav";
                File mFile = new File(File_Path);
                try {
                    wavFile = WavFile.openWavFile(mFile);
                    //Toast.makeText(Main2Activity.this, String.valueOf(wavFile.getNumFrames()), Toast.LENGTH_SHORT).show();
                } catch (IOException | WavFile.WavFileException e) {
                    e.printStackTrace();
                }
                int bufSize = (int) wavFile.getNumFrames();
                temp = wavFile.getDuration();
               // temp2 = bufSize/temp;
                temp2 = wavFile.getBlockAlign();
                DataBuffer = new float[bufSize];

                //addEntry(wavFile.readFrames());

                wavFile.readFrames(DataBuffer, bufSize);

                for (int j = 0; j < DataBuffer.length; j+=4) {
                    //float value = buffer[j];
                    addEntry(DataBuffer[j]);
                }

                wavFile.close();


            } catch (IOException | WavFile.WavFileException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graph.setVisibility(View.VISIBLE);
                    thrd.interrupt();
                    H.removeCallbacks(thrd);
                    Upload();
                }
            });
        }



    };

    @Override
    public void QA(){

        float[] Data1 = transform(DataBuffer);
        float[] Data = transform(Data1);

        Quality_Assessment QA = new Quality_Assessment(MainActivity.this,Data);
        int assess = QA.Process();
        Toast.makeText(MainActivity.this, String.valueOf(assess), Toast.LENGTH_SHORT).show();

        if (assess == 1)
            Upload();
    }


    public static int log2(int bits) {
        if (bits == 0) {
            return 0;
        }
        return 31 - Integer.numberOfLeadingZeros(bits);
    }
    @Override
    public float[] transform(float[] s){
        int m = s.length;
       // assert isPowerOfTwo(m);
        int n = log2(m);
        int j = 2;
        int i = 1;
        for(int l = 0 ; l < n ; l++ ){
            m = m/2;
            for(int k=0; k < m;k++){
                float a = (s[j*k]+s[j*k + i])/2.0f;
                float c = (s[j*k]-s[j*k + i])/2.0f;
               /* if(preserveEnergy){
                    a = a/sqrtTwo;
                    c = c/sqrtTwo;
                }*/
                s[j*k] = a;
                s[j*k+i] = c;
            }
            i = j;
            j = j * 2;
        }
        return s;
    }

    void Pick_File() {
        Intent filePickerDialogIntent = new Intent(this, FilePickerActivity.class);
        filePickerDialogIntent.putExtra(FilePickerActivity.THEME_TYPE, ThemeType.DIALOG);
        filePickerDialogIntent.putExtra(FilePickerActivity.REQUEST, Request.FILE);
        startActivityForResult(filePickerDialogIntent, REQUEST_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if ((requestCode == REQUEST_FILE) && (resultCode == RESULT_OK)) {
         //     Toast.makeText(this, "File Selected: " + data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH), Toast.LENGTH_LONG).show();
       //    file_name.setText(data.);
           File_Path = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
           new ProgressTask(MainActivity.this).execute();
           /*if(!thrd.isAlive()) {

               H.postDelayed(thrd,500);

           }else {
               Toast.makeText(MainActivity.this, "Thread chal rha hai -.-", Toast.LENGTH_SHORT).show();
           }*/
        }
    }

    private void GraphAxis(){
        xVal = 0;
        data = new LineData();
        data.setValueTextColor(Color.WHITE);

        graph.setData(data);
        graph.setTouchEnabled(true);

        graph.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

                graph.centerViewToAnimated(e.getX(), e.getY(), graph.getData().getDataSetByIndex(h.getDataSetIndex())
                        .getAxisDependency(), 500);
               // Toast.makeText(MainActivity.this, "x = "+String.valueOf(e.getX())+"\ny = "+String.valueOf(e.getY()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        XAxis axis = graph.getXAxis();
        //axis.setEnabled(false);
        axis.setTextColor(Color.parseColor("#000000"));
        axis.setGranularity(0.1f);
        //axis.setDrawGridLines(false);

        YAxis left = graph.getAxisLeft();
        left.setEnabled(false);
       // left.setTextColor(Color.parseColor("#000000"));

        YAxis right = graph.getAxisRight();
        right.setEnabled(false);
    }

    private int xVal = 0;
    private void addEntry(float val) {

        LineData data = graph.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            // double x = (Math.random() * 4000) ;
            data.addEntry(new Entry(xVal++, val), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            graph.notifyDataSetChanged();

          //  graph.setVisibility(View.VISIBLE);
            // limit the number of visible entries
            graph.setVisibleXRangeMaximum(3500);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            //graph.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "PCG Signal");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor("#055a6b"));
        set.setLineWidth(2f);
        set.setCircleColor(Color.parseColor("#00FFFFFF"));
        set.setCircleRadius(0f);
        set.setFillAlpha(00);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
       /* set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);*/
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        return set;
    }

    int count;
    private boolean success = false;

    ProgressBar Progress;
    TextView text;

    private void Upload(){

        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_layout);

        text = dialog.findViewById(R.id.dia_text);
        text.setText("Uploading File...");
        Progress = dialog.findViewById(R.id.progress);

        Progress.setIndeterminate(true);
        dialog.show();

        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://lubdub-1a71d.appspot.com");


        StorageReference WavRef = storageRef.child(auth.getUid()+"/"+strDate);

        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(File_Path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = WavRef.putStream(stream);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
               // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                File temp = new File(File_Path);
                temp.delete();

                DateFormat df = new SimpleDateFormat("h:mm a");
                final String time = df.format(Calendar.getInstance().getTime());

                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                final String date = df2.format(Calendar.getInstance().getTime());

                Map<String,Object> taskMap = new HashMap<>();
                taskMap.put("Path",auth.getUid()+"/"+strDate);
                taskMap.put("Date",date);
                taskMap.put("Time",time);
                taskMap.put("Analyzed","0");
               // taskMap.put("Fetched","0");
                taskMap.put("Score","None");
                db.child("Admin/"+auth.getUid()+"/Signal/").updateChildren(taskMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //progressDialog.dismiss();
                        text.setText("Processing...");

                        //dialog.dismiss();
                    }
                });

                //   db.child(auth.getUid()).push().setValue("Profile");

          //      Toast.makeText(MainActivity.this, taskSnapshot.getMetadata().getName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    boolean deleted;
    public boolean deleteFile(String path){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://lubdub-1a71d.appspot.com");
        StorageReference signal = storageRef.child(path);


        signal.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleted = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                deleted = false;
            }
        });
        return deleted;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(MainActivity.this,Profile.class);
        i.putExtra("Fetch",true);
        startActivity(i);
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean> {
        private Dialog Dia;
        private Context context;
        private TextView msg;
        private ProgressBar prog;

        public ProgressTask(Context activity) {
            context = activity;
            this.Dia = new Dialog(context);
            this.Dia.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.Dia.setCancelable(false);
            this.Dia.setContentView(R.layout.dialog_layout);
        }

        /** progress dialog to show user that the backup is processing. */

        /** application context. */

        protected void onPreExecute() {

            this.msg = this.Dia.findViewById(R.id.dia_text);
            this.msg.setText("Plotting Signal...");
            this.prog = this.Dia.findViewById(R.id.progress);
            this.prog.setIndeterminate(true);

            this.Dia.show();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            this.Dia.dismiss();
            graph.setVisibility(View.VISIBLE);
            Upload();
        }

        protected Boolean doInBackground(final String... args) {
            try{

                WavFile wavFile = null;
                /// graph.setVisibility(INVISIBLE);
                GraphAxis();
                try {
                    // String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testwave.wav";
                    File mFile = new File(File_Path);
                    try {
                        wavFile = WavFile.openWavFile(mFile);
                        //Toast.makeText(Main2Activity.this, String.valueOf(wavFile.getNumFrames()), Toast.LENGTH_SHORT).show();
                    } catch (IOException | WavFile.WavFileException e) {
                        e.printStackTrace();
                    }
                    int bufSize = (int) wavFile.getNumFrames();
                    temp = wavFile.getDuration();
                    // temp2 = bufSize/temp;
                    temp2 = wavFile.getBlockAlign();
                    DataBuffer = new float[bufSize];

                    //addEntry(wavFile.readFrames());

                    wavFile.readFrames(DataBuffer, bufSize);

                    for (int j = 0; j < DataBuffer.length; j+=4) {
                        //float value = buffer[j];
                        addEntry(DataBuffer[j]);
                    }

                    wavFile.close();


                } catch (IOException | WavFile.WavFileException e) {
                    e.printStackTrace();
                }

                return true;
            } catch (Exception e){

                return false;
            }
        }


    }
}
