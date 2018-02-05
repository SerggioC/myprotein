package com.cruz.sergio.myproteinpricechecker.helper;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cruz.sergio.myproteinpricechecker.MainActivity;
import com.cruz.sergio.myproteinpricechecker.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

/**
 * Created by Sergio on 03/10/2017.
 */

public class Alarm extends BroadcastReceiver {
    public static final int MINIMUM_DELTA_INTERVAL = 3 * 60 * 60 * 1000; // 3hr em ms
    public static final String ACTION_ALARM_REF = "ACTION_ALARM";
    public static final int ALARM_REQ_CODE = 0;
    public static final String LAST_DB_UPDATE_PREF_KEY = "last_db_update";
    static final String JSON_METHOD = "getUpdatedJSONPrice";
    static final String BASE_METHOD = "getUpdatedBasePrice";
    public static UpdateCompleteListener listener;
    static Boolean isJob;
    static Boolean isSingleLine;
    static String singleLineID;
    static SharedPreferences sharedPref;
    private static int cursorSize;
    private static int currentCursor;
    private static SharedPreferences defaultSharedPreferences;

    public static void updatePricesOnReceive(Context context, Boolean isJob, Boolean isSingleLine, String singleLineID) {
        Log.d("Sergio>", "Alarm updatePricesOnReceive()");
        Alarm.isJob = isJob;
        Alarm.isSingleLine = isSingleLine;
        Alarm.singleLineID = singleLineID;
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String job_status = "null";

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Preferência de atualização do utilizador (max freq = 3hr)
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long DELTA_INTERVAL = Long.parseLong(sharedPrefs.getString("sync_frequency", String.valueOf(MINIMUM_DELTA_INTERVAL)));
        if (DELTA_INTERVAL < MINIMUM_DELTA_INTERVAL || DELTA_INTERVAL < 0) DELTA_INTERVAL = MINIMUM_DELTA_INTERVAL;

        // Parâmetro único centralizado para guardar última atualização da Database, mais eficiente que o anterior (checkar a DB toda).
        sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long last_update_date = sharedPref.getLong(LAST_DB_UPDATE_PREF_KEY, now);
        long calculated_delta_t = now - last_update_date;

        // Limitar fazer demasiados requests quando o tempo é menor que MINIMUM_DELTA_INTERVAL ou (DELTA_INTERVAL) 6hr
        if (calculated_delta_t < DELTA_INTERVAL && isJob) {
            Log.w("Sergio>", "Alarm OnReceive: \n" +
                    "Too soon to save to database wait up Job!");
            job_status = "postpone";
        } else if (calculated_delta_t > DELTA_INTERVAL && isJob || !isJob || isSingleLine) {
            Log.w("Sergio>", "Alarm OnReceive: \n" +
                    "Starting update!");

            String singleLineRef = isSingleLine ? " WHERE " + ProductsContract.ProductsEntry._ID + " = '" + singleLineID + "'" : "";
            Cursor cursor = db.rawQuery("SELECT " +
                    ProductsContract.ProductsEntry._ID + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE + " , " +
                    ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS + " , " +
                    ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE +
                    " FROM " + ProductsContract.ProductsEntry.TABLE_NAME
                    + singleLineRef, null);

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
                    Boolean show_notifications = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS)) == 1;
                    double notify_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE));

                    CursorObj cursorObj =
                            new CursorObj(row_id, product_name, jsonURL, baseURL, min_price_value, max_price_value, actual_price_value, actual_price_date, previous_price_value, show_notifications, notify_value);

                    if (!TextUtils.isEmpty(jsonURL)) {
                        Log.d("Sergio>", context + " onStartJob: JSON_METHOD= " + JSON_METHOD);
                        AsyncTask<CursorObj, Void, Boolean> checkInternet_forBGMethod = new checkInternetAsyncMethod2(JSON_METHOD, context);
                        checkInternet_forBGMethod.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                    } else if (!TextUtils.isEmpty(baseURL)) {
                        Log.i("Sergio>", context + " onStartJob: BASE_METHOD= " + BASE_METHOD);
                        AsyncTask<CursorObj, Void, Boolean> checkInternet_forBGMethod = new checkInternetAsyncMethod2(BASE_METHOD, context);
                        checkInternet_forBGMethod.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                    }
                }
                job_status = "complete";
                cursor.close();
            } else {
                Log.w("Sergio>", "updatePricesOnStart: " + "cursor Size = 0, empty database!");
                job_status = "empty db";
                if (listener != null) {
                    listener.onUpdateReady(false, isSingleLine, null);
                }
            }
        }

        if (isJob) {
            db = dbHelper.getWritableDatabase();

            String lastTimeStr = null;
            Cursor cursor = db.rawQuery("SELECT time FROM jobs WHERE _id = (SELECT MAX(_id) FROM jobs)", null);
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
            cursor.close();
        }
        db.close();
    }

    private static void savePriceToDB(Context context, CursorObj cursorObj, String priceString) {
        long currentTimeMillis = System.currentTimeMillis();
        double price_value;
        if (!StringUtil.isBlank(priceString)) {
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
        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, price_value); // preço (valor em double, mais preciso que float)
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

        } else if (price_value > cursorObj.max_price) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceString);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, price_value);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, currentTimeMillis);
        }

        Boolean globalNotifications = defaultSharedPreferences.getBoolean("notifications_global_key", true);

        if (cursorObj.show_notifications && price_value != 0 && globalNotifications) {
            if ((price_value <= cursorObj.notify_value && cursorObj.notify_value != 0) ||
                    (price_value < cursorObj.actual_price && cursorObj.notify_value == 0)) {
                create_Notification(cursorObj.prod_name, priceString, context, cursorObj.row_id);
            }
        }


        db.update(ProductsContract.ProductsEntry.TABLE_NAME, productContentValues,
                ProductsContract.ProductsEntry._ID + " = '" + cursorObj.row_id + "'", null);
        db.close();

        currentCursor++;
        if (currentCursor == cursorSize) {
            if (!isSingleLine) {
                // Guardar ultimo update à DB nas SharedPreferences
                //SharedPreferences sharedPref = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putLong(LAST_DB_UPDATE_PREF_KEY, currentTimeMillis);
                editor.commit();
            }

            if (listener != null) {
                listener.onUpdateReady(true, isSingleLine, ProductsContract.ProductsEntry.buildProductsUri(cursorObj.row_id));
            }
        }

    }

    private static void create_Notification(String prod_name, String priceString, Context context, int notificationID) {
        Uri ringtoneURI = Uri.parse(defaultSharedPreferences.getString("ringtone_notifications_key", "content://settings/system/notification_sound")); // Default notification Sound

        NotificationCompat.Builder notification_Builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(prod_name + " price has dropped to " + priceString)
                .setSound(ringtoneURI)
                .setLights(Color.RED, 500, 400)
                .setTicker(prod_name + " price has dropped to " + priceString);

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification_Builder.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // Sets an ID for the notification
        // Builds the notification and issues it.
        mNotifyMgr.notify(notificationID, notification_Builder.build());


        Boolean shouldVibrate = defaultSharedPreferences.getBoolean("vibrate_notifications_key", true);

        if (shouldVibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 200, 100, 200, 100, 200}, 1));
                } else {
                    vibrator.vibrate(new long[]{0, 200, 100, 200, 100, 200}, -1); // Vibrate pattern 3 vibrations with pause
                }
            }

        }

    }

    public void setUpdateCompleteListener(UpdateCompleteListener listener) {
        Alarm.listener = listener;
    }

    public void setAlarm(Context context) {
        // Preferência de atualização do utilizador (max freq = 3hr)
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        long DELTA_INTERVAL = Long.parseLong(sharedPrefs.getString("sync_frequency", String.valueOf(MINIMUM_DELTA_INTERVAL)));
        if (DELTA_INTERVAL < MINIMUM_DELTA_INTERVAL || DELTA_INTERVAL < 0) DELTA_INTERVAL = MINIMUM_DELTA_INTERVAL;

        // Parâmetro único centralizado para guardar última atualização da Database, mais eficiente que o anterior (checkar a DB toda).
        sharedPref = context.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        long now = System.currentTimeMillis();
        long last_update_date = sharedPref.getLong(LAST_DB_UPDATE_PREF_KEY, now);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION_ALARM_REF, null, context, Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQ_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Se já ultrapassou o tempo executa agora, else executa daqui à diferença que falta.
        long triggerAtMillis = now > last_update_date + DELTA_INTERVAL ? now : last_update_date + DELTA_INTERVAL; // now + DELTA_INTERVAL - (now - last_update_date)

       // *** Repete o alarme a cada DELTA_INTERVAL segundos ***
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, DELTA_INTERVAL, pendingIntent);

        Log.i("Sergio>", this + "Alarm extends BroadcastReceiver setAlarm()\n" +
                "triggerAtMillis (Date)= " + new Date(triggerAtMillis));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Sergio>", this + " onReceive() Received alarm intent");
        updatePricesOnReceive(context, true, false, null);
    }

    public void cancelAlarm(Context context) {
        Log.i("Sergio>", this + " Alarm extends BroadcastReceiver cancelAlarm()");
        Intent intent = new Intent(ACTION_ALARM_REF, null, context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, ALARM_REQ_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    // This interface defines the type of messages I want to communicate to my owner
    public interface UpdateCompleteListener {
        void onUpdateReady(Boolean isReady, Boolean isSingleLine, Uri uri);
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
        Boolean show_notifications;
        double notify_value;

        CursorObj(int row_id, String prod_name, String jsonURL, String baseURL,
                  double min_price, double max_price, double actual_price,
                  long actual_price_date, double previous_price, Boolean show_notifications, double notify_value) {
            this.row_id = row_id;
            this.prod_name = prod_name;
            this.jsonURL = jsonURL;
            this.baseURL = baseURL;
            this.min_price = min_price;
            this.max_price = max_price;
            this.actual_price = actual_price;
            this.actual_price_date = actual_price_date;
            this.previous_price = previous_price;
            this.show_notifications = show_notifications;
            this.notify_value = notify_value;
        }

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
            return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.isAvailable();
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
                        Log.e("Sergio>", this + " onPostExecute: invalid method given in switch, Alarm");
                        break;
                    }
                }
            } else {
                if (listener != null) {
                    listener.onUpdateReady(false, isSingleLine, null);
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
                    priceJson = json.isNull("price") ? null : (String) json.get("price");
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
                    if (StringUtil.isBlank(price)) {
                        price = resultDocument.getElementsByClass("product-price price").text();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            savePriceToDB(context, cursorObj, price);
        }
    }

}
