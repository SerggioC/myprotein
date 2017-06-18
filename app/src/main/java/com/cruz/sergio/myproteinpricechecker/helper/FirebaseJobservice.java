package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

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

import static android.database.DatabaseUtils.dumpCursorToString;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.ping;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

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

    // This interface defines the type of messages I want to communicate to my owner
    public interface UpdateCompleteListener {
        void onUpdateReady(Boolean isReady);
    }

/*
    // Constructor where listener events are ignored
    public FirebaseJobservice() {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
    }
*/

    public void setUpdateCompleteListener(UpdateCompleteListener listener) {
        FirebaseJobservice.listener = listener;
    }

    static class CursorObj {
        int row_id;
        String jsonURL;
        String baseURL;
        double min_price;
        double max_price;

        CursorObj(int row_id, String jsonURL, String baseURL, double min_price, double max_price) {
            this.row_id = row_id;
            this.jsonURL = jsonURL;
            this.baseURL = baseURL;
            this.min_price = min_price;
            this.max_price = max_price;
        }
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here
        Log.w("Sergio>", this + "\nonStartJob");
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

        Cursor cursor = db.rawQuery("SELECT " +
                        ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry._ID + " , " +
                        ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " , " +
                        ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " , " +
                        ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE + " , " +
                        ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE +
                " FROM " + ProductsContract.ProductsEntry.TABLE_NAME +
                " INNER JOIN " + ProductsContract.PricesEntry.TABLE_NAME + " ON " +
                ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry._ID + " = " +
                ProductsContract.PricesEntry.TABLE_NAME + "." + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS +
                " WHERE " + ProductsContract.PricesEntry.TABLE_NAME + "." + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + " = " +
                "(SELECT MAX(" + ProductsContract.PricesEntry.TABLE_NAME + "." + ProductsContract.PricesEntry._ID + ")" + " FROM " + ProductsContract.PricesEntry.TABLE_NAME +
                ");"


                , null);
        cursorSize = cursor.getCount();
        String cursorToString = dumpCursorToString(cursor);
        Log.i("Sergio>", "updatePricesOnStart\ncursorToString= " + cursorToString);

        if (cursorSize > 0) {
            currentCursor = 0;

            while (cursor.moveToNext()) {
                int row_id = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
                String jsonURL = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS));
                String baseURL = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
                double min_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE));
                double max_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE));
                CursorObj cursorObj = new CursorObj(row_id, jsonURL, baseURL, min_price_value, max_price_value);

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
        } else {
            Log.w("Sergio>", "updatePricesOnStart: " + "cursor Size = 0, empty database!");
            if (listener != null) {
                listener.onUpdateReady(false);
            }
        }

        if (isJob) {
            db = dbHelper.getWritableDatabase();

            String lastTimeStr = null;
            cursor = db.rawQuery("SELECT time FROM jobs WHERE _id = (SELECT MAX(_id) FROM jobs)", null);
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                lastTimeStr = cursor.getString(cursor.getColumnIndex("time"));
            }

            long now = System.currentTimeMillis();
            long lastTime = lastTimeStr != null ? Timestamp.valueOf(lastTimeStr).getTime() : now;
            long duration = now - lastTime;
            String time_diff = TimeUnit.MILLISECONDS.toMinutes(duration) + "min " + TimeUnit.MILLISECONDS.toSeconds(duration) + "s";

            Timestamp timestamp = new Timestamp(now);
            ContentValues contentValues = new ContentValues();
            contentValues.put("time", timestamp.toString());
            contentValues.put("diff", time_diff);
            long jobsRowId = db.insert("jobs", null, contentValues);
            if (jobsRowId < 0L) {
                Log.e("Sergio>", context + "\nonStartJob: Error inserting job to DataBase!");
            } else {
                Log.i("Sergio>", context + "\nonStartJob: Added Job to database!");
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
                e.printStackTrace();
                Log.e("Sergio>", " onPostExecute GetUpdatedPriceJSON JSONException error" + e);
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
                    e.printStackTrace();
                    Log.e("Sergio>", " onPostExecute GetUpdatedPriceJSON JSONException error" + e);
                }
            }

            if (priceJson != null) {
                savePriceToDB(context, cursorObj, priceJson);
            }

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
            if (resultDocument != null) {
                String price = resultDocument.getElementsByClass("priceBlock_current_price").text();
                if (price != null) savePriceToDB(context, cursorObj, price);
            }
        }
    }

    private static void savePriceToDB(Context context, CursorObj cursorObj, String priceString) {
        long currentTimeMillis = System.currentTimeMillis();
        Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9; só ficam números . e ,
        Matcher match = regex.matcher(priceString);

        String strPrice = match.replaceAll("");
        strPrice = strPrice.replaceAll(",", ".");
        double price_value = Double.parseDouble(strPrice);

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

        if (price_value < cursorObj.min_price) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, priceString);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE, price_value);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, currentTimeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRICE_VARIATION, -1);
            // TODO: Send alert to user? There's a new lower price!
        } else if (price_value > cursorObj.max_price) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceString);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, price_value);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, currentTimeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRICE_VARIATION, 1);
        } else {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRICE_VARIATION, 0);
        }

        db.update(ProductsContract.ProductsEntry.TABLE_NAME, productContentValues,
                ProductsContract.ProductsEntry._ID + " = '" + cursorObj.row_id + "'", null);
        db.close();

        currentCursor++;
        if (currentCursor == cursorSize && !isJob) {
            Log.w("Sergio>", context + "\nonPostExecute: \n" +
                    "currentCursor= " + currentCursor + "\n" +
                    "cursorSize " + cursorSize);

            if (listener != null) {
                listener.onUpdateReady(true);
            }
        }

    }

}

