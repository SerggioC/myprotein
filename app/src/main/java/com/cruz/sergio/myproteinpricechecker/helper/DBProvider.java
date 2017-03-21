package com.cruz.sergio.myproteinpricechecker.helper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class DBProvider extends ContentProvider {
    private DBHelper mOpenHelper;
    static final int PRODUCTS = 100;
    static final int PRODUCTS_ID = 101;
    static final int PRICES = 200;
    static final int PRICES_ID = 201;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursorProvider;
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            case PRODUCTS: {
                cursorProvider = database.query(
                        ProductsContract.ProductsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PRICES: {
                cursorProvider = database.query(
                        ProductsContract.PriceEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursorProvider.setNotificationUri(getContext().getContentResolver(), uri);
        return cursorProvider;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                return ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
            case PRODUCTS_ID:
                return ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;
            case PRICES:
                return ProductsContract.PriceEntry.CONTENT_DIR_TYPE;
            case PRICES_ID:
                return ProductsContract.PriceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case PRODUCTS: {
                long _id = db.insert(ProductsContract.ProductsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductsContract.ProductsEntry.buildProductsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
            }
            case PRICES: {
                long _id = db.insert(ProductsContract.PriceEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductsContract.PriceEntry.buildPricesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.d("Sergio>>>", "insert: returnUri= " + returnUri);

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /*
    Students: Here is where you need to create the UriMatcher. This UriMatcher will
    match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
    and LOCATION integer constants defined above.  You can test this by uncommenting the
    testUriMatcher test within TestUriMatcher.
 */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ProductsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, ProductsContract.PATH_PRODUCTS, PRODUCTS);
        matcher.addURI(authority, ProductsContract.PATH_PRODUCTS + "/#", PRODUCTS_ID);
        matcher.addURI(authority, ProductsContract.PATH_PRICES, PRICES);
        matcher.addURI(authority, ProductsContract.PATH_PRICES + "/#", PRICES_ID);

        return matcher;
    }


}
