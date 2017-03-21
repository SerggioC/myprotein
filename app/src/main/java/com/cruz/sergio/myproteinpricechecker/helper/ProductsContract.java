package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

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
    public static final String PATH_MINMAX = "minmax";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class ProductsEntry implements BaseColumns {

        // CONTENT_URI = content://com.cruz.sergio.myproteinpricechecker/products
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCTS).build();

        // CONTENT_DIR_TYPE = vnd.android.cursor.dir/com.cruz.sergio.myproteinpricechecker/products
        // MIME type para a tabela
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // CONTENT_ITEM_TYPE = vnd.android.cursor.item/com.cruz.sergio.myproteinpricechecker/products
        // MIME type para uma row
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //Para uma row especifica
        public static Uri buildProductsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // Products table
        public static final String TABLE_NAME = "products_table";
        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRODUCT_SUBTITLE = "product_subtitle";
        public static final String COLUMN_PRODUCT_DESCRIPTION = "product_description";
        public static final String COLUMN_WEBSTORE_NAME = "webstore_name";
        public static final String COLUMN_PRODUCT_BASE_URL = "product_base_url";
        public static final String COLUMN_MP_WEBSTORE_DOMAIN_URL = "mp_webstore_domain"; // from shared prefs
        public static final String COLUMN_MP_SHIPPING_LOCATION = "mp_shipping_location"; // from shared prefs
        public static final String COLUMN_MP_CURRENCY = "mp_currency";                  // from shared prefs
        public static final String COLUMN_MP_CURRENCY_SYMBOL = "mp_currency_symbol";    // from shared prefs
        public static final String COLUMN_MP_LOCALE = "mp_locale";                      // from shared prefs
        public static final String COLUMN_MP_JSON_URL_DETAILS = "json_url_Details";
        public static final String COLUMN_MP_VARIATION1 = "mp_variation1";
        public static final String COLUMN_MP_VARIATION2 = "mp_variation2";
        public static final String COLUMN_MP_VARIATION3 = "mp_variation3";
        public static final String COLUMN_MP_OPTIONS1 = "mp_options1";
        public static final String COLUMN_MP_OPTIONS2 = "mp_options2";
        public static final String COLUMN_MP_OPTIONS3 = "mp_options3";
        public static final String COLUMN_MP_BASE_IMG_URL = "mp_base_img_url";
        public static final String COLUMN_MP_ZOOM_IMG_URL = "mp_zoom_img_url";
        public static final String COLUMN_MIN_PRICE = "min_price";
        public static final String COLUMN_MIN_PRICE_DATE = "min_price_date";
        public static final String COLUMN_MAX_PRICE = "max_price";
        public static final String COLUMN_MAX_PRICE_DATE = "max_price_date";
        public static final String COLUMN_MP_VARIATION_NAME1 = "mp_variation_name1";
        public static final String COLUMN_MP_VARIATION_NAME2 = "mp_variation_name2";
        public static final String COLUMN_MP_VARIATION_NAME3 = "mp_variation_name3";
        public static final String COLUMN_MP_OPTIONS_NAME1 = "mp_options_name1";
        public static final String COLUMN_MP_OPTIONS_NAME2 = "mp_options_name2";
        public static final String COLUMN_MP_OPTIONS_NAME3 = "mp_options_name3";
        public static final String COLUMN_CUSTOM_PRODUCT_ID = "mp_custom_product_id";

        public static final String[] ALL_PRODUCT_COLUMNS_PROJECTION = new String[]{ //Todas as colunas da tabela
                ProductsEntry._ID,
                ProductsEntry.COLUMN_PRODUCT_ID,
                ProductsEntry.COLUMN_PRODUCT_NAME,
                ProductsEntry.COLUMN_PRODUCT_SUBTITLE,
                ProductsEntry.COLUMN_PRODUCT_DESCRIPTION,
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
                ProductsEntry.COLUMN_MP_BASE_IMG_URL,
                ProductsEntry.COLUMN_MP_ZOOM_IMG_URL,
                ProductsEntry.COLUMN_MIN_PRICE,
                ProductsEntry.COLUMN_MIN_PRICE_DATE,
                ProductsEntry.COLUMN_MAX_PRICE,
                ProductsEntry.COLUMN_MAX_PRICE_DATE,
                ProductsEntry.COLUMN_MP_VARIATION_NAME1,
                ProductsEntry.COLUMN_MP_VARIATION_NAME2,
                ProductsEntry.COLUMN_MP_VARIATION_NAME3,
                ProductsEntry.COLUMN_MP_OPTIONS_NAME1,
                ProductsEntry.COLUMN_MP_OPTIONS_NAME2,
                ProductsEntry.COLUMN_MP_OPTIONS_NAME3,
                ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID
        };
    }

    public static final class PriceEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICES).build();

        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICES;

        // Prices table
        public static final String TABLE_NAME = "prices_table";
        public static final String COLUMN_ID_PRODUCTS = "_id_products"; // foreign key = _ID
        public static final String COLUMN_PRODUCT_PRICE = "product_price";
        public static final String COLUMN_PRODUCT_PRICE_VALUE = "product_price_value";
        public static final String COLUMN_PRODUCT_PRICE_DATE = "product_price_date";

        public static Uri buildPricesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String[] ALL_PRICE_COLUMNS_PROJECTION = new String[]{
                PriceEntry._ID,
                PriceEntry.COLUMN_ID_PRODUCTS,
                PriceEntry.COLUMN_PRODUCT_PRICE,
                PriceEntry.COLUMN_PRODUCT_PRICE_VALUE,
                PriceEntry.COLUMN_PRODUCT_PRICE_DATE
        };
    }

    public static final class MinMaxEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MINMAX).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MINMAX;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MINMAX;

        // Min Max prices table
        public static final String TABLE_NAME = "minmax_table";
        public static final String COLUMN_PRODUCT_ID = "product_id"; // foreign key
        public static final String COLUMN_MIN_PRICE = "min_price";
        public static final String COLUMN_MIN_PRICE_DATE = "min_price_date";
        public static final String COLUMN_MAX_PRICE = "max_price";
        public static final String COLUMN_MAX_PRICE_DATE = "max_price_date";

        public static Uri buildMinMaxUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}