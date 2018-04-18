package com.fyp_lubdub;

import java.util.ArrayList;

public class Hist_Date implements Parent<Hist_Time>{

    // a recipe contains several ingredients
    private ArrayList<String> time;
    private String date;
    public Hist_Date(String date, ArrayList<String> time) {
        this.time = time;
        this.date = date;
    }

    @Override
    public ArrayList<String> getChildList() {
        return time;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
