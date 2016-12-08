package com.uni.dpriho200.mobdev4;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Daniel Prihodko, S1338994 on 11/14/2016.
 */

class UniClass implements Parcelable {
    private static final String dayFormat = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String groupFormat = "dd-MMM-yyyy";

    private static final String[] locPrefixes = {
        "CEE",
        "MS",
        "W",
        "M",
        "A",
        "H",
        "B",
        "C"
    };

    private static final LatLng[] locations = {
        new LatLng(55.866109, -4.250205),
        new LatLng(55.868481, -4.250827),
        new LatLng(55.867196, -4.251079),
        new LatLng(55.866946, -4.249979),
        new LatLng(55.866738, -4.249142),
        new LatLng(55.866268, -4.250832),
        new LatLng(55.866409, -4.251615),
        new LatLng(55.867664, -4.249212),
    };
    private static final LatLng defaultLoc = new LatLng(55.866500, -4.250382); //uni location

    private String room, description, type;
    private Date start, end;
    private int id;
    private LatLng location = defaultLoc;
    private int notesCount = 0;

    UniClass(JSONObject json) throws JSONException {
        parseDescription(json.getString("text"));
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

    // also sets the location
    private void parseDescription(String desc) {
        desc = desc.replaceAll("(</?b>)", "");
        String[] lines = desc.split("(<br>)");

        // first have to check if it's a global event or not -
        if(lines[0].equals("Global Event")) {
            type = lines[0];
            room = lines[1]; //there might be a room or not
            description = type + "\n" + (room.isEmpty() ? room + "\n" : "") + lines[3];
        } else {
            // first figuring out what are the coords of the building to go to for class
            room = lines[2];
            for (int i = 0; i < locPrefixes.length; i++) {
                if (room.startsWith(locPrefixes[i])) {
                    location = locations[i];
                    break;
                }
            }

            // description follows a specific format: Module\nCourse Codes\nRoom\nLecturer\nTime\Type
            // we're gonna keep only the things we need: Module, Room, Time, Type
            type = lines[5].trim();
            description = lines[0] + "\n" + room + "\n" + type + ", " + lines[4];
        }
    }

    @Override
    public String toString() {
        return description;
    }

    int getId() { return id; }
    Date getStart() { return start; }
    LatLng getLocation() { return location; }
    String getRoom() { return room; }
    String getType() { return type; }
    int getNotesCount() { return notesCount; }

    void setNotesCount(int val) { notesCount = val; }

    int compareTo(UniClass other)
    {
        return start.compareTo(other.getStart());
    }

    // Parcelable implementation
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
        dest.writeDoubleArray(new double[] { location.latitude, location.longitude });
        dest.writeString(room);
        dest.writeString(description);
        dest.writeString(type);
        dest.writeInt(id);
        dest.writeInt(notesCount);

        DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
        dest.writeString(formatter.format(start));
        dest.writeString(formatter.format(end));
    }

    private UniClass(Parcel in) {
        double[] coords = in.createDoubleArray();
        location = new LatLng(coords[0], coords[1]);
        room = in.readString();
        description = in.readString();
        type = in.readString();
        id = in.readInt();
        notesCount = in.readInt();
        try {
            DateFormat formatter = new SimpleDateFormat(dayFormat, Locale.US);
            start = formatter.parse(in.readString());
            end = formatter.parse(in.readString());
        } catch(Exception e) { Log.e("CW", e.toString()); }
    }
}
