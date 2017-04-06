package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Sergio on 12/03/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 7;

    static final String DATABASE_NAME = "pricetracker.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductsContract.ProductsEntry.TABLE_NAME + " (" +
                ProductsContract.ProductsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_LOCALE + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_BASE_IMG_URL + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_ZOOM_IMG_URL + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " TEXT NOT NULL" +
//                " FOREIGN KEY (" + ProductsContract.ProductsEntry.COLUMN_PRODUCT_ID + ") REFERENCES " +
//                ProductsContract.PriceEntry.TABLE_NAME + " (" + ProductsContract.PriceEntry._ID + ") " +
                " );";

        final String SQL_CREATE_PRICES_TABLE = "CREATE TABLE " + ProductsContract.PriceEntry.TABLE_NAME + " (" +
                ProductsContract.PriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsContract.PriceEntry.COLUMN_ID_PRODUCTS + " INTEGER NOT NULL, " + // igual a _ID da Products_Table
                ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE + " TEXT NOT NULL, " +
                ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE_VALUE + " REAL NOT NULL, " +
                ProductsContract.PriceEntry.COLUMN_PRODUCT_PRICE_DATE + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + ProductsContract.PriceEntry.COLUMN_ID_PRODUCTS + ") REFERENCES " +
                ProductsContract.ProductsEntry.TABLE_NAME + " (" + ProductsContract.ProductsEntry._ID + ") " +
                " );";

        Log.i("Sergio>>>", "SQL_CREATE_PRODUCT_TABLE= " + SQL_CREATE_PRODUCT_TABLE);
        Log.i("Sergio>>>", "SQL_CREATE_PRICES_TABLE= " + SQL_CREATE_PRICES_TABLE);

        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
        db.execSQL(SQL_CREATE_PRICES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.ProductsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.PriceEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.ProductsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.PriceEntry.TABLE_NAME);
        onCreate(db);
    }

}
