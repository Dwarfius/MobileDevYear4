package com.uni.dpriho200.mobdev4;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dwarfius on 11/14/2016.
 */

class UniClass implements Parcelable {
    private static final String dayFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String groupFormat = "dd-MMM-yyyy";

    private String description;
    private Date start, end;
    private int id;

    UniClass(JSONObject json) throws JSONException
    {
        description = parseDescription(json.getString("text"));
        id = Integer.parseInt(json.getString("id"));

        // The way they store the start-end data is a bit messed up
        // Group points to the actual week, while Start/End point to the
        // day of the week and time. In order to get the actual Start/End
        // it needs to be "reformatted" to Week(Group) + dayOffset(Start) + time(Start/End)
        DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
        DateFormat groupFormatter = new SimpleDateFormat(groupFormat, Locale.US);
        try {
            // first, figuring the day offset in the week
            Calendar cal = Calendar.getInstance();
            start = formatter.parse(json.getString("start"));
            cal.setTime(start);
            int dayOffset = cal.get(Calendar.DAY_OF_WEEK); //assuming it is 0based
            int hourOffset = cal.get(Calendar.HOUR_OF_DAY);

            // adjusting the start day
            Date group = groupFormatter.parse(json.getString("resource"));
            cal.setTime(group);
            cal.set(Calendar.DAY_OF_WEEK, dayOffset);
            cal.set(Calendar.HOUR_OF_DAY, hourOffset);
            start = cal.getTime();

            // now, exactly the same for end
            end = formatter.parse(json.getString("end"));
            cal.setTime(end);
            dayOffset = cal.get(Calendar.DAY_OF_WEEK); //assuming it is 0based
            hourOffset = cal.get(Calendar.HOUR_OF_DAY);

            cal.setTime(group);
            cal.set(Calendar.DAY_OF_WEEK, dayOffset);
            cal.set(Calendar.HOUR_OF_DAY, hourOffset);
            end = cal.getTime();
        } catch (ParseException e) {
            Log.e("CW", e.toString());
        }
    }

    private String parseDescription(String desc)
    {
        desc = desc.replaceAll("(</?b>)", "");
        String[] lines = desc.split("(<br>)");
        // description follows a specific format: Module\nCourse Codes\nRoom\nLecturer\nTime\Type
        // we're gonna keep only the things we need: Module, Room, Time, Type
        desc = lines[0] + "\n" + lines[2] + "\n" + lines[5] + " " + lines[4];
        return desc;
    }

    @Override
    public String toString() {
        return description;
    }

    // Parcelable implementation
    // Creator
    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public UniClass createFromParcel(Parcel in) {
            return new UniClass(in);
        }

        public UniClass[] newArray(int size) {
            return new UniClass[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeInt(id);

        DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
        dest.writeString(formatter.format(start));
        dest.writeString(formatter.format(end));
    }

    // "De-parcel object
    UniClass(Parcel in) {
        description = in.readString();
        id = in.readInt();
        try {
            DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
            start = formatter.parse(in.readString());
            end = formatter.parse(in.readString());
        } catch(Exception e) { Log.e("CW", e.toString()); }
    }

    Date getStart() {
        return start;
    }

    int compareTo(UniClass other)
    {
        return start.compareTo(other.getStart());
    }
}
