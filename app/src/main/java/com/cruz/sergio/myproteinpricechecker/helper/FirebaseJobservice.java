package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.StaticLayout;
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
 */
public class FirebaseJobservice extends JobService {
    SQLiteDatabase db;
    static final int FAILED = 0;
    static final int SUCCEEDED = 1;



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
        db = dbHelper.getWritableDatabase();


        Cursor cursor = db.rawQuery("SELECT " +
                ProductsContract.ProductsEntry._ID + " , " + ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS +
                " FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);

        for (int i = 0; i < cursor.getCount(); i++) {
            int row_id = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
            String jsonURL = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS));


            AsyncTask<String, Void, Boolean> checkInternet_getUpdatedPrice = new checkInternetAsyncMethod2("getUpdatedPrice");
            checkInternet_getUpdatedPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(row_id), jsonURL);
        }


        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put("job_info", timestamp.toString());
        long jobsRowId = db.insert("jobs", null, contentValues);
        if (jobsRowId < 0L) {
            Log.e("Sergio>", this + "\nonStartJob: Error inserting job to DataBase!");
        } else {
            Log.i("Sergio>", this + "\nonStartJob: Added Job to database!");
        }
        cursor.close();
        db.close();
        return false; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.w("Sergio>", this + "\nonStopJob: FirebaseJobservice");

        return true; // Answers the question: "Should this job be retried?"
    }


    public class checkInternetAsyncMethod2 extends AsyncTask<String, Void, Boolean> {
        String method;
        String row_id;
        String json_url_param;

        checkInternetAsyncMethod2(String method) {
            this.method = method;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            row_id = params[0];
            json_url_param = params[1];

            ConnectivityManager connManager = (ConnectivityManager)
                    FirebaseJobservice.this.getSystemService(Context.CONNECTIVITY_SERVICE);
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
                    case "getUpdatedPrice": {
                        AsyncTask<String, Void, JSONObject> getUpdatedPrice = new GetUpdatedPrice();
                        getUpdatedPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, row_id, json_url_param);
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

    public class GetUpdatedPrice extends AsyncTask<String, Void, JSONObject> {
        String row_id;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected JSONObject doInBackground(String... params) {
            row_id = params[0];
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[1])
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
            int priceInsertToDBResult = FAILED;

            String priceJson = null;
            if (json != null) {
                try {
                    priceJson = (String) json.get("price");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (priceJson != null) {

                Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9; só ficam números . e ,
                Matcher match = regex.matcher(priceJson);
                priceJson = match.replaceAll("");
                priceJson = priceJson.replaceAll(",", ".");
                double price_value = Double.parseDouble(priceJson);

                ContentValues priceContentValue = new ContentValues(3);
                priceContentValue.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, priceJson); // preço com o símbolo
                priceContentValue.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, price_value); // preço (valor em float)
                priceContentValue.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, System.currentTimeMillis());

                long priceRowID = db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, priceContentValue);
                if (priceRowID < 0) {
                    priceInsertToDBResult = SUCCEEDED;
                } else {
                    priceInsertToDBResult = FAILED;
                }

                Cursor cursor = db.rawQuery("UPDATE " + ProductsContract.PricesEntry.TABLE_NAME + " SET " +
                        ProductsContract.ProductsEntry., null);

                UPDATE COMPANY SET ADDRESS = 'Texas' WHERE ID = 6;

            }

        }
    }


}

