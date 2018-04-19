 package com.fyp_lubdub;

 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.annotation.NonNull;
 import android.support.v7.app.AppCompatActivity;
 import android.view.View;
 import android.widget.Button;
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

    private Button strt,stop,pause,open;
    private TextView file_name;
    private LineChart graph;
    LineData data = null;

    private WavAudioRecorder WavRecorder = null;
    private Thread thrd ;
    private Handler H;

    private String strDate,File_Path;

    private FirebaseAuth auth;

    ProgressDialog progressDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        strt = findViewById(R.id.start);
        open = findViewById(R.id.OE);
        file_name = findViewById(R.id.file_name);
        graph = findViewById(R.id.bar);

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
                strt.setBackground(getResources().getDrawable(R.drawable.microphone_red));
                open.setEnabled(false);
            } else {
                WavRecorder.stop();
                WavRecorder.release();
                WavRecorder = null;
                //strt.setText("Record");
                strt.setBackground(getResources().getDrawable(R.drawable.microphone_green));
                open.setEnabled(true);

                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Performing Quality Assessment...");
                progressDialog.show();
                H.postDelayed(thrd,500);

            }

            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pick_File();
            }
        });


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
                    /*mFrameGains[i] = (int) Math.sqrt(gain);
                    if (mProgressListener != null) {
                        boolean keepGoing = mProgressListener.reportProgress(i * 1.0 / mFrameGains.length);
                        if (!keepGoing) {
                            break;
                        }
                    }*/

                //temp = (int) wavFile.getSampleRate();
                wavFile.close();


            } catch (IOException | WavFile.WavFileException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graph.setVisibility(View.VISIBLE);
                   // Toast.makeText(MainActivity.this, String.valueOf(Data) +"  "+ String.valueOf(temp2), Toast.LENGTH_SHORT).show();
                    thrd.interrupt();
                //    Upload();
                    H.removeCallbacks(thrd);
                QA();
                }
            });
        }

    };

    @Override
    public void QA(){
        progressDialog.setMessage("Performing Quality Assessment...");
        float[] Data1 = transform(DataBuffer);
        float[] Data = transform(Data1);

        Quality_Assessment QA = new Quality_Assessment(MainActivity.this,Data);
        int assess = QA.Process();
        Toast.makeText(MainActivity.this, String.valueOf(assess), Toast.LENGTH_SHORT).show();

        if (assess == 1)
            Upload();
        progressDialog.hide();
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
           if(!thrd.isAlive()) {
               progressDialog = new ProgressDialog(MainActivity.this);
               progressDialog.setIndeterminate(true);
               progressDialog.setMessage("Plotting Signal...");
               progressDialog.show();
               H.postDelayed(thrd,500);
               progressDialog.hide();
           }else {
               Toast.makeText(MainActivity.this, "Thread chal rha hai -.-", Toast.LENGTH_SHORT).show();
           }
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
        left.setTextColor(Color.parseColor("#000000"));

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

    private void Upload(){
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://lubdub-1a71d.appspot.com");

// Create a reference to "mountains.jpg"
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
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                DateFormat df = new SimpleDateFormat("h:mm a");
                final String time = df.format(Calendar.getInstance().getTime());

                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                final String date = df2.format(Calendar.getInstance().getTime());


                Map<String,Object> taskMap = new HashMap<>();
                taskMap.put("Path",auth.getUid()+"/"+strDate);
                final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child(auth.getUid()+"/New/").updateChildren(taskMap);

                //   db.child(auth.getUid()).push().setValue("Profile");
                db.child(auth.getUid()+"/Signals/"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        count= (int) dataSnapshot.getChildrenCount();
                        Map<String,Object> map = new HashMap<>();
                        map.put(String.valueOf(count),time);
                        db.child(auth.getUid()+"/Signals/"+date).updateChildren(map);
                        success = true;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
          //      Toast.makeText(MainActivity.this, taskSnapshot.getMetadata().getName(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(MainActivity.this,Profile.class);
        i.putExtra("Fetch",true);
        startActivity(i);
    }
}
