package com.uni.dpriho200.mobdev4;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Daniel Prihodko, S1338994 on 11/22/2016.
 */

class Note implements Parcelable {
    private long id = -1;
    private String note;
    private int classId;
    private String userId;

    Note(String note, int classId, String userId) {
        this.note = note;
        this.classId = classId;
        this.userId = userId;
    }

    long getId() { return id; }
    void setId(long val) { id = val; }

    String getNote() { return note; }
    void setNote(String val) { note = val; }

    int getClassId() { return classId; }

    String getUserId() { return  userId; }

    // Parcelable implementation
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Note createFromParcel(Parcel in) { return new Note(in); }

        public Note[] newArray(int size) { return new Note[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(note);
        dest.writeInt(classId);
        dest.writeString(userId);
    }

    protected Note(Parcel in) {
        id = in.readLong();
        note = in.readString();
        classId = in.readInt();
        userId = in.readString();
    }
}

class AlarmNote extends Note {
    private static final String dayFormat = "yyyy-MM-dd'T'HH:mm";
    private Date alarmTime;

    AlarmNote(String note, int classId, String userId, Date alarmTime)
    {
        super(note, classId, userId);
        this.alarmTime = alarmTime;
    }

    Date getAlarmTime() { return alarmTime; }
    void setAlarmTime(Date val) { alarmTime = val; }

    // Parcelable implementation
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public AlarmNote createFromParcel(Parcel in) {
            return new AlarmNote(in);
        }

        public AlarmNote[] newArray(int size) {
            return new AlarmNote[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        DateFormat format = new SimpleDateFormat(dayFormat, Locale.US);
        dest.writeString(format.format(alarmTime));
    }

    private AlarmNote(Parcel in) {
        super(in);
        DateFormat format = new SimpleDateFormat(dayFormat, Locale.US);
        try {
            alarmTime = format.parse(in.readString());
        } catch(Exception e) { Log.e("CW", e.toString()); }
    }
}
