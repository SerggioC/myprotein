package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.MAX_NOTIFY_VALUE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.detailsActivityIsActive;
import static com.cruz.sergio.myproteinpricechecker.R.id.variation1_tv;
import static com.cruz.sergio.myproteinpricechecker.SearchFragment.ADDED_NEW_PROD_REF;
import static com.cruz.sergio.myproteinpricechecker.SearchFragment.PRZ_Domain;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.imageSizesToUse;
import static com.cruz.sergio.myproteinpricechecker.helper.Alarm.LAST_DB_UPDATE_PREF_KEY;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomSlimToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.IOEXCEPTION;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.MALFORMED_URL;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.NET_TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.STATUS_NOT_OK;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.STATUS_OK;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.TIMEOUT;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UNSUPPORTED_MIME_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UnregisterBroadcastReceiver;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.getHTMLDocument_with_NetCipher;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.userAgent;
import static java.lang.Double.parseDouble;
import static org.jsoup.helper.StringUtil.isBlank;


/**
 * Created by Sergio on 23/10/2017.
 **/

public class DetailsActivityProzis extends AppCompatActivity {
   Activity mActivity;
   Boolean gotPrice = false;
   String pptList_SSB;
   ContentValues productContentValues;
   ContentValues priceContentValues;
   String customProductID;
   String prz_locale;
   String prz_country;
   String productID;
   String productName;
   String webstoreName;
   String URL_suffix;
   TextView priceTV;
   Boolean addedNewProduct = false;
   ImageView productImageView;
   LinearLayout llOptions;
   LinearLayout ll_variations;
   LinearLayout linearLayoutSpiners;
   Boolean gotImages = false;
   JSONArray JSON_ArrayArray_Images;
   ImageSwitcher image_switcher_details;
   android.support.v7.widget.SwitchCompat alertSwitch;
   android.support.design.widget.TextInputEditText alertTextView;
   ProgressBar progressBarVariations;
   ProgressBar priceProgressBar;
   double notify_value = 0;
   Timer timer;
   boolean is_web_address;

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
      setContentView(R.layout.details_activity_prozis);
      mActivity = this;
      detailsActivityIsActive = true;

