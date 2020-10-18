package me.eli.jitteralarm.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.GregorianCalendar;
import java.util.Objects;

import me.eli.jitteralarm.R;
import me.eli.jitteralarm.MainActivity;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.DatabaseHelper;

/**
 * Created by Eli on 1/7/2016.
 */
public class BootUpReceiver extends BroadcastReceiver {

    private Context context;
    private DatabaseHelper helper;
    private SharedPreferences sp;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        //Only continue for boot up intents
        if(!Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED))
            return;

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        helper = new DatabaseHelper(context);
        for(AlarmInfo alarm : helper.getAllAlarms()){
            //Alarm already should have gone off, play missed alarm notification then reset alarm
            if(System.currentTimeMillis() > alarm.getCalendarFromNextTriggerDate().getTimeInMillis()){
                playExpiredAlarmNotification(alarm);
                restartAlarm(alarm, true);
            //Alarm hasn't missed it's trigger, set it for established trigger time
            } else {
                restartAlarm(alarm, false);
            }
        }
    }

    private void playExpiredAlarmNotification(AlarmInfo alarm){
        String CHANNEL_ID = "JITTERALARM_MISSED_ALARM";

        String channelName = "Notification Channel for Missed Alarms";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.shouldVibrate();

        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if(notificationManager != null)
            notificationManager.createNotificationChannel(channel);

        Intent startMain = new Intent(context, MainActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startMainPI = PendingIntent.getActivity(context, 0, startMain, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("You missed your \"" + alarm.getAlarmName() + "\" alarm!")
                .setContentText("This alarm was suppose to trigger at " + alarm.getNextTriggerDate())
                .setSmallIcon(R.drawable.ic_alarm)
                .setLights(Color.GREEN, 500, 2000)
                .setAutoCancel(true)
                .setContentIntent(startMainPI)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if(notificationManager != null)
            notificationManager.notify(1, builder.build());
    }

    //Set alarm to either current nextTriggerTime if not already passed, or a newly generated nextTriggerTime if the time HAS already passed
    //Expired is true if the time this alarm was suppose to run already passed on a previous day, false otherwise
    public void restartAlarm(AlarmInfo alarmToSet, boolean expired){
        if(expired){ //If the time for this alarm has already passed (notification sent already), generate next trigger date and set it
            GregorianCalendar nextDate = alarmToSet.generateTriggerDate(true);
            alarmToSet.setNextTriggerDate(nextDate);
            helper.updateNextTriggerDate(alarmToSet);
        }

        //Schedule this alarm to trigger AlarmReceiver with correct name/requestCode information
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("name", alarmToSet.getAlarmName());
        int requestCode = sp.getInt(alarmToSet.getAlarmName(), -1);
        alarmIntent.putExtra("requestCode", requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmToSet.getCalendarFromNextTriggerDate().getTimeInMillis(), pendingIntent); //Set alarm to run at exact time of next trigger date

        //Log successfully set alarms for testing purposes
        Log.d("test", "-------------------------------------------");
        Log.d("test", "Restarted alarm '" + alarmToSet.toString() + "' with request code '" + requestCode + "', will next trigger at " + alarmToSet.getNextTriggerDate());
        Log.d("test", "-------------------------------------------");
    }
}