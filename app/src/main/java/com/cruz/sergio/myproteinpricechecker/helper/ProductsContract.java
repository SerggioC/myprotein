package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Sergio on 12/03/2017.
 */

public class ProductsContract {

   // The "Content authority" is a name for the entire content provider, similar to the
   // relationship between a domain name and its website.  A convenient string to use for the
   // content authority is the package name for the app, which is guaranteed to be unique on the
   // device.
   public static final String CONTENT_AUTHORITY = "com.cruz.sergio.myproteinpricechecker";

   // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
   // the content provider.
   public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

   // Possible paths (appended to base content URI for possible URI's)
   // For instance, content://com.cruz.sergio.myproteinpricechecker/products/ is a valid path for
   // looking at products data. content://com.cruz.sergio.myproteinpricechecker/givemeroot/ will fail,
   // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
   // At least, let's hope not.
   public static final String PATH_PRODUCTS = "products";
   public static final String PATH_PRICES = "prices";
   public static final String PATH_VOUCHERS = "vouchers";
   public static final String PATH_CARTS = "carts";

   public static final class ProductsEntry implements BaseColumns {

      // CONTENT_URI = content://com.cruz.sergio.myproteinpricechecker/products
      public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCTS).build();

      // CONTENT_DIR_TYPE = vnd.android.cursor.dir/com.cruz.sergio.myproteinpricechecker/products
      // MIME type para a tabela
      public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

      // CONTENT_ITEM_TYPE = vnd.android.cursor.item/com.cruz.sergio.myproteinpricechecker/products
      // MIME type para uma row
      public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
      // Products table
      public static final String TABLE_NAME = "products_table";
      public static final String COLUMN_PRODUCT_ID = "product_id";
      public static final String COLUMN_PRODUCT_NAME = "product_name";
      public static final String COLUMN_PRODUCT_SUBTITLE = "product_subtitle";
      public static final String COLUMN_PRODUCT_DESCRIPTION = "product_description";
      public static final String COLUMN_PRODUCT_BRAND = "product_brand";
      public static final String COLUMN_WEBSTORE_NAME = "webstore_name";
      public static final String COLUMN_PRODUCT_BASE_URL = "product_base_url";
      public static final String COLUMN_MP_WEBSTORE_DOMAIN_URL = "mp_webstore_domain"; // from shared prefs
      public static final String COLUMN_MP_SHIPPING_LOCATION = "mp_shipping_location"; // from shared prefs
      public static final String COLUMN_MP_CURRENCY = "mp_currency";                  // from shared prefs
      public static final String COLUMN_MP_CURRENCY_SYMBOL = "mp_currency_symbol";    // from shared prefs
      public static final String COLUMN_MP_LOCALE = "mp_locale";                      // from shared prefs
      public static final String COLUMN_MP_JSON_URL_DETAILS = "json_url_details";
      public static final String COLUMN_MP_VARIATION1 = "mp_variation1";
      public static final String COLUMN_MP_VARIATION2 = "mp_variation2";
      public static final String COLUMN_MP_VARIATION3 = "mp_variation3";
      public static final String COLUMN_MP_OPTIONS1 = "mp_options1";
      public static final String COLUMN_MP_OPTIONS2 = "mp_options2";
      public static final String COLUMN_MP_OPTIONS3 = "mp_options3";
      public static final String COLUMN_MIN_PRICE = "min_price";
      public static final String COLUMN_MIN_PRICE_VALUE = "min_price_value";
      public static final String COLUMN_MIN_PRICE_DATE = "min_price_date";
      public static final String COLUMN_MAX_PRICE = "max_price";
      public static final String COLUMN_MAX_PRICE_VALUE = "max_price_value";
      public static final String COLUMN_MAX_PRICE_DATE = "max_price_date";
      public static final String COLUMN_ACTUAL_PRICE = "actual_price";
      public static final String COLUMN_ACTUAL_PRICE_VALUE = "actual_price_value";
      public static final String COLUMN_ACTUAL_PRICE_DATE = "actual_price_date";
      public static final String COLUMN_PREVIOUS_PRICE_VALUE = "previous_price_value";
      public static final String COLUMN_PREVIOUS_PRICE_DATE = "previous_price_date";
      public static final String COLUMN_MP_VARIATION_NAME1 = "mp_variation_name1";
      public static final String COLUMN_MP_VARIATION_NAME2 = "mp_variation_name2";
      public static final String COLUMN_MP_VARIATION_NAME3 = "mp_variation_name3";
      public static final String COLUMN_MP_OPTIONS_NAME1 = "mp_options_name1";
      public static final String COLUMN_MP_OPTIONS_NAME2 = "mp_options_name2";
      public static final String COLUMN_MP_OPTIONS_NAME3 = "mp_options_name3";
      public static final String COLUMN_CUSTOM_PRODUCT_ID = "mp_custom_product_id";
      public static final String COLUMN_ARRAYLIST_IMAGES = "mp_array_images";
      public static final String COLUMN_NOTIFICATIONS = "notifications";
      public static final String COLUMN_NOTIFY_VALUE = "notify_value";
      public static final String COLUMN_IS_IN_CART = "is_in_cart";
      public static final String COLUMN_CART_ID = "cart_id";
      public static final String[] ALL_PRODUCT_COLUMNS_PROJECTION = new String[]{ //Todas as colunas da tabela
          ProductsEntry._ID,
          ProductsEntry.COLUMN_PRODUCT_ID,
          ProductsEntry.COLUMN_PRODUCT_NAME,
          ProductsEntry.COLUMN_PRODUCT_SUBTITLE,
          ProductsEntry.COLUMN_PRODUCT_DESCRIPTION,
          ProductsEntry.COLUMN_PRODUCT_BRAND,
          ProductsEntry.COLUMN_WEBSTORE_NAME,
          ProductsEntry.COLUMN_PRODUCT_BASE_URL,
          ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL,
          ProductsEntry.COLUMN_MP_SHIPPING_LOCATION,
          ProductsEntry.COLUMN_MP_CURRENCY,
          ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL,
          ProductsEntry.COLUMN_MP_LOCALE,
          ProductsEntry.COLUMN_MP_JSON_URL_DETAILS,
          ProductsEntry.COLUMN_MP_VARIATION1,
          ProductsEntry.COLUMN_MP_VARIATION2,
          ProductsEntry.COLUMN_MP_VARIATION3,
          ProductsEntry.COLUMN_MP_OPTIONS1,
          ProductsEntry.COLUMN_MP_OPTIONS2,
          ProductsEntry.COLUMN_MP_OPTIONS3,
          ProductsEntry.COLUMN_MIN_PRICE,
          ProductsEntry.COLUMN_MIN_PRICE_VALUE,
          ProductsEntry.COLUMN_MIN_PRICE_DATE,
          ProductsEntry.COLUMN_MAX_PRICE,
          ProductsEntry.COLUMN_MAX_PRICE_VALUE,
          ProductsEntry.COLUMN_MAX_PRICE_DATE,
          ProductsEntry.COLUMN_ACTUAL_PRICE,
          ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE,
          ProductsEntry.COLUMN_ACTUAL_PRICE_DATE,
          ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE,
          ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE,
          ProductsEntry.COLUMN_MP_VARIATION_NAME1,
          ProductsEntry.COLUMN_MP_VARIATION_NAME2,
          ProductsEntry.COLUMN_MP_VARIATION_NAME3,
          ProductsEntry.COLUMN_MP_OPTIONS_NAME1,
          ProductsEntry.COLUMN_MP_OPTIONS_NAME2,
          ProductsEntry.COLUMN_MP_OPTIONS_NAME3,
          ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID,
          ProductsEntry.COLUMN_ARRAYLIST_IMAGES,
          ProductsEntry.COLUMN_NOTIFICATIONS,
          ProductsEntry.COLUMN_NOTIFY_VALUE,
          ProductsEntry.COLUMN_IS_IN_CART,
          ProductsEntry.COLUMN_CART_ID
      };

