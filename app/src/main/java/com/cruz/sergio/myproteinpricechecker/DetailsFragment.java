package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.imageSizesToUse;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.makeNoNetworkSnackBar;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;

public class DetailsFragment extends Fragment {
    Activity mActivity;
    Boolean gotPrice = true;
    ArrayList<String> description;
    ContentValues productContentValues;
    Fragment thisFragment;
    Snackbar noNetworkSnackBar;
    String customProductID;
    String MP_Domain;
    String pref_MP_Locale;
    String productID;
    String url;
    String URL_suffix;
    TextView priceTV;
    LinearLayout ll_variations;
    LinearLayout linearLayoutSpiners;
    Boolean addedNewProduct = false;
    ImageView productImageView;
    Boolean gotImages = false;
    JSONArray JSON_ArrayArray_Images;
    ImageSwitcher image_switcher_details;
    Timer timer;

    ArrayList<String> all_image_sizes;
    final static String[] MP_ALL_IMAGE_TYPES = new String[]{
            "extrasmall",   // 20/20
            "small",        // 50/50
            "smallthumb",   // 60/60
            "thumbnail",    // 70/70
            "smallprod",    // 100/100
            "product",      // 130/130
            "large",        // 180/180
            "list",         // 200/200
            "raw",          // 270/270
            "largeproduct", // 300/300
            "quickview",    // 350/350
            "carousel",     // 480/480
            "extralarge",   // 600/600
            "zoom",         // 960/960
            "magnify"};    // 1600/1600

    final static String[] MP_XX_IMAGE_TYPES = new String[]{
            "20x20",        // 20/20
            "50x50",        // 50/50
            "60x60",        // 60/60
            "70x70",        // 70/70
            "100x100",      // 100/100
            "130x130",      // 130/130
            "180x180",      // 180/180
            "200x200",      // 200/200
            "270x270",      // 270/270
            "300x300",      // 300/300
            "350x350",      // 350/350
            "480x480",      // 480/480
            "600x600",      // 600/600
            "960x960",      // 960/960
            "1600x1600"};   // 1600/1600

    final static String[] MP_BB_IMAGE_TYPES = new String[]{
            "/20/20/",        // 20/20
            "/50/50/",        // 50/50
            "/60/60/",        // 60/60
            "/70/70/",        // 70/70
            "/100/100/",      // 100/100
            "/130/130/",      // 130/130
            "/180/180/",      // 180/180
            "/200/200/",      // 200/200
            "/270/270/",      // 270/270
            "/300/300/",      // 300/300
            "/350/350/",      // 350/350
            "/480/480/",      // 480/480
            "/600/600/",      // 600/600
            "/960/960/",      // 960/960
            "/1600/1600/"};   // 1600/1600

    public static AddedNewProductListener listener;

    interface AddedNewProductListener {
        void onProductAdded(Boolean addedNew);
    }

