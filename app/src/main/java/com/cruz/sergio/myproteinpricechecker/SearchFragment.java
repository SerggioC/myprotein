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
import android.support.v7.widget.AppCompatCheckedTextView;
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

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.cruz.sergio.myproteinpricechecker.DetailsActivityMyprotein.ADDED_NEW_PROD_REF;
import static com.cruz.sergio.myproteinpricechecker.DetailsActivityMyprotein.HAD_INTERNET_OFF_REF;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.IOEXCEPTION;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.MALFORMED_URL;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.STATUS_NOT_OK;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.STATUS_OK;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UNSUPPORTED_MIME_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.showCustomSlimToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

public class SearchFragment extends Fragment {
    static final int FIRST_ELEMENT = 0;
    static final int NO_RESULTS = 1;
    static final int CARD_INFO = 2;
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
    int pageNumber_MP = 1;
    int pageNumber_PRZ = 1;
    ListView resultsListView;
    ProgressBar horizontalProgressBar;
    Boolean hasAsyncTaskRuning = false;
    boolean btn_clear_visible = false;
    EditText searchTV;
    String[] SUPPORTED_WEBSTORES = new String[]{"myprotein", "prozis", "bulkpowders", "myvitamins"}; // simple name
    String[] WEBSTORES_NAMES = new String[]{"Myprotein", "Prozis", "Bulk Powders", "Myvitamins"}; // Display name
    boolean[] which_webstores_checked = new boolean[]{true, true, false, false};
    ArrayList<String> webstoresToUse = new ArrayList(SUPPORTED_WEBSTORES.length);
    ArrayList<String> webstoreNamesToUse = new ArrayList(WEBSTORES_NAMES.length);
    int webStoreIndex = 0;
    int numberOfWebstoresToUse;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();

//        SharedPreferences sharedPref = mActivity.getPreferences(MODE_PRIVATE);
        SharedPreferences sharedPref = mActivity.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
        for (int i = 0; i < WEBSTORES_NAMES.length; i++) {
            which_webstores_checked[i] = sharedPref.getBoolean(SUPPORTED_WEBSTORES[i], which_webstores_checked[i]);
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
                if (webStoreIndex == numberOfWebstoresToUse) {
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
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                        if (listView.getCheckedItemCount() == 0) {
                            listView.setItemChecked(whichButton, true);
                            which_webstores_checked[whichButton] = true;
                        }
                        // disallow last 2 webstores
                        if (listView.isItemChecked(2)) {
                            ((AppCompatCheckedTextView) listView.getChildAt(2)).setChecked(false);
                            which_webstores_checked[2] = false;
                        }
                        if (listView.isItemChecked(3)) {
                            ((AppCompatCheckedTextView) listView.getChildAt(3)).setChecked(false);
                            which_webstores_checked[3] = false;
                        }
                        if (listView.getCheckedItemCount() == 0) {
                            ((AppCompatCheckedTextView) listView.getChildAt(0)).setChecked(false);
                            listView.setItemChecked(0, true);
                            which_webstores_checked[0] = true;
                        }
                    }
                });

                builderDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webstoresToUse.clear();
                        webstoreNamesToUse.clear();
                        SharedPreferences sharedPref = mActivity.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        for (int i = 0; i < which_webstores_checked.length; i++) {
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
        Intent intent = new Intent(mActivity, DetailsActivityMyprotein.class);
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
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                hasMorePages = true;
                adapter = null;
                resultsListView.setAdapter(null);
                fullListProductCards.clear();
                myproteinProductCards.clear();
                prozisProductCards.clear();
                bulkpowdersProductCards.clear();
                myvitaminsProductCards.clear();
                int storeIndex = 0;

                if (webstoresToUse.contains("myprotein")) {
                    AsyncTask<String, Void, ConnectionObject> myproteinSearch = new MyproteinSearch(storeIndex++);
                    myproteinSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("prozis")) {
                    AsyncTask<String, Void, ConnectionObject> prozisSearch = new ProzisSearch(storeIndex++);
                    prozisSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("bulkpowders")) {
                    AsyncTask<String, Void, ConnectionObject> bulkpowdersSearch = new BulkpowdersSearch(storeIndex++);
                    bulkpowdersSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, URLEncoder.encode(searchString));
                }
                if (webstoresToUse.contains("myvitamins")) {
                    AsyncTask<String, Void, ConnectionObject> myvitaminsSearch = new MyvitaminsSearch(storeIndex++);
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

    class ConnectionObject {
        Document resultDocument;
        int resultStatus;

        ConnectionObject(Document resultDocument, int resultStatus) {
            this.resultDocument = resultDocument;
            this.resultStatus = resultStatus;
        }
    }

    private class MyproteinSearch extends AsyncTask<String, Void, ConnectionObject> {
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
        protected ConnectionObject doInBackground(String... params) {
            Document resultDocument = null;
            int resultStatus;

            try {
                searchString = params[0];
                String nextPage = "";
                if (params.length > 1) {
                    nextPage = StringUtil.isBlank(params[1]) ? "" : params[1];
                }
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String pref_MP_Domain = prefManager.getString("mp_website_location", "pt-pt");
                String shippingCountry = prefManager.getString("mp_shipping_location", "PT"); //"PT";
                String currency = prefManager.getString("mp_currencies", "EUR"); //"EUR";
                String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
                String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
                String queryStrURL = MP_Domain + "elysium.search?search=" + searchString + URL_suffix + nextPage;
                Log.i("Sergio>>>", "MyproteinSearch: querystr=" + queryStrURL);

                resultDocument = Jsoup.connect(queryStrURL)
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
                resultStatus = STATUS_OK;
            } catch (UnsupportedMimeTypeException e1) {
                resultStatus = UNSUPPORTED_MIME_TYPE;
            } catch (java.net.MalformedURLException e2) {
                resultStatus = MALFORMED_URL;
            } catch (java.net.SocketTimeoutException e3) {
                resultStatus = TIMEOUT;
            } catch (HttpStatusException e4) {
                resultStatus = STATUS_NOT_OK;
            } catch (IOException e4) {
                resultStatus = IOEXCEPTION;
            }
            return new ConnectionObject(resultDocument, resultStatus);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ConnectionObject resultObject) {
            super.onPostExecute(resultObject);
            Document resultDocument = resultObject.resultDocument;
            int resultStatus = resultObject.resultStatus;

            Elements resultProductCards = new Elements(0);
            if (resultStatus == STATUS_NOT_OK || resultStatus == IOEXCEPTION || resultStatus == MALFORMED_URL || resultStatus == UNSUPPORTED_MIME_TYPE) {
                NetworkUtils.showCustomSlimToast(mActivity, "Error connecting to " + webstoreNamesToUse.get(thisWebstoreIndex) + " website\nException Code = " + resultStatus, Toast.LENGTH_LONG);
                myproteinProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);
            } else if (resultStatus == TIMEOUT) {
                NetworkUtils.showCustomSlimToast(mActivity, webstoreNamesToUse.get(thisWebstoreIndex) + " website not responding", Toast.LENGTH_LONG);
                myproteinProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);
            } else {
                Element divSearchResults = resultDocument.getElementById("divSearchResults");
                if (divSearchResults != null) {
                    resultProductCards = divSearchResults.getElementsByClass("item"); //Todos os resultados em "cards"
                }
            }

            int rpc_size = resultProductCards.size();
            if (rpc_size == 0) {
//                if (myproteinProductCards.size() > 0) {
//                    // no caso de dar erro ao obter resultados de páginas seguintes
//                } else if (myproteinProductCards.size() == 0) {
//                    // Não obteve resultados
//                }
                if (pageNumber_MP == 1) {
                    myproteinProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                }
                searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);

            } else if (rpc_size > 0) {
                if (pageNumber_MP == 1) {
                    myproteinProductCards.add(new ProductCards(FIRST_ELEMENT, webstoreNamesToUse.get(thisWebstoreIndex)));
                }
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

                    ProductCards productCard = new ProductCards(webstoreNamesToUse.get(thisWebstoreIndex), webstoreNamesToUse.get(thisWebstoreIndex), productID, productTitleStr, productHref, productPrice, imgURL, pptList_stringarray, pptList_SSB, CARD_INFO);
                    myproteinProductCards.add(productCard);
                }

                Element pagination = resultDocument.getElementsByClass("pagination").get(0);
                String pages = pagination.child(0).attr("data-total-pages");
                if (StringUtil.isBlank(pages)) {
                    pages = "1";
                }
                int numPages = Integer.parseInt(pages);
                if (numPages > 1 && hasMorePages) {
                    pageNumber_MP++;
                    AsyncTask<String, Void, ConnectionObject> performSearch = new MyproteinSearch(thisWebstoreIndex);
                    performSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString, "&pageNumber_MP=" + pageNumber_MP);
                }
                if (pageNumber_MP == numPages) {
                    hasMorePages = false;
                    pageNumber_MP = 1;
                    searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, myproteinProductCards);
                }
            }
        }
    }

    class ProzisSearch extends AsyncTask<String, Void, ConnectionObject> {
        String PRZ_Domain = "https://www.prozis.com";
        String searchString;
        int thisWebstoreIndex;
        int searchTypeURL = 1;
        int pagesToSearchPRZ = 2;

        public ProzisSearch(int thisWebstoreIndex) {
            this.thisWebstoreIndex = thisWebstoreIndex;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hasAsyncTaskRuning = true;
        }

        @Override
        protected ConnectionObject doInBackground(String... params) {
            Document resultDocument = null;
            int resultStatus;

            try {
                searchString = params[0];
                if (params.length > 1) {
                    pageNumber_PRZ = Integer.parseInt(params[1]);
                }
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                String prz_country = prefManager.getString("prz_website_location", "pt").toLowerCase();
                String prz_language = prefManager.getString("prz_language", "pt");
                //String queryStrURL = PRZ_Domain + "/" + prz_country + "/" + prz_language + "/" + "search/q/page/" + pageNumber_PRZ + "/text/" + searchString;
                String queryStrURL = PRZ_Domain + "/" + prz_country + "/" + prz_language + "/" + "search/q/ctype/inline/page/" + pageNumber_PRZ + "/text/" + searchString;
                Log.i("Sergio>>>", "ProzisSearch: queryStrURL=" + queryStrURL);

//1              https://www.prozis.com/pt/pt/search/q/text/whey
//2              https://www.prozis.com/pt/pt/search?text=whey
//3              https://www.prozis.com/pt/pt/catalog/search-suggestions?text=whey
//4              https://www.prozis.com/pt/pt/search/q/page/1/text/whey?jsonpCallback=jsonpCallback
//5              https://www.prozis.com/pt/pt/search/q/ctype/inline/page/1/text/whey
                searchTypeURL = 5;

                resultDocument = Jsoup.connect(queryStrURL)
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT)
                        .ignoreContentType(true)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
                resultStatus = STATUS_OK;
            } catch (UnsupportedMimeTypeException e1) {
                resultStatus = UNSUPPORTED_MIME_TYPE;
            } catch (java.net.MalformedURLException e2) {
                resultStatus = MALFORMED_URL;
            } catch (java.net.SocketTimeoutException e3) {
                resultStatus = TIMEOUT;
            } catch (HttpStatusException e4) {
                resultStatus = STATUS_NOT_OK;
            } catch (IOException e4) {
                resultStatus = IOEXCEPTION;
            }
            return new ConnectionObject(resultDocument, resultStatus);
        }

        @Override
        protected void onPostExecute(ConnectionObject resultObject) {
            super.onPostExecute(resultObject);
            Document resultDocument = resultObject.resultDocument;
            int resultStatus = resultObject.resultStatus;

            if (resultStatus == STATUS_NOT_OK || resultStatus == IOEXCEPTION || resultStatus == MALFORMED_URL || resultStatus == UNSUPPORTED_MIME_TYPE) {
                NetworkUtils.showCustomSlimToast(mActivity, "Error connecting to " + webstoreNamesToUse.get(thisWebstoreIndex) + " website\nException Code = " + resultStatus, Toast.LENGTH_LONG);
                prozisProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
            } else if (resultStatus == TIMEOUT) {
                NetworkUtils.showCustomSlimToast(mActivity, webstoreNamesToUse.get(thisWebstoreIndex) + " website not responding", Toast.LENGTH_LONG);
                prozisProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
            } else {
                // ASSERT resultDocument != null
                Elements resultProductCards = new Elements(0);

                if (searchTypeURL == 5) {


                    Element div_Products_list = resultDocument.getElementById("products_list");
                    if (div_Products_list != null) {
                        resultProductCards = div_Products_list.getElementsByClass("product_row"); //Todos os resultados em "cards"
                    }

                    int rpc_size = resultProductCards.size();
                    if (rpc_size == 0) {
                        if (pageNumber_PRZ == 1) {
                            prozisProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                        }
                        searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                        Log.w("Sergio>", this + "onPostExecute: \n" + "no prozis results search type " + searchTypeURL);
                    } else {
                        if (pageNumber_PRZ == 1) {
                            prozisProductCards.add(new ProductCards(FIRST_ELEMENT, webstoreNamesToUse.get(thisWebstoreIndex)));
                        }
                        for (int i = 0; i < rpc_size; i++) {
                            Element singleResult = resultProductCards.get(i);
                            String productURL = PRZ_Domain + singleResult.getElementsByClass("product_imge").attr("href");
                            String imgURL = "https:" + singleResult.getElementsByClass("product_imge").first().child(0).attr("src");
                            String productName = singleResult.getElementsByClass("product_name").text();
                            String productPrice = singleResult.getElementsByClass("product-page-price").text();
                            String brand = singleResult.getElementsByClass("product_brand").text();
                            String productID = singleResult.getElementsByClass("tipsy-add-to-favorites").attr("data-osit-id");
                            String productDescription = singleResult.getElementsByClass("product_description").text();

                            //<a data-osit-lists="" data-osit-id="NUT00/1330530003" href="#" onclick="return false;" class="tipsy-add-to-favorites"><i class="icon-star"></i></a>

                            ProductCards productCard = new ProductCards(webstoreNamesToUse.get(thisWebstoreIndex), brand, productID, productName, productURL, productPrice, imgURL, new ArrayList<String>(), new SpannableStringBuilder(productDescription), CARD_INFO);
                            prozisProductCards.add(productCard);
                        }

                        if (pageNumber_PRZ == pagesToSearchPRZ) {
                            searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                            pageNumber_PRZ = 1;
                        } else {
                            pageNumber_PRZ++;
                            AsyncTask<String, Void, ConnectionObject> prozisSearch = new ProzisSearch(thisWebstoreIndex);
                            prozisSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString, String.valueOf(pageNumber_PRZ));
                        }

                    }


                } else if (searchTypeURL == 1) {

                    Element div_Products_list = resultDocument.getElementById("products_list");
                    if (div_Products_list != null) {
                        resultProductCards = div_Products_list.getElementsByClass("product_col no-touch"); //Todos os resultados em "cards"
                    }

                    int rpc_size = resultProductCards.size();
                    if (rpc_size == 0) {
                        prozisProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                        searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                        Log.w("Sergio>", this + "onPostExecute: \n" + "no prozis results search type 1");
                    } else {
                        if (pageNumber_PRZ == 1) {
                            prozisProductCards.add(new ProductCards(FIRST_ELEMENT, webstoreNamesToUse.get(thisWebstoreIndex)));
                        }
                        for (int i = 0; i < rpc_size; i++) {
                            Element singleResult = resultProductCards.get(i);
                            String productURL = PRZ_Domain + singleResult.getElementsByClass("product_imge").attr("href");
                            String imgURL = "https:" + singleResult.getElementsByClass("product_imge").first().child(0).attr("src");
                            String productName = singleResult.getElementsByClass("product_name").text();
                            String productPrice = singleResult.getElementsByClass("product_price_info").text();
                            String brand = singleResult.getElementsByClass("product_brand").text();
                            String productID = singleResult.getElementsByClass("tipsy-add-to-favorites").attr("data-osit-id");

                            //<a data-osit-lists="" data-osit-id="NUT00/1330530003" href="#" onclick="return false;" class="tipsy-add-to-favorites"><i class="icon-star"></i></a>

                            ProductCards productCard = new ProductCards(webstoreNamesToUse.get(thisWebstoreIndex), brand, productID, productName, productURL, productPrice, imgURL, new ArrayList<String>(), null, CARD_INFO);
                            prozisProductCards.add(productCard);
                        }

                        if (pageNumber_PRZ == pagesToSearchPRZ) {
                            searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                            pageNumber_PRZ = 1;
                        } else {
                            pageNumber_PRZ++;
                            AsyncTask<String, Void, ConnectionObject> prozisSearch = new ProzisSearch(thisWebstoreIndex);
                            prozisSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString, String.valueOf(pageNumber_PRZ));
                        }

                    }


                } else if (searchTypeURL == 3) {
                    Elements div_Products_list = resultDocument.getElementsByClass("search-container");
                    int rpc_size = div_Products_list.size();
                    if (rpc_size == 0) {
                        prozisProductCards.add(new ProductCards(NO_RESULTS, webstoreNamesToUse.get(thisWebstoreIndex)));
                        searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                        Log.w("Sergio>", this + "onPostExecute: \n" + "no results search type 3");
                    } else {
                        if (pageNumber_PRZ == 1) {
                            prozisProductCards.add(new ProductCards(FIRST_ELEMENT, webstoreNamesToUse.get(thisWebstoreIndex)));
                        }
                        for (int i = 0; i < rpc_size; i++) {
                            Element singleResult = resultProductCards.get(i);
                            String productURL = PRZ_Domain + singleResult.getElementsByClass("product_imge").attr("href");
                            String imgURL = "https:" + singleResult.getElementsByClass("product_imge").first().child(0).attr("src");
                            String productName = singleResult.getElementsByClass("product_name").text();
                            String productPrice = singleResult.getElementsByClass("product_price_info").text();
                            String brand = singleResult.getElementsByClass("product_brand").text();
                            ProductCards productCard = new ProductCards(webstoreNamesToUse.get(thisWebstoreIndex), brand, "TODO", productName, productURL, productPrice, imgURL, new ArrayList<String>(), null, CARD_INFO);
                            prozisProductCards.add(productCard);
                        }
                        if (pageNumber_PRZ == pagesToSearchPRZ) {
                            searchCompleteListener.onSearchComplete(true, thisWebstoreIndex, prozisProductCards);
                            pageNumber_PRZ = 1;
                        } else {
                            pageNumber_PRZ++;
                            AsyncTask<String, Void, ConnectionObject> prozisSearch = new ProzisSearch(thisWebstoreIndex);
                            prozisSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, searchString, String.valueOf(pageNumber_PRZ));
                        }
                    }
                }


            }

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
            View view = null;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (product.cardPosition == NO_RESULTS) {
                view = inflater.inflate(android.R.layout.simple_list_item_1, null);
                ((TextView) view.findViewById(android.R.id.text1)).setText("No results from " + product.webstoreName);

            } else if (product.cardPosition == FIRST_ELEMENT) {
                view = inflater.inflate(R.layout.showmore, null);
                ((TextView) view.findViewById(R.id.showmore_TV)).setText("Results from " + product.webstoreName);

            } else if (product.cardPosition == CARD_INFO) {
                //get the inflater and inflate the XML layout for each item
                view = inflater.inflate(R.layout.product_card, null);

                ImageView productImageView = (ImageView) view.findViewById(R.id.product_image);
                String imgURL = product.imgURL;
                if (imgURL != null) {
                    Glide.with(mActivity).load(imgURL).error(R.drawable.noimage).into(productImageView);
                } else {
                    Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
                }

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent();
                        switch (product.webstoreName) {
                            case "Myprotein": {
                                intent = new Intent(mActivity, DetailsActivityMyprotein.class);
                                break;
                            }
                            case "Prozis": {
                                intent = new Intent(mActivity, DetailsActivityProzis.class);
                                break;
                            }
                            case "Bulk Powders": {
                                intent = new Intent(mActivity, DetailsActivityBulkPowders.class);
                                break;
                            }
                            case "Myvitamins": {
                                intent = new Intent(mActivity, DetailsActivityMyvitamins.class);
                                break;
                            }
                        }

                        intent.putExtra("productID", product.productID);
                        intent.putExtra("productBrand", product.productBrand);
                        intent.putExtra("productTitleStr", product.productTitleStr);
                        intent.putExtra("webstoreName", product.webstoreName);
                        intent.putExtra("url", product.productHref);
                        intent.putExtra("image_url", product.imgURL);
                        intent.putExtra("is_web_address", false);
                        intent.putStringArrayListExtra("description", product.pptList_stringarray);
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
                ((TextView) view.findViewById(R.id.product_brand_pc)).setText(product.productBrand);
            }

            return view;
        }
    }

    class ProductCards {
        String productBrand;
        String productID;
        String productTitleStr;
        String productHref;
        String productPrice;
        String imgURL;
        ArrayList<String> pptList_stringarray;
        SpannableStringBuilder pptList_SSB;
        String webstoreName;
        int cardPosition;

        ProductCards(String webstoreName, String productBrand, String productID, String productTitleStr, String productHref, String productPrice, String imgURL, ArrayList<String> pptList_stringarray, SpannableStringBuilder pptList_SSB, int cardPosition) {
            this.productBrand = productBrand;
            this.webstoreName = webstoreName;
            this.productID = productID;
            this.productTitleStr = productTitleStr;
            this.productHref = productHref;
            this.productPrice = productPrice;
            this.imgURL = imgURL;
            this.pptList_stringarray = pptList_stringarray;
            this.pptList_SSB = pptList_SSB;
            this.cardPosition = cardPosition;
        }

        ProductCards(int cardPosition, String webstoreName) {
            this.cardPosition = cardPosition;
            this.webstoreName = webstoreName;
        }

    }


}













