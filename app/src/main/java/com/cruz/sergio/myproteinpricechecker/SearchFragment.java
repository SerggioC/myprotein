package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static com.cruz.sergio.myproteinpricechecker.DetailsActivity.ADDED_NEW_PROD_REF;
import static com.cruz.sergio.myproteinpricechecker.DetailsActivity.HAD_INTERNET_OFF_REF;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.showCustomSlimToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

public class SearchFragment extends Fragment {
    public static SearchCompleteListener searchCompleteListener;
    public static UpdateGraphForNewProduct updateGraphListener;
    public static AddedNewProductListener addedNewProductListener;
    public int SEARCH_REQUEST_CODE = 1;
    public int VOICE_REQUEST_CODE = 2;
    Activity mActivity;
    ArrayAdapter adapter;
    ArrayList<ProductCards> fullListProductCards = new ArrayList<>();
    ArrayList<ProductCards> myproteinProductCards = new ArrayList<>();
    ArrayList<ProductCards> prozisProductCards = new ArrayList<>();
    ArrayList<ProductCards> bulkpowdersProductCards = new ArrayList<>();
    ArrayList<ProductCards> myvitaminsProductCards = new ArrayList<>();
    Boolean hasMorePages = true;
    int pageNumber = 1;
    ListView resultsListView;
    ProgressBar horizontalProgressBar;
    Boolean hasAsyncTaskRuning = false;
    boolean btn_clear_visible = false;
    EditText searchTV;
    String[] SUPPORTED_WEBSTORES = new String[]{"myprotein", "prozis", "bulkpowders", "myvitamins"};
    String[] WEBSTORES_NAMES = new String[]{"Myprotein", "Prozis", "Bulk Powders", "Myvitamins"};
    boolean[] which_webstores_checked = new boolean[]{true, true, true, true};
    ArrayList<String> webstoresToUse = new ArrayList(SUPPORTED_WEBSTORES.length);
    ArrayList<String> webstoreNamesToUse = new ArrayList(WEBSTORES_NAMES.length);
    int webStoreIndex = 0;
    int numberOfWebstoresToUse;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        for (int i = 0; i < WEBSTORES_NAMES.length; i++) {
            which_webstores_checked[i] = sharedPref.getBoolean(SUPPORTED_WEBSTORES[i], true);
            if (which_webstores_checked[i]) {
                webstoresToUse.add(SUPPORTED_WEBSTORES[i]);
                webstoreNamesToUse.add(WEBSTORES_NAMES[i]);
            }
        }
        numberOfWebstoresToUse = webstoresToUse.size();

