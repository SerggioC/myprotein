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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.MAX_NOTIFY_VALUE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.detailsActivityIsActive;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.SearchFragment.ADDED_NEW_PROD_REF;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.imageSizesToUse;
import static com.cruz.sergio.myproteinpricechecker.helper.Alarm.LAST_DB_UPDATE_PREF_KEY;
import static com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain.MP_DESKTOP_SITES;
import static com.cruz.sergio.myproteinpricechecker.helper.MyProteinDomain.MP_MOBILE_SITES;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UnregisterBroadcastReceiver;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.showCustomToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;
import static java.lang.Double.parseDouble;

public class DetailsActivityMyprotein extends AppCompatActivity {
   public static final String MYP_CONTENT_SERVER = "https://s4.thcdn.com/";
   final static String[] MP_ALL_IMAGE_SIZE_NAMES = new String[]{
       "extrasmall",   // 20/20
       "small",        // 50/50
       "smallthumb",   // 60/60
       "thumbnail",    // 70/70
       "thumbnails",    // 70/70
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
   Activity mActivity;
   boolean gotPrice = true;
   ArrayList<String> description;
   ContentValues productContentValues;
   String customProductID;
   String MP_Domain;
   String pref_MP_Locale;
   String productID;
   String productName;
   String webstoreName;
   String url;
   String URL_suffix = "";
   TextView priceTV;
   LinearLayout ll_variations;
   LinearLayout linearLayoutSpiners;
   boolean addedNewProduct = false;
   ImageView productImageView;
   boolean gotImages = false;
   JSONArray JSON_ArrayArray_Images;
   ImageSwitcher image_switcher_details;
   android.support.v7.widget.SwitchCompat alertSwitch;
   android.support.design.widget.TextInputEditText alertTextView;
   double notify_value = 0;
   Timer timer;
   Boolean is_web_address;
   ArrayList<String> all_image_sizeNames;
   boolean isMobileSite = false;
   boolean isDesktopSite = false;
   String imgURLFromExtras = null;


   @Override
   protected void onStart() {
      super.onStart();
      NetworkUtils.createBroadcast(mActivity);
   }

   @Override
   public void onPause() {
      setActivityResult();
      detailsActivityIsActive = false;
      UnregisterBroadcastReceiver(mActivity);
      super.onPause();
   }

   @Override
   public void onBackPressed() {
      setActivityResult();
      super.onBackPressed();
   }

   public void setActivityResult() {
      if (addedNewProduct) {
         Intent intent = new Intent();
         intent.putExtra(ADDED_NEW_PROD_REF, addedNewProduct);
         setResult(RESULT_OK, intent);
         finish();
      }
   }


   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.details_activity_layout);
      mActivity = this;
      detailsActivityIsActive = true;

