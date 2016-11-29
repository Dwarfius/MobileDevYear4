package com.uni.dpriho200.mobdev4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;

import com.kizitonwose.colorpreference.ColorDialog;
import com.kizitonwose.colorpreference.ColorShape;

import java.util.ArrayList;

public class CalendarView extends AppCompatActivity implements AdapterView.OnItemClickListener {
    String user;
    CalendarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        NotesDB.init(this);

        Intent intent = getIntent();
        ArrayList<UniClass> classes = intent.getParcelableArrayListExtra("Classes");
        user = intent.getStringExtra("User");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Calendar View - " + user);

        adapter = CalendarAdapter.get(this, classes);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_calendar_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_settings:
                Intent prefsIntent = new Intent(this, PrefsActivity.class);
                startActivity(prefsIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, DetailView.class);
        intent.putExtra("Class", (UniClass)parent.getItemAtPosition(position));
        intent.putExtra("User", user);
        startActivity(intent);
    }
}
