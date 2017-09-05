package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import org.jsoup.helper.StringUtil;

import java.net.URLEncoder;

public class SearchActivity extends AppCompatActivity {
    Boolean hasAsyncTaskRuning = false;
    Activity mActivity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);



        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            performSearch(query);
        }


    }


    public void performSearch(String searchString) {
        hideKeyBoard();
        if (hasAsyncTaskRuning) {
            NetworkUtils.showCustomToast(mActivity, "Ongoing search...", R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
        } else {
            if (StringUtil.isBlank(searchString)) {
                NetworkUtils.showCustomToast(mActivity, "Search product name or enter product URL", R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
            } else {
                //horizontalProgressBar.setVisibility(View.VISIBLE);
                AsyncTask<String, Void, Boolean> internetAsyncTask = new checkInternetAsyncTask();
                internetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString);
            }
        }
    }


    private void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("Sergio>>>", "hideKeyboard error: ", e);
        }
    }

    public class checkInternetAsyncTask extends AsyncTask<String, Void, Boolean> {
        String searchString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hasAsyncTaskRuning = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            this.searchString = params[0];
            //Importante porque ao executar o ping bloqueia o interface
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                searchString = URLEncoder.encode(searchString);
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
                String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
                String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
                String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
                String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
//                queryStr = MP_Domain + "elysium.search?search=" + searchString + URL_suffix;
//
//                Log.i("Sergio>>>", "performSearch: querystr=" + queryStr);
//
//                hasMorePages = true;
//                resultsListView.setAdapter(null);
//                arrayListProductCards.clear();
//
//                AsyncTask<String, Void, Document> performSearch = new performSearch();
//                performSearch.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryStr);

            } else {
//                if (horizontalProgressBar.getVisibility() == View.VISIBLE) {
//                    horizontalProgressBar.setVisibility(View.GONE);
//                }
//                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
//                    noNetworkSnackBar.show();
//                } else {
//                    makeNoNetworkSnackBar(mActivity);
//                }
//                hasAsyncTaskRuning = false;
          }
        }
    }

}
