package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Sergio on 02/10/2017.
 * Delete this.
 */

public class RepeatingAlarmService extends Service {
    Alarm alarm = new Alarm();
    Context context;

    RepeatingAlarmService(){

    }

    public RepeatingAlarmService(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Sergio>", this + " onStartCommand\n" +
                "alarm set onstartcommad" + alarm);
        alarm.setAlarm(context);
        return START_STICKY;
    }

    // Used in lower API
    @Override
    public void onStart(Intent intent, int startId) {
        Log.i("Sergio>", this + " onStartCommand\n" +
                "alarm set onstartcommad" + alarm);
        alarm.setAlarm(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
