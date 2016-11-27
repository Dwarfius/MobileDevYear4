package com.uni.dpriho200.mobdev4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;

import com.kizitonwose.colorpreference.ColorDialog;
import com.kizitonwose.colorpreference.ColorShape;

import java.util.ArrayList;

public class CalendarView extends AppCompatActivity implements AdapterView.OnItemClickListener,
        ColorDialog.OnColorSelectedListener {
    String user;
    ArrayList<UniClass> classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        NotesDB.init(this);

        Intent intent = getIntent();
        classes = intent.getParcelableArrayListExtra("Classes");
        user = intent.getStringExtra("User");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Calendar View - " + user);

        CalendarAdapter adapter = CalendarAdapter.get(this, classes);

        ListView list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
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
                /*
                new ColorDialog.Builder(this)
                        .setColorShape(ColorShape.CIRCLE)
                        .setSelectedColor(Color.GREEN)
                        .setTag("TAG")
                        .show();*/
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

    // ColorDialog.OnColorSelectedListener
    @Override
    public void onColorSelected(int newColor, String tag) {
        switch (tag){
            case "TAG":
                //change the toolbar color with newColor
                break;
        }
    }
}
