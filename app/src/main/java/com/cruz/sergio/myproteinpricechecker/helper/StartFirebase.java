package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    static int START_INTERVAL;
    static int DEFAULT_START_INTERVAL = 3 * 60 * 60; // 3hr em segundos
    static int DELTA_INTERVAL = 10 * 60; // 10 minutos em segundos

    public static void createJobDispatcher(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        START_INTERVAL = Integer.parseInt(sharedPrefs.getString("sync_frequency", String.valueOf(DEFAULT_START_INTERVAL)));
        if (START_INTERVAL < 0) START_INTERVAL = 0;

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value"); // unused

        Job myJob = dispatcher.newJobBuilder()
                .setService(FirebaseJobservice.class) // the JobService that will be called
                .setTag("Sergio_tag-update_prices") // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER) // persist past a device reboot
                .setTrigger(Trigger.executionWindow(START_INTERVAL, START_INTERVAL + DELTA_INTERVAL)) // start between START_INTERVAL and START + DELTA seconds from now
                .setReplaceCurrent(true) // overwrite an existing job with the same tag
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL) // retry with exponential backoff
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_ANY_NETWORK
                        // only run when the device is charging
                        // Constraint.DEVICE_CHARGING,
                )
                .setExtras(myExtrasBundle)
                .build();
        dispatcher.mustSchedule(myJob);
    }
}
