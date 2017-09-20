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
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.GETNEWS_ONSTART;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

public class NewsFragment extends Fragment {
    Activity mActivity;
    SwipeRefreshLayout refreshNewsLayout;
    LinearLayout ll_news_vertical;
    RecyclerView newsListView;
    private static final int NUMBER_OF_NEWS_SITES = 2;
    public int news_fetched = 0;
    List<String> list_NewsContent = new ArrayList<>();

    NewsFetchedListener listener;
    public interface NewsFetchedListener {
        void OnNewsFetched(Boolean fetched);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        list_NewsContent.add("header");

        listener = new NewsFetchedListener() {
            @Override
            public void OnNewsFetched(Boolean fetched) {
                if (fetched) {
                    news_fetched++;
                    if (news_fetched == NUMBER_OF_NEWS_SITES) {
                        newsListView.setAdapter(new RecyclerViewAdapter(list_NewsContent));
                    }
                } else {
                    // sem internet
                    newsListView.setAdapter(new RecyclerViewAdapter(list_NewsContent));
                }
                refreshNewsLayout.setRefreshing(false);
            }
        };
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

        newsListView = (android.support.v7.widget.RecyclerView) mActivity.findViewById(R.id.news_listview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        newsListView.setLayoutManager(linearLayoutManager);

        refreshNewsLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.news_swiperefresh);
        refreshNewsLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        newsListView.setAdapter(null);
                        list_NewsContent.clear();
                        list_NewsContent.add("header");
                        news_fetched = 0;
                        getNews();
                    }
                }
        );

        if (GETNEWS_ONSTART) {
            refreshNewsLayout.setRefreshing(true);
            getNews();
        } else {
            list_NewsContent.clear();
            list_NewsContent.add("header");
            newsListView.setAdapter(new RecyclerViewAdapter(list_NewsContent));
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private List<String> mDataset;
        static final int HEADER_VIEW = 0;
        static final int ITEM_VIEW = 1;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            public WebView mWebView;

            public ViewHolder(CardView c) {
                super(c);
                mWebView = (WebView) c.findViewById(R.id.news_webview);
            }

            public ViewHolder(RelativeLayout r) {
                super(r);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public RecyclerViewAdapter(List<String> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        // set the view's size, margins, paddings and layout parameters
        @Override
        public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HEADER_VIEW) {
                RelativeLayout r = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.news_header_view, parent, false);
                return new ViewHolder(r);
            } else {
                CardView c = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.news_webview_layout, parent, false);
                return new ViewHolder(c);
            }
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            if (position > 0) {
                holder.mWebView.loadData(mDataset.get(position), "text/html; charset=utf-8", "utf-8");
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public int getItemViewType(int position) {
            super.getItemViewType(position);
            if (position == 0) {
                return HEADER_VIEW;
            } else {
                return ITEM_VIEW;
            }
        }


    }

    public void getNews() {
//        int childCount = ll_news_vertical.getChildCount();
//        if (childCount > 1) {
//            ll_news_vertical.removeViews(1, childCount - 1);
//        }
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
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
                listener.OnNewsFetched(false);
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
                    String replace = "href=\"/";
                    for (int i = 0; i < newsElements.size(); i++) {
                        Element element = newsElements.get(i);
                        String newsText = element.html();
                        int indexOfHref = newsText.indexOf(replace);
                        if (indexOfHref > 0) {
                            newsText = newsText.replaceAll(replace, "href=\"" + url);
                        }
                        list_NewsContent.add(getCSS_Styling_MP(newsText));

                        //set_webView(newsText, 0);
                    }
                } else {

                    list_NewsContent.add("Myprotein news not available");

                    //set_webView("Myprotein news not available", -1);

                }
            }
            listener.OnNewsFetched(true);
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

                        list_NewsContent.add(getCSS_Styling_PRZ(newsText));

                        //set_webView(newsText, 1);
                    }
                } else {
                    list_NewsContent.add("Prozis news not available");
                    //set_webView("No News Found", -1);

                }
            }

            listener.OnNewsFetched(true);
        }
    }

    private void set_webView(String news_text, int style) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.news_webview_layout, null);
        WebView webView = (WebView) view.findViewById(R.id.news_webview);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

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