      public static final int _ID_INDEX = 0;
      public static final int COLUMN_PRODUCT_ID_INDEX = 1;
      public static final int COLUMN_PRODUCT_NAME_INDEX = 2;
      public static final int COLUMN_PRODUCT_SUBTITLE_INDEX = 3;
      public static final int COLUMN_PRODUCT_DESCRIPTION_INDEX = 4;
      public static final int COLUMN_PRODUCT_BRAND_INDEX = 5;
      public static final int COLUMN_WEBSTORE_NAME_INDEX = 6;
      public static final int COLUMN_PRODUCT_BASE_URL_INDEX = 7;
      public static final int COLUMN_MP_WEBSTORE_DOMAIN_URL_INDEX = 8;
      public static final int COLUMN_MP_SHIPPING_LOCATION_INDEX = 9;
      public static final int COLUMN_MP_CURRENCY_INDEX = 10;
      public static final int COLUMN_MP_CURRENCY_SYMBOL_INDEX = 11;
      public static final int COLUMN_MP_LOCALE_INDEX = 12;
      public static final int COLUMN_MP_JSON_URL_DETAILS_INDEX = 13;
      public static final int COLUMN_MP_VARIATION1_INDEX = 14;
      public static final int COLUMN_MP_VARIATION2_INDEX = 15;
      public static final int COLUMN_MP_VARIATION3_INDEX = 16;
      public static final int COLUMN_MP_OPTIONS1_INDEX = 17;
      public static final int COLUMN_MP_OPTIONS2_INDEX = 18;
      public static final int COLUMN_MP_OPTIONS3_INDEX = 19;
      public static final int COLUMN_MIN_PRICE_INDEX = 20;
      public static final int COLUMN_MIN_PRICE_VALUE_INDEX = 21;
      public static final int COLUMN_MIN_PRICE_DATE_INDEX = 22;
      public static final int COLUMN_MAX_PRICE_INDEX = 23;
      public static final int COLUMN_MAX_PRICE_VALUE_INDEX = 24;
      public static final int COLUMN_MAX_PRICE_DATE_INDEX = 25;
      public static final int COLUMN_ACTUAL_PRICE_INDEX = 26;
      public static final int COLUMN_ACTUAL_PRICE_VALUE_INDEX = 27;
      public static final int COLUMN_ACTUAL_PRICE_DATE_INDEX = 28;
      public static final int COLUMN_PREVIOUS_PRICE_VALUE_INDEX = 29;
      public static final int COLUMN_PREVIOUS_PRICE_DATE_INDEX = 30;
      public static final int COLUMN_MP_VARIATION_NAME1_INDEX = 31;
      public static final int COLUMN_MP_VARIATION_NAME2_INDEX = 32;
      public static final int COLUMN_MP_VARIATION_NAME3_INDEX = 33;
      public static final int COLUMN_MP_OPTIONS_NAME1_INDEX = 34;
      public static final int COLUMN_MP_OPTIONS_NAME2_INDEX = 35;
      public static final int COLUMN_MP_OPTIONS_NAME3_INDEX = 36;
      public static final int COLUMN_CUSTOM_PRODUCT_ID_INDEX = 37;
      public static final int COLUMN_ARRAYLIST_IMAGES_INDEX = 38;
      public static final int COLUMN_NOTIFICATIONS_INDEX = 39;
      public static final int COLUMN_NOTIFY_VALUE_INDEX = 40;
      public static final int COLUMN_IS_IN_CART_INDEX = 41;
      public static final int COLUMN_CART_ID_INDEX = 42;

