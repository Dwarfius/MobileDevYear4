package com.uni.dpriho200.mobdev4;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dwarfius on 11/14/2016.
 */

class UniClass implements Parcelable {
    static final String dayFormat = "yyyy-MM-dd'T'HH:mm:ss";
    static final String groupFormat = "dd-MMM-yyyy";

    private String description;
    private Date start, end;
    private Date group;
    private int id;

    UniClass(JSONObject json) throws JSONException
    {
        description = json.getString("text");
        id = Integer.parseInt(json.getString("id"));
        DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
        DateFormat groupFormatter = new SimpleDateFormat(groupFormat, Locale.US);
        try {
            group = groupFormatter.parse(json.getString("resource"));
            start = formatter.parse(json.getString("start"));
            end = formatter.parse(json.getString("end"));
        } catch (ParseException e) {
            Log.e("CW", e.toString());
        }
    }

    @Override
    public String toString() {
        DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
        return description  + " from " + formatter.format(start) + " to " + formatter.format(end);
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

        DateFormat formatter = new SimpleDateFormat(groupFormat, Locale.US);
        dest.writeString(formatter.format(group));

        formatter = new SimpleDateFormat(dayFormat, Locale.US);
        dest.writeString(formatter.format(start));
        dest.writeString(formatter.format(end));
    }

    // "De-parcel object
    UniClass(Parcel in) {
        description = in.readString();
        id = in.readInt();
        try {
            DateFormat formatter = new SimpleDateFormat(groupFormat, Locale.US);
            group = formatter.parse(in.readString());

            formatter = new SimpleDateFormat(dayFormat, Locale.US);
            start = formatter.parse(in.readString());
            end = formatter.parse(in.readString());
        } catch(Exception e) { Log.e("CW", e.toString()); }
    }

    Date getStart() {
        return start;
    }

    Date getGroup() {
        return group;
    }

    int compareTo(UniClass other)
    {
        return group.compareTo(other.getGroup()) * 5 + start.compareTo(other.getStart());
    }
}
