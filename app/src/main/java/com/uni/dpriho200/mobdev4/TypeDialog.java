package com.uni.dpriho200.mobdev4;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

/**
 * Created by Daniel Prihodko, S1338994 on 11/26/2016.
 */

public class TypeDialog extends AppCompatDialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("New Note - Pick A Type");
        builder.setItems(new String[]{"Simple", "Alarm"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AppCompatDialogFragment dialog;
                if(i == 0)
                    dialog = new NoteDialog();
                else
                    dialog = new AlarmDialog();
                dialog.setArguments(getArguments());
                dialog.show(getFragmentManager(), "Note");
            }
        });
        return builder.create();
    }
}
