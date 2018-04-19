package com.fyp_lubdub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class Hist_Adapter extends BaseExpandableListAdapter {
    private Context _context;
    private ArrayList<String> Date; // header titles
    // child data in format of header title, child title
    private ArrayList<String> Time;
    private Map<String,ArrayList<String>> Data;

    public Hist_Adapter(Context context,ArrayList<String> Date, Map<String,ArrayList<String>> Data){
        this._context = context;
        this.Date = Date;
        this.Data = Data;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        /*return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosititon);*/
        return this.Data.get(this.Date.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert infalInflater != null;
            convertView = infalInflater.inflate(R.layout.list_child, null);
        }

        TextView txtListChild = convertView.findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.Data.get(this.Date.get(groupPosition)).size();
        /*Toast.makeText(_context, String.valueOf(groupPosition), Toast.LENGTH_SHORT).show();
        return 0;*/
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.Data.get(this.Date.get(groupPosition));
    }

    @Override
    public int getGroupCount() {
        return this.Date.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = Date.get(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert infalInflater != null;
            convertView = infalInflater.inflate(R.layout.list_parent, null);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        //lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
