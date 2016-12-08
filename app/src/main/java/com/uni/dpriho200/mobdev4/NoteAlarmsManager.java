package com.uni.dpriho200.mobdev4;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by Daniel Prihodko, S1338994 on 12/1/2016.
 */

public class NoteAlarmsManager extends BroadcastReceiver {

    static void createAlarm(AlarmNote note, Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent notifIntent = new Intent("com.uni.dpriho200.mobdev4.DISPLAY_NOTIFICATION");
        notifIntent.addCategory("android.intent.category.DEFAULT");
        notifIntent.putExtra("Note", note);

        // using the unique id of the note for tracking it later
        PendingIntent broadcast = PendingIntent.getBroadcast(context, (int)note.getId(), notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar cal = Calendar.getInstance();
        cal.setTime(note.getAlarmTime());
        // android present multiple ways to schedule alarms, mainly differing in precision
        // trying to use the most accurate one
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);

        Log.i("CW", "Scheduled notif " + note.getId());
    }

    static void cancelAlarm(AlarmNote note, Context context) {
        // to cancel an alarm it must be exactly same Intents (see Intent#filterEquals(Intent))
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent("android.media.action.DISPLAY_NOTIFICATION");
        notificationIntent.addCategory("android.intent.category.DEFAULT");
        PendingIntent broadcast = PendingIntent.getBroadcast(context, (int)note.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(broadcast);

        Log.i("CW", "Cancelled notif " + note.getId());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // getting the note that was scheduled
        AlarmNote note = intent.getParcelableExtra("Note");
        Log.i("CW", "Received notif " + note.getId());

        // constructing what to display on click of the notification
        // and passing the data
        Intent notificationIntent = new Intent(context, LogIn.class);
        notificationIntent.putExtra("AlarmTriggerNote", note);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // we need to provide an intent with a proper backstack - constructing it
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // creating the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        String message = note.getUserId() + ": " + note.getNote();
        Notification notification = builder.setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("GCU Pal Alarm!")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.icon_alarm)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent).build();

        // making it appear in the notifications
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }
}
