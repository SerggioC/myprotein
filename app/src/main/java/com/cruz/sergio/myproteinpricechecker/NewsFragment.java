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
                        getNews();
                    }
                }
        );
        ll_news_vertical = (LinearLayout) mActivity.findViewById(R.id.ll_news_vertical);
        getNews();
    }

    public void getNews() {
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
                String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);

                AsyncTask<String, Void, Document> getMPNewsAsync = new GetMPNewsAsync();
                getMPNewsAsync.executeOnExecutor(THREAD_POOL_EXECUTOR, MP_Domain);

                String PRZ_Domain = "https://www.prozis.com/pt/pt/";

                AsyncTask<String, Void, Document> getPRZNewsAsync = new GetPRZNewsAsync();
                getPRZNewsAsync.executeOnExecutor(THREAD_POOL_EXECUTOR, PRZ_Domain);

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

    class GetMPNewsAsync extends AsyncTask<String, Void, Document> {
        String url;

        @Override
        protected Document doInBackground(String... params) {
            this.url = params[0];
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(url)
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

                if (newsElements != null && newsElements.size() != 0) {
                    String replace = "<a href=\"/";
                    for (int i = 0; i < newsElements.size(); i++) {
                        Element element = newsElements.get(i);
                        String newsText = element.html();
                        int indexOfHref = newsText.indexOf(replace);
                        if (indexOfHref > 0) {
                            newsText = newsText.replaceAll(replace, "<a href=" + url);
                        }
                        set_webView(newsText, 0);
                    }
                } else {
                    set_webView("No News Found", -1);

                }
            }

            refreshNewsLayout.setRefreshing(false);

        }
    }

    class GetPRZNewsAsync extends AsyncTask<String, Void, Document> {
        String url;

        @Override
        protected Document doInBackground(String... params) {
            this.url = params[0];
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(url)
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
                Elements newsElements = document.getElementsByClass("slider-item");
                if (newsElements != null && newsElements.size() != 0) {
                    String replace1 = "href=\"/";
                    String replace2 = "src=\"//";
                    for (int i = 1; i < newsElements.size(); i++) {
                        String newsText = newsElements.get(i).html();

                        int indexofHref = newsText.indexOf(replace1);
                        if (indexofHref > 0) {
                            newsText = newsText.replaceAll(replace1, "href=\"" + "https://www.prozis.com/");
                        }

                        int indexOfimg_src = newsText.indexOf(replace2);
                        if (indexOfimg_src > 0) {
                            newsText = newsText.replaceAll(replace2, "src=\"" + "https://");
                        }

                        set_webView(newsText, 1);
                    }
                } else {
                    set_webView("No News Found", -1);

                }
            }

            refreshNewsLayout.setRefreshing(false);

        }
    }

    private void set_webView(String news_text, int style) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.news_webview_layout, null);
        WebView webView = (WebView) view.findViewById(R.id.news_webview);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);

        switch (style) {
            case 0:
                news_text = getCSS_Styling_MP(news_text);
                break;
            case 1:
                news_text = getCSS_Styling_PRZ(news_text);
                break;
        }
        webView.loadData(news_text, "text/html; charset=utf-8", "utf-8");
        ll_news_vertical.addView(view);
    }

    private String getCSS_Styling_MP(String bodyHTML) {
        String style = "<style>" +
                "div{" +
                "background-repeat:no-repeat;" +
                "background-position: center center;" +
                "background-size: cover;" +
                "font-size:x-small;" +
                "}" +
                "</style>";
        return "<html><head>" + style + "</head><body>" + bodyHTML + "</body></html>";
    }

    private String getCSS_Styling_PRZ(String bodyHTML) {
        String style = "<style>" +
                "img{" +
                "width:100%;" +
                "align-content:center center;" +
                "}" +
                "</style>";
        return "<html><head>" + style + "</head><body>" + bodyHTML + "</body></html>";
    }
}
