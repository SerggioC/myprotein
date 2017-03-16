package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
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

import static android.database.DatabaseUtils.dumpCursorToString;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.normalizeDate;

public class DetailsFragment extends Fragment {
    Activity mActivity;
    String url;
    ArrayList<String> description;
    ContentValues productIdContentValues;
    String productID;
    TextView priceTV;
    Fragment thisFragment;
    Boolean gotPrice = true;
    Snackbar noNetworkSnackBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        thisFragment = this;
        productIdContentValues = new ContentValues(); //content values para a DB
        //Reset_DataBase();
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentTransaction ft = MainActivity.mFragmentManager.beginTransaction();
        ft.hide(getParentFragment());
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.show(SearchFragment.thisSearchFragment);
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
            url = extras.getString("url");
            productID = extras.getString("productID");
            description = extras.getStringArrayList("description");

            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL, url);

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tick, null);
            SpannableStringBuilder pptList_SSB = new SpannableStringBuilder();
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            String drawableStr = drawable.toString();
            String description_DB = "";
            for (int i = 0; i < description.size(); i++) {
                pptList_SSB.append(drawableStr);
                pptList_SSB.setSpan(new ImageSpan(drawable), pptList_SSB.length() - drawableStr.length(), pptList_SSB.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                pptList_SSB.append(" " + description.get(i) + "\n");
                description_DB += description.get(i) + "\n";
            }
            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, description_DB);

            //Lista da descrição enviado da activity anterior (SearchFragment.java)
            ((TextView) mActivity.findViewById(R.id.p_description)).setText(pptList_SSB);

            if (NetworkUtils.hasActiveNetworkConnection(mActivity)) {
                // Aqui saca a página do produto em html para depois aplicar o parse com jsoup
                AsyncTask<String, Void, Document> getProductPage = new getProductPage();
                getProductPage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
            } else {
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }
        }

        priceTV = (TextView) mActivity.findViewById(R.id.price_tv);

        mActivity.findViewById(R.id.open_in_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri address = Uri.parse(url);
                Intent browser = new Intent(Intent.ACTION_VIEW, address);
                startActivity(browser);
            }
        });

        //Guardar produto na DB
        mActivity.findViewById(R.id.button_add_to_db).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceTV.getText().toString();

                ContentValues priceContentValues = new ContentValues();
                priceContentValues.put(ProductsContract.PriceEntry.COLUMN_PRODUCT_ID, productID);
                priceContentValues.put(ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE, price);
                priceContentValues.put(ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE_DATE, normalizeDate(System.currentTimeMillis()));

                DBHelper dbHelper = new DBHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID, productID);

                String[] colunas = new String[]{
                        ProductsContract.ProductsEntry._ID,
                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID,
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1,
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2,
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3};

                Cursor cursor_exist_product_id = db.query(
                        ProductsContract.ProductsEntry.TABLE_NAME,
                        colunas,
                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " = " + productID, // WHERE COLUMN_PRODUCT_ID = productID
                        null, null, null, null
                );

