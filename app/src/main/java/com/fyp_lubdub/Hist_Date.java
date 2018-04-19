package com.fyp_lubdub;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.ArrayList;
import java.util.List;

public class Hist_Date implements Parent<String> {

    // a recipe contains several ingredients
    private ArrayList<String> time;
    private String date;
    public Hist_Date(String date, ArrayList<String> time) {
        this.time = time;
        this.date = date;
    }

    @Override
    public List<String> getChildList() {
        return time;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
