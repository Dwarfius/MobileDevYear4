package com.uni.dpriho200.mobdev4;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
 * Created by Daniel Prihodko, S1338994 on 11/14/2016.
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
        DateFormat dayFormat = new SimpleDateFormat("EEEE, d", Locale.US);
        for(UniClass uniClass : items)
        {
            Date start = uniClass.getStart();
            calendar.setTime(start);

            int week = calendar.get(Calendar.WEEK_OF_YEAR); // need week_of_year to avoid split on month transition
            if(week != currWeek) // if new week - add week header
            {
                processedItems.add(new WeekRow(groupFormatter.format(start)));
                currWeek = week;
            }

            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if(day != currDay) // if new day - add day header
            {
                String label = dayFormat.format(start) + getDaySuffix(calendar.get(Calendar.DAY_OF_MONTH));
                processedItems.add(new DayRow(label));
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

            int color = getColorFromPrefs(prefs, "WeekBg", R.color.weekHeader);
            view.setBackgroundColor(color);
            color = getColorFromPrefs(prefs, "WeekTextColor", R.color.White);
            header.setTextColor(color);
        }
        else if(rowType == DayRow.ItemType)
        {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row_day, parent, false);
            }
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(((DayRow)item).day);

            int color = getColorFromPrefs(prefs, "DayBg", R.color.dayHeader);
            view.setBackgroundColor(color);
            color = getColorFromPrefs(prefs, "DayTextColor", R.color.White);
            header.setTextColor(color);
        }
        else
        {
            if(view == null) {
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.row_class, parent, false);
            }
            UniClass uniClass = (UniClass)item;
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(uniClass.toString());

            String type = uniClass.getType();
            int textColor = getColorFromPrefs(prefs, type + "TextColor", R.color.Black);

            View container = view.findViewById(R.id.notesContainer);
            if(uniClass.getNotesCount() > 0) {
                container.setVisibility(View.VISIBLE);
                TextView notesCountLbl = (TextView)view.findViewById(R.id.notesCount);
                notesCountLbl.setText(Integer.toString(uniClass.getNotesCount()));

                notesCountLbl.setTextColor(textColor);
                ((ImageView)view.findViewById(R.id.notesIcon)).setColorFilter(textColor);
            } else
                container.setVisibility(View.GONE);

            int color = getColorFromPrefs(prefs, type + "Color", R.color.White);
            view.setBackgroundColor(color);
            header.setTextColor(textColor);
        }
        return view;
    }

    // utility methods
    private int getColorFromPrefs(SharedPreferences prefs, String prefId, @ColorRes int defaultColorId) {
        int color = prefs.getInt(prefId, Integer.MAX_VALUE);
        if(color == Integer.MAX_VALUE) {
            if(Build.VERSION.SDK_INT >= 23)
                color = getContext().getResources().getColor(defaultColorId, null);
            else
                color = getContext().getResources().getColor(defaultColorId);
        }
        return color;
    }

    private static String getDaySuffix(int day) {
        switch (day % 10){
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    UniClass findById(int id) {
        for(Object o: items) {
            if(o instanceof UniClass && ((UniClass) o).getId() == id)
                return (UniClass) o;
        }
        return null;
    }
}