      //Para uma row especifica
      public static Uri buildProductsUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }


   }

   public static final class PricesEntry implements BaseColumns {
      public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICES).build();

      public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICES;
      public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICES;

      // Prices table
      public static final String TABLE_NAME = "prices_table";
      public static final String COLUMN_ID_PRODUCTS = "_id_products"; // foreign key = _ID
      public static final String COLUMN_PRODUCT_PRICE = "product_price";
      public static final String COLUMN_PRODUCT_PRICE_VALUE = "product_price_value";
      public static final String COLUMN_PRODUCT_PRICE_DATE = "product_price_date";
      public static final String[] ALL_PRICE_COLUMNS_PROJECTION = new String[]{
          PricesEntry._ID,
          PricesEntry.COLUMN_ID_PRODUCTS,
          PricesEntry.COLUMN_PRODUCT_PRICE,
          PricesEntry.COLUMN_PRODUCT_PRICE_VALUE,
          PricesEntry.COLUMN_PRODUCT_PRICE_DATE
      };

      public static Uri buildPricesUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }
   }

   public static final class VouchersEntry implements BaseColumns {
      public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VOUCHERS).build();

      public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOUCHERS;
      public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_VOUCHERS;

      // Vouchers table
      public static final String TABLE_NAME = "vouchers_table";
      public static final String COLUMN_VOUCHER_CODE = "voucher_code";
      public static final String COLUMN_VOUCHER_DISCOUNT = "voucher_discount";
      public static final String COLUMN_VOUCHER_VALIDITY = "voucher_validity";
      public static final String COLUMN_VOUCHER_WEBSTORE = "voucher_webstore";
      public static final String COLUMN_VOUCHER_COUNTRY_CODE = "voucher_country_code";
      public static final String COLUMN_VOUCHER_IS_ACTIVE = "voucher_is_active";
      public static final String[] ALL_VOUCHERS_COLUMNS_PROJECTION = new String[]{
          VouchersEntry._ID,
          VouchersEntry.COLUMN_VOUCHER_CODE,
          VouchersEntry.COLUMN_VOUCHER_DISCOUNT,
          VouchersEntry.COLUMN_VOUCHER_VALIDITY,
          VouchersEntry.COLUMN_VOUCHER_WEBSTORE,
          VouchersEntry.COLUMN_VOUCHER_COUNTRY_CODE,
          VouchersEntry.COLUMN_VOUCHER_IS_ACTIVE
      };

      public static Uri buildVouchersUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }

   }

   public static final class CartsEntry implements BaseColumns {
      public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CARTS).build();

      public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARTS;
      public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARTS;

      // Carts table
      public static final String TABLE_NAME = "carts_table";
      public static final String COLUMN_ID_PRODUCTS = "_id_products";
      public static final String COLUMN_CART_ID = "cart_id";
      public static final String[] ALL_CARTS_COLUMNS_PROJECTION = new String[]{
          CartsEntry._ID,
          CartsEntry.COLUMN_ID_PRODUCTS,
          CartsEntry.COLUMN_CART_ID
      };

      public static Uri buildCartsUri(long id) {
         return ContentUris.withAppendedId(CONTENT_URI, id);
      }

   }
}