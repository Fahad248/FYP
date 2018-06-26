package com.fyp_lubdub;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

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

import java.io.File;
import java.io.IOException;

public class PlotPlay extends AppCompatActivity {

    String filepath;
    LineChart graph;
    Button front, back;
    SeekBar Seek;
    LineData data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_play);

        graph = findViewById(R.id.graph);
        front = findViewById(R.id.front);
        back = findViewById(R.id.back);

        filepath = getIntent().getStringExtra("Path");

        new PlotPlay.ProgressTask(PlotPlay.this).execute();
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean> {
        private Dialog Dia;
        private Context context;
        private TextView msg;
        private ProgressBar prog;
        private float[] DataBuffer;

        public ProgressTask(Context activity) {
            context = activity;
            this.Dia = new Dialog(context);
            this.Dia.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.Dia.setCancelable(false);
            this.Dia.setContentView(R.layout.dialog_layout);
        }


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
        }

        protected Boolean doInBackground(final String... args) {
            try{

                WavFile wavFile = null;
                /// graph.setVisibility(INVISIBLE);
                GraphAxis();
                try {
                    // String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testwave.wav";
                    File mFile = new File(filepath);
                    try {
                        wavFile = WavFile.openWavFile(mFile);
                        //Toast.makeText(Main2Activity.this, String.valueOf(wavFile.getNumFrames()), Toast.LENGTH_SHORT).show();
                    } catch (IOException | WavFile.WavFileException e) {
                        e.printStackTrace();
                    }
                    int bufSize = (int) wavFile.getNumFrames();

                    // temp2 = bufSize/temp;
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
}
