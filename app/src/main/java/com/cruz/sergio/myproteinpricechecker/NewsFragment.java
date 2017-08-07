package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;


public class NewsFragment extends Fragment {
    Activity mActivity;
    SwipeRefreshLayout refreshNewsLayout;
    LinearLayout ll_news_vertical;
    String MP_Domain;
    String[] NEWS_SITES = new String[]{
            "https://pt.myprotein.com",
            "https://www.prozis.com/pt/pt",

    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_fragment, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshNewsLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.news_swiperefresh);
        refreshNewsLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getVouchers();
                    }
                }
        );
        ll_news_vertical = (LinearLayout) mActivity.findViewById(R.id.ll_news_vertical);
        getVouchers();
    }

    public void getVouchers() {
        int childCount = ll_news_vertical.getChildCount();
        if (childCount > 1) {
            ll_news_vertical.removeViews(1, childCount - 1);
        }
        AsyncTask<Void, Void, Boolean> internetAsyncTask = new checkInternetAsyncTask();
        internetAsyncTask.execute();
    }

    public class checkInternetAsyncTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            //Importante porque ao executar o ping bloqueia o interface7
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
                MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);

                String news_url = NEWS_SITES[0];

                Log.i("Sergio>>>", "news_url=" + news_url);

                AsyncTask<String, Void, Document> getNewsAsync = new GetNewsAsync();
                getNewsAsync.executeOnExecutor(THREAD_POOL_EXECUTOR, news_url);

            } else {
                refreshNewsLayout.setRefreshing(false);
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }
        }
    }

    class GetNewsAsync extends AsyncTask<String, Void, Document> {

        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultDocument;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);

            if (document != null) {
                Elements newsElements = document.getElementsByClass("carouselMain__slide");
                Log.i("Sergio>", this + " onPostExecute\nnewsElements= " + newsElements);

                if (newsElements == null || newsElements.size() == 0) {
                    set_webView("No News Found");

                } else {
                    for (int i = 0; i < newsElements.size(); i++) {
                        Element element = newsElements.get(i);
                        String voucherText = element.html();

                        String replace = "<a href=\"/";
                        int indexOfHref = voucherText.indexOf(replace);
                        if (indexOfHref > 0) {
                            voucherText = voucherText.replaceAll(replace, "<a href=" + MP_Domain);
                        }

                        set_webView(voucherText);
                    }
                }
            }

            refreshNewsLayout.setRefreshing(false);

        }
    }

    private void set_webView(String news_text) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.news_webview_layout, null);
        WebView webView = (WebView) view.findViewById(R.id.news_webview);
        webView.loadData(news_text, "text/html; charset=utf-8", "utf-8");
        ll_news_vertical.addView(view);
    }

}
