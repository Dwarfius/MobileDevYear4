package com.uni.dpriho200.mobdev4;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Dwarfius on 11/22/2016.
 */

class NotesAdapter extends ArrayAdapter<Note> {
    private final Context context;
    private final List<Note> notes;

    NotesAdapter(Context context, List<Note> notes)
    {
        super(context, -1, notes);
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.note_row, parent, false);
        }
        Note item = notes.get(position);
        boolean isAlarm = item instanceof AlarmNote;
        ImageView imgView = (ImageView)view.findViewById(R.id.rowTypeImage);
        imgView.setImageResource(isAlarm ? R.drawable.icon_alarm : R.drawable.icon_note);
        TextView titleView = (TextView)view.findViewById(R.id.title);
        titleView.setText(item.getTitle());
        return view;
    }
}