        searchCompleteListener = new SearchCompleteListener() {
            @Override
            public void onSearchComplete(Boolean isComplete, int thisWebstoreIndex, ArrayList<ProductCards> listProductCards) {
                fullListProductCards.addAll(listProductCards);

                webStoreIndex++;
                if (webStoreIndex < numberOfWebstoresToUse){
                    // Adicionar ao ultimo elemento para fazer "carregar resultados da próxima loja"
                    fullListProductCards.add(new ProductCards(true));
                } else if (webStoreIndex == numberOfWebstoresToUse) {
                    horizontalProgressBar.setVisibility(View.GONE);
                    hasAsyncTaskRuning = false;
                    webStoreIndex = 0;
                }

                if (adapter == null) {
                    adapter = new ProductAdapter(mActivity, R.layout.product_card, fullListProductCards);
                    resultsListView.setAdapter(adapter);
                    adapter.setNotifyOnChange(true);
                } else {
                    adapter.notifyDataSetChanged();
                }

            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resultsListView = (ListView) mActivity.findViewById(R.id.results);
        resultsListView.addHeaderView(View.inflate(mActivity, R.layout.search_result_header_view, null));

        searchTV = (EditText) resultsListView.findViewById(R.id.searchTextView);
        searchTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) { //search no keyboard
                    String querystr = searchTV.getText().toString();
                    checkSearchQuery(querystr);
                    return true;
                }
                return false;
            }

        });

        //final View btn_search = resultsListView.findViewById(R.id.btn_search);
        final View btn_clear = resultsListView.findViewById(R.id.btn_clear);
        final View btn_voice = resultsListView.findViewById(R.id.btn_voice);
        final View btn_options = resultsListView.findViewById(R.id.btn_search_options);

        searchTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && !btn_clear_visible) {
                    //btn_search.setVisibility(View.VISIBLE);
                    btn_clear.setVisibility(View.VISIBLE);
                    btn_voice.setVisibility(View.GONE);
                    btn_clear_visible = true;
                }
                if (s.length() == 0) {
                    //btn_search.setVisibility(View.GONE);
                    btn_clear.setVisibility(View.GONE);
                    btn_voice.setVisibility(View.VISIBLE);
                    btn_clear_visible = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        resultsListView.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String querystr = searchTV.getText().toString();
                checkSearchQuery(querystr);
            }
        });

        btn_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(intent, VOICE_REQUEST_CODE);
                } catch (ActivityNotFoundException a) {
                    NetworkUtils.showCustomToast(mActivity, "Your device doesn't support Speech Recognition", R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                }

            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTV.setText("");
            }
        });

        btn_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builderDialog = new AlertDialog.Builder(mActivity);
                builderDialog.setTitle("Webstores to search from");

                final boolean[] in_which = which_webstores_checked.clone();
                // Creating multiple selection by using setMultiChoiceItem method
                builderDialog.setMultiChoiceItems(WEBSTORES_NAMES, which_webstores_checked, new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                        // Não permitir desmarcar todos os items
                        AlertDialog adialog = (AlertDialog) dialog;
                        ListView listView = adialog.getListView();
                        if (listView.getCheckedItemCount() == 0) {
                            listView.setItemChecked(whichButton, true);
                        }
                    }
                });

                builderDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webstoresToUse.clear();
                        for (int i = 0; i < which_webstores_checked.length; i++) {
                            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putBoolean(SUPPORTED_WEBSTORES[i], which_webstores_checked[i]);
                            editor.commit();
                            if (which_webstores_checked[i]) {
                                webstoresToUse.add(SUPPORTED_WEBSTORES[i]);
                                webstoreNamesToUse.add(WEBSTORES_NAMES[i]);
                            }
                        }
                        numberOfWebstoresToUse = webstoresToUse.size();
                    }
                });

                builderDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                which_webstores_checked = in_which.clone();
                            }
                        });

                AlertDialog alert = builderDialog.create();
                alert.show();
            }
        });

        ArrayList item = new ArrayList(1);
        item.add("");
        ArrayAdapter noAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, item);
        resultsListView.setAdapter(noAdapter);

        horizontalProgressBar = (ProgressBar) resultsListView.findViewById(R.id.progressBarHorizontal);

    }

    public void checkSearchQuery(String querystr) {
        if (Patterns.WEB_URL.matcher(querystr).matches()) {
            boolean isSupportedWebstore = false;
            for (int i = 0; i < SUPPORTED_WEBSTORES.length; i++) {
                if (querystr.contains(SUPPORTED_WEBSTORES[i])) {
                    isSupportedWebstore = true;
                }
            }
            if (isSupportedWebstore) {
                go_to_webAdress_details(querystr);
            } else {
                showCustomSlimToast(mActivity, "Unsupported Webstore\nSend request to support this Webstore", Toast.LENGTH_LONG);
            }
        } else {
            performSearch(querystr);
        }
    }

    private void go_to_webAdress_details(String url_from_querystr) {
        Intent intent = new Intent(mActivity, DetailsActivity.class);
        intent.putExtra("url", url_from_querystr);
        intent.putExtra("is_web_address", true);
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(
                mActivity,
                android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();
        startActivityForResult(intent, SEARCH_REQUEST_CODE, bundle);
    }

    private void hideKeyBoard() {
        try {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mActivity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("Sergio>>>", "hideKeyboard error: ", e);
        }
    }

    public void performSearch(String searchString) {
        hideKeyBoard();
        if (hasAsyncTaskRuning) {
            NetworkUtils.showCustomToast(mActivity, "Search in progress...", R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
        } else {
            if (StringUtil.isBlank(searchString)) {
                NetworkUtils.showCustomToast(mActivity, "Search product name or enter product URL", R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_LONG);
            } else {
                horizontalProgressBar.setVisibility(View.VISIBLE);

                AsyncTask<String, Void, Boolean> internetAsyncTask = new checkInternetAsyncTask();
                internetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString);

            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Quando altera a orientação do ecrã
        resultsListView.setAdapter(adapter);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setUpdateGraphListener(UpdateGraphForNewProduct listener) {
        this.updateGraphListener = listener;
    }

    public void setNewProductListener(AddedNewProductListener listener) {
        this.addedNewProductListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Boolean addedProduct = data.getExtras().getBoolean(ADDED_NEW_PROD_REF);
            if (addedProduct) {
                updateGraphListener.onProductAdded(addedProduct);
                addedNewProductListener.onProductAdded(addedProduct);
            }
            if (data.getExtras().getBoolean(HAD_INTERNET_OFF_REF)) {
                if (noNetworkSnackBar != null) {
                    noNetworkSnackBar = null;
                    makeNoNetworkSnackBar(mActivity);
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                    noNetworkSnackBar.show();
                }
            }
        }
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String voiceText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
            if (voiceText.isEmpty()) {
                NetworkUtils.showCustomToast(mActivity, "Search product name or enter product URL", R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
            } else {
                searchTV.setText(voiceText);
                performSearch(voiceText);
            }
        }

    }


    public interface SearchCompleteListener {
        void onSearchComplete(Boolean isComplete, int thisWebstoreIndex, ArrayList<ProductCards> listProductCards);
    }

    interface UpdateGraphForNewProduct {
        void onProductAdded(Boolean addedNew);
    }

    interface AddedNewProductListener {
        void onProductAdded(Boolean addedNew);
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
                hasMorePages = true;
                resultsListView.setAdapter(null);
                fullListProductCards.clear();
                myproteinProductCards.clear();
                prozisProductCards.clear();
                bulkpowdersProductCards.clear();
                myvitaminsProductCards.clear();
                int storeIndex = 0;

                if (webstoresToUse.contains("myprotein")) {
                    AsyncTask<String, Void, Document> myproteinSearch = new MyproteinSearch(storeIndex++);
                    myproteinSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("prozis")) {
                    AsyncTask<String, Void, Document> prozisSearch = new ProzisSearch(storeIndex++);
                    prozisSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("bulkpowders")) {
                    AsyncTask<String, Void, Document> bulkpowdersSearch = new BulkpowdersSearch(storeIndex++);
                    bulkpowdersSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("myvitamins")) {
                    AsyncTask<String, Void, Document> myvitaminsSearch = new MyvitaminsSearch(storeIndex++);
                    myvitaminsSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }

            } else {
                if (horizontalProgressBar.getVisibility() == View.VISIBLE) {
                    horizontalProgressBar.setVisibility(View.GONE);
                }
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
                hasAsyncTaskRuning = false;
            }
        }
    }

    private class MyproteinSearch extends AsyncTask<String, Void, Document> {
        String searchString;
        int thisWebstoreIndex;

        MyproteinSearch(int thisWebstoreIndex) {
            this.thisWebstoreIndex = thisWebstoreIndex;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hasAsyncTaskRuning = true;
        }

        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument = null;
            try {
                searchString = params[0];
                String nextPage = "";
                if (params.length > 1) {
                    nextPage = StringUtil.isBlank(params[1]) ? "" : params[1];
                }
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
                String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
                String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
                String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
                String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
                String queryStrURL = MP_Domain + "elysium.search?search=" + searchString + URL_suffix + nextPage;

                Log.i("Sergio>>>", "MyproteinSearch: querystr=" + queryStrURL);

                resultDocument = Jsoup.connect(queryStrURL)
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Document resultDocument) {
            super.onPostExecute(resultDocument);
            Elements resultProductCards = new Elements(0);

            if (resultDocument != null) {
                Element divSearchResults = resultDocument.getElementById("divSearchResults");
                if (divSearchResults != null) {
                    resultProductCards = divSearchResults.getElementsByClass("item"); //Todos os resultados em "cards"
                }
            }

            //#divSearchResults > div:nth-child(1) > div:nth-child(1)
            //Log.i("Sergio>>>", "onPostExecute: " + resultProductCards);

            int rpc_size = resultProductCards.size();
            if (rpc_size == 0 && myproteinProductCards != null) {
                if (myproteinProductCards.size() > 0) {
                    // no caso de dar erro ao obter resultados de páginas seguintes
                    searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);

                } else if (myproteinProductCards.size() == 0) {
                    // Não obteve resultados
                    myproteinProductCards.add(new ProductCards(true, webstoreNamesToUse.get(thisWebstoreIndex)));
                }

            } else if (rpc_size > 0) {
                for (int i = 0; i < rpc_size; i++) {
                    Element singleProductCard = resultProductCards.get(i);
                    Element productTitle = singleProductCard.getElementsByClass("product-title").first();
                    String productTitleStr;
                    String productHref;
                    if (productTitle != null) {
                        productTitleStr = productTitle.text();
                        productHref = productTitle.attr("href");
                    } else {
                        //failed getting product title
                        productTitleStr = "(No info)";
                        productHref = "";
                    }

                    String productPrice = singleProductCard.select(".price").first().ownText(); // .removeClass("from-text")
                    String productID = singleProductCard.child(0).attr("data-product-id");
                    //String productID2 = singleProductCard.select("span").first().attr("data-product-id");
                    Element productImage = singleProductCard.select("img").first();
                    String imgURL = null;
                    if (productImage != null) {
                        imgURL = productImage.attr("src"); // https://s1.thcdn.com/ às vezes retorna apenas isto
                        if (imgURL != null && !(imgURL.contains(".jpg") || imgURL.contains(".jpeg") || imgURL.contains(".png") || imgURL.contains(".bmp"))) {
                            imgURL = null;
                        }
                    }
                    Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tick, null);
                    SpannableStringBuilder pptList_SSB = new SpannableStringBuilder();
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    String drawableStr = drawable.toString();

                    ArrayList<String> pptList_stringarray = new ArrayList<>();
                    // Listagem das infos e propriedades do produto * class="product-key-benefits"
                    Elements SingleProductProperties = singleProductCard.getElementsByClass("product-key-benefits");
                    for (Element Properties : SingleProductProperties) {
                        Elements pptList = Properties.select("li");
                        int pptSize = pptList.size();
                        for (int m = 0; m < pptSize; m++) {
                            pptList_stringarray.add(pptList.get(m).text());

                            pptList_SSB.append(drawableStr);
                            pptList_SSB.setSpan(new ImageSpan(drawable), pptList_SSB.length() - drawableStr.length(), pptList_SSB.length(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            pptList_SSB.append(" " + pptList.get(m).text() + "\n");
                        }
                    }

                    ProductCards productCard = new ProductCards(productID, productTitleStr, productHref, productPrice, imgURL, pptList_stringarray, pptList_SSB);
                    myproteinProductCards.add(productCard);
                }


                Element pagination = resultDocument.getElementsByClass("pagination").get(0);
                String pages = pagination.child(0).attr("data-total-pages");
                if (StringUtil.isBlank(pages)) {
                    pages = "1";
                }
                int numPages = Integer.parseInt(pages);
                if (numPages > 1 && hasMorePages) {
                    pageNumber++;
                    AsyncTask<String, Void, Document> performSearch = new MyproteinSearch(thisWebstoreIndex);
                    performSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString, "&pageNumber=" + pageNumber);
                }
                if (pageNumber == numPages) {
                    hasMorePages = false;
                    pageNumber = 1;
                    searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);
                }
            }
        }
    }

    class ProzisSearch extends AsyncTask<String, Void, Document> {
        String searchString;
        int thisWebstoreIndex;

        public ProzisSearch(int thisWebstoreIndex) {
            this.thisWebstoreIndex = thisWebstoreIndex;
        }

        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument;
            try {
                searchString = URLEncoder.encode(params[0]);

                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String pref_PRZ_Domain = prefManager.getString("mp_website_location", "www");
                String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
                String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
                String PRZ_Domain = MyProteinDomain.getHref(pref_PRZ_Domain);
                String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
                String queryStrURL = PRZ_Domain + "elysium.search?search=" + searchString + URL_suffix;

                Log.i("Sergio>>>", "MyproteinSearch: querystr=" + queryStrURL);

                resultDocument = Jsoup.connect(queryStrURL)
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return resultDocument;
        }

        @Override
        protected void onPostExecute(Document document) {
            super.onPostExecute(document);
            ProductCards productCard = new ProductCards("prozis", "prozis", "prozis", "prozis", "prozis", null, null);
            prozisProductCards.add(productCard);
            searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);

        }
    }


    public class ProductAdapter extends ArrayAdapter {
        ArrayList<ProductCards> products;
        Context context;
        int customViewID;

        ProductAdapter(Context context, int customViewID, ArrayList<ProductCards> products) {
            super(context, customViewID, products);
            this.context = context;
            this.customViewID = customViewID;
            this.products = products;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ProductCards product = products.get(position);
            View view;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (product.hasNoResults != null) {
                view = inflater.inflate(android.R.layout.simple_list_item_1, null);
                ((TextView) view.findViewById(android.R.id.text1)).setText("No results from " + product.webstoreName);

            } else if (product.hasNextWebstore != null) {
                view = inflater.inflate(R.layout.showmore, null);
                ((TextView) view.findViewById(R.id.showmore_TV)).setText("Load more results...");

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ProgressBar) v.findViewById(R.id.progressBarShowMore)).setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });

            } else {
                //get the inflater and inflate the XML layout for each item
                view = inflater.inflate(R.layout.product_card, null);

                ImageView productImageView = (ImageView) view.findViewById(R.id.product_image);
                String imgURL = product.imgURL;
                if (imgURL != null) {
                    Glide.with(mActivity).load(imgURL).into(productImageView);
                } else {
                    Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(mActivity, DetailsActivity.class);
                        intent.putExtra("url", product.productHref);
                        intent.putStringArrayListExtra("description", product.pptList_stringarray);
                        intent.putExtra("productID", product.productID);
                        intent.putExtra("image_url", product.imgURL);
                        intent.putExtra("is_web_address", false);

                        //startActivity(intent);
                        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(
                                mActivity,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out).toBundle();
                        startActivityForResult(intent, SEARCH_REQUEST_CODE, bundle);

                    }
                });
                ((TextView) view.findViewById(R.id.titleTextView)).setText(product.productTitleStr);
                ((TextView) view.findViewById(R.id.product_description)).setText(product.pptList_SSB);
                ((TextView) view.findViewById(R.id.price_textView)).setText(product.productPrice);
            }

            return view;
        }
    }

    class ProductCards {
        String productID;
        String productTitleStr;
        String productHref;
        String productPrice;
        String imgURL;
        ArrayList<String> pptList_stringarray;
        SpannableStringBuilder pptList_SSB;
        Boolean hasNextWebstore;
        Boolean hasNoResults;
        String webstoreName;

        ProductCards(String productID, String productTitleStr, String productHref, String productPrice, String imgURL, ArrayList<String> pptList_stringarray, SpannableStringBuilder pptList_SSB) {
            this.productID = productID;
            this.productTitleStr = productTitleStr;
            this.productHref = productHref;
            this.productPrice = productPrice;
            this.imgURL = imgURL;
            this.pptList_stringarray = pptList_stringarray;
            this.pptList_SSB = pptList_SSB;
        }

        ProductCards(Boolean hasNextWebstore) {
            this.hasNextWebstore = hasNextWebstore;
        }

        ProductCards(Boolean hasNoResults, String webstoreName) {
            this.hasNoResults = hasNoResults;
            this.webstoreName = webstoreName;
        }
    }


}













