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

import java.util.concurrent.TimeUnit;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.DEFAULT_START_INTERVAL;

/**
 * Created by Sergio on 04/06/2017.
 */

public class StartFirebase {

    public static void createJobDispatcher(Context context, int START_INTERVAL, int END_INTERVAL) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        END_INTERVAL = Integer.parseInt(sharedPrefs.getString("sync_frequency", String.valueOf(TimeUnit.MINUTES.toSeconds(DEFAULT_START_INTERVAL))));
        END_INTERVAL = 10;

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));

        Bundle myExtrasBundle = new Bundle();
        myExtrasBundle.putString("some_key", "some_value");

        Job myJob = dispatcher.newJobBuilder()
                .setService(FirebaseJobservice.class) // the JobService that will be called
                .setTag("Sergio_tag-update_prices") // uniquely identifies the job
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER) // persist past a device reboot
                .setTrigger(Trigger.executionWindow(START_INTERVAL, END_INTERVAL)) // start between START_INTERVAL and END_INTERVAL seconds from now
                .setReplaceCurrent(false) // overwrite an existing job with the same tag
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL) // retry with exponential backoff
                // constraints that need to be satisfied for the job to run
                .setConstraints(
                        // only run on an unmetered network
                        Constraint.ON_ANY_NETWORK
                        // only run when the device is charging
                        //Constraint.DEVICE_CHARGING,
                )
                .setExtras(myExtrasBundle)
                .build();

        dispatcher.mustSchedule(myJob);
    }
}
