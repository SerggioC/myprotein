package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cruz.sergio.myproteinpricechecker.MainActivity;
import com.cruz.sergio.myproteinpricechecker.R;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.ping;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;
import static com.cruz.sergio.myproteinpricechecker.helper.StartFirebase.JOB_BUNDLE;
import static com.cruz.sergio.myproteinpricechecker.helper.StartFirebase.MINIMUM_DELTA_INTERVAL;

/**
 * Created by Sergio on 03/06/2017.
 * Extends Service
 * https://github.com/firebase/firebase-jobdispatcher-android
 */
public class FirebaseJobservice extends JobService {
    static final String JSON_METHOD = "getUpdatedJSONPrice";
    static final String BASE_METHOD = "getUpdatedBasePrice";
    private static int cursorSize;
    private static int currentCursor;
    static Boolean isJob;
    public static UpdateCompleteListener listener;
    static int delta_time = 0;

    // This interface defines the type of messages I want to communicate to my owner
    public interface UpdateCompleteListener {
        void onUpdateReady(Boolean isReady);
    }

    public void setUpdateCompleteListener(UpdateCompleteListener listener) {
        FirebaseJobservice.listener = listener;
    }

    static class CursorObj {
        int row_id;
        String prod_name;
        String jsonURL;
        String baseURL;
        double min_price;
        double max_price;
        double actual_price;
        long actual_price_date;
        double previous_price;

        CursorObj(int row_id, String prod_name, String jsonURL, String baseURL, double min_price, double max_price, double actual_price, long actual_price_date, double previous_price) {
            this.row_id = row_id;
            this.prod_name = prod_name;
            this.jsonURL = jsonURL;
            this.baseURL = baseURL;
            this.min_price = min_price;
            this.max_price = max_price;
            this.actual_price = actual_price;
            this.actual_price_date = actual_price_date;
            this.previous_price = previous_price;
        }
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here
        Log.w("Sergio>", this + "\nonStartJob");
        Bundle extras = job.getExtras();
        if (extras != null) {
            delta_time = extras.getInt(JOB_BUNDLE);
        } else {
            delta_time = MINIMUM_DELTA_INTERVAL;
        }

        updatePricesOnStart(this, true);
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.w("Sergio>", this + "\nonStopJob: FirebaseJobservice");
        return true; // Answers the question: "Should this job be retried?"
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Sergio>", this + "\nonDestroy: Destroying FirebaseJobservice");
    }

    public static void updatePricesOnStart(Context context, Boolean isJob) {
        FirebaseJobservice.isJob = isJob;
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String job_status = "null";

        Cursor cursor = db.rawQuery("SELECT " + ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE +
                        " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.PricesEntry._ID + " = " +
                        "(SELECT MAX(" + ProductsContract.PricesEntry._ID + ") FROM " +
                        ProductsContract.PricesEntry.TABLE_NAME + ");"
                , null);

        long now = System.currentTimeMillis();
        long last_saved_date = now;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            last_saved_date = cursor.getLong(cursor.getColumnIndex(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE));
        }

        long calculated_delta_t = TimeUnit.MILLISECONDS.toSeconds(now - last_saved_date);

