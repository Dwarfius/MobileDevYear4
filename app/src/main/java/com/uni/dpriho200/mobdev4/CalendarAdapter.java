package com.uni.dpriho200.mobdev4;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dwarfius on 11/14/2016.
 */

class CalendarAdapter extends ArrayAdapter<Object> {
    //helper classes to build up the list in a fancy way
    private static class WeekRow {
        static final int ItemType = 0;
        String week;
        WeekRow(String week) { this.week = week; }
    }

    private static class DayRow {
        static final int ItemType = 1;
        String day;
        DayRow(String day) { this.day = day; }
    }
    private static final int ClassItemType = 2;

    private final Context context;
    private final List<Object> items;

    private CalendarAdapter(Context context, List<Object> items) {
        super(context, -1, items);
        this.context = context;
        this.items = items;
    }

    static CalendarAdapter get(Context context, List<UniClass> items)
    {
        //first of all, sorting our data
        Collections.sort(items, new Comparator<UniClass>() {
            @Override
            public int compare(UniClass o1, UniClass o2) {
                return o1.compareTo(o2);
            }
        });

        List<Object> processedItems = new ArrayList<>();
        int currWeek = -1;
        int currDay = -1;
        Calendar calendar = Calendar.getInstance();
        DateFormat groupFormatter = new SimpleDateFormat("d MMMM, yyyy", Locale.US);
        DateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);
        for(UniClass uniClass : items)
        {
            Date start = uniClass.getStart();
            calendar.setTime(start);

            int week = calendar.get(Calendar.WEEK_OF_YEAR); //need of_year to avoid split on month transition
            if(week != currWeek)
            {
                processedItems.add(new WeekRow(groupFormatter.format(start)));
                currWeek = week;
            }

            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if(day != currDay)
            {
                processedItems.add(new DayRow(dayFormat.format(start)));
                currDay = day;
            }
            processedItems.add(uniClass);
        }
        return new CalendarAdapter(context, processedItems);
    }

    @Override
    public int getViewTypeCount() { return 3; } //week, day and class types

    // gonna help out the framework with deciding what views to forward through convertView
    @Override
    public int getItemViewType(int pos) {
        Object item = items.get(pos);
        if(item instanceof WeekRow)
            return WeekRow.ItemType;
        else if(item instanceof DayRow)
            return DayRow.ItemType;
        else
            return ClassItemType;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return items.get(position) instanceof UniClass;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Object item = items.get(position);
        View view = convertView;
        int rowType = getItemViewType(position);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(rowType == WeekRow.ItemType)
        {
            //avoiding unnecessary queries if we can, if not - then create new objects
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row_week, parent, false);
            }
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(((WeekRow)item).week);
            int color = prefs.getInt("WeekBg", -1);
            if(color == -1) {
                if(Build.VERSION.SDK_INT >= 23)
                    color = getContext().getResources().getColor(R.color.weekHeader, null);
                else
                    color = getContext().getResources().getColor(R.color.weekHeader);
            }
            view.setBackgroundColor(color);
        }
        else if(rowType == DayRow.ItemType)
        {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.day_row, parent, false);
            }
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(((DayRow)item).day);
            int color = prefs.getInt("DayBg", -1);
            if(color == -1) {
                if(Build.VERSION.SDK_INT >= 23)
                    color = getContext().getResources().getColor(R.color.dayHeader, null);
                else
                    color = getContext().getResources().getColor(R.color.dayHeader);
            }
            view.setBackgroundColor(color);
        }
        else
        {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.class_row, parent, false);
            }
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(item.toString());
            String type = ((UniClass)item).getType();
            int color = prefs.getInt(type + "Color", -1);
            if(color == -1) {
                if(Build.VERSION.SDK_INT >= 23)
                    color = getContext().getResources().getColor(R.color.White, null);
                else
                    color = getContext().getResources().getColor(R.color.White);
            }
            view.setBackgroundColor(color);
        }
        return view;
    }
}
