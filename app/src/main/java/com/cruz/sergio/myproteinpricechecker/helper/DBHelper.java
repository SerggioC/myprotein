package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sergio on 12/03/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

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
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_BRAND + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_WEBSTORE_DOMAIN_URL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_SHIPPING_LOCATION + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_MP_LOCALE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_JSON_URL_DETAILS + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE + " REAL, " +
                ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE + " REAL, " +
                ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE + " REAL, " +
                ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE + " REAL, " +
                ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE + " INTEGER, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_VARIATION_NAME3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3 + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_CUSTOM_PRODUCT_ID + " TEXT NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS + " INTEGER NOT NULL, " +
                ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE + "  REAL, " +
                ProductsContract.ProductsEntry.COLUMN_IS_IN_CART + " TEXT, " +
                ProductsContract.ProductsEntry.COLUMN_CART_ID + " TEXT " +
                " );";

        final String SQL_CREATE_PRICES_TABLE = "CREATE TABLE " + ProductsContract.PricesEntry.TABLE_NAME + " (" +
                ProductsContract.PricesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + " INTEGER NOT NULL, " + // igual a _ID da Products_Table
                ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE + " TEXT NOT NULL, " +
                ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_VALUE + " REAL NOT NULL, " +
                ProductsContract.PricesEntry.COLUMN_PRODUCT_PRICE_DATE + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS + ") REFERENCES " +
                ProductsContract.ProductsEntry.TABLE_NAME + " (" + ProductsContract.ProductsEntry._ID + ")" +
                " ON DELETE CASCADE);";

        final String SQL_CREATE_VOUCHERS_TABLE = "CREATE TABLE " + ProductsContract.VouchersEntry.TABLE_NAME + " (" +
                ProductsContract.VouchersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_CODE + " TEXT NOT NULL, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_DISCOUNT + " TEXT, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_VALIDITY + " TEXT, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_WEBSTORE + " TEXT NOT NULL, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_COUNTRY_CODE + " TEXT, " +
                ProductsContract.VouchersEntry.COLUMN_VOUCHER_IS_ACTIVE + " INTEGER NOT NULL" +
                ");";

        final String SQL_CREATE_CARTS_TABLE = "CREATE TABLE " + ProductsContract.CartsEntry.TABLE_NAME + " (" +
                ProductsContract.CartsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductsContract.CartsEntry.COLUMN_ID_PRODUCTS + " INTEGER NOT NULL, " +
                ProductsContract.CartsEntry.COLUMN_CART_ID + " INTEGER NOT NULL, " + // cart ID A criar no código
                " FOREIGN KEY (" + ProductsContract.CartsEntry.COLUMN_ID_PRODUCTS + ") REFERENCES " +
                ProductsContract.ProductsEntry.TABLE_NAME + " (" + ProductsContract.ProductsEntry._ID + ")" +
                " ON DELETE CASCADE);";


        String SQL_CREATE_JOBS_TABLE = "CREATE TABLE jobs (_id INTEGER PRIMARY KEY AUTOINCREMENT, time TEXT NOT NULL, diff TEXT NOT NULL, status TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
        db.execSQL(SQL_CREATE_PRICES_TABLE);
        db.execSQL(SQL_CREATE_VOUCHERS_TABLE);
        db.execSQL(SQL_CREATE_CARTS_TABLE);
        db.execSQL(SQL_CREATE_JOBS_TABLE);

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
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.PricesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.VouchersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.CartsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS jobs");
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.ProductsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.PricesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.VouchersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ProductsContract.CartsEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS jobs");
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            // db.execSQL("PRAGMA foreign_keys=ON;");
            db.setForeignKeyConstraintsEnabled(true);
        }
    }
}
