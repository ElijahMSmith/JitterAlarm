package me.eli.jitteralarm.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.eli.jitteralarm.Temp;

/**
 * Created by Eli on 1/19/2016.
 */
public class StartMainReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*Intent i = new Intent(context, Temp.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("FROM_BOOTUP", true);
        context.startActivity(i);*/
    }
}