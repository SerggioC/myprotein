package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static com.cruz.sergio.myproteinpricechecker.R.id.webview;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

/**
 * Created by Sergio on 06/03/2017.
 */

public class VoucherFragment extends Fragment {

    Activity mActivity;
    SwipeRefreshLayout mySwipeRefreshLayout;
    ListView voucherListView;
    LinearLayout ll_scroll;
    String MP_Domain;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.voucher_fragment_layout, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mySwipeRefreshLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getVouchers();
                    }
                }
        );
        ll_scroll = (LinearLayout) mActivity.findViewById(R.id.ll_scroll);
        getVouchers();
    }

    public void getVouchers() {
        int childCount = ll_scroll.getChildCount();
        if (childCount > 1) {
            ll_scroll.removeViews(1, childCount - 1);
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

                String voucher_url_sufix;
                if (pref_MP_Domain.equals("az-az")) {
                    voucher_url_sufix = "vaucer-kodlari.list";
                } else if (pref_MP_Domain.equals("bs-ba") || pref_MP_Domain.equals("sr-rs")) {
                    voucher_url_sufix = "vaucer-kodovi.list";
                } else if (pref_MP_Domain.equals("fr-ca")) {
                    voucher_url_sufix = "codes-de-reduction.list";
                } else if(pref_MP_Domain.equals("pt-pt")) {
                    voucher_url_sufix = "cupoes-desconto-myprotein.list";
                } else {
                    voucher_url_sufix = "voucher-codes.list";
                }

                String voucher_url = MP_Domain + voucher_url_sufix;

                //Log.i("Sergio>>>", "voucher_url=" + voucher_url);

                AsyncTask<String, Void, Document> getVouchersAsync = new GetVouchersAsync();
                getVouchersAsync.execute(voucher_url);

            } else {
                mySwipeRefreshLayout.setRefreshing(false);

                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }
        }
    }

    class GetVouchersAsync extends AsyncTask<String, Void, Document> {
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
                Elements voucherElements = document.getElementsByClass("voucher-info-wrapper");

                if (voucherElements == null || voucherElements.size() == 0) {

                    set_webView("No Vouchers Found");

                } else {
                    for (Element singlevoucherElement : voucherElements) {
                        String voucherText = singlevoucherElement.html();

                        // <a href="/clothing/all-clothing.list" class="voucher-button btn-primary btn">Compra já</a>

                        String replace = "<a href=\"/";
                        int indexOfHref = voucherText.indexOf(replace);
                        if (indexOfHref > 0) {
                            voucherText = voucherText.replaceAll(replace, "<a href=" + MP_Domain);
                        }

                        set_webView(voucherText);
                    }
                }
            }

            mySwipeRefreshLayout.setRefreshing(false);

        }
    }

    private void set_webView(String voucherText) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.voucher_webview, null);
        WebView webView = (WebView) view.findViewById(webview);
        webView.loadData(voucherText, "text/html; charset=utf-8", "utf-8");
        ll_scroll.addView(view);
    }

    private class postToWebviewAdapter extends ArrayAdapter {
        ArrayList<String> items;
        Context context;
        int customViewID;

        postToWebviewAdapter(Context context, int customViewID, ArrayList<String> items) {
            super(context, customViewID, items);
            this.context = context;
            this.customViewID = customViewID;
            this.items = items;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.voucher_webview, null);

            String voucher_code = items.get(position);

            WebView webView = (WebView) view.findViewById(webview);
            webView.loadData(voucher_code, "text/html; charset=utf-8", "utf-8");
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

            return view;

        }
    }

}