      Toolbar toolbar = findViewById(R.id.toolbar);
      toolbar.setNavigationOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            onBackPressed();
         }
      });
      int dpvalue = 6;
      float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpvalue, getResources().getDisplayMetrics());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
         toolbar.setElevation(pixels);
      } else {
         ViewCompat.setElevation(toolbar, pixels);
      }
      // Corrigir posição da toolbar que fica debaixo da status bar (dar padding para cima do tamanho da status bar)
      toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

      productContentValues = new ContentValues();  //content values para a DB
      priceContentValues = new ContentValues();    //

      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
      CACHE_IMAGES = sharedPrefs.getBoolean("cache_images", false);

      llOptions = mActivity.findViewById(R.id.ll_options);
      ll_variations = mActivity.findViewById(R.id.ll_variations);
      linearLayoutSpiners = mActivity.findViewById(R.id.ll_spinners);
      productImageView = mActivity.findViewById(R.id.p_details_image);
      productImageView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Toast.makeText(mActivity, "Clicked Image", Toast.LENGTH_SHORT).show();
         }
      });
      image_switcher_details = mActivity.findViewById(R.id.image_switcher_details);
      priceTV = mActivity.findViewById(R.id.price_tv);
      progressBarVariations = mActivity.findViewById(R.id.progressBarRound);
      priceProgressBar = mActivity.findViewById(R.id.priceProgressBarRound);

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

      alertSwitch.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            radioGroup.setEnabled(alertSwitch.isChecked());
            radio_every.setEnabled(alertSwitch.isChecked());
            radio_target.setEnabled(alertSwitch.isChecked());
            alertTextView.setEnabled(alertSwitch.isChecked() && radio_target.isChecked());
            alertTextView.setActivated(alertSwitch.isChecked() && radio_target.isChecked());
            alertTextView.setText(alertSwitch.isChecked() && radio_target.isChecked() ? String.valueOf(notify_value) : "");
            textView1.setEnabled(alertSwitch.isChecked() && radio_target.isChecked());
            textView1.setActivated(alertSwitch.isChecked() && radio_target.isChecked());
         }
      });

      radio_every.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            alertTextView.setEnabled(radio_target.isChecked());
            alertTextView.setActivated(radio_target.isChecked());
            alertTextView.setText(radio_target.isChecked() ? String.valueOf(notify_value) : "");
            textView1.setEnabled(radio_target.isChecked());
            textView1.setActivated(radio_target.isChecked());
         }
      });

      radio_target.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            alertTextView.setEnabled(radio_target.isChecked());
            alertTextView.setActivated(radio_target.isChecked());
            alertTextView.setText(radio_target.isChecked() ? String.valueOf(notify_value) : "");
            textView1.setEnabled(radio_target.isChecked());
            textView1.setActivated(radio_target.isChecked());
         }
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
            return true;
         }
      });

      // bugs do sistema android...
      //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
      // parâmetro no manifest android:windowSoftInputMode="stateHidden|adjustPan"

      final String url;

      progressBarVariations.setVisibility(View.VISIBLE);
      priceProgressBar.setVisibility(View.VISIBLE);

      Bundle extras = getIntent().getExtras();
      if (extras != null) {
         url = extras.getString("url");
         Log.i("Sergio>", this + " onViewCreated: passed url=\n" + url);

         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL, url);
         is_web_address = extras.getBoolean("is_web_address");
         webstoreName = extras.getString("webstoreName");

         if (!is_web_address) {
            prz_locale = extras.getString("language");
            prz_country = extras.getString("country");


            List<String> country_names = java.util.Arrays.asList(getResources().getStringArray(R.array.pref_prz_country_titles));
            List<String> country_values = java.util.Arrays.asList(getResources().getStringArray(R.array.pref_prz_country_values));
            String country_name = country_names.get(country_values.indexOf(prz_country.toUpperCase()));
            productID = extras.getString("productID");
            pptList_SSB = extras.getString("ppt_list_SSB");
            String imgURL = extras.getString("image_url");
            productName = extras.getString("productTitleStr");
            String productBrand = extras.getString("productBrand");

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL, PRZ_Domain);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_LOCALE, prz_locale);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME, webstoreName + " " + country_name);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME, productName);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION, pptList_SSB);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BRAND, productBrand);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE, "");


            ((TextView) mActivity.findViewById(R.id.title_tv)).setText(productName);
            ((TextView) mActivity.findViewById(R.id.p_subtitle_tv)).setText(productBrand + "\n" + webstoreName + " " + country_name);
            ((TextView) mActivity.findViewById(R.id.p_description)).setText(pptList_SSB);
            if (imgURL != null) {
               Glide.with(mActivity).load(imgURL).error(R.drawable.noimage).into(productImageView);
            } else {
               Glide.with(mActivity).load(R.drawable.noimage).into(productImageView);
            }


            if (NetworkUtils.hasActiveNetworkConnection(mActivity)) {
               getProductOptions(url);
            } else {
               NetworkUtils.redrawNoNetworkSnackBar(mActivity);
            }

         } else {

            if (NetworkUtils.hasActiveNetworkConnection(mActivity)) {

            } else {
               NetworkUtils.redrawNoNetworkSnackBar(mActivity);
            }
         }


      } else {
         showCustomToast(mActivity, "Error getting product details. Try again.", R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
         url = PRZ_Domain;
         MainActivity.mFragmentManager.popBackStack();

         progressBarVariations.setVisibility(View.GONE);
         priceProgressBar.setVisibility(View.GONE);

      }


      mActivity.findViewById(R.id.open_in_browser).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browser);
         }
      });

        /*
         *   Guardar produto na DB ao clicar no botão
        */
      mActivity.findViewById(R.id.button_add_to_db).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            long timeMillis = System.currentTimeMillis();

            String notifyValueStr = alertTextView.getText().toString();
            if (isBlank(notifyValueStr)) {
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

            priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE, timeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE, timeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE, timeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE, timeMillis);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE, 0);

            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS, alertSwitch.isChecked() ? 1 : 0);
            productContentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE, notify_value);

            if (JSON_ArrayArray_Images != null) { // Imagens do JSON (Mais completo)
               ArrayList<ArrayList<String>> arrayListArrayListImageURIs = new ArrayList<>();
               for (int i = 0; i < JSON_ArrayArray_Images.length(); i++) {
                  JSONArray json_array_i = JSON_ArrayArray_Images.optJSONArray(i);
                  ArrayList<String> arrayListImageURIs = new ArrayList<>();
                  for (int j = 0; j < json_array_i.length(); j++) {
                     try {
                        String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                        String url = (String) ((JSONObject) json_array_i.get(j)).get("url");
                        if (url != null && CACHE_IMAGES) {
                           for (int k = 0; k < imageSizesToUse.length; k++) {
                              if (size.equals(imageSizesToUse[k])) {
                                 String filename = customProductID + "_" + imageSizesToUse[k] + "_index_" + i + ".jpg"; // ex.: tamanho 200x200 imageSizesToUse
                                 saveImageWithGlide(url, filename); //guarda as imagens todas. index_i=variação imageSizesToUse[k]=tamanho
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
                  }
               }
            }
            db.close();
         }
      });


   }

   private void getProductOptions(String url) {
      AsyncTask<String, Void, ConnectionObject> getProductOptionsAsyncTask = new GetProductOptions();
      getProductOptionsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
   }


   public void saveImageWithGlide(String imageURL, final String filename) {
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
                      } catch (IOException e) {
                         e.printStackTrace();
                      }
                      return null;
                   }
                }.execute();
             }
          });
   }

   public int getStatusBarHeight() {
      int height = 0;
      int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
      if (resourceId > 0) height = getResources().getDimensionPixelSize(resourceId);
      return height;
   }


   class ConnectionObject {
      Document resultDocument;
      int resultStatus;

      ConnectionObject(Document resultDocument, int resultStatus) {
         this.resultDocument = resultDocument;
         this.resultStatus = resultStatus;
      }
   }

   private class GetProductOptions extends AsyncTask<String, Void, ConnectionObject> {
      String url;

      @Override
      protected void onPreExecute() {
         super.onPreExecute();
         priceTV.setVisibility(View.GONE);
         priceProgressBar.setVisibility(View.VISIBLE);
      }

      @Override
      protected ConnectionObject doInBackground(String... params) {
         Document resultDocument = null;
         int resultStatus;
         url = params[0];

         try {
            resultDocument = Jsoup.connect(url)
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
            try {
               Log.w("Sergio>", this + "doInBackground IOException: Going NetCipher");
               resultDocument = getHTMLDocument_with_NetCipher(url);
               resultStatus = STATUS_OK;
            } catch (Exception e41) {
               resultStatus = IOEXCEPTION;
               e41.printStackTrace();
            }
         }
         return new ConnectionObject(resultDocument, resultStatus);
      }

      @Override
      protected void onPostExecute(ConnectionObject resultObject) {
         super.onPostExecute(resultObject);
         final Document resultDocument = resultObject.resultDocument;
         int resultStatus = resultObject.resultStatus;

         if (resultStatus == STATUS_NOT_OK || resultStatus == IOEXCEPTION || resultStatus == MALFORMED_URL || resultStatus == UNSUPPORTED_MIME_TYPE) {
            showCustomSlimToast(mActivity,
                "Error connecting to " + url + " website\nException Code = " + resultStatus,
                Toast.LENGTH_LONG);

         } else if (resultStatus == TIMEOUT) {
            showCustomSlimToast(mActivity,
                webstoreName + " website not responding.",
                Toast.LENGTH_LONG);

         } else {

            if (resultDocument == null) {
               showCustomSlimToast(mActivity,
                   "Failed getting product page.",
                   Toast.LENGTH_LONG);
            } else {
               JSONObject jsonData = null;
               final String[] priceString = {null};

               // Se tiver opções
               Elements selectTag = resultDocument.getElementsByClass("var-sel first trigger-choosen"); // "var-sel first trigger-choosen"
               if (selectTag != null) {
                  final ArrayList<String> variationNamesArray;
                  final ArrayList<String> variationValuesArray;
                  Element first = selectTag.first();
                  if (first != null) {
                     Elements options = first.getElementsByTag("option");
                     int size = options.size();
                     if (size > 0) {
                        llOptions.setVisibility(View.VISIBLE);
                        variationNamesArray = new ArrayList<>(size);
                        variationValuesArray = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                           String optionText = options.get(i).text().trim().replaceAll("\n", "");
                           String optionValue = options.get(i).attr("value").trim().replaceAll("\n", "");

                           if (!StringUtil.isBlank(optionText)) {
                              variationNamesArray.add(optionText);
                              variationValuesArray.add(optionValue);
                           }
                        }

                        String variationName = options.first().attr("data-placeholder");
                        ((TextView) mActivity.findViewById(variation1_tv)).setText(variationName);

                        jsonData = getJSONFromScriptTag(resultDocument);

                        final Spinner spinner = mActivity.findViewById(R.id.spinner1);

                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                            mActivity,
                            R.layout.simple_spinner_item,
                            variationNamesArray);
                        spinnerArrayAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(spinnerArrayAdapter);
                        spinner.setSelection(0, false);
                        final JSONObject finalJsonData = jsonData;
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                           @Override
                           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                              gotImages = false;
                              priceString[0] = getPriceMethodFromJSONData(finalJsonData, variationValuesArray.get(spinner.getSelectedItemPosition()));
                              if (priceString[0] == null) {
                                 priceString[0] = getPriceFromHTML(resultDocument);
                              }
                              mActivity.findViewById(R.id.button_add_to_db).setEnabled(true);
                              priceTV.setVisibility(View.VISIBLE);
                              priceTV.setText(priceString[0]);
                           }

                           @Override
                           public void onNothingSelected(AdapterView<?> parent) {
                           }
                        });
                        priceString[0] = getPriceMethodFromJSONData(jsonData, variationValuesArray.get(spinner.getSelectedItemPosition()));
                     }

                  }
               }

               if (priceString[0] == null) priceString[0] = getPriceFromHTML(resultDocument);

               // Produto fora de stock
               //boolean isOutOfStock = resultDocument.hasClass("out-of-stock-notice") || resultDocument.hasClass("out-of-stock");
               boolean isOutOfStock = resultDocument.getElementsByClass("out-of-stock-notice").size() > 0 ||
                   resultDocument.getElementsByClass("out-of-stock").size() > 0;
               if (isOutOfStock) {
                  SpannableString ss = new SpannableString(" Out of Stock");
                  ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.red_light)), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                  ss.setSpan(new RelativeSizeSpan(0.7f), 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                  ((TextView) mActivity.findViewById(R.id.title_tv)).append(ss);
               }

               mActivity.findViewById(R.id.button_add_to_db).setEnabled(true);
               priceTV.setVisibility(View.VISIBLE);
               priceTV.setText(priceString[0]);

               // TODO SACAR IMAGENS

               String zoomTagClassName = "zoomed-image slick-slide";
               Elements zoomTags = resultDocument.getElementsByClass(zoomTagClassName);
               int zsize = zoomTags.size();
               for (int i = 0; i < zsize; i++) {
                  String imgURL = "https:" + zoomTags.get(i).child(0).attr("src");
                  JSONArray inner_array = new JSONArray();

                  JSONObject innerObject = new JSONObject();
                  try {
                     innerObject.put("url", imgURL);
                     innerObject.put("size", "1000x1000");
                  } catch (JSONException e) {
                     e.printStackTrace();
                  }
                  inner_array.put(innerObject);
                  JSON_ArrayArray_Images.put(inner_array);
               }
               Log.i("Sergio>", this + " onPostExecute\nJSON_ArrayArray_Images= " + JSON_ArrayArray_Images);

            }

         }
         priceProgressBar.setVisibility(View.GONE);
         progressBarVariations.setVisibility(View.GONE);
      }

      private String getPriceFromHTML(Document resultDocument) {
         String priceCurrency = resultDocument.getElementById("ob-prod-price").text();
         String priceString = resultDocument.getElementById("ob-prod-price").attr("content");
         String currencySymbol = priceCurrency.replace(priceString, "");
         double priceValue = parseDouble(priceString.replaceAll(",", "."));

         addToContentValues(priceCurrency, priceValue, currencySymbol);

         return StringUtil.isBlank(priceCurrency) || priceCurrency.equals("0") ? "N/A" : priceCurrency;
      }

      private JSONObject getJSONFromScriptTag(Document document) {
         JSONObject jsonData = null;
         Elements scriptTags = document.getElementsByTag("script");
         for (Element tag : scriptTags) {
            for (DataNode node : tag.dataNodes()) {
               String script = node.getWholeData();
               if (script.contains("App.js.vars")) {
                  script = script.substring(script.indexOf("{"), script.lastIndexOf("}") + 1).replace("\\/", "/");
                  try {
                     jsonData = new JSONObject(script);
                  } catch (JSONException e) {
                     e.printStackTrace();
                  }
               }
            }
         }
         return jsonData;
      }

      private String getPriceMethodFromJSONData(JSONObject jsonData, String optionKey) {
         String priceCurrency = "N/A";
         double priceValue;

         if (jsonData != null) {
            String JSONProdID = null;
            try {
               JSONProdID = jsonData.has("productId") ? jsonData.getString("productId").toString() : null;
            } catch (JSONException e) {
               e.printStackTrace();
            }
            if (jsonData.has("var" + JSONProdID)) {
               try {
                  jsonData = jsonData.getJSONObject("var" + JSONProdID);
               } catch (JSONException e) {
                  e.printStackTrace();
               }
            }

            if (jsonData.has(optionKey)) {

               try {
                  JSONObject optionObject = (JSONObject) jsonData.get(optionKey);
                  if (optionObject.has("sku"))
                     productID = optionObject.get("sku").toString();

                  String priceString = "0";
                  if (optionObject.has("variant_price"))
                     priceString = optionObject.get("variant_price").toString();

                  if (optionObject.has("variant_price_formated"))
                     priceCurrency = optionObject.get("variant_price_formated").toString();

                  priceValue = parseDouble(priceString.replaceAll(",", "."));
                  String currencySymbol = priceCurrency.replace(priceString, "");

                  Log.i("Sergio>", this + " onPostExecute\n" +
                      "optionObject= " + optionObject + "\n" +
                      "productID= " + productID + "\n" +
                      "priceString=" + priceString + "\n" +
                      "priceValue= " + priceValue + "\n" +
                      "priceCurrency= " + priceCurrency + "\n" +
                      "currencySymbol= " + currencySymbol);

                  customProductID = "loc" + prz_locale + prz_country + "pid" + productID;
                  addToContentValues(priceCurrency, priceValue, currencySymbol);

               } catch (JSONException e) {
                  e.printStackTrace();
                  return null;
               }
            }
         }
         return priceCurrency.equals("0") ? null : priceCurrency;
      }

      private void addToContentValues(String priceCurrency, double priceValue, String currencySymbol) {
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID, customProductID);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID, productID);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE, priceCurrency);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE, priceValue);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL, currencySymbol);

         priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE, priceCurrency);
         priceContentValues.put(ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE, priceValue);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE, priceCurrency);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE, priceValue);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE, priceCurrency);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE, priceValue);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE, priceCurrency);
         productContentValues.put(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE, priceValue);
      }

   }
}
