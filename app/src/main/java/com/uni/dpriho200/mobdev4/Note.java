package com.uni.dpriho200.mobdev4;

import java.util.Date;

/**
 * Created by Dwarfius on 11/22/2016.
 */

class Note {
    protected String title, note;

    Note(String title, String note)
    {
        this.title = title;
        this.note = note;
    }

    String getTitle() { return title; }
    void setTitle(String val) { title = val; }

    String getNote() { return note; }
    void setNote(String val) { note = val; }
}

class AlarmNote extends Note {
    private Date alarmTime;

    AlarmNote(String title, String note, Date alarmTime)
    {
        super(title, note);
        this.alarmTime = alarmTime;
    }

    Date getAlarmTime() { return alarmTime; }
    void setAlarmTime(Date val) { alarmTime = val; }
}
