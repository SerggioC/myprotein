package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Sergio on 04/10/2017.
 * Usado para criar o alarme quando o telefone reinicia.
 */
public class AutoStartAlarm extends BroadcastReceiver {
    Alarm alarm = new Alarm();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) || intent.getAction().equals(Intent.ACTION_REBOOT)) {
            alarm.setAlarm(context);
            Log.w("Sergio>", this + " class AutoStartAlarm extends BroadcastReceiver \n" +
                    "Setting alarm on received boot completed.");
        }
    }
}

