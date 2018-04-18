package com.fyp_lubdub;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;

public class Quality_Assessment {
    private float[] Data;
    private float RMSSD, Criterial, Percent;
    private Context con;
    float thresh = (float) 0.3;
    int min_dis = 5;

    Quality_Assessment(Context con, float[] Data){
        this.Data = Data;
        this.con = con;
    }

    protected int Process(){

        // RMSSD
        float temp = 0, temp1;
        for (int i = 0; i<Data.length-1;i++){
            temp1 = (float) Math.pow(Math.abs(Data[i+1]-Data[i]),2);
            temp = temp + temp1;
        }

        RMSSD = (float) Math.sqrt(temp/Data.length);

        if (RMSSD > 0.14) return 0;

        int count = 0;
        for (int i = 1; i<Data.length-1; i++){
            boolean bool1 = (Data[i-1]<0 && Data[i+1]>0);
            boolean bool2 = (Data[i-1]>0 && Data[i+1]<0);
            if (bool1 || bool2)
                count +=1;
        }
        Criterial = (float) (count / Data.length);

        if (Criterial >= 0.05 ) return 0;
        // No. of Peaks

        int fs=4000;//fs of signal
        int seg = (int) (fs*0.2/4);
        int ovlap =  (seg/4);
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
        for (int k=0;k<window.size();k++) {
            if (window.get(k)==1)
                Percent=Percent+1;
        }
        Percent=(Percent/window.size()) * 100;

        if (Percent <10) return 0;

        Toast.makeText(con, "RMSSD: "+String.valueOf(RMSSD)+"\nCriterial: "+String.valueOf(Criterial)
                +"\nPercent: "+String.valueOf(Percent), Toast.LENGTH_LONG).show();
        return 1;
    }

    private static int detect_peaks(float [] arrayIn,int start,int end,double mph,int mpd) {
        int peaksCounter=0;
        for (int i=start;i<=end-1;i++){
            if (arrayIn[i]>=mph){
                peaksCounter++;
            }
        }
        return peaksCounter;
    }
}
