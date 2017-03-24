package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.noNetworkSnackBar;

public class SearchFragment extends Fragment {
    static SearchFragment thisSearchFragment;
    Activity mActivity;
    ArrayAdapter adapter;
    ArrayList<ProductCards> arrayListProductCards = new ArrayList<>();
    Boolean hasMorePages = true;
    int pageNumber = 1;
    ListView resultsListView;
    ProgressBar horizontalProgressBar;
    String queryStr = "";
    Boolean hasAsyncTaskRuning = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        thisSearchFragment = this;
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

        final EditText searchTextView = (EditText) resultsListView.findViewById(R.id.searchTextView);
        searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) { //search no keyboard
                    String querystr = searchTextView.getText().toString();
                    performSearch(querystr);
                    return true;
                }
                return false;
            }
        });

        resultsListView.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String querystr = searchTextView.getText().toString();
                performSearch(querystr);
            }
        });
        resultsListView.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextView.setText("");
            }
        });
        ArrayList item = new ArrayList(1);
        item.add("");
        ArrayAdapter noAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, item);
        resultsListView.setAdapter(noAdapter);

        horizontalProgressBar = (ProgressBar) resultsListView.findViewById(R.id.progressBarHorizontal);

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
            DetailsFragment.showCustomToast(mActivity, "Ongoing search...", R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_SHORT);
        } else {
            if (!searchString.equals("")) {
                horizontalProgressBar.setVisibility(View.VISIBLE);

                AsyncTask<String, Void, Boolean> internetAsyncTask = new checkInternetAsyncTask();
                internetAsyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, searchString);

            } else if (searchString.equals("")) {
                DetailsFragment.showCustomToast(mActivity, "Nothing to search...", R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_SHORT);
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
                queryStr = MP_Domain + "elysium.search?search=" + searchString + URL_suffix;

                Log.i("Sergio>>>", "performSearch: querystr=" + queryStr);

                hasMorePages = true;
                resultsListView.setAdapter(null);
                arrayListProductCards.clear();

                AsyncTask<String, Void, Document> performSearch = new performSearch();
                performSearch.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryStr);

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

    private class performSearch extends AsyncTask<String, Void, Document> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            hasAsyncTaskRuning = true;
        }

        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .timeout(5000)
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
                resultProductCards = resultDocument.getElementById("divSearchResults").getElementsByClass("item"); //Todos os resultados em "cards"
            }

            //#divSearchResults > div:nth-child(1) > div:nth-child(1)
            //Log.i("Sergio>>>", "onPostExecute: " + resultProductCards);

            if (resultProductCards.size() == 0) {
                ArrayList item = new ArrayList();
                item.add("No Results");
                ArrayAdapter noAdapter = new ArrayAdapter(mActivity, android.R.layout.simple_list_item_1, item);
                resultsListView.setAdapter(noAdapter);
                horizontalProgressBar.setVisibility(View.GONE);
                hasAsyncTaskRuning = false;
            } else if (resultProductCards.size() > 0) {

                for (Element singleProductCard : resultProductCards) { // Selecionar um único "Card" Produto
                    //Log.d("Sergio>>>", "Product Card= "+ singleProductCard);

                    Element productTitle = singleProductCard.getElementsByClass("product-title").first();
                    String productTitleStr;
                    String productHref;
                    if (productTitle != null) {
                        productTitleStr = productTitle.text();
                        productHref = productTitle.attr("href");
                        //Log.d("Sergio>>>", "productTitleStr= " + productTitleStr + "\nhref= " + productHref);
                    } else {
                        //failed getting product title
                        productTitleStr = "(No info)";
                        productHref = "";
                    }

                    String productPrice = singleProductCard.select(".price").first().ownText(); // .removeClass("from-text")
                    //Log.i("Sergio>>>", "productPrice= " + productPrice);

                    String productID = singleProductCard.child(0).attr("data-product-id");
                    //Log.i("Sergio>>>", "productID= " + productID);
                    String productID2 = singleProductCard.select("span").first().attr("data-product-id");
                    //Log.i("Sergio>>>", "productID= " + productID2);

                    Element productImage = singleProductCard.select("img").first();
                    String imgURL = "";

                    if (productImage != null) {
                        imgURL = productImage.attr("src");
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
                    arrayListProductCards.add(productCard);

                }

                Element pagination = resultDocument.getElementsByClass("pagination").get(0);
                String pages = pagination.child(0).attr("data-total-pages");
                if (pages == null || pages.equals("")) {
                    pages = "1";
                }
                int numPages = Integer.parseInt(pages);
                if (numPages > 1 && hasMorePages) {
                    pageNumber++;
                    AsyncTask<String, Void, Document> performSearch = new performSearch();
                    performSearch.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, queryStr + "&pageNumber=" + pageNumber);
                }

                if (pageNumber == numPages) {
                    hasMorePages = false;
                    queryStr = "";
                    pageNumber = 1;

                    adapter = new ProductAdapter(mActivity, R.layout.product_card, arrayListProductCards);
                    resultsListView.setAdapter(adapter);
                    horizontalProgressBar.setVisibility(View.GONE);
                    hasAsyncTaskRuning = false;
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

            //get the inflater and inflate the XML layout for each item
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.product_card, null);

            ImageView productImageView = (ImageView) view.findViewById(R.id.product_image);
            String imgURL = product.imgURL;

            if (imgURL != null && (imgURL.contains(".jpg") || imgURL.contains(".jpeg") || imgURL.contains(".bmp") || imgURL.contains(".png"))) {
                Glide.with(mActivity).load(imgURL).into(productImageView);
            } else {
                //failed getting product image
                Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Bundle productBundle = new Bundle();
                    productBundle.putString("url", product.productHref);
                    productBundle.putStringArrayList("description", product.pptList_stringarray);
                    productBundle.putString("productID", product.productID);
                    DetailsFragment detailsFragment = new DetailsFragment();
                    detailsFragment.setArguments(productBundle);

                    FragmentTransaction ft = MainActivity.mFragmentManager.beginTransaction();
                    ft.hide(getParentFragment());
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.add(android.R.id.content, detailsFragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            });
            ((TextView) view.findViewById(R.id.titleTextView)).setText(product.productTitleStr);
            ((TextView) view.findViewById(R.id.product_description)).setText(product.pptList_SSB);
            ((TextView) view.findViewById(R.id.price_textView)).setText(product.productPrice);
//            view.findViewById(R.id.image_watch).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Uri address = Uri.parse(product.productHref);
//                    Intent browser = new Intent(Intent.ACTION_VIEW, address);
//                    startActivity(browser);
//                }
//            });
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

        ProductCards(String productID, String productTitleStr, String productHref, String productPrice, String imgURL, ArrayList<String> pptList_stringarray, SpannableStringBuilder pptList_SSB) {
            this.productID = productID;
            this.productTitleStr = productTitleStr;
            this.productHref = productHref;
            this.productPrice = productPrice;
            this.imgURL = imgURL;
            this.pptList_stringarray = pptList_stringarray;
            this.pptList_SSB = pptList_SSB;
        }
    }

    class getPrice extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
        protected String doInBackground(String... params) {

            String price = null;
            try {
                Document doc = Jsoup.connect(params[0])
                        .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                        .timeout(5000)
                        .get();
                price = doc.getElementsByClass("priceBlock_current_price").text();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return price;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String price) {
            super.onPostExecute(price);

            Toast theToast = Toast.makeText(getContext(), "Price = " + price, Toast.LENGTH_SHORT);
            theToast.setGravity(Gravity.CENTER, 0, 0);
            theToast.show();
        }

    }

}













