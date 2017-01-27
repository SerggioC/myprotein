package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cruz.sergio.myproteinpricechecker.helper.CreateCardView;
import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Ratan on 7/29/2015.
 */
public class SearchFragment extends Fragment {
    Activity mActivity;
    int i = 0;
    int j = 0;
    int k = 0;
    int l = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText searchTextView = (EditText) getActivity().findViewById(R.id.searchTextView);
        searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    String querystr = searchTextView.getText().toString();

                    if (!querystr.equals("")) {
                        j++;
                        performSearch(querystr);
                    } else if (querystr.equals("")) {
                        Toast theToast = Toast.makeText(getContext(), "Nothing to search", Toast.LENGTH_SHORT);
                        theToast.setGravity(Gravity.CENTER, 0, 0);
                        theToast.show();
                    }

                    return true;
                }
                return false;
            }
        });

        searchTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String querystr = s.toString().trim();

                if (!querystr.equals("")) {
                    j++;
                    //performSearch(querystr);
                }
            }
        });


        mActivity.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextView.setText("");
                k++;
                Toast theToast = Toast.makeText(getContext(), "deleting k= " + k, Toast.LENGTH_SHORT);
                theToast.setGravity(Gravity.CENTER, 0, 0);
                theToast.show();
            }
        });

        mActivity.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String querystr = searchTextView.getText().toString();
                if (!querystr.equals("")) {
                    l++;
                    performSearch(querystr);
                } else if (querystr.equals("")) {
                    Toast theToast = Toast.makeText(getContext(), "Nothing to search", Toast.LENGTH_SHORT);
                    theToast.setGravity(Gravity.CENTER, 0, 0);
                    theToast.show();
                }
            }
        });

    }

    public void performSearch(String searchString) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
        String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
        String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
        String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
        String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
        String queryStr = MP_Domain + "elysium.search?search=" + searchString + URL_suffix;

        Log.i("Sergio>>>", "performSearch: querystr=" + queryStr);

        AsyncTask<String, Void, Document> performSearch = new performSearch();
        performSearch.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, queryStr);
/*
        String MyproteinURL = "http://pt.myprotein.com/sports-nutrition/impact-whey-protein/10530943.html";
        AsyncTask<String, Void, String> getItemPrice = new getPrice();
        getItemPrice.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, MyproteinURL);
*/
    }



    private class performSearch extends AsyncTask<String, Void, Document> {
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
         **/
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
            LinearLayout resultsLinearLayout = (LinearLayout) mActivity.findViewById(R.id.results2);

            //Log.i("Sergio>>>", "onPostExecute: resultDocument= " + resultDocument);

            Element elementsSelected = resultDocument.getElementById("divSearchResults");
//            Log.i("Sergio>>>", "onPostExecute: selected= " + elementsSelected);
//            Log.i("Sergio>>>", "onPostExecute: html= " + elementsSelected.html());

            Elements ProductCards = resultDocument.getElementById("divSearchResults").getElementsByClass("item"); //Todos os resultados em "cards"
            //#divSearchResults > div:nth-child(1) > div:nth-child(1)
            for (Element singleProductCard : ProductCards) { // Selecionar um unico "Card" Produto
                //Log.d("Sergio>>>", "Product Card= "+ singleProductCard);

                Element productTitle = singleProductCard.getElementsByClass("product-title").first();
                String productTitleStr = "";
                String productHref = "";
                if (productTitle != null) {
                    productHref = productTitle.attr("href");
                    productTitleStr = productTitle.text();
                    Log.d("Sergio>>>", "productTitleStr= " + productTitleStr + "\nhref= " + productHref);
                } else {
                    //failed getting product title
                }

                String productPrice = singleProductCard.select(".price").first().ownText(); // .removeClass("from-text")
                Log.i("Sergio>>>", "productPrice= " + productPrice);

                String productID = singleProductCard.child(0).attr("data-product-id");
                Log.i("Sergio>>>", "productID= " + productID);
                String productID2 = singleProductCard.select("span").first().attr("data-product-id");
                Log.i("Sergio>>>", "productID= " + productID2);

                Element productImage = singleProductCard.select("img").first();
                String imgURL = "";
                String pptListStr = "";
                if (productImage != null) {

                    Log.i("Sergio>>>", " productImage.attr= " + productImage.attr("src"));

                    imgURL = productImage.attr("src");


                    Elements SingleProductProperties = singleProductCard.getElementsByClass("product-key-benefits");
                    for (Element Properties : SingleProductProperties) {
                    /*
                    * Listagem das infos e propriedades do produto
                    * class="product-key-benefits"
                    **/
                        Elements pptList = Properties.select("li");
                        int pptSize = pptList.size();

                        for (int m = 0; m < pptSize; m++) {
                            pptListStr += pptList.get(m).text() + "\n";
                        }
                        Log.i("Sergio>>>", "ProductProperties= " + pptListStr);
                    }
                }

                CreateCardView.create(mActivity, resultsLinearLayout, productID, productTitleStr, productHref, productPrice, imgURL, pptListStr);

            }

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













