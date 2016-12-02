package com.uni.dpriho200.mobdev4;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Dwarfius on 11/26/2016.
 */

public class AlarmDialog extends AppCompatDialogFragment implements TimePicker.OnTimeChangedListener {
    private int hour, minutes;
    NoteDialogInterface listener;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final Bundle extraInfo = getArguments();
        final AlarmNote passedNote = extraInfo.getParcelable("Note");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_alarm, null);
        final EditText editText = (EditText)v.findViewById(R.id.editText);
        final DatePicker datePicker = (DatePicker)v.findViewById(R.id.datePicker);
        final TimePicker timePicker = (TimePicker)v.findViewById(R.id.timePicker);
        final Button doneBtn = (Button)v.findViewById(R.id.doneBtn);
        timePicker.setOnTimeChangedListener(this);

        if(passedNote != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(passedNote.getAlarmTime());
            if (Build.VERSION.SDK_INT >= 23) {
                timePicker.setHour(calendar.get(Calendar.HOUR));
                timePicker.setMinute(calendar.get(Calendar.MINUTE));
            } else {
                timePicker.setCurrentHour(calendar.get(Calendar.HOUR));
                timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
            }
            datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            editText.setText(passedNote.getNote());
        }
        builder.setView(v);

        builder.setTitle(passedNote == null ? "New Note" : "Update Note");
        builder.setPositiveButton(passedNote == null ? "Add" : "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        hour, minutes);
                Date alarmDate = calendar.getTime();
                String noteText = editText.getText().toString();

                AlarmNote note = extraInfo.getParcelable("Note");
                if(note == null) {
                    int classId = extraInfo.getInt("ClassId");
                    String userId = extraInfo.getString("UserId");
                    note = new AlarmNote(noteText, classId, userId, alarmDate);
                } else {
                    note.setNote(noteText);
                    note.setAlarmTime(alarmDate);
                }
                listener.onFinishedEditing(note);
            }
        });
        builder.setNeutralButton("Cancel", null);
        if(passedNote != null) {
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    listener.onDeleted(passedNote);
                }
            });
        }

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                v.requestFocus();
            }
        });
        // resizing is needed to fit everything on screen + hiding of the pickers
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) {
                    datePicker.setVisibility(View.GONE);
                    timePicker.setVisibility(View.GONE);
                    doneBtn.setVisibility(View.VISIBLE);
                } else {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.VISIBLE);
                    doneBtn.setVisibility(View.GONE);
                    // have to force keyboard hide even though edit text looses focus
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onTimeChanged(TimePicker picker, int hour, int minutes) {
        this.hour = hour;
        this.minutes = minutes;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (NoteDialogInterface)activity;
    }
}