      Toolbar toolbar = findViewById(R.id.toolbar);
      toolbar.setNavigationOnClickListener(v -> onBackPressed());
      int dpvalue = 6;
      float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpvalue, getResources().getDisplayMetrics());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         toolbar.setElevation(pixels);
      } else {
         ViewCompat.setElevation(toolbar, pixels);
      }
      // Corrigir posição da toolbar que fica debaixo da status bar (dar padding para cima do tamanho da status bar)
      toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

      productContentValues = new ContentValues(); //content values para a DB
      all_image_sizeNames = new ArrayList<>(Arrays.asList(MP_ALL_IMAGE_SIZE_NAMES));

      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
      CACHE_IMAGES = sharedPrefs.getBoolean("cache_images", false);

      ll_variations = mActivity.findViewById(R.id.ll_variations);
      linearLayoutSpiners = mActivity.findViewById(R.id.spiners);
      productImageView = mActivity.findViewById(R.id.p_details_image);
      productImageView.setOnClickListener(v -> Toast.makeText(mActivity, "Clicked Image", Toast.LENGTH_SHORT).show());
      image_switcher_details = mActivity.findViewById(R.id.image_switcher_details);

      // Notifications section //
      alertSwitch = mActivity.findViewById(R.id.switch_notify);
      alertTextView = mActivity.findViewById(R.id.tv_alert_value);
      final android.support.v7.widget.AppCompatRadioButton radio_every = mActivity.findViewById(R.id.radioButton_every);
      final android.support.v7.widget.AppCompatRadioButton radio_target = mActivity.findViewById(R.id.radioButton_target);
      final RadioGroup radioGroup = mActivity.findViewById(R.id.radioGroup_notify);
      final TextView textView1 = mActivity.findViewById(R.id.tv_alert1);

      alertSwitch.setChecked(true);
      radioGroup.setEnabled(true);

      radio_every.setEnabled(true);
      radio_every.setChecked(true);

      radio_target.setEnabled(true);
      radio_target.setChecked(false);

      textView1.setEnabled(radio_target.isChecked());
      textView1.setActivated(radio_target.isChecked());
      textView1.clearFocus();

      alertTextView.setEnabled(radio_target.isChecked());
      alertTextView.setActivated(radio_target.isChecked());
      alertTextView.clearFocus();

      alertSwitch.setOnClickListener(v -> {
         radioGroup.setEnabled(alertSwitch.isChecked());
         radio_every.setEnabled(alertSwitch.isChecked());
         radio_target.setEnabled(alertSwitch.isChecked());
         alertTextView.setEnabled(alertSwitch.isChecked() && radio_target.isChecked());
         alertTextView.setActivated(alertSwitch.isChecked() && radio_target.isChecked());
         alertTextView.setText(alertSwitch.isChecked() && radio_target.isChecked() ? String.valueOf(notify_value) : "");
         textView1.setEnabled(alertSwitch.isChecked() && radio_target.isChecked());
         textView1.setActivated(alertSwitch.isChecked() && radio_target.isChecked());
      });

      radio_every.setOnClickListener(v -> {
         alertTextView.setEnabled(radio_target.isChecked());
         alertTextView.setActivated(radio_target.isChecked());
         alertTextView.setText(radio_target.isChecked() ? String.valueOf(notify_value) : "");
         textView1.setEnabled(radio_target.isChecked());
         textView1.setActivated(radio_target.isChecked());
      });

      radio_target.setOnClickListener(v -> {
         alertTextView.setEnabled(radio_target.isChecked());
         alertTextView.setActivated(radio_target.isChecked());
         alertTextView.setText(radio_target.isChecked() ? String.valueOf(notify_value) : "");
         textView1.setEnabled(radio_target.isChecked());
         textView1.setActivated(radio_target.isChecked());
      });


      alertTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView textview, int actionId, KeyEvent event) {
            try {
               notify_value = parseDouble(textview.getText().toString());
            } catch (NumberFormatException e) {
               e.printStackTrace();
               notify_value = parseDouble(String.valueOf(MAX_NOTIFY_VALUE));
            }
            Log.i("Sergio>", this + " onEditorAction\nnotify_value= " + notify_value);
            return true;
         }
      });

      // bugs do sistema android...
      // getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
      // ou parâmetro no manifest: android:windowSoftInputMode="stateHidden|adjustPan"


      Bundle extras = getIntent().getExtras();

      if (extras != null) {
         mActivity.findViewById(R.id.progressBarRound).setVisibility(View.VISIBLE);
         url = extras.getString("url");
         Log.i("Sergio>", this + " onViewCreated: passed url=\n" + url);

         is_web_address = extras.getBoolean("is_web_address");
         webstoreName = extras.getString("webstoreName");
         pref_MP_Locale = extras.getString("language");
         String shippingCountry = extras.getString("shipping_country");
         String currency = extras.getString("currency");


         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BRAND, webstoreName);
         if (!is_web_address) {
            isDesktopSite = true;

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION, shippingCountry);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY, currency);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_LOCALE, pref_MP_Locale);

            MP_Domain = MyProteinDomain.getHref(pref_MP_Locale);
            URL_suffix = "settingsSaved=Y&shippingcountry=" + shippingCountry + "&switchcurrency=" + currency + "&countrySelected=Y";
            url += "?" + URL_suffix;
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL, url);

            List<String> texts = java.util.Arrays.asList(getResources().getStringArray(R.array.pref_mp_website_titles));
            List<String> values = java.util.Arrays.asList(getResources().getStringArray(R.array.pref_mp_website_values));
            String country_name = texts.get(values.indexOf(pref_MP_Locale));
            productID = extras.getString("productID");
            description = extras.getStringArrayList("description");
            imgURLFromExtras = extras.getString("image_url");
            productName = extras.getString("productTitleStr");
            customProductID = "loc" + pref_MP_Locale + "pid" + productID;

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL, MP_Domain);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME, webstoreName + " " + country_name);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID, customProductID);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME, productName);

            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tick, null);
            SpannableStringBuilder pptList_SSB = new SpannableStringBuilder();
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            String drawableStr = drawable.toString();
            String description_DB = "";
            for (int i = 0; i < description.size(); i++) {
               pptList_SSB.append(drawableStr);
               pptList_SSB.setSpan(new ImageSpan(drawable), pptList_SSB.length() - drawableStr.length(), pptList_SSB.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
               pptList_SSB.append(" " + description.get(i) + "\n");
               description_DB += description.get(i) + "\n";
            }
            if (description_DB.length() > 0) {
               description_DB = description_DB.substring(0, description_DB.length() - 1); //Remover ultimo caractere \n
            }
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, description_DB);

            //Lista da descrição enviado da activity anterior (SearchFragment.java) com imagem de lista (tick) à esquerda
            ((TextView) mActivity.findViewById(R.id.p_description)).setText(pptList_SSB);
            if (imgURLFromExtras != null) {
               Glide.with(mActivity).load(imgURLFromExtras).error(R.drawable.noimage).into(productImageView);
            } else {
               Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
            }

            AsyncTask<String, Void, Boolean> get_product_page = new checkInternetAsyncMethods("getProductPage");
            get_product_page.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
         } else {

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL, url);
            AsyncTask<String, Void, Boolean> get_product_page = new checkInternetAsyncMethods("getProductPageFromWebAddress");
            get_product_page.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
         }


      } else {
         showCustomToast(mActivity, "Error getting product details.", R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
         url = "https://www.myprotein.com/";
         MainActivity.mFragmentManager.popBackStack();
      }

      priceTV = mActivity.findViewById(R.id.price_tv);

      mActivity.findViewById(R.id.open_in_browser).setOnClickListener(v -> {
         Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
         startActivity(browser);
      });

        /*
         *   Guardar produto na DB ao clicar no botão
        */
      mActivity.findViewById(R.id.button_add_to_db).setOnClickListener(v -> {
         long timeMillis = System.currentTimeMillis();
         String price = priceTV.getText().toString();
         String priceString = price;

         Pattern regex = Pattern.compile("[^.,\\d]+"); // matches . , e números de 0 a 9
         Matcher match = regex.matcher(price);
         price = match.replaceAll("");
         price = price.replaceAll(",", ".");
         double price_value = parseDouble(price);

         String notifyValueStr = alertTextView.getText().toString();

         if (StringUtil.isBlank(notifyValueStr)) {
            notify_value = 0;
         } else {
            try {
               notify_value = parseDouble(notifyValueStr);
            } catch (NumberFormatException e) {
               // número demasiado grande
               e.printStackTrace();
               notify_value = parseDouble(String.valueOf(MAX_NOTIFY_VALUE));
            }
         }

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
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE, 0);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS, alertSwitch.isChecked() ? 1 : 0);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE, notify_value);

         if (JSON_ArrayArray_Images != null) { // Imagens do JSON (Mais completo)
            for (int i = 0; i < JSON_ArrayArray_Images.length(); i++) {
               JSONArray json_array_i = JSON_ArrayArray_Images.optJSONArray(i);
               for (int j = 0; j < json_array_i.length(); j++) {
                  try {
                     String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                     String url = (String) ((JSONObject) json_array_i.get(j)).get("url");
                     if (url != null && CACHE_IMAGES) {
                        for (int k = 0; k < imageSizesToUse.length; k++) {
                           if (size.equals(imageSizesToUse[k])) {
                              String filename = customProductID + "_" + imageSizesToUse[k] + "_index_" + i + ".jpg"; // ex.: tamanho 200x200 imageSizesToUse
                              saveImageWithGlide(url, filename); //guarda as imagens todas. index_i=variação imageSizesToUse[k]=tamanho
                              ((JSONObject) JSON_ArrayArray_Images.optJSONArray(i).get(j)).put("file", filename);
                           }
                        }
                     }
                  } catch (JSONException e) {
                     e.printStackTrace();
                  }
               }
            }
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES, JSON_ArrayArray_Images.toString().replace("\\", ""));

         }

         DBHelper dbHelper = new DBHelper(mActivity);
         SQLiteDatabase db = dbHelper.getWritableDatabase();

         Cursor exists_CustomPID = db.rawQuery("SELECT 1 FROM " +
             ProductsContract.ProductsEntry.TABLE_NAME + " WHERE " +
             ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " = '" + customProductID + "' LIMIT 1", null); //Atenção à single quote (')

         int numEntries = exists_CustomPID.getCount();
         exists_CustomPID.close();

         if (numEntries >= 1) { // Só poderá haver uma entrada
            showCustomToast(mActivity, "Product already in DataBase!",
                R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_LONG);
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

                  Cursor dbSize = db.rawQuery("SELECT COUNT(*) FROM" + ProductsContract.ProductsEntry.TABLE_NAME, null);
                  if (dbSize.getCount() == 1) {
                     SharedPreferences.Editor editor = mActivity.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE).edit();
                     editor.putLong(LAST_DB_UPDATE_PREF_KEY, timeMillis);
                     editor.commit();
                  }
                  dbSize.close();
               }
            }
         }
         db.close();
      });


   }

   public void fixToolbar() {
      Toolbar toolbar = mActivity.findViewById(R.id.details_toolbar);

      // Corrigir posição da toolbar que fica debaixo da statusbar
      //toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
      toolbar.setPadding(0, 0, 0, 0);


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


      // To fit bellow the statusbar
//        View decorView = getWindow().getDecorView();
//        decorView.setFitsSystemWindows(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            decorView.requestFitSystemWindows();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
//        //fixToolbar();


   }

   public int getStatusBarHeight() {
      int height = 0;
      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
      if (resourceId > 0) height = getResources().getDimensionPixelSize(resourceId);
      return height;
   }

   public void saveImageWithGlide(String imageURL, final String filename) {
      Glide.with(mActivity)
          .load(imageURL)
          .asBitmap()
          .toBytes(Bitmap.CompressFormat.JPEG, 100)
          .error(R.drawable.noimage)
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
                      } catch (IOException e) {
                         e.printStackTrace();
                      }
                      return null;
                   }
                }.execute();
             }
          });
   }

   private void getImagesFromHTML_Tags(Document resultDocument) {
      ArrayList<String> arrayListImageURLsToLoad = new ArrayList<>();
      JSON_ArrayArray_Images = new JSONArray();

      // extract images from script tags

      if (isDesktopSite) {
         Elements scriptTags = resultDocument.getElementsByTag("script");
         if (scriptTags.size() > 0) {
            for (Element tag : scriptTags) {
               for (DataNode node : tag.dataNodes()) {
                  String script = node.getWholeData();
                  if (script.contains("arProductImages")) {

                     String[] imageFileTypes = new String[]{".jpg", ".png", ".bmp"};
                     String IMG_FILETYPE = "";
                     for (int i = 0; i < imageFileTypes.length; i++) {
                        if (script.contains(imageFileTypes[i]))
                           IMG_FILETYPE = imageFileTypes[i];
                     }

                     int indexOfarray = script.indexOf("arProductImages[");
                     while (indexOfarray >= 0 && !IMG_FILETYPE.isEmpty()) {
                        gotImages = false;
                        String urlToLoad = null;
                        String sub_script = script.substring(indexOfarray, script.indexOf(");", indexOfarray));
                        JSONArray inner_array = new JSONArray();

                        int indexOfHttps = sub_script.indexOf("https");
                        while (indexOfHttps >= 0) {
                           String imgURL = sub_script.substring(indexOfHttps, sub_script.indexOf(IMG_FILETYPE, indexOfHttps) + 4);

                           String size = "";
                           for (int k = 0; k < MP_BB_IMAGE_TYPES.length; k++) {
                              if (imgURL.contains(MP_BB_IMAGE_TYPES[k])) {
                                 size = MP_XX_IMAGE_TYPES[k];
                                 break;
                              }

                           }

                           for (int k = 0; k < imageSizesToUse.length; k++) {
                              if (size.equals(imageSizesToUse[k])) {
                                 urlToLoad = imgURL;
                                 gotImages = true;
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
                        if (gotImages)
                           arrayListImageURLsToLoad.add(urlToLoad);
                     }
                  }
               }
            }
         }
      }


      // extract images from jzoom divs
      try {


         Elements outerZoomImageDivs = resultDocument.getElementsByClass("jZoom_imageLinks").first().children();
         int oSize = outerZoomImageDivs.size();
         int innerSize = outerZoomImageDivs.get(0).children().size();
         for (int i = 0; i < innerSize; i++) {
            gotImages = false;
            String urlToLoad = null;
            JSONArray innerArray = new JSONArray();

            for (int j = 0; j < oSize; j++) {
               String sizeName = outerZoomImageDivs.get(j).className().replace("jZoom_", "").toLowerCase();
               sizeName = MP_XX_IMAGE_TYPES[all_image_sizeNames.indexOf(sizeName)];
               Elements innerDivs = outerZoomImageDivs.get(j).children();
               String imgURL = MYP_CONTENT_SERVER + innerDivs.get(i).attr("data-image-link");
               if (!StringUtil.isBlank(imgURL)) {
                  JSONObject innerObject = new JSONObject();
                  innerObject.put("size", sizeName);
                  innerObject.put("url", imgURL);
                  innerArray.put(innerObject);
                  for (int k = 0; k < imageSizesToUse.length; k++) {
                     if (sizeName.equals(imageSizesToUse[k])) {
                        urlToLoad = imgURL;
                        gotImages = true;
                     }
                  }
               }

            }
            if (gotImages) arrayListImageURLsToLoad.add(urlToLoad);


            Elements product_Images_OnDisplay = isMobileSite ? resultDocument.getElementsByClass("product-img rsImg") :
                resultDocument.getElementsByClass("m-unit-1 firstContainer");

            gotImages = false;
            urlToLoad = null;
            String imageURL = isMobileSite ? product_Images_OnDisplay.get(i).attr("src") :
                product_Images_OnDisplay.get(i).child(0).child(0).attr("src");
            if (!StringUtil.isBlank(imageURL)) {
               JSONObject innerObject = new JSONObject();
               innerObject.put("size", isMobileSite ? "300x300" : "480x480");
               innerObject.put("url", imageURL);
               innerArray.put(innerObject);
               JSON_ArrayArray_Images.put(innerArray);

               for (int k = 0; k < imageSizesToUse.length; k++) {
                  if ("300x300".equals(imageSizesToUse[k])) {
                     urlToLoad = imageURL;
                     gotImages = true;
                  }
               }
               if (gotImages) arrayListImageURLsToLoad.add(urlToLoad);
            }

         }

      } catch (JSONException e) {
         e.printStackTrace();
      } catch (IndexOutOfBoundsException i) {
         i.printStackTrace();
      }

      Log.i("Sergio>", this + " onPostExecute\nJSON_ArrayArray_Images= " + JSON_ArrayArray_Images.toString().replace("\\", "") + "\n" +
          "arrayListImageURLsToLoad= \n" + arrayListImageURLsToLoad);

      gotImages = arrayListImageURLsToLoad.size() > 0;

      if (gotImages) {
         placeImagesFromURL_Details(arrayListImageURLsToLoad);
         productImageView.setVisibility(View.GONE);
         image_switcher_details.setVisibility(View.VISIBLE);

      } else if (!gotImages && imgURLFromExtras != null) {
         productImageView.setVisibility(View.VISIBLE);
         image_switcher_details.setVisibility(View.GONE);
         Glide.with(mActivity).load(imgURLFromExtras).error(R.drawable.noimage).into(productImageView);

      } else if (!gotImages && imgURLFromExtras == null) {
         productImageView.setVisibility(View.VISIBLE);
         image_switcher_details.setVisibility(View.GONE);
         Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
      }


   }

   private void get_Available_Options() {
      String full_JSON_URL = MP_Domain + "variations.json?productId=" + productID + "&" + URL_suffix;
      AsyncTask<String, Void, Boolean> checkinternetAsyncTask = new checkInternetAsyncMethods("GetAvailableOptionsFromJSON");
      checkinternetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, full_JSON_URL);
      Log.i("Sergio>", this + " get_Available_Options: \nfull_JSON_URL=\n" + full_JSON_URL);
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

      Log.i("Sergio>>>", "getPriceMethod: \nJSON_URL_Details=\n" + JSON_URL_Details);

      productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS, JSON_URL_Details);

      AsyncTask<String, Void, Boolean> checkinternetAsyncTask = new checkInternetAsyncMethods("GetPriceFromJSON");
      checkinternetAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, JSON_URL_Details);

   }

   private void placeImagesFromURL_Details(final ArrayList<String> arrayListImageURLsToLoad) {
      final int size = arrayListImageURLsToLoad.size();
      final ArrayList<Bitmap> arrayListImageBitmap = new ArrayList<>(size);

      for (int i = 0; i < size; i++) {
         final int finalI = i;
         Glide.with(mActivity)
             .load(arrayListImageURLsToLoad.get(i))
             .asBitmap()
             .error(R.drawable.noimage)
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
                      }
                      timer = new Timer();
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
                  image_switcher_details.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                  currentIndex[0]++;
                  if (currentIndex[0] == size) currentIndex[0] = 0;
               }
            });
         }
      }, 0, 5000);

      image_switcher_details.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mActivity.runOnUiThread(new Runnable() {
               public void run() {
                  image_switcher_details.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                  currentIndex[0]++;
                  if (currentIndex[0] == size) currentIndex[0] = 0;
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

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
   }

   //FIXME unneeded crap.
   public class checkInternetAsyncMethods extends AsyncTask<String, Void, Boolean> {
      String method;
      String backGround_param; //url

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
               case "GetAvailableOptionsFromJSON": {
                  AsyncTask<String, Void, JSONObject> getDetailsFromJSON = new GetAvailableOptionsFromJSON();
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
               case "getProductPageFromWebAddress": {
                  AsyncTask<String, Void, Document> getProductPage = new getProductPageWebAddress();
                  getProductPage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, backGround_param);
                  break;
               }
               default: {
                  Log.e("Sergio>", this + " onPostExecute: invalid method given in switch");
                  break;
               }
            }

         } else {
            NetworkUtils.redrawNoNetworkSnackBar(mActivity);
         }
      }
   }

   private class getProductPage extends AsyncTask<String, Void, Document> {

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

         if (detailsActivityIsActive) {
            if (resultDocument != null) {
//                    Element titleElem = resultDocument.getElementsByClass("product-title").first();  // Titulo ou nome do produto
//                    String titlee = titleElem != null ? titleElem.text() : "N/A";

               ((TextView) mActivity.findViewById(R.id.title_tv)).append(productName);

               Elements subtitle_element = resultDocument.getElementsByClass("product-sub-name");
               String subtitle = subtitle_element != null ? subtitle_element.text() : "N/A";
               ((TextView) mActivity.findViewById(R.id.p_subtitle)).append(subtitle);
               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE, subtitle);

               getImagesFromHTML_Tags(resultDocument);

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
                  Pattern regex = Pattern.compile("[.,\\d]+"); // matches . , e números de 0 a 9
                  Matcher match = regex.matcher(price);
                  String currency_symbol = match.replaceAll("");
                  productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL, currency_symbol);

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
            NetworkUtils.showCustomSlimToast(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT);

            //Toast.makeText(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT).show();
         }

      }

   }

   private class getProductPageWebAddress extends AsyncTask<String, Void, Document> {
      String url;

      @Override
      protected Document doInBackground(String... params) {
         this.url = params[0];
         Document resultDocument = null;
         try {
            resultDocument = Jsoup.connect(url)
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

         if (detailsActivityIsActive) {
            if (resultDocument != null) {

               // check if it's mobile site
               isMobileSite = false;
               isDesktopSite = false;
               String theSite = "";
               for (int i = 0; i < MP_MOBILE_SITES.length; i++) {
                  if (url.contains(MP_MOBILE_SITES[i])) {
                     isMobileSite = true;
                     theSite = MP_MOBILE_SITES[i];
                     break;
                  }
               }
               if (!isMobileSite) {
                  for (int i = 0; i < MP_DESKTOP_SITES.length; i++) {
                     if (url.contains(MP_DESKTOP_SITES[i])) {
                        isDesktopSite = true;
                        theSite = MP_DESKTOP_SITES[i];
                        break;
                     }
                  }
               }

               String country_name = MyProteinDomain.getCountryFromUrl(theSite);
               MP_Domain = "https://" + theSite + "/";

               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL, MP_Domain);
               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME, webstoreName + " " + country_name);

               Element titleElem = resultDocument.getElementsByClass("product-title").first();  // Titulo ou nome do produto
               String title = titleElem != null ? titleElem.text() : "N/A";
               ((TextView) mActivity.findViewById(R.id.title_tv)).append(title);
               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME, title);

               Elements subtitle_element = null;
               if (isMobileSite) {
                  subtitle_element = resultDocument.getElementsByClass("product-subtitle");
               } else if (isDesktopSite) {
                  subtitle_element = resultDocument.getElementsByClass("product-sub-name");
               }
               String subtitle = subtitle_element != null ? subtitle_element.text() : "no info";
               ((TextView) mActivity.findViewById(R.id.p_subtitle)).append(subtitle);
               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE, subtitle);

               // Labels: Sabor, Quantidade, Embalagem

//                  alternativa para sacar o productID dentro da página => mais lento!
//                  productID = resultDocument.getElementsByClass("productoptions").first().getElementsByTag("script").html();
//                  productID = productID.substring(productID.indexOf("productId") + 12, productID.indexOf(";"));

               String product_id_from_url = url.substring(url.lastIndexOf("/", url.indexOf(".html")) + 1, url.indexOf(".html"));
               productID = product_id_from_url;
               customProductID = theSite + "pid" + productID;
               productContentValues.put(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID, customProductID);

               Elements productVariationsLabels = new Elements();
               if (isMobileSite) {
                  Elements elementsByClass = resultDocument.getElementsByClass("product-variations");
                  if (elementsByClass.size() > 0) {
                     productVariationsLabels = elementsByClass.get(0).getElementsByTag("legend");
                  }
               } else if (isDesktopSite) {
                  productVariationsLabels = resultDocument.getElementsByClass("productVariations__label");
               }

               Log.w("Sergio>", this + "onPostExecute: \n" +
                   "productVariationsLabels= " + productVariationsLabels + "\n" +
                   "productID= " + productID + "\n" +
                   "customProductID= " + customProductID);

               JSON_ArrayArray_Images = new JSONArray();
               getImagesFromHTML_Tags(resultDocument);

               int pvl_size = productVariationsLabels.size();
               if (pvl_size > 0) {

                  for (int i = 0; i < pvl_size; i++) {
                     String variationText = productVariationsLabels.get(i).text();
                     if (variationText.isEmpty()) continue;
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

                  String price;
                  if (isMobileSite) {
                     price = resultDocument.getElementsByClass("product-price price").text();
                  } else {
                     price = resultDocument.getElementsByClass("priceBlock_current_price").text();
                  }

                  Pattern regex = Pattern.compile("[.,\\d]+"); // matches . , e números de 0 a 9
                  Matcher match = regex.matcher(price);
                  String currency_symbol = match.replaceAll("");
                  productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL, currency_symbol);

                  priceTV.setText(price);
                  gotPrice = true;
                  mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);
                  mActivity.findViewById(R.id.button_add_to_db).setEnabled(true);
                  mActivity.findViewById(R.id.ll_description).setVisibility(View.VISIBLE);
               }

            } else {
               showCustomToast(mActivity, "Error getting webpage. \nRetry.", R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
            }

         } else {
            NetworkUtils.showCustomSlimToast(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT);
         }

      }

   }

   private class GetAvailableOptionsFromJSON extends AsyncTask<String, Void, JSONObject> {
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
         if (detailsActivityIsActive) {
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
                           getPriceMethod(arrayArray_opt_ids, variation_ids);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                     });
                  }

                  getPriceMethod(arrayArray_opt_ids, variation_ids);

                  mActivity.findViewById(R.id.ll_description).setVisibility(View.VISIBLE);


               } catch (JSONException erro) {
                  erro.printStackTrace();
                  Log.w("Sergio>", this + " onPostExecute: falhou ao fazer o parse do json \n exception = " + erro);
               }
            } else {
               Log.w("Sergio>", this + " onPostExecute: falhou ao sacar o json ");
               mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);
               NetworkUtils.showCustomSlimToast(mActivity, "Error getting price.", Toast.LENGTH_SHORT);
            }

         } else {
            NetworkUtils.showCustomSlimToast(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT);
         }

      }

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
                .timeout(NET_TIMEOUT) // limite de tempo para receber a página
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
         if (detailsActivityIsActive) {
            String priceJson = null;
            if (json != null) {
               try {
                  if (json.has("price")) {
                     priceJson = (String) json.get("price");
                  } else {
                     priceTV.setText("N/A");
                     gotPrice = false;
                  }
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
               priceTV.setText("N/A");
               gotPrice = false;
            }
            if (!gotImages) {
               //
               //  Criar o JSONArray com array das imagens a colocar na base de dados. no array está um JSON Object ()
               //
               ArrayList<String> arrayListImageURLsToLoad = new ArrayList<>();

               try {
                  // Parse do url das imagens se existirem no json
                  if (json.has("images")) {
                     JSONArray json_images = json.getJSONArray("images");

                     JSON_ArrayArray_Images = new JSONArray();
                     JSONArray inner_array = new JSONArray();

                     int image_index = 0;
                     for (int i = 0; i < json_images.length(); i++) {
                        JSONObject image_i = (JSONObject) json_images.get(i);
                        int current_img_index = image_i.getInt("index");                            // 0, 1, 2...
                        String image_type = image_i.getString("type");                              // tamanho: "small", "extralarge" "zoom" ...
                        image_type = MP_XX_IMAGE_TYPES[all_image_sizeNames.indexOf(image_type)];        // Traduzir o nome para 20x20, 100x100 ...
                        String image_url = MYP_CONTENT_SERVER + image_i.getString("name");     // url

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

                     // Carregar imagens no ecrã atual (details)

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


                  } else {
                     gotImages = false;
                  }

                  if (gotImages) {
                     placeImagesFromURL_Details(arrayListImageURLsToLoad);
                     productImageView.setVisibility(View.GONE);
                     image_switcher_details.setVisibility(View.VISIBLE);

                  } else if (!gotImages && imgURLFromExtras != null) {
                     productImageView.setVisibility(View.VISIBLE);
                     image_switcher_details.setVisibility(View.GONE);
                     Glide.with(mActivity).load(imgURLFromExtras).error(R.drawable.noimage).into(productImageView);

                  } else if (!gotImages && imgURLFromExtras == null) {
                     productImageView.setVisibility(View.VISIBLE);
                     image_switcher_details.setVisibility(View.GONE);
                     Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
                  }

               } catch (JSONException erro) {
                  erro.printStackTrace();
                  gotImages = false;
                  Log.w("Sergio>", this + " onPostExecute: falhou ao fazer o parse do json \n exception = " + erro);
               }

            }

            mActivity.findViewById(R.id.priceProgressBarRound).setVisibility(View.GONE);
            priceTV.setVisibility(View.VISIBLE);
            mActivity.findViewById(R.id.progressBarRound).setVisibility(View.GONE);

         } else {
            NetworkUtils.showCustomSlimToast(mActivity, "Details Screen Terminated", Toast.LENGTH_SHORT);

            //Toast.makeText(mActivity, "Details Fragment Terminated", Toast.LENGTH_SHORT).show();
         }

      }
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

}








