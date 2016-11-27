package com.uni.dpriho200.mobdev4;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

/**
 * Created by Dwarfius on 11/26/2016.
 */

interface NoteDialogInterface {
    void onFinishedEditing(Note note);
}

public class NoteDialog extends AppCompatDialogFragment {
    NoteDialogInterface listener;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        final Bundle extraInfo = getArguments();
        Note passedNote = extraInfo.getParcelable("Note");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_note, null);
        final EditText editText = (EditText)v.findViewById(R.id.editText);
        if(passedNote != null)
            editText.setText(passedNote.getNote());
        builder.setView(v);

        builder.setTitle(passedNote == null ? "New Note" : "Update Note");
        builder.setPositiveButton(passedNote == null ? "Add" : "Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String noteText = editText.getText().toString();
                Note note = extraInfo.getParcelable("Note");
                if(note == null) {
                    int classId = extraInfo.getInt("ClassId");
                    String userId = extraInfo.getString("UserId");
                    note = new Note(noteText, classId, userId);
                }
                else
                    note.setNote(noteText);
                listener.onFinishedEditing(note);
            }
        });
        builder.setNegativeButton("Cancel", null);

        // Dialog windows usually don't move on keyboard reveal, changing this here
        Dialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        listener = (NoteDialogInterface)activity;
    }
}
