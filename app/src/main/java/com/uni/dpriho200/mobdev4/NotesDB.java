package com.uni.dpriho200.mobdev4;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dwarfius on 11/26/2016.
 */

class NotesDB extends SQLiteOpenHelper {
    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final int version = 1;
    private static final String tableName = "Notes";
    private static final String createStmt1 = "CREATE TABLE " + tableName +
            "(NoteId INTEGER PRIMARY KEY AUTOINCREMENT," +
            "UserId TEXT NOT NULL, " +
            "ClassId INTEGER NOT NULL, " +
            "Note TEXT," +
            "AlarmTime TEXT DEFAULT NULL);"; //sqlite doesn't have DATE as a storage type

    private static NotesDB instance = null;
    static void init(Context context) {
        if(instance == null)
            instance = new NotesDB(context.getApplicationContext());
    }

    private NotesDB(Context context) {
        super(context, tableName, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createStmt1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("CW", "onUpgrade NYI");
    }

    // The purpose of the helper, originally, is just to help with opening and upgrading of the db,
    // but I see no reason to spread out the db api over the app, so it'll be consolidated here.
    static void insert(Note note) {
        String alarmTime = null;
        if(note instanceof  AlarmNote)
        {
            DateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
            alarmTime = format.format(((AlarmNote) note).getAlarmTime());
        }

        ContentValues values = new ContentValues();
        values.put("UserId", note.getUserId());
        values.put("ClassId", note.getClassId());
        values.put("Note", note.getNote());
        values.put("AlarmTime", alarmTime);
        note.setId(instance.getWritableDatabase().insert(tableName, null, values));
    }

    static void update(Note note)
    {
        String alarmTime = null;
        if(note instanceof  AlarmNote)
        {
            DateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
            alarmTime = format.format(((AlarmNote) note).getAlarmTime());
        }

        ContentValues values = new ContentValues();
        values.put("UserId", note.getUserId());
        values.put("ClassId", note.getClassId());
        values.put("Note", note.getNote());
        values.put("AlarmTime", alarmTime);
        instance.getWritableDatabase().update(tableName, values, "NoteId = " + note.getId(), null);
    }

    static void delete(Note note) {
        instance.getWritableDatabase().delete(tableName, "NoteId = " + note.getId(), null);
    }

    static List<Note> select(int classId, String userId) {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = instance.getWritableDatabase().rawQuery("SELECT * FROM " + tableName +
                " WHERE ClassId = " + classId + " AND UserId = ?", new String[] { userId });
        DateFormat format = new SimpleDateFormat(dateFormat, Locale.US);
        while(cursor.moveToNext()) {
            Note note;
            long id = cursor.getLong(0);
            String noteText = cursor.getString(3);
            if(!cursor.isNull(4)) {
                String alarmStr = cursor.getString(4);
                Date alarmDate = new Date();
                // have to be safe, even though it can't happen
                try { alarmDate = format.parse(alarmStr); } catch (Exception e) { Log.e("CW", e.toString()); }
                note = new AlarmNote(noteText, classId, userId, alarmDate);
            }
            else
                note = new Note(noteText, classId, userId);

            note.setId(id);
            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    static int count(int classId, String userId)
    {
        Cursor cursor = instance.getWritableDatabase().rawQuery("SELECT COUNT(*) FROM " + tableName +
                " WHERE ClassId = " + classId + " AND UserId = ?", new String[] { userId });
        cursor.moveToNext();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
