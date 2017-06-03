package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.sql.Timestamp;

/**
 * Created by Sergio on 03/06/2017.
 * Extends Service
 */
public class FirebaseJobservice extends JobService {

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w("Sergio>", this + "\nonDestroy: Destroying FirebaseJobservice");
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        Log.d("Sergio>", this + "\nonStartJob");

        // Do some work here
        DBHelper dbHelper = new DBHelper(FirebaseJobservice.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put("job_info", timestamp.toString());
        long jobsRowId = db.insert("jobs", null, contentValues);
        if (jobsRowId < 0L) {
            Log.e("Sergio>", this + "\nonStartJob: Error inserting job to DataBase!");
        } else {
            Log.i("Sergio>", this + "\nonStartJob: Added Job to database!");
        }
        db.close();
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.w("Sergio>", this + "\nonStopJob: FirebaseJobservice");

        return true; // Answers the question: "Should this job be retried?"
    }
}

