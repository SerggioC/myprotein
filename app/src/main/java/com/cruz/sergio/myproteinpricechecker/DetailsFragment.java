package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DetailsFragment extends Fragment {
    Activity mActivity;
    String url;
    String description;
    String productID;
    TextView priceTV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.details_fragment_layout, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            productID = extras.getString("productID");
            url = extras.getString("url");
            description = extras.getString("description");
            AsyncTask<String, Void, Document> getProductPage = new getProductPage();
            getProductPage.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, url);
        }
        mActivity.findViewById(R.id.open_in_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri address = Uri.parse(url);
                Intent browser = new Intent(Intent.ACTION_VIEW, address);
                startActivity(browser);
            }
        });
        priceTV = (TextView) mActivity.findViewById(R.id.price_tv);

    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentTransaction ft = MainActivity.mFragmentManager.beginTransaction();
        ft.hide(getParentFragment());
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.show(SearchFragment.thisSearchFragment);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showImage(final String image) {

        ImageView imageView = (ImageView) mActivity.findViewById(R.id.dialog_image);

        Glide.with(mActivity).load(image).into(imageView);


    }

    private class getProductPage extends AsyncTask<String, Void, Document> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
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
        protected void onPostExecute(Document resultDocument) {
            super.onPostExecute(resultDocument);

            String url_img480 = resultDocument.getElementsByClass("product-img").first().attr("src");

            //Lista da descrição da activity anterior
            ((TextView) mActivity.findViewById(R.id.p_description)).setText(description);

            //Imagem do produto
            ImageView productImageView = (ImageView) mActivity.findViewById(R.id.p_details_image);
            if (url_img480.contains(".jpg") || url_img480.contains(".bmp") || url_img480.contains(".png") || url_img480.contains(".jpeg")) {
                Glide.with(mActivity).load(url_img480).into(productImageView);
            } else {
                //failed getting product image
                Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
            }


            final String url_img600 = resultDocument.getElementsByClass("product-img-zoom-action").first().attr("href");
            Log.i("Sergio>>>", "onPostExecute: url_img600= " + url_img600);

            productImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImage(url_img600);
                }
            });


            Elements titleElem = resultDocument.getElementsByClass("product-title");
            String title = titleElem.first().text();
            ((TextView) mActivity.findViewById(R.id.title_tv)).append(title);
            String subtitle = resultDocument.getElementsByClass("product-sub-name").text();
            ((TextView) mActivity.findViewById(R.id.p_subtitle)).append(subtitle);

            final ArrayList<HashMap> arraylistHashMap = new ArrayList<>(3);
            final ArrayList<String> opts_id = new ArrayList<>(3);
            Elements productVariations = resultDocument.getElementsByClass("productVariations__select");
            for (Element option : productVariations) {
                Log.i("Sergio>>>", "onPostExecute: " + option.attr("id"));
                opts_id.add(option.attr("id").replace("opts-",""));
                Elements optionBoxes = option.getElementsByAttribute("value");
                LinkedHashMap<String, String> hmap = new LinkedHashMap<>();
                for (Element optionBox_i : optionBoxes) {
                    Log.i("Sergio>>>", "onPostExecute: " + optionBox_i.attr("value"));
                    Log.d("Sergio>>>", "onPostExecute: " + optionBox_i.text());
                    hmap.put(optionBox_i.attr("value"), optionBox_i.text());
                }
                arraylistHashMap.add(hmap);
            }

            ArrayList<String> variationLabels = new ArrayList<>();
            Elements productVariationsLabels = resultDocument.getElementsByClass("productVariations__label");
            for (Element variation : productVariationsLabels) {
                Log.i("Sergio>>>", "onPostExecute: " + variation.text());
                variationLabels.add(variation.text());
            }

            LinearLayout ll_variations = (LinearLayout) mActivity.findViewById(R.id.ll_variations);

            Log.w("Sergio>>>", "onPostExecute: arraylistHashMap" + arraylistHashMap);
            mActivity.findViewById(R.id.progressBar).setVisibility(View.GONE);

            int i = 0;
            final LinearLayout linearLayoutSpiners = (LinearLayout) mActivity.findViewById(R.id.spiners);
            final ArrayList<ArrayList<String>> arrayArrayKeys= new ArrayList<>();
            for (HashMap map : arraylistHashMap) {
                final ArrayList<String> spinnerArrayValues = new ArrayList();
                final ArrayList<String> spinnerArrayKeys = new ArrayList();
                Iterator it = map.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    spinnerArrayValues.add(pair.getValue().toString());
                    spinnerArrayKeys.add(pair.getKey().toString());
                    it.remove(); // avoids a ConcurrentModificationException
                }
                arrayArrayKeys.add(spinnerArrayKeys);
                Log.w("Sergio>>>", "onPostExecute: arrayArrayKeys= " + arrayArrayKeys );
                //Spinner spinner = new Spinner(mActivity);
                final RelativeLayout relativeLayoutSpiners = (RelativeLayout) linearLayoutSpiners.getChildAt(i);
                relativeLayoutSpiners.setVisibility(View.VISIBLE);
                final Spinner oneSpinner = (Spinner) relativeLayoutSpiners.getChildAt(0);

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                        mActivity,
                        R.layout.simple_spinner_item,
                        spinnerArrayValues);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                oneSpinner.setAdapter(spinnerArrayAdapter);
                oneSpinner.setSelection(0, false);
                oneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
                        String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
                        String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
                        String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
                        String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
                        String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
                        String options = "&selected=3";

                        //int optionsCount = linearLayoutSpiners.getChildCount();
                        int optionsCount = arrayArrayKeys.size();
                        for (int j = 0; j < optionsCount; j++) {
                            int selecteditemposition = ((Spinner) ((RelativeLayout) linearLayoutSpiners.getChildAt(j)).getChildAt(0)).getSelectedItemPosition();
                            arrayArrayKeys.get(j);
                            opts_id.get(j);
                            String index = String.valueOf(j + 1);
                            options += "&variation" + index + "=" + opts_id.get(j) + "&option" + index + "=" + arrayArrayKeys.get(j).get(selecteditemposition);
                        }

                        String JSON_URL_Details = MP_Domain + "variations.json?productId=" + productID + options + URL_suffix;
                        //String jsonurl = "https://pt.myprotein.com/variations.json?productId=10530943";
                        //String options = "&selected=3 &variation1=5 &option1=2413 &variation2=6 &option2=2407 &variation3=7&option3=5935"
                        //String mais = "&settingsSaved=Y&shippingcountry=PT&switchcurrency=GBP&countrySelected=Y"

                        Log.d("Sergio>>>", "onItemSelected: JSON_URL_Details " + JSON_URL_Details);

                        AsyncTask<String, Void, JSONObject> getProductJSON = new getProductJSON();
                        getProductJSON.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, JSON_URL_Details);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                TextView textView_Variations = (TextView) ll_variations.getChildAt(i);
                textView_Variations.setText(variationLabels.get(i));
                textView_Variations.setVisibility(View.VISIBLE);
                i++;
            }

            String price = resultDocument.getElementsByClass("priceBlock_current_price").text();
            priceTV.setText(price);
        }
    }

    //JSON SEARCH
    // https://pt.myprotein.com/pt_PT/EUR/elysium.searchjson?search=impact+whey+protein
    //
    private class getProductJSON extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .timeout(5000)
                        .ignoreContentType(true)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(resultDocument.text());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);
            Log.i("Sergio>>>", "onPostExecute: json= " + json);
            String price = null;
            try {
                price = (String) json.get("price");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (price != null) {
                priceTV.setText(price);
            } else {
                priceTV.setText("N/A");
            }
        }
    }
}



























