package com.uni.dpriho200.mobdev4;

import android.content.Context;
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
        String week;
        WeekRow(String week) { this.week = week; }
    }

    private static class DayRow {
        String day;
        DayRow(String day) { this.day = day; }
    }

    private final Context context;
    private final List<Object> items;

    private CalendarAdapter(Context context, List<Object> items) {
        super(context, -1, items);
        this.context = context;
        this.items = items;
    }

    static CalendarAdapter get(Context context, ArrayList<UniClass> items)
    {
        //first of all, sorting our data
        Collections.sort(items, new Comparator<UniClass>() {
            @Override
            public int compare(UniClass o1, UniClass o2) {
                return o1.compareTo(o2);
            }
        });

        ArrayList<Object> processedItems = new ArrayList<Object>();
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
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return items.get(position) instanceof UniClass;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;
        Object item = items.get(position);
        if(item instanceof WeekRow)
        {
            view = inflater.inflate(R.layout.week_row, parent, false);
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(((WeekRow)item).week);
        }
        else if(item instanceof DayRow)
        {
            view = inflater.inflate(R.layout.day_row, parent, false);
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(((DayRow)item).day);
        }
        else
        {
            view = inflater.inflate(R.layout.class_row, parent, false);
            TextView header = (TextView)view.findViewById(R.id.header);
            header.setText(item.toString());
        }
        return view;
    }
}
