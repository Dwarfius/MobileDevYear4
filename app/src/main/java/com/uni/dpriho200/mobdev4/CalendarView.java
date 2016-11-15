package com.uni.dpriho200.mobdev4;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class CalendarView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        Intent intent = getIntent();
        ArrayList<UniClass> classes = intent.getParcelableArrayListExtra("Classes");
        CalendarAdapter adapter = CalendarAdapter.get(this, classes);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }
}