/*
                Cursor cursor_exist_product_id = db.rawQuery("SELECT " +
                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " , " +
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1 + " , " +
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2 + " , " +
                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3 +
                        " FROM " + ProductsContract.ProductsEntry.TABLE_NAME +
                        " WHERE " + ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " = " + productID
                        , null);
*/

                Log.d("Sergio>>>", "cursor_exist_product_id.getCount()= " + cursor_exist_product_id.getCount());
                Log.i("Sergio>>>", this + " dumpCursorToString cursor_exist_product_id= " + dumpCursorToString(cursor_exist_product_id));

                int data0 = 0;
                String data1 = "";
                String data2 = "";
                String data3 = "";
                String data4 = "";

                if (cursor_exist_product_id.moveToFirst()) {
                    while (!cursor_exist_product_id.isAfterLast()) {
                        data0 = cursor_exist_product_id.getInt(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry._ID));
                        data1 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID));
                        data2 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1));
                        data3 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2));
                        data4 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3));

                        Log.i("Sergio>>>", "onClick: cursor_exist_product_id " +
                                "\n _ID= " + data0 +
                                "\n COLUMN_PRODUCT_ID= " + data1 +
                                "\n COLUMN_MP_OPTIONS1= " + data2 +
                                "\n COLUMN_MP_OPTIONS2= " + data3 +
                                "\n COLUMN_MP_OPTIONS3= " + data4);
                        cursor_exist_product_id.moveToNext();
                    }
                }
                cursor_exist_product_id.close();

                if (data1.equals(productID) &&
                        data2.equals(productIdContentValues.get(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1)) &&
                        data3.equals(productIdContentValues.get(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2)) &&
                        data4.equals(productIdContentValues.get(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3))) {
                    //Então a variante já está na DataBase
                    Toast.makeText(mActivity, "A variante já está na DataBase", Toast.LENGTH_SHORT).show();
                } else {
                    long productRowID = db.insert(ProductsContract.ProductsEntry.TABLE_NAME, null, productIdContentValues);
                    long priceRowId = db.insert(ProductsContract.PriceEntry.TABLE_NAME, null, priceContentValues);

                    Log.i("Sergio>>>", this + " onClick: productRowID= " + productRowID + " db= " + db);
                    Log.i("Sergio>>>", this + " onClick: PriceRowId= " + priceRowId);
                }

                db.close();
            }
        });

        mActivity.findViewById(R.id.drop_db).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reset_DataBase();
            }
        });

    }

    public void outputFull_PricesDB_toLog() {
        DBHelper dbHelper = new DBHelper(getContext());

        SQLiteDatabase dbread = dbHelper.getReadableDatabase();
        Cursor fullPricesDB_Cursor = dbread.rawQuery("SELECT * FROM " + ProductsContract.PriceEntry.TABLE_NAME, null);
        Log.i("Sergio>>>", this + " dumpCursorToString fullPricesDB_Cursor= " + dumpCursorToString(fullPricesDB_Cursor));

        if (fullPricesDB_Cursor.moveToFirst()) {
            while (!fullPricesDB_Cursor.isAfterLast()) {
                int data0 = fullPricesDB_Cursor.getInt(fullPricesDB_Cursor.getColumnIndex(ProductsContract.PriceEntry._ID));
                String data1 = fullPricesDB_Cursor.getString(fullPricesDB_Cursor.getColumnIndex(ProductsContract.PriceEntry.COLUMN_PRODUCT_ID));
                String data2 = fullPricesDB_Cursor.getString(fullPricesDB_Cursor.getColumnIndex(ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE));
                String data3 = fullPricesDB_Cursor.getString(fullPricesDB_Cursor.getColumnIndex(ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE_DATE));

                Log.i("Sergio>>>", "onClick: PriceEntry \n_ID= " + data0 +
                        "\n COLUMN_PRODUCT_ID= " + data1 +
                        "\n COLUMN_PRODUCT_PRICE= " + data2 +
                        "\n COLUMN_PRODUCT_PRICE_DATE= " + data3);
                fullPricesDB_Cursor.moveToNext();
            }
        }
        Log.e("Sergio>>>", "onClick: alldb cursor= " + fullPricesDB_Cursor);
        dbread.close();
        fullPricesDB_Cursor.close();
    }


    public void outputFull_ProductsDB_toLog() {
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase readableDatabase = dbHelper.getReadableDatabase();
        Cursor fullDB = readableDatabase.rawQuery("SELECT * FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);

        if (fullDB.moveToFirst()) {
            while (!fullDB.isAfterLast()) {
                int data = fullDB.getInt(fullDB.getColumnIndex(ProductsContract.ProductsEntry._ID));
                String data1 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID));
                String data2 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
                String data3 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE));
                String data4 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION));
                String data5 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME));
                String data6 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
                String data7 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_BASE_DOMAIN));
                String data8 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION));
                String data9 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY));
                String data10 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS));
                String data11 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_VARIATION1));
                String data12 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_VARIATION2));
                String data13 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_VARIATION3));
                String data14 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1));
                String data15 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2));
                String data16 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3));
                String data17 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_BASE_IMG_URL));
                String data18 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_ZOOM_IMG_URL));
                String data19 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE));
                long data20 = fullDB.getLong(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE));
                String data21 = fullDB.getString(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
                long data22 = fullDB.getLong(fullDB.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE));

                Log.i("Sergio>>>", "onClick: ProductsEntry " +
                        "\n_ID= " + data +
                        " \nCOLUMN_PRODUCT_ID= " + data1 +
                        " \nCOLUMN_PRODUCT_NAME= " + data2 +
                        " \nCOLUMN_PRODUCT_SUBTITLE= " + data3 +
                        " \nCOLUMN_PRODUCT_DESCRIPTION= " + data4 +
                        " \nCOLUMN_WEBSTORE_NAME= " + data5 +
                        " \nCOLUMN_PRODUCT_BASE_URL= " + data6 +
                        " \nCOLUMN_MP_WEBSTORE_BASE_DOMAIN= " + data7 +
                        " \nCOLUMN_MP_SHIPPING_LOCATION= " + data8 +
                        " \nCOLUMN_MP_CURRENCY= " + data9 +
                        " \nCOLUMN_MP_JSON_URL_DETAILS= " + data10 +
                        " \nCOLUMN_MP_VARIATION1= " + data11 +
                        " \nCOLUMN_MP_VARIATION2= " + data12 +
                        " \nCOLUMN_MP_VARIATION3= " + data13 +
                        " \nCOLUMN_MP_OPTIONS1= " + data14 +
                        " \nCOLUMN_MP_OPTIONS2= " + data15 +
                        " \nCOLUMN_MP_OPTIONS3= " + data16 +
                        " \nCOLUMN_MP_BASE_IMG_URL= " + data17 +
                        " \nCOLUMN_MP_ZOOM_IMG_URL= " + data18 +
                        " \nCOLUMN_MAX_PRICE= " + data19 +
                        " \nCOLUMN_MAX_PRICE_DATE= " + data20 +
                        " \nCOLUMN_MIN_PRICE= " + data21 +
                        " \nCOLUMN_MIN_PRICE_DATE= " + data22
                );
                fullDB.moveToNext();
            }
        }
        Log.e("Sergio>>>", "onClick: alldb cursor2= " + fullDB);

        readableDatabase.close();
        fullDB.close();
    }

    public void Reset_DataBase() {
        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.ProductsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.PriceEntry.TABLE_NAME);
        dbHelper.onCreate(db);
    }

    private class getProductPage extends AsyncTask<String, Void, Document> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.findViewById(R.id.progressBarRound).setVisibility(View.VISIBLE);
        }

        @Override
        protected Document doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
                        .timeout(0)
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

            if (thisFragment.isVisible()) {
                Element titleElem = resultDocument.getElementsByClass("product-title").first();  // Titulo ou nome do produto
                String title;
                if (titleElem == null) {
                    title = "N/A";
                } else {
                    title = titleElem.text();
                }
                ((TextView) mActivity.findViewById(R.id.title_tv)).append(title);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME, title);

                Elements subtitle_element = resultDocument.getElementsByClass("product-sub-name");
                String subtitle;
                if (subtitle_element == null) {
                    subtitle = "N/A";
                } else {
                    subtitle = subtitle_element.text(); //Subtitulo
                }
                ((TextView) mActivity.findViewById(R.id.p_subtitle)).append(subtitle);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE, subtitle);

                Element first_prod_img = resultDocument.getElementsByClass("product-img").first();
                String url_img480 = null;
                if (first_prod_img != null) {
                    url_img480 = first_prod_img.attr("src");
                }
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_BASE_IMG_URL, url_img480);

                //Imagem do produto
                ImageView productImageView = (ImageView) mActivity.findViewById(R.id.p_details_image);
                if (url_img480 != null && (url_img480.contains(".jpg") || url_img480.contains(".bmp") || url_img480.contains(".png") || url_img480.contains(".jpeg"))) {
                    Glide.with(mActivity).load(url_img480).into(productImageView);
                } else {
                    //failed getting product image
                    Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
                }

                //Imagem para aplicar o zoom
                Element first_img_zoom_action = resultDocument.getElementsByClass("product-img-zoom-action").first();
                String url_img600 = null;
                if (first_img_zoom_action != null) {
                    url_img600 = first_img_zoom_action.attr("href");
                }
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_ZOOM_IMG_URL, url_img600);

                Log.i("Sergio>>>", "onPostExecute: url_img600= " + url_img600);

                productImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mActivity, "Clicked Image", Toast.LENGTH_SHORT).show();
                    }
                });

                final ArrayList<HashMap> arraylistHashMap = new ArrayList<>(3);
                final ArrayList<String> opts_id = new ArrayList<>(3);
                Elements productVariations = resultDocument.getElementsByClass("productVariations__select");
                for (Element option : productVariations) {
                    Log.i("Sergio>>>", "onPostExecute: " + option.attr("id"));
                    opts_id.add(option.attr("id").replace("opts-", ""));
                    Elements optionBoxes = option.getElementsByAttribute("value");
                    LinkedHashMap<String, String> hmap = new LinkedHashMap<>();
                    for (Element optionBox_i : optionBoxes) {
                        Log.i("Sergio>>>", "onPostExecute: " + optionBox_i.attr("value") + "= " + optionBox_i.text());

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

                int i = 0;
                final LinearLayout linearLayoutSpiners = (LinearLayout) mActivity.findViewById(R.id.spiners);
                final ArrayList<ArrayList<String>> arrayArrayKeys = new ArrayList<>();
                for (HashMap map : arraylistHashMap) {
                    final ArrayList<String> spinnerArrayValues = new ArrayList();
                    final ArrayList<String> spinnerArrayKeys = new ArrayList();
                    Iterator it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        spinnerArrayValues.add(pair.getValue().toString());
                        spinnerArrayKeys.add(pair.getKey().toString());     //id's
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                    arrayArrayKeys.add(spinnerArrayKeys);
                    Log.w("Sergio>>>", "onPostExecute: arrayArrayKeys= " + arrayArrayKeys);

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
                            getPriceMethod(arrayArrayKeys, linearLayoutSpiners, opts_id);
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

                mActivity.findViewById(R.id.ll_description).setVisibility(View.VISIBLE);

                if (arraylistHashMap.size() > 0) {
                    getPriceMethod(arrayArrayKeys, linearLayoutSpiners, opts_id);
                } else {
                    String price = resultDocument.getElementsByClass("priceBlock_current_price").text();
                    priceTV.setText(price);
                    gotPrice = true;
                    mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);
                }

            } else {
                Toast.makeText(mActivity, "Details Fragment Terminated", Toast.LENGTH_SHORT).show();
            }

        }

        private void getPriceMethod(ArrayList<ArrayList<String>> arrayArrayKeys, LinearLayout linearLayoutSpiners, ArrayList<String> opts_id) {

            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String pref_MP_Domain = prefManager.getString("mp_website_location", "en-gb");
            String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
            String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
            String MP_Domain = MyProteinDomain.getHref(pref_MP_Domain);
            String URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
            String options = "&selected=3";

            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_BASE_DOMAIN, MP_Domain);
            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION, shippingCountry);
            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY, currency);
            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME, "MyProtein");

            for (int j = 0; j < arrayArrayKeys.size(); j++) {
                int selecteditemposition = ((Spinner) ((RelativeLayout) linearLayoutSpiners.getChildAt(j)).getChildAt(0)).getSelectedItemPosition();
                arrayArrayKeys.get(j);
                opts_id.get(j);
                String index = String.valueOf(j + 1);
                options += "&variation" + index + "=" + opts_id.get(j) + "&option" + index + "=" + arrayArrayKeys.get(j).get(selecteditemposition);
                productIdContentValues.put("mp_variation" + index, opts_id.get(j));
                productIdContentValues.put("mp_options" + index, arrayArrayKeys.get(j).get(selecteditemposition));
            }

            String JSON_URL_Details = MP_Domain + "variations.json?productId=" + productID + options + URL_suffix;
            //String jsonurl = "https://pt.myprotein.com/variations.json?productId=10530943";
            //String options = "&selected=3 &variation1=5 &option1=2413 &variation2=6 &option2=2407 &variation3=7 &option3=5935"
            //String mais = "&settingsSaved=Y&shippingcountry=PT&switchcurrency=GBP&countrySelected=Y"

            productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS, JSON_URL_Details);

            Log.d("Sergio>>>", "getPriceMethod: JSON_URL_Details " + JSON_URL_Details);

            DBHelper dbHelper = new DBHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);

            int rows = 0;
            if (cursor.getCount() > 0) {
                //rows = cursor.getInt(0);
            }

            if (rows < 2) {
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, 0);
            } else {
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, 0);
                productIdContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, 0);
            }

            cursor.close();
            db.close();

            if (NetworkUtils.hasActiveNetworkConnection(mActivity)) {
                // Aqui saca o JSON com o preço e outros detalhes do produto
                AsyncTask<String, Void, JSONObject> GetPriceFromJSON = new GetPriceFromJSON();
                GetPriceFromJSON.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, JSON_URL_Details);
            } else {
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }

        }

        public class GetPriceFromJSON extends AsyncTask<String, Void, JSONObject> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.VISIBLE);
                priceTV.setVisibility(View.GONE);
            }

            @Override
            protected JSONObject doInBackground(String... url) {
                Document resultDocument = null;
                try {
                    resultDocument = Jsoup.connect(url[0])
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
                if (thisFragment.isVisible()) {
                    String priceJson = null;
                    try {
                        priceJson = (String) json.get("price");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        priceTV.setText("N/A");
                        gotPrice = false;
                    }
                    if (priceJson != null) {
                        priceTV.setText(priceJson);
                        gotPrice = true;
                    }
                    priceTV.setVisibility(View.VISIBLE);
                    mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.GONE);
                    mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);

                } else {
                    Toast.makeText(mActivity, "Details Fragment Terminated", Toast.LENGTH_SHORT).show();
                }

            }
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
            //Log.i("Sergio>>>", "onPostExecute: json= " + json);

            try {
                Log.d("Sergio>>>", "variations = " + json.getJSONArray("variations"));

                final ArrayList<VariationsObj> variationsObjArrayList = new ArrayList<>();
                for (int i = 0; i < json.getJSONArray("variations").length(); i++) {
                    JSONObject variation_i = (JSONObject) json.getJSONArray("variations").get(i);

                    Log.i("Sergio>>>", "id: " + variation_i.getInt("id"));
                    Log.i("Sergio>>>", "variation: " + variation_i.getString("variation"));
                    Log.d("Sergio>>>", "json.getJSONArray(variations).get(" + i + ") " + variation_i);

                    // As várias opções
                    ArrayList<String> options_id = new ArrayList<>();
                    ArrayList<String> options_name = new ArrayList<>();
                    JSONArray optionsArray = variation_i.getJSONArray("options");
                    for (int j = 0; j < optionsArray.length(); j++) {
                        JSONObject option = optionsArray.getJSONObject(j);
                        Log.i("Sergio>>>", "id = " + option.getInt("id") + "\n name= " + option.getString("name") + "\n value= " + option.getString("value"));
                        options_id.add(String.valueOf(option.getInt("id")));
                        options_name.add(option.getString("name"));
                    }

                    VariationsObj variationsObj = new VariationsObj(String.valueOf(variation_i.getInt("id")), variation_i.getString("variation"), options_id, options_name);
                    variationsObjArrayList.add(variationsObj);
                }

                for (int i = 0; i < variationsObjArrayList.size(); i++) {
                    LinearLayout ll_variations = (LinearLayout) mActivity.findViewById(R.id.ll_variations);
                    TextView textView_Variations = (TextView) ll_variations.getChildAt(i);
                    textView_Variations.setText(variationsObjArrayList.get(i).variation_name);
                }

                final LinearLayout linearLayoutSpiners = (LinearLayout) mActivity.findViewById(R.id.spiners);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
    }

    class VariationsObj {
        String variation_ID;
        String variation_name;
        ArrayList<String> options_id;
        ArrayList<String> options_name;

        VariationsObj(String variation_ID, String variation_name, ArrayList<String> options_id, ArrayList<String> options_name) {
            this.variation_ID = variation_ID;
            this.variation_name = variation_name;
            this.options_id = options_id;
            this.options_name = options_name;
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}




