    public void setNewProductListener(AddedNewProductListener listener) {
        this.listener = listener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        thisFragment = this;
        productContentValues = new ContentValues(); //content values para a DB
        all_image_sizes = new ArrayList<>(Arrays.asList(MP_ALL_IMAGE_TYPES));
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentTransaction ft = MainActivity.mFragmentManager.beginTransaction();
        ft.hide(getParentFragment());
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.show(SearchFragment.thisSearchFragment);
        if (addedNewProduct && listener != null) {
            listener.onProductAdded(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.details_fragment_layout, container, false);
    }

    public int getStatusBarHeight() {
        int height = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) height = getResources().getDimensionPixelSize(resourceId);
        return height;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.details_toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mFragmentManager.popBackStack();
            }
        });
        Resources resources = getResources();
        int dpvalue = 6;
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpvalue, resources.getDisplayMetrics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(pixels);
        } else {
            ViewCompat.setElevation(toolbar, pixels);
        }

        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(mActivity);
        pref_MP_Locale = prefManager.getString("mp_website_location", "en-gb"); // pt-pt
        String shippingCountry = prefManager.getString("mp_shipping_location", "GB"); //"PT";
        String currency = prefManager.getString("mp_currencies", "GBP"); //"EUR";
        MP_Domain = MyProteinDomain.getHref(pref_MP_Locale);
        //URL_suffix = "&settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
        URL_suffix = "settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";

        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL, MP_Domain);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_LOCALE, pref_MP_Locale);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION, shippingCountry);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY, currency);
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME, "MyProtein");

        ll_variations = (LinearLayout) mActivity.findViewById(R.id.ll_variations);
        linearLayoutSpiners = (LinearLayout) mActivity.findViewById(R.id.spiners);
        productImageView = (ImageView) mActivity.findViewById(R.id.p_details_image);
        productImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mActivity, "Clicked Image", Toast.LENGTH_SHORT).show();
            }
        });
        image_switcher_details = (ImageSwitcher) mActivity.findViewById(R.id.image_switcher_details);
        Bundle extras = getArguments();
        if (extras != null) {
            url = extras.getString("url");
            Log.i("Sergio>", this + " onViewCreated: url=\n" + url);
            productID = extras.getString("productID");
            description = extras.getStringArrayList("description");
            String imgURL = extras.getString("image_url");
            customProductID = "loc" + pref_MP_Locale + "pid" + productID;
            url += "?" + URL_suffix;

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID, customProductID);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL, url);

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
            if (description_DB.length() > 0) {
                description_DB = description_DB.substring(0, description_DB.length() - 1); //Remover ultimo caractere \n
            }
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, description_DB);

            //Lista da descrição enviado da activity anterior (SearchFragment.java) com imagens à esquerda
            ((TextView) mActivity.findViewById(R.id.p_description)).setText(pptList_SSB);

            if (imgURL != null) {
                Glide.with(mActivity).load(imgURL).into(productImageView);
            } else {
                Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
            }

            AsyncTask<String, Void, Boolean> get_product_page = new checkInternetAsyncMethods("getProductPage");
            get_product_page.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);

        } else {
            showCustomToast(mActivity, "Error getting product details. Try again.", R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
            MainActivity.mFragmentManager.popBackStack();
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

        /*
         *   Guardar produto na DB ao clicar no botão
         *
        */
        mActivity.findViewById(R.id.button_add_to_db).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeMillis = System.currentTimeMillis();
                String price = priceTV.getText().toString();
                String priceString = price;

                Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9
                Matcher match = regex.matcher(price);
                price = match.replaceAll("");
                price = price.replaceAll(",", ".");
                double price_value = Double.parseDouble(price);

                ContentValues priceContentValues = new ContentValues();
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, priceString);
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, price_value);
                priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, timeMillis);

                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID, productID);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, priceString);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE, price_value);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, timeMillis);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceString);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, price_value);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, timeMillis);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE, priceString);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE, price_value);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE, timeMillis);
                productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRICE_VARIATION, 0);

                if (JSON_ArrayArray_Images != null) { // Imagens do JSON (Mais completo)
                    ArrayList<ArrayList<String>> arrayListArrayListImageURIs = new ArrayList<>();
                    for (int i = 0; i < JSON_ArrayArray_Images.length(); i++) {
                        JSONArray json_array_i = JSON_ArrayArray_Images.optJSONArray(i);
                        ArrayList<String> arrayListImageURIs = new ArrayList<>();
                        for (int j = 0; j < json_array_i.length(); j++) {
                            try {
                                String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                                String url = (String) ((JSONObject) json_array_i.get(j)).get("url");
                                if (url != null) {
                                    for (int k = 0; k < imageSizesToUse.length; k++) {
                                        if (size.equals(imageSizesToUse[k])) {
                                            String filename = customProductID + "_" + imageSizesToUse[k] + "_index_" + i + ".jpg"; // ex.: tamanho 200x200 imageSizesToUse
                                            saveImageWithGlide(url, filename); //guarda as imagens todas. index_i=variação imageSizesTounUse[k]=tamanho
                                            arrayListImageURIs.add(filename);

                                            ((JSONObject) JSON_ArrayArray_Images.optJSONArray(i).get(j)).put("file", filename);

                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        arrayListArrayListImageURIs.add(arrayListImageURIs);
                    }
                    Log.w("Sergio>", this + "\nonClick: \n" + "JSON_ArrayArray_Images=\n" + JSON_ArrayArray_Images);

                    Log.i("Sergio>", this + "onClick:\narrayListArrayListImageURIs=\n" + arrayListArrayListImageURIs);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES, JSON_ArrayArray_Images.toString().replace("\\", ""));

                }

                DBHelper dbHelper = new DBHelper(getContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Cursor exists_CustomPID = db.rawQuery("SELECT 1 FROM " +
                        ProductsContract.ProductsEntry.TABLE_NAME + " WHERE " +
                        ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " = '" + customProductID + "' LIMIT 1", null); //Atenção à single quote (')
//                Cursor exists_CustomPID = db.query(ProductsContract.ProductsEntry.TABLE_NAME,
//                        new String[] {ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID},
//                        ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " = '" + customProductID + "' LIMIT 1", //Atenção à single quote (')
//                        null, null, null, null
//                        );
                int numEntries = exists_CustomPID.getCount();
                exists_CustomPID.close();
//                String[] colunas = new String[]{
//                        ProductsContract.ProductsEntry._ID,
//                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID,
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1,
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2,
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3,
//                        ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID
//                };
//
//                Cursor cursor_exist_product_id = db.query(
//                        ProductsContract.ProductsEntry.TABLE_NAME,
//                        colunas,    // SELECT (Colunas a selecionar)
//                        //ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " = " + customProductID + "1",
//                        WHERE COLUMN_CUSTOM_PRODUCT_ID = customProductID (linhas a apresentar)
//                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " = " + productID, // WHERE COLUMN_CUSTOM_PRODUCT_ID = customProductID (linhas a apresentar)
//                        null, null, null, null
//                );
//
//                Cursor cursor_exist_product_id = db.rawQuery("SELECT " +
//                        ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " , " +
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1 + " , " +
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2 + " , " +
//                        ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3 +
//                        " FROM " + ProductsContract.ProductsEntry.TABLE_NAME +
//                        " WHERE " + ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " = " + productID
//                        , null);

//
//                Log.i("Sergio>>>", "cursor_exist_product_id.getCount()= " + cursor_exist_product_id.getCount());
//                Log.i("Sergio>>>", this + " dumpCursorToString cursor_exist_product_id= \n" + dumpCursorToString(cursor_exist_product_id));
//
//                int data0;
//                String data1;
//                String data2;
//                String data3;
//                String data4;
//                String data5;
//                if (cursor_exist_product_id.moveToFirst()) {
//                    while (!cursor_exist_product_id.isAfterLast()) {
//                        data0 = cursor_exist_product_id.getInt(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry._ID));
//                        data1 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID));
//                        data2 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1));
//                        data3 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2));
//                        data4 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3));
//                        data5 = cursor_exist_product_id.getString(cursor_exist_product_id.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID));
//
//                        Log.i("Sergio>>>", "onClick: cursor_exist_product_id " +
//                                "\n _ID= " + data0 +
//                                "\n COLUMN_PRODUCT_ID= " + data1 +
//                                "\n COLUMN_MP_OPTIONS1= " + data2 +
//                                "\n COLUMN_MP_OPTIONS2= " + data3 +
//                                "\n COLUMN_MP_OPTIONS3= " + data4 +
//                                "\n COLUMN_CUSTOM_PRODUCT_ID= " + data5);
//                        cursor_exist_product_id.moveToNext();
//                        Log.w("Sergio>>>", "onClick: " + "data5= " + data5 + " COLUMN_CUSTOM_PRODUCT_ID= " + productContentValues.get(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID));
//                        if (data5.equals(productContentValues.get(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID)))
//                            isInDataBase = true;
//                    }
//                }
//                cursor_exist_product_id.close();
                //}
                if (numEntries >= 1) { // Só poderá haver uma entrada
                    showCustomToast(mActivity, "Product already in DataBase!",
                            R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_LONG);
                } else {
                    long productRowID = db.insert(ProductsContract.ProductsEntry.TABLE_NAME, null, productContentValues);
                    if (productRowID < 0L) {
                        showCustomToast(mActivity, "Error inserting product to DataBase " +
                                        ProductsContract.ProductsEntry.TABLE_NAME + "! Try again.",
                                R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                    } else {
                        // A _ID do produto vai entrar para a Tabela dos preços
                        // ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS = ProductsContract.ProductsEntry._ID
                        // Podem existir vários _id_products iguais na tabela de preços
                        priceContentValues.put(ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS, productRowID);
                        long priceRowId = db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, priceContentValues);
                        if (priceRowId < 0L) {
                            showCustomToast(mActivity, "Error inserting product to DataBase " +
                                            ProductsContract.PricesEntry.TABLE_NAME + "! Try again.",
                                    R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                        } else {
                            showCustomToast(mActivity, "Now following product price!",
                                    R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);
                            addedNewProduct = true;
                        }
                        Log.w("Sergio>>>", this + " onClick: db= " + db);
                        Log.i("Sergio>>>", this + " onClick: productRowID= " + productRowID + " PriceRowId= " + priceRowId);
                        Log.d("Sergio>>>", this + " onClick: productContentValues= " + productContentValues);
                    }
                }
                db.close();
            }
        });

    }

    public boolean saveImageWithGlide(String imageURL, final String filename) {
        final Boolean[] has_saved_image = {false};
        Glide.with(mActivity)
                .load(imageURL)
                .asBitmap()
                .toBytes(Bitmap.CompressFormat.JPEG, 100)
                .asIs()
                .format(PREFER_ARGB_8888)
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(new SimpleTarget<byte[]>() {
                    @Override
                    public void onResourceReady(final byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... params) {
                                FileOutputStream outputStream;
                                try {
                                    outputStream = mActivity.openFileOutput(filename, Context.MODE_PRIVATE);
                                    outputStream.write(resource);
                                    outputStream.flush();
                                    outputStream.close();
                                    has_saved_image[0] = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    has_saved_image[0] = false;
                                }
                                return null;
                            }
                        }.execute();
                    }
                });
        return has_saved_image[0];
    }

    public StringBuffer readFile(String fileName) {
        try {
            File file = new File(mActivity.getFilesDir(), fileName);
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = input.readLine()) != null) {
                buffer.append(line);
            }
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                        .userAgent(userAgent)
                        .method(Connection.Method.GET)
                        .timeout(NET_TIMEOUT) //sem limite de tempo
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
                if (resultDocument != null) {
                    Element titleElem = resultDocument.getElementsByClass("product-title").first();  // Titulo ou nome do produto
                    String title = titleElem != null ? titleElem.text() : "N/A";
                    ((TextView) mActivity.findViewById(R.id.title_tv)).append(title);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME, title);

                    Elements subtitle_element = resultDocument.getElementsByClass("product-sub-name");
                    String subtitle = subtitle_element != null ? subtitle_element.text() : "N/A";
                    ((TextView) mActivity.findViewById(R.id.p_subtitle)).append(subtitle);
                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE, subtitle);

                    // Labels: Sabor, Quantidade, Embalagem
                    Elements productVariationsLabels = resultDocument.getElementsByClass("productVariations__label");
                    int pvl_size = productVariationsLabels.size();
                    if (pvl_size > 0) {
                        for (int i = 0; i < pvl_size; i++) {
                            String variationText = productVariationsLabels.get(i).text();
                            TextView textView_Variations = (TextView) ll_variations.getChildAt(i);
                            textView_Variations.setText(variationText);
                            textView_Variations.setVisibility(View.VISIBLE);
                            RelativeLayout rL_Spiners = (RelativeLayout) linearLayoutSpiners.getChildAt(i);
                            rL_Spiners.setVisibility(View.VISIBLE);
                            int vIndex = i + 1;
                            productContentValues.put("mp_variation_name" + vIndex, variationText);
                        }
                        get_Available_Options();

                    } else {
                        // Sem opções de sabor, embalagem, tamanho para selecionar
                        String price = resultDocument.getElementsByClass("priceBlock_current_price").text();

                        Log.w("Sergio>", this + " onPostExecute: \n" + "price=\n" + price);

                        Pattern regex = Pattern.compile("[.,\\d]+"); // matches . , e números de 0 a 9
                        Matcher match = regex.matcher(price);
                        String currency_symbol = match.replaceAll("");
                        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL, currency_symbol);

                        getImagesFromScriptTag(resultDocument);

                        priceTV.setText(price);
                        gotPrice = true;
                        mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);
                        mActivity.findViewById(R.id.button_add_to_db).setEnabled(true);
                        mActivity.findViewById(R.id.ll_description).setVisibility(View.VISIBLE);

                    }
                } else {
                    showCustomToast(mActivity, "Error getting webpage.",
                            R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                }

            } else {
                Toast.makeText(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT).show();
            }

        }

        private void getImagesFromScriptTag(Document resultDocument) {
            Elements scriptTags = resultDocument.getElementsByTag("script");
            for (Element tag : scriptTags) {
                for (DataNode node : tag.dataNodes()) {
                    String script = node.getWholeData();
                    if (script.contains("arProductImages")) {
                        JSON_ArrayArray_Images = new JSONArray();

                        int indexOfarray = script.indexOf("arProductImages[");

                        while (indexOfarray >= 0) {
                            String sub_script = script.substring(indexOfarray, script.indexOf(");", indexOfarray));
                            JSONArray inner_array = new JSONArray();

                            int indexOfHttps = sub_script.indexOf("https");
                            while (indexOfHttps >= 0) {
                                String imgURL = sub_script.substring(indexOfHttps, sub_script.indexOf(".jpg", indexOfHttps) + 4);

                                String size = "";
                                for (int k = 0; k < MP_BB_IMAGE_TYPES.length; k++) {
                                    if (imgURL.contains(MP_BB_IMAGE_TYPES[k])) {
                                        size = MP_XX_IMAGE_TYPES[k];
                                    }
                                }

                                JSONObject innerObject = new JSONObject();
                                try {
                                    innerObject.put("url", imgURL);
                                    innerObject.put("size", size);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                inner_array.put(innerObject);

                                indexOfHttps = sub_script.indexOf("https", indexOfHttps + 1);
                            }

                            JSON_ArrayArray_Images.put(inner_array);

                            indexOfarray = script.indexOf("arProductImages[", indexOfarray + 1);
                        }
                        Log.i("Sergio>", this + "\ngetImagesFromScriptTag:\nJSON_ArrayArray_Images from Base=\n" + JSON_ArrayArray_Images);
                    }
                }
            }
        }
    }

    private void get_Available_Options() {
        String full_JSON_URL = MP_Domain + "variations.json?productId=" + productID + "&" + URL_suffix;
        AsyncTask<String, Void, Boolean> checkinternetAsyncTask = new checkInternetAsyncMethods("get_Available_Options");
        checkinternetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, full_JSON_URL);
        Log.i("Sergio>", this + " get_Available_Options: \nfull_JSON_URL=\n" + full_JSON_URL);
    }

    public class checkInternetAsyncMethods extends AsyncTask<String, Void, Boolean> {
        String method;
        String backGround_param;

        checkInternetAsyncMethods(String method) {
            this.method = method;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            backGround_param = params[0];
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                switch (method) {
                    case "get_Available_Options": {
                        AsyncTask<String, Void, JSONObject> getDetailsFromJSON = new GetDetailsFromJSON();
                        getDetailsFromJSON.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, backGround_param);
                        break;
                    }
                    case "GetPriceFromJSON": {
                        AsyncTask<String, Void, JSONObject> GetPriceFromJSON = new GetPriceFromJSON();
                        GetPriceFromJSON.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, backGround_param);
                        break;
                    }
                    case "getProductPage": {
                        AsyncTask<String, Void, Document> getProductPage = new getProductPage();
                        getProductPage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, backGround_param);
                        break;
                    }
                    default: {
                        Log.w("Sergio>", this + " onPostExecute: invalid method given in switch");
                        break;
                    }
                }

            } else {
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                    Log.w("Sergio>", this + " onPostExecute: \n" + "noNetworkSnackBar is null or not shown=\n" + noNetworkSnackBar);
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }
        }
    }

    private class GetDetailsFromJSON extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... url_param) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(url_param[0])
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT) //sem limite de tempo para receber a página
                        .ignoreContentType(true) // ignorar o tipo de conteúdo
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = null;
            try {
                jsonObject = resultDocument == null ? null : new JSONObject(resultDocument.text());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);
            if (thisFragment.isVisible()) {

                if (json != null) {
                    try {
                        JSONArray variations_Array = json.getJSONArray("variations"); //3

                        final ArrayList<String> variation_ids = new ArrayList<>();
                        final ArrayList<ArrayList<String>> arrayArray_opt_ids = new ArrayList<>();
                        ArrayList<ArrayList<String>> arrayArray_opt_names = new ArrayList<>();
                        for (int i = 0; i < variations_Array.length(); i++) {

                            JSONObject variation_i = (JSONObject) variations_Array.get(i);
                            String variation_id = String.valueOf(variation_i.getInt("id"));
                            variation_ids.add(variation_id);

                            ArrayList<String> arraylist_options_ids = new ArrayList<>();
                            ArrayList<String> arraylist_options_names = new ArrayList<>();

                            JSONArray variation_options = variation_i.getJSONArray("options");
                            for (int j = 0; j < variation_options.length(); j++) {
                                JSONObject option_i = (JSONObject) variation_options.get(j);
                                String option_id = String.valueOf(option_i.getInt("id"));
                                String option_name = option_i.getString("name");
                                arraylist_options_ids.add(option_id);
                                arraylist_options_names.add(option_name);
                            }
                            arrayArray_opt_ids.add(arraylist_options_ids);
                            arrayArray_opt_names.add(arraylist_options_names);

                            final RelativeLayout relativeLayoutSpiners = (RelativeLayout) linearLayoutSpiners.getChildAt(i);
                            final Spinner oneSpinner = (Spinner) relativeLayoutSpiners.getChildAt(0);

                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                                    mActivity,
                                    R.layout.simple_spinner_item,
                                    arraylist_options_names);
                            spinnerArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                            oneSpinner.setAdapter(spinnerArrayAdapter);
                            oneSpinner.setSelection(0, false);
                            oneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    gotImages = false;
                                    addedNewProduct = false;
                                    getPriceMethod(arrayArray_opt_ids, variation_ids);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                        }
                        Log.d("Sergio>", this + " onPostExecute: \nvariation_ids=\n" + variation_ids);
                        Log.d("Sergio>", this + " onPostExecute: \narrayArray_opt_ids=\n" + arrayArray_opt_ids);
                        Log.d("Sergio>", this + " onPostExecute: \narraylist_options_names=\n" + arrayArray_opt_names);

                        getPriceMethod(arrayArray_opt_ids, variation_ids);

                        mActivity.findViewById(R.id.ll_description).setVisibility(View.VISIBLE);


                    } catch (JSONException erro) {
                        erro.printStackTrace();
                        Log.w("Sergio>", this + " onPostExecute: falhou ao fazer o parse do json \n exception = " + erro);
                    }
                } else {
                    Log.w("Sergio>", this + " onPostExecute: falhou ao sacar o json ");
                    mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);
                }

            } else {
                Toast.makeText(mActivity, "Details Fragment Terminated", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void getPriceMethod(ArrayList<ArrayList<String>> arrayArrayKeys, ArrayList<String> opts_id) {
        mActivity.findViewById(R.id.button_add_to_db).setEnabled(false);
        String options = "&selected=3";
        customProductID = "loc" + pref_MP_Locale + "pid" + productID;

        for (int j = 0; j < arrayArrayKeys.size(); j++) {
            Spinner spinner = (Spinner) ((RelativeLayout) linearLayoutSpiners.getChildAt(j)).getChildAt(0);
            int selecteditemposition = spinner.getSelectedItemPosition();
            String spinnerValue = spinner.getSelectedItem().toString();
            arrayArrayKeys.get(j);
            opts_id.get(j);
            String index = String.valueOf(j + 1);
            options += "&variation" + index + "=" + opts_id.get(j) + "&option" + index + "=" + arrayArrayKeys.get(j).get(selecteditemposition);
            productContentValues.put("mp_variation" + index, opts_id.get(j));
            productContentValues.put("mp_options" + index, arrayArrayKeys.get(j).get(selecteditemposition));
            productContentValues.put("mp_options_name" + index, spinnerValue);
            customProductID += "vid" + index + opts_id.get(j) + "oid" + index + arrayArrayKeys.get(j).get(selecteditemposition);
        }
        // vid = variation id / oid = option id
        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID, customProductID);
        Log.d(" Sergio>>>", this + " getPriceMethod: customProductID =" + customProductID);

        String JSON_URL_Details = MP_Domain + "variations.json?productId=" + productID + options + "&" + URL_suffix;
        //String jsonurl = "https://pt.myprotein.com/variations.json?productId=10530943";
        //String options = "&selected=3 &variation1=5 &option1=2413 &variation2=6 &option2=2407 &variation3=7 &option3=5935"
        //String mais = "&settingsSaved=Y&shippingcountry=PT&switchcurrency=GBP&countrySelected=Y"

        productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS, JSON_URL_Details);

        Log.i("Sergio>>>", "getPriceMethod: \nJSON_URL_Details=\n" + JSON_URL_Details);

        DBHelper dbHelper = new DBHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);

        int rows = 0;
        if (cursor.getCount() > 0) {
            //rows = cursor.getInt(0);
        }

        if (rows < 2) {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, 0);
        } else {
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, 0);
        }

        cursor.close();
        db.close();

        AsyncTask<String, Void, Boolean> checkinternetAsyncTask = new checkInternetAsyncMethods("GetPriceFromJSON");
        checkinternetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, JSON_URL_Details);

    }

    public class GetPriceFromJSON extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            priceTV.setVisibility(View.GONE);
            mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... url_param) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(url_param[0])
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT) //sem limite de tempo para receber a página
                        .ignoreContentType(true) // ignorar o tipo de conteúdo
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            JSONObject jsonObject = null;
            try {
                jsonObject = resultDocument == null ? null : new JSONObject(resultDocument.text());
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
                if (json != null) {
                    try {
                        priceJson = (String) json.get("price");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        priceTV.setText("N/A");
                        gotPrice = false;
                    }
                } else {
                    priceTV.setText("N/A");
                    gotPrice = false;
                }

                if (priceJson != null) {
                    priceTV.setText(priceJson);
                    gotPrice = true;
                    mActivity.findViewById(R.id.button_add_to_db).setEnabled(true);
                    Pattern regex = Pattern.compile("[.,\\d]+"); // matches . , e números de 0 a 9
                    Matcher match = regex.matcher(priceJson);
                    String currency_symbol = match.replaceAll("");

                    productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL, currency_symbol);

                } else {
                    gotPrice = false;
                }
                if (!gotImages) {
                    //
                    //  Criar o JSONArray com array das imagens a colocar na base de dados. no array está um JSON Object ()
                    //
                    try {
                        // Parse do url das imagens se existirem no json
                        JSONArray json_images = json.getJSONArray("images");

                        JSON_ArrayArray_Images = new JSONArray();
                        JSONArray inner_array = new JSONArray();

                        int image_index = 0;
                        for (int i = 0; i < json_images.length(); i++) {
                            JSONObject image_i = (JSONObject) json_images.get(i);
                            int current_img_index = image_i.getInt("index");                            // 0, 1, 2...
                            String image_type = image_i.getString("type");                              // tamanho: "small", "extralarge" "zoom" ...
                            image_type = MP_XX_IMAGE_TYPES[all_image_sizes.indexOf(image_type)];        // Traduzir o nome para 20x20, 100x100 ...
                            String image_url = "https://s4.thcdn.com/" + image_i.getString("name");     // url

                            if (current_img_index == image_index) {
                                JSONObject innerObject = new JSONObject();
                                innerObject.put("size", image_type);
                                innerObject.put("url", image_url);
                                inner_array.put(innerObject);

                            } else {
                                JSON_ArrayArray_Images.put(inner_array);
                                JSONObject innerObject = new JSONObject();
                                innerObject.put("size", image_type);
                                innerObject.put("url", image_url);
                                inner_array = new JSONArray();
                                inner_array.put(innerObject);

                                image_index = current_img_index;
                            }
                        }
                        JSON_ArrayArray_Images.put(inner_array);
                        Log.i("Sergio>", this + "onPostExecute:\nJSON_ArrayArray_Images=\n" + JSON_ArrayArray_Images.toString().replace("\\", ""));

                        // Carregar imagens no ecrã atual (details)
                        ArrayList<String> arrayListImageURLsToLoad = new ArrayList<>();
                        for (int i = 0; i < JSON_ArrayArray_Images.length(); i++) {
                            JSONArray json_array_i = JSON_ArrayArray_Images.optJSONArray(i);
                            String urlToLoad = null;
                            for (int j = 0; j < json_array_i.length(); j++) {
                                try {
                                    String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                                    String url = (String) ((JSONObject) json_array_i.get(j)).get("url");
                                    if (url != null) {
                                        for (int k = 0; k < imageSizesToUse.length; k++) {
                                            if (size.equals(imageSizesToUse[k])) {
                                                urlToLoad = url;
                                                gotImages = true;
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    gotImages = false;
                                }
                            }
                            if (gotImages) {
                                arrayListImageURLsToLoad.add(urlToLoad);
                            }
                        }
                        if (gotImages) {
                            placeImagesFromURL_Details(arrayListImageURLsToLoad);
                            productImageView.setVisibility(View.GONE);
                            image_switcher_details.setVisibility(View.VISIBLE);
                            Log.i("Sergio>", this + "bindView:\narrayListImageURLsToLoad=\n" + arrayListImageURLsToLoad);
                        }

                    } catch (JSONException erro) {
                        erro.printStackTrace();
                        if (erro.toString().contains("No value for images")) {
                            gotImages = false;
                        }
                        Log.w("Sergio>", this + " onPostExecute: falhou ao fazer o parse do json \n exception = " + erro);
                    }

                }

                mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.GONE);
                priceTV.setVisibility(View.VISIBLE);
                mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);

            } else {
                Toast.makeText(mActivity, "Details Fragment Terminated", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void placeImagesFromURL_Details(final ArrayList<String> arrayListImageURLsToLoad) {
        final int size = arrayListImageURLsToLoad.size();
        final ArrayList<Bitmap> arrayListImageBitmap = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            final int finalI = i;
            Glide.with(mActivity)
                    .load(arrayListImageURLsToLoad.get(i))
                    .asBitmap()
                    .asIs()
                    .format(PREFER_ARGB_8888)
                    .dontTransform()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(final Bitmap resource, GlideAnimation glideAnimation) {
                            arrayListImageBitmap.add(resource);
                            if (finalI == size - 1) {
                                if (timer != null) {
                                    timer.cancel();
                                    timer.purge();
                                    timer = new Timer();
                                } else {
                                    timer = new Timer();
                                }
                                bitmapsReady(arrayListImageBitmap);
                            }
                        }
                    });
        }

    }

    private void bitmapsReady(final ArrayList<Bitmap> arrayListImageBitmap) {
        final int size = arrayListImageBitmap.size();
        image_switcher_details.removeAllViews();
        image_switcher_details.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                return getNewImageView(100);
            }
        });
        // Declare in and out animations and load them using AnimationUtils class
        Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
        fadeIn.setDuration(1200);
        Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
        fadeOut.setDuration(1200);

        // set the animation type to ImageSwitcher
        image_switcher_details.setInAnimation(fadeIn);
        image_switcher_details.setOutAnimation(fadeOut);
        //Set the schedule function and rate
        final int[] currentIndex = {0};
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //Called every 5000 milliseconds
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        currentIndex[0]++;
                        if (currentIndex[0] == size) currentIndex[0] = 0;
                        image_switcher_details.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                    }
                });
            }
        }, 0, 5000);

        image_switcher_details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        currentIndex[0]++;
                        if (currentIndex[0] == size) currentIndex[0] = 0;
                        image_switcher_details.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                    }
                });
            }
        });
    }

    private View getNewImageView(int pixels_widthHeight) {
        ImageView imageView = new ImageView(mActivity);
        imageView.setPadding(0, 2, 2, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setAdjustViewBounds(true);
        int widthHeight = (int) (pixels_widthHeight * scale + 0.5f);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(widthHeight, widthHeight));
        return imageView;
    }

    public static void showCustomToast(Activity cActivity, String toastText, int icon_RID, int text_color_RID, int duration) {
        LayoutInflater inflater = cActivity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) cActivity.findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.toast_layout_text);
        text.setText(toastText);
        text.setTextColor(ContextCompat.getColor(cActivity, text_color_RID));
        ImageView imageV = (ImageView) layout.findViewById(R.id.toast_img);
        imageV.setImageResource(icon_RID);
        Toast theCustomToast = new Toast(cActivity);
        theCustomToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        theCustomToast.setDuration(duration);
        theCustomToast.setView(layout);
        theCustomToast.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //JSON SEARCH
    // https://pt.myprotein.com/pt_PT/EUR/elysium.searchjson?search=impact+whey+protein
    private class getProductJSON extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            Document resultDocument = null;
            try {
                resultDocument = Jsoup.connect(params[0])
                        .userAgent(userAgent)
                        .timeout(NET_TIMEOUT)
                        .ignoreContentType(true)
                        .maxBodySize(0) //sem limite de tamanho do doc recebido
                        .get();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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


    public class checkInternetAsync extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
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
    }

    public class checkInternetPriceMethodAsync extends AsyncTask<String, Void, Boolean> {
        String json_url;

        @Override
        protected Boolean doInBackground(String... params) {
            json_url = params[0];
            return NetworkUtils.hasActiveNetworkConnection(mActivity);
        }

        @Override
        protected void onPostExecute(Boolean hasInternet) {
            super.onPostExecute(hasInternet);

            if (hasInternet) {
                // Aqui saca a página do produto em html para depois aplicar o parse com jsoup
                AsyncTask<String, Void, JSONObject> GetPriceFromJSON = new GetPriceFromJSON();
                GetPriceFromJSON.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, json_url);
            } else {
                if (noNetworkSnackBar != null && !noNetworkSnackBar.isShown()) {
                    noNetworkSnackBar.show();
                } else {
                    makeNoNetworkSnackBar(mActivity);
                }
            }
            mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.GONE);
            priceTV.setVisibility(View.VISIBLE);
        }
    }
}








