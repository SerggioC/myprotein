package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.Timestamp;

/**
 * Created by Sergio on 05/05/2017.
 */
//unused
public class UpdatePricesService extends IntentService {

    public UpdatePricesService() {
        super(null);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdatePricesService(String name) {
        super("UpdatePricesService");
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("Sergio>", this + "\nonStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Sergio>", this + "\nonBind");
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        Log.i("Sergio>", this + "\nonCreate");
        super.onCreate();
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        Log.i("Sergio>", this + "\nonStart");
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("Sergio>", this + "\nonDestroy");
        super.onDestroy();
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Sergio>", this + "\nonUnbind");
        return super.onUnbind(intent);
    }

    /**
     * This method is invoked on the worker thread with a request to process.  Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic. * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else. * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}. * param intent The value passed to {@link
     * Context#startService(Intent)}. * This may be null if the service is being restarted after
     * its process has gone away; see * {@link Service#onStartCommand} * for details.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DBHelper dbHelper = new DBHelper(UpdatePricesService.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put("job_info", timestamp.toString());
        long jobsRowId = db.insert("jobs", null, contentValues);
        if (jobsRowId < 0L) {
            Log.e("Sergio>", this + "\nonHandleIntent: Error inserting job to DataBase!");
        } else {
            Log.d("Sergio>", this + "\nonHandleIntent: Added Job to database!");
        }
        db.close();
        Log.i("Sergio>", this + "\nonHandleIntent");
    }


}
