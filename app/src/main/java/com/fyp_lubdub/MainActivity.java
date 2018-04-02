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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import be.tarsos.dsp.wavelet.HaarWaveletTransform;

import static com.github.developerpaul123.filepickerlibrary.FilePickerActivity.REQUEST_FILE;

public class MainActivity extends AppCompatActivity {

    HaarWaveletTransform hvt;
    private Button strt,stop,pause,open;
    private LineChart graph;
    LineData data = null;

    private WavAudioRecorder WavRecorder = null;
    private Thread thrd ;
    private Handler H;

    private String File_Path;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        strt = findViewById(R.id.start);
        open = findViewById(R.id.OE);
        graph = findViewById(R.id.bar);

      //  graph.setVisibility(INVISIBLE);
        GraphAxis();

        H = new Handler();
        thrd = new Thread(THREAD);

        strt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            if (WavRecorder == null) {
                GraphAxis();
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = sdf.format(c.getTime());
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
               // Toast.makeText(MainActivity.this, "Recording Ended", Toast.LENGTH_SHORT).show();
              //  H.postDelayed(thrd,500);
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
    int data_len = 0;
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

                /*for (int j = 0; j < DataBuffer.length; j+=1) {
                    //float value = buffer[j];
                    addEntry(DataBuffer[j]);
                }*/
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
                   // graph.setVisibility(VISIBLE);
                   // Toast.makeText(MainActivity.this, String.valueOf(Data) +"  "+ String.valueOf(temp2), Toast.LENGTH_SHORT).show();
                    thrd.interrupt();
                //    Upload();
                    H.removeCallbacks(thrd);
                QA();
                }
            });
        }

    };

    public void QA(){
        progressDialog.setMessage("Performing Quality Assessment...");
        float[] Data1 = transform(DataBuffer);
        float[] Data = transform(Data1);

        for (int j = 0; j < Data.length; j+=1) {
            Data[j] = Data[j]*4;
            addEntry(Data[j]);
        }
        graph.setVisibility(View.VISIBLE);
        float[] Ps = new float[Data.length];
        ArrayList<Integer>locs = new ArrayList<Integer>();
        //ArrayList<Integer> Peaks = new ArrayList<Integer>();
        float thresh = (float) 0.3;
        int min_dis = 5;

        // RMSSD
        float temp = 0, temp1;
        for (int i = 0; i<Data.length-1;i++){
            temp1 = (float) Math.pow(Math.abs(Data[i+1]-Data[i]),2);
            temp = temp + temp1;
            if (Data[i]>=thresh) {
                Ps[i] = 1; locs.add(i);
            }
            else
                Ps[i] = 0;
        }

        float RMSSD = (float) Math.sqrt(temp/Data.length);

        // Criterial
        int count = 0;
        for (int i = 1; i<Data.length-1; i++){
            boolean bool1 = (Data[i-1]<0 && Data[i+1]>0);
            boolean bool2 = (Data[i-1]>0 && Data[i+1]<0);
            if (bool1 || bool2)
                count +=1;
        }
        float Criterial = (float) (count / Data.length);

        // No. of Peaks

        int fs=4000;//fs of signal
        int seg = (int) (fs*0.2/4);
        int ovlap = (int) (seg/4);
        int start = 0;
        int endl = seg;
        int no_of_peaks;
        ArrayList<Integer> window = new ArrayList<Integer>();
        for (int k = 0;k < (2*Data.length/seg-1);k++) {
            no_of_peaks=detect_peaks(Data,start,endl,0.3,5);
            if (no_of_peaks<=15)
                window.add(1);
            else
                window.add(0);
            start=endl-ovlap;
            endl=endl+ovlap;
        }
        int prcnt=0;
        for (int k=0;k<window.size();k++) {
            if (window.get(k)==1)
                prcnt=prcnt+1;
        }
        prcnt=prcnt/window.size();
        prcnt=prcnt*100;
        Toast.makeText(this, "RMSSD: "+String.valueOf(RMSSD)+"\nCriterial: "+String.valueOf(Criterial)
                +"\nPercent: "+String.valueOf(prcnt), Toast.LENGTH_LONG).show();
        if (RMSSD<= 0.14 && Criterial < 0.05  && prcnt >= 10 )
            Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "0", Toast.LENGTH_SHORT).show();
      //  Toast.makeText(this,String.valueOf(Wins1)+'\n'+String.valueOf(TWins), Toast.LENGTH_SHORT).show();

        progressDialog.hide();
    }

    public static int detect_peaks(float [] arrayIn,int start,int end,double mph,int mpd) {
        int peaksCounter=0;
        for (int i=start;i<=end-1;i++){
            if (arrayIn[i]>=mph){
                peaksCounter++;
            }
        }
        return peaksCounter;
    }
    public static int log2(int bits) {
        if (bits == 0) {
            return 0;
        }
        return 31 - Integer.numberOfLeadingZeros(bits);
    }
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
           File_Path = data.getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
           if(!thrd.isAlive()) {
               progressDialog = new ProgressDialog(MainActivity.this);
               progressDialog.setIndeterminate(true);
               progressDialog.setMessage("Plotting Signal...");
               progressDialog.show();
               H.postDelayed(thrd,500);
           }else {
               Toast.makeText(this, "Thread chal rha hai -.-", Toast.LENGTH_SHORT).show();
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

    private void Upload(){
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://lubdub-1a71d.appspot.com");

// Create a reference to "mountains.jpg"
        StorageReference WavRef = storageRef.child("test.wav");

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
                // Handle unsuccessful uploads
                Toast.makeText(MainActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(MainActivity.this, taskSnapshot.getMetadata().getName(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
