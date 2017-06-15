package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by Sergio on 04/06/2017.
 * Firebase JobDispatcher
 * https://github.com/firebase/firebase-jobdispatcher-android
 */

public class StartFirebase {
    static int START_INTERVAL = 0;
    static int DELTA_INTERVAL; // em segundos
    static int DEFAULT_DELTA_INTERVAL = 3 * 60 * 60; // em segundos

    public static void createJobDispatcher(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        DELTA_INTERVAL = Integer.parseInt(sharedPrefs.getString("sync_frequency", String.valueOf(DEFAULT_DELTA_INTERVAL)));
        if (DELTA_INTERVAL < 0) DELTA_INTERVAL = DEFAULT_DELTA_INTERVAL;
        if (START_INTERVAL < 0) START_INTERVAL = 0;
        START_INTERVAL = 0;
        DELTA_INTERVAL = 1800; // 30 minutos

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(FirebaseJobservice.class) // the JobService that will be called
                .setTag("update_prices") // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER) // persist past a device reboot
                .setTrigger(Trigger.executionWindow(START_INTERVAL, START_INTERVAL + DELTA_INTERVAL)) // start between START_INTERVAL and START + DELTA seconds from now
                .setReplaceCurrent(true) // overwrite an existing job with the same tag
                .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR) // retry with exponential backoff
                .setConstraints(Constraint.ON_ANY_NETWORK) // constraints that need to be satisfied for the job to run
                .build();
        dispatcher.mustSchedule(myJob);
    }
}
