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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.ping;

/**
 * Created by Sergio on 03/06/2017.
 * Extends Service
 * https://github.com/firebase/firebase-jobdispatcher-android
 */
public class FirebaseJobservice extends JobService {
    public static final String JSON_METHOD = "getUpdatedJSONPrice";
    public static final String BASE_METHOD = "getUpdatedBasePrice";
    private static int cursorSize;
    private  static int currentCursor;
    static Boolean gotIsJob;


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Sergio>", this + "\nonDestroy: Destroying FirebaseJobservice");
    }

    public static class CursorObj {
        public int row_id;
        public String jsonURL;
        public String baseURL;
        public double min_price;
        public double max_price;

        public CursorObj(int row_id, String jsonURL, String baseURL, double min_price, double max_price) {
            this.row_id = row_id;
            this.jsonURL = jsonURL;
            this.baseURL = baseURL;
            this.min_price = min_price;
            this.max_price = max_price;
        }
    }



    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface UpdateCompleteListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        void onUpdateReady(Boolean isReady);
    }

    public static UpdateCompleteListener listener;

    // Constructor where listener events are ignored
    public FirebaseJobservice() {
        // set null or default listener or accept as argument to constructor
        this.listener = null;
    }


    public void setUpdateCompleteListener(UpdateCompleteListener listener) {
        this.listener = listener;
    }


    @Override
    public boolean onStartJob(JobParameters job) {
        // Do some work here
        Log.w("Sergio>", this + "\nonStartJob");
        updatePricesOnStart(this, true);
        return true; // Answers the question: "Is there still work going on?"
    }

    public static void updatePricesOnStart(Context context, Boolean isJob) {
        gotIsJob = isJob;
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " +
                ProductsContract.ProductsEntry._ID + " , " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " , " +
                ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " , " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE + " , " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE +
                " FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);
        cursorSize = cursor.getCount();

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
                currentCursor++;
                AsyncTask<CursorObj, Void, Boolean> checkInternet_forBGMethod = new checkInternetAsyncMethod2(BASE_METHOD, context);
                checkInternet_forBGMethod.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
            }
        }

        cursor.close();
        db.close();

        if (isJob) {
            db = dbHelper.getWritableDatabase();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ContentValues contentValues = new ContentValues();
            contentValues.put("job_info", timestamp.toString());
            long jobsRowId = db.insert("jobs", null, contentValues);
            if (jobsRowId < 0L) {
                Log.e("Sergio>", context + "\nonStartJob: Error inserting job to DataBase!");
            } else {
                Log.i("Sergio>", context + "\nonStartJob: Added Job to database!");
            }
            db.close();
        }
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.w("Sergio>", this + "\nonStopJob: FirebaseJobservice");

        return true; // Answers the question: "Should this job be retried?"
    }


    public static class checkInternetAsyncMethod2 extends AsyncTask<CursorObj, Void, Boolean> {
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
            Log.i("Sergio>", this + "\ndoInBackground asynctaskmethod2, is connected? " + isConnected + " method= " + method);
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
                        AsyncTask<CursorObj, Void, JSONObject> getUpdatedPrice = new GetUpdatedPriceBase(context);
                        getUpdatedPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cursorObj);
                        break;
                    }
                    default: {
                        Log.e("Sergio>", this + " onPostExecute: invalid method given in switch, FirebaseJobService");
                        break;
                    }
                }

            }
        }
    }

    public static class GetUpdatedPriceJSON extends AsyncTask<CursorObj, Void, JSONObject> {
        CursorObj cursorObj;
        Context context;

        public GetUpdatedPriceJSON(Context context) {
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
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .timeout(0) //sem limite de tempo para receber a página
                        .ignoreContentType(true) // ignorar o tipo de conteúdo
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = null;
            try {
                if (resultDocument == null) {
                    jsonObject = null;
                } else {
                    jsonObject = new JSONObject(resultDocument.text());
                }
            } catch (JSONException e) {
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
                    e.printStackTrace();
                }
            }

            if (priceJson != null) {
                long currentTimeMillis = System.currentTimeMillis();
                Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9; só ficam números . e ,
                Matcher match = regex.matcher(priceJson);

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
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, priceJson); // preço com o símbolo
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, price_value); // preço (valor em float)
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, currentTimeMillis);
                db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, priceContentValues);

                // Atualizar preço atual na tabela de produtos ->
                ContentValues productContentValues = new ContentValues();
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE, priceJson);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE, price_value);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE, currentTimeMillis);

                if (price_value < cursorObj.min_price) {
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, priceJson);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE, price_value);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, currentTimeMillis);
                    // TODO: Send alert to user? There's a new lower price!
                }

                if (price_value > cursorObj.max_price) {
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceJson);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, price_value);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, currentTimeMillis);
                }

                db.update(ProductsContract.ProductsEntry.TABLE_NAME, productContentValues,
                        ProductsContract.ProductsEntry._ID + " = '" + cursorObj.row_id + "'", null);
                db.close();

                currentCursor++;
                if (currentCursor == cursorSize && !gotIsJob) {
                    Log.w("Sergio>", this + "\nonPostExecute: \n" +
                            "currentCursor= " + currentCursor +  "\n" +
                            "cursorSize " + cursorSize);
                    listener.onUpdateReady(true);
                }


            }

        }

    }

    private static class GetUpdatedPriceBase extends AsyncTask<CursorObj, Void, JSONObject> {
        Context context;

        public GetUpdatedPriceBase(Context context) {
            this.context = context;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected JSONObject doInBackground(CursorObj... params) {
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            listener.onUpdateReady(true);



        }
    }
}