        // Limitar fazer demasiados requests quando o tempo é menor que MINIMUM_DELTA_INTERVAL
        if (calculated_delta_t < delta_time && isJob) {
            Log.w("Sergio>", "FirebaseJobservice updatePricesOnStart: \n" +
                    "Too soon to save to database wait up Job!");
            job_status = "postpone";
        } else if (calculated_delta_t > delta_time && isJob || !isJob) {
            Log.w("Sergio>", "FirebaseJobservice updatePricesOnStart: \n" +
                    "Starting update!");
            cursor = db.rawQuery("SELECT " +
                    ProductsContract.ProductsEntry._ID + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE +
                    " FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);

            cursorSize = cursor.getCount();

            if (cursorSize > 0) {
                currentCursor = 0;

                while (cursor.moveToNext()) {
                    int row_id = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
                    String product_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
                    String jsonURL = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS));
                    String baseURL = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
                    double min_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE));
                    double max_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE));
                    double actual_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE));
                    long actual_price_date = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE));
                    double previous_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE));

                    CursorObj cursorObj =
                            new CursorObj(row_id, product_name, jsonURL, baseURL, min_price_value, max_price_value, actual_price_value, actual_price_date, previous_price_value);

                    if (!TextUtils.isEmpty(jsonURL)) {
                        Log.d("Sergio>", context + "\nonStartJob:\nJSON_METHOD= " + JSON_METHOD);
                        AsyncTask<CursorObj, Void, Boolean> checkInternet_forBGMethod = new checkInternetAsyncMethod2(JSON_METHOD, context);
                        checkInternet_forBGMethod.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                    } else if (!TextUtils.isEmpty(baseURL)) {
                        Log.i("Sergio>", context + "\nonStartJob:\nBASE_METHOD= " + BASE_METHOD);
                        AsyncTask<CursorObj, Void, Boolean> checkInternet_forBGMethod = new checkInternetAsyncMethod2(BASE_METHOD, context);
                        checkInternet_forBGMethod.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                    }
                }
                job_status = "complete";
            } else {
                Log.w("Sergio>", "updatePricesOnStart: " + "cursor Size = 0, empty database!");
                job_status = "empty db";
                if (listener != null) {
                    listener.onUpdateReady(false);
                }
            }
        }

        if (isJob) {
            db = dbHelper.getWritableDatabase();

            String lastTimeStr = null;
            cursor = db.rawQuery("SELECT time FROM jobs WHERE _id = (SELECT MAX(_id) FROM jobs)", null);
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                lastTimeStr = cursor.getString(cursor.getColumnIndex("time"));
            }

            long lastTime = lastTimeStr != null ? Timestamp.valueOf(lastTimeStr).getTime() : now;
            long duration = now - lastTime;
            long hr = TimeUnit.MILLISECONDS.toHours(duration);
            long min = TimeUnit.MILLISECONDS.toMinutes(duration -= TimeUnit.HOURS.toMillis(hr));
            long sec = TimeUnit.MILLISECONDS.toSeconds(duration - TimeUnit.MINUTES.toMillis(min));

            String time_diff = hr + "hr " + min + "min " + sec + "s";

            Timestamp timestamp = new Timestamp(now);
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", timestamp.toString());
            contentValues.put("diff", time_diff);
            contentValues.put("status", job_status);
            long jobsRowId = db.insert("jobs", null, contentValues);
            if (jobsRowId < 0L) {
                Log.e("Sergio>", context + "\nonStartJob: Error inserting job to DataBase!");
            } else {
                Log.i("Sergio>", context + "\nonStartJob: Added Job info to database!");
            }
        }

        cursor.close();
        db.close();
    }

    static class checkInternetAsyncMethod2 extends AsyncTask<CursorObj, Void, Boolean> {
        String method;
        Context context;
        CursorObj cursorObj;

        public checkInternetAsyncMethod2(String method, Context context) {
            this.method = method;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(CursorObj... params) {
            cursorObj = params[0];

            ConnectivityManager connManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
            if (isConnected) {
                try {
                    if (ping()) {
                        return true;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return false;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                switch (method) {
                    case JSON_METHOD: {
                        AsyncTask<CursorObj, Void, JSONObject> getUpdatedPrice = new GetUpdatedPriceJSON(context);
                        getUpdatedPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                        break;
                    }
                    case BASE_METHOD: {
                        AsyncTask<CursorObj, Void, Document> getUpdatedPrice = new GetUpdatedPriceBase(context);
                        getUpdatedPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                        break;
                    }
                    default: {
                        Log.e("Sergio>", this + " onPostExecute: invalid method given in switch, FirebaseJobService");
                        break;
                    }
                }
            } else {
                if (listener != null) {
                    listener.onUpdateReady(false);
                }
            }
        }
    }

    static class GetUpdatedPriceJSON extends AsyncTask<CursorObj, Void, JSONObject> {
        CursorObj cursorObj;
        Context context;

        GetUpdatedPriceJSON(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(CursorObj... params) {
            cursorObj = params[0];
            String json_url = cursorObj.jsonURL;

            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(json_url)
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT) //sem limite de tempo para receber a página
                        .ignoreContentType(true) // ignorar o tipo de conteúdo
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = null;
            try {
                jsonObject = resultDocument == null ? null : new JSONObject(resultDocument.text());
            } catch (JSONException e) {
                Log.e("Sergio>", " onPostExecute GetUpdatedPriceJSON JSONException error" + e);
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);
            String priceJson = null;
            if (json != null) {
                try {
                    priceJson = (String) json.get("price");
                } catch (JSONException e) {
                    Log.e("Sergio>", " onPostExecute GetUpdatedPriceJSON JSONException error" + e);
                    e.printStackTrace();
                }
            }
            savePriceToDB(context, cursorObj, priceJson);
        }


    }

    private static class GetUpdatedPriceBase extends AsyncTask<CursorObj, Void, Document> {
        Context context;
        CursorObj cursorObj;

        GetUpdatedPriceBase(Context context) {
            this.context = context;
        }

        @Override
        protected Document doInBackground(CursorObj... params) {
            cursorObj = params[0];
            Document resultDocument = null;
            String base_url = cursorObj.baseURL;
            try {
                resultDocument = Jsoup.connect(base_url)
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT) //sem limite de tempo
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultDocument;
        }

        @Override
        protected void onPostExecute(Document resultDocument) {
            super.onPostExecute(resultDocument);
            String price = null;
            if (resultDocument != null) {
                try {
                    price = resultDocument.getElementsByClass("priceBlock_current_price").text();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            savePriceToDB(context, cursorObj, price);
        }
    }

    private static void savePriceToDB(Context context, CursorObj cursorObj, String priceString) {
        long currentTimeMillis = System.currentTimeMillis();
        double price_value;
        if (priceString != null) {
            Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9; só ficam números . e ,
            Matcher match = regex.matcher(priceString);

            String strPrice = match.replaceAll("");
            strPrice = strPrice.replaceAll(",", ".");
            price_value = Double.parseDouble(strPrice);
        } else {
            price_value = 0d;
            priceString = "";
        }

        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //
        // Adicionar mais um registo à tabela de preços +
        //
        ContentValues priceContentValues = new ContentValues(4);
        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS, cursorObj.row_id); // _ID do produto
        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, priceString); // preço com o símbolo
        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, price_value); // preço (valor em float)
        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, currentTimeMillis);
        db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, priceContentValues);

        // Atualizar preço atual na tabela de produtos ->
        ContentValues productContentValues = new ContentValues();
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE, priceString);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE, price_value);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE, currentTimeMillis);

        if (price_value != cursorObj.actual_price && price_value != 0) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE, cursorObj.actual_price);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE, cursorObj.actual_price_date);
        }

        if (price_value < cursorObj.min_price && price_value != 0) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, priceString);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE, price_value);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, currentTimeMillis);

            create_Notification(cursorObj.prod_name, priceString, context, cursorObj.row_id);

        } else if (price_value > cursorObj.max_price) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceString);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, price_value);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, currentTimeMillis);
        }

        db.update(ProductsContract.ProductsEntry.TABLE_NAME, productContentValues,
                ProductsContract.ProductsEntry._ID + " = '" + cursorObj.row_id + "'", null);
        db.close();

        currentCursor++;
        if (currentCursor == cursorSize) {
            Log.w("Sergio>", "savePriceToDB: \n" + "listener= " + listener);
            if (listener != null) {
                listener.onUpdateReady(true);
            }
        }

    }

    private static void create_Notification(String prod_name, String priceString, Context context, int notificationID) {
        NotificationCompat.Builder notification_Builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(prod_name + " price has dropped to " + priceString);

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification_Builder.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // Sets an ID for the notification
        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationID, notification_Builder.build());

    }

}

