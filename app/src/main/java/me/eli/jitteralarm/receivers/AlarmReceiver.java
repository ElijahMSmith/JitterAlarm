package me.eli.jitteralarm.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.GregorianCalendar;
import java.util.Random;

import me.eli.jitteralarm.R;
import me.eli.jitteralarm.MainActivity;
import me.eli.jitteralarm.utilities.AlarmInfo;
import me.eli.jitteralarm.utilities.DatabaseHelper;

public class AlarmReceiver extends BroadcastReceiver {

    private String name;
    private int requestCode;
    private Context context;
    private Uri randomSound;
    private int channelAddendum;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        name = intent.getStringExtra("name");
        requestCode = intent.getIntExtra("requestCode", -1);

        Random r = new Random();
        int randomNumber = 0;
        while(randomNumber == 0){
            randomNumber = r.nextInt(122);
        }

        channelAddendum = randomNumber;
        final int rawResourceID = context.getResources().getIdentifier("a" + randomNumber, "raw", context.getPackageName());
        SoundPool pool = getSoundPool();
        pool.load(context, rawResourceID, 0);

        randomSound = Uri.parse("android.resource://"
                + context.getPackageName() + "/"
                + rawResourceID);
        pool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                soundPool.play(rawResourceID, 1f, 1f, 0, 0,1f);
                playNoto();
                restartAlarm();
            }
        });
    }

    private SoundPool getSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .build();
        return new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    private void playNoto(){
        String channelName = "JitterAlarm Notification Channel";
        String channelDescription = "Notification channel for JitterAlarm";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        long[] vibrate = {250, 350, 250, 350, 750, 750};

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();

        String CHANNEL_ID = "JITTERALARM";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID + channelAddendum, channelName, importance);
        channel.setSound(randomSound, audioAttributes);
        channel.setDescription(channelDescription);
        channel.shouldVibrate();
        channel.setVibrationPattern(vibrate);

        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if(notificationManager != null)
            notificationManager.createNotificationChannel(channel);

        Intent startMain = new Intent(context, MainActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startMainPI = PendingIntent.getActivity(context, 0, startMain, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID + channelAddendum)
                .setContentTitle(name)
                .setContentText("Your alarm just went off!")
                .setSmallIcon(R.drawable.ic_alarm)
                .setLights(Color.GREEN, 500, 2000)
                .setAutoCancel(true)
                .setContentIntent(startMainPI)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if(notificationManager != null) //Compiler demands we account for it possibly being null
            notificationManager.notify(1, builder.build());
    }

    //Finds the alarm that just ran in the database, generate the next date to run, then reset alarm here.
    public void restartAlarm(){
        DatabaseHelper helper = new DatabaseHelper(context);
        AlarmInfo triggeredAlarm = helper.getAlarmInfo(name);

        //If we don't find the alarm, let me know (only helpful for testing) then breaks out of method
        if(triggeredAlarm == null){
            Log.d("test", "Couldn't reset alarm " + name + " because it was not found in the database!");
            return;
        }

        GregorianCalendar nextDate = triggeredAlarm.generateTriggerDate();
        triggeredAlarm.setNextTriggerDate(nextDate);

        //Creates new offset alarm structure
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("name", name); //All the identifying information we need. Receiver can look up alarm to reset it.
        alarmIntent.putExtra("requestCode", requestCode); //All the identifying information we need. Receiver can look up alarm to reset it.

        //This request code is guaranteed to be unique for each alarm, no alarms will override each other
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextDate.getTimeInMillis(), pendingIntent); //Set alarm to run at exact time of next trigger date

        //Log successfully set alarms for testing purposes
        Log.d("test", "Restarted alarm '" + triggeredAlarm.toString() + "', will next trigger at " + triggeredAlarm.getNextTriggerDate());
    }
}