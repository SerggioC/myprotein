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
    static final int PRODUCTS = 100;
    static final int PRODUCTS_ID = 101;
    static final int PRICES = 200;
    static final int PRICES_ID = 201;
    static final int VOUCHERS = 300;
    static final int VOUCHERS_ID = 301;
    static final int CARTS = 400;
    static final int CARTS_ID = 401;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DBHelper mOpenHelper;

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
        matcher.addURI(authority, ProductsContract.PATH_VOUCHERS, VOUCHERS);
        matcher.addURI(authority, ProductsContract.PATH_VOUCHERS + "/#", VOUCHERS_ID);
        matcher.addURI(authority, ProductsContract.PATH_CARTS, CARTS);
        matcher.addURI(authority, ProductsContract.PATH_CARTS + "/#", CARTS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();

        switch (sUriMatcher.match(uri)) {
            case PRODUCTS: {
                cursor = database.query(
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
                cursor = database.query(
                        ProductsContract.PricesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case VOUCHERS: {
                cursor = database.query(
                        ProductsContract.VouchersEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CARTS: {
                cursor = database.query(
                        ProductsContract.CartsEntry.TABLE_NAME,
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
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case PRODUCTS:
                return ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
            case PRODUCTS_ID:
                return ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;
            case PRICES:
                return ProductsContract.PricesEntry.CONTENT_DIR_TYPE;
            case PRICES_ID:
                return ProductsContract.PricesEntry.CONTENT_ITEM_TYPE;
            case VOUCHERS:
                return ProductsContract.VouchersEntry.CONTENT_DIR_TYPE;
            case VOUCHERS_ID:
                return ProductsContract.VouchersEntry.CONTENT_ITEM_TYPE;
            case CARTS:
                return ProductsContract.CartsEntry.CONTENT_DIR_TYPE;
            case CARTS_ID:
                return ProductsContract.CartsEntry.CONTENT_ITEM_TYPE;
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
                break;
            }
            case PRICES: {
                long _id = db.insert(ProductsContract.PricesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductsContract.PricesEntry.buildPricesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case VOUCHERS: {
                long _id = db.insert(ProductsContract.VouchersEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductsContract.VouchersEntry.buildVouchersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CARTS: {
                long _id = db.insert(ProductsContract.CartsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = ProductsContract.CartsEntry.buildCartsUri(_id);
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

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int deleted_rows;
        String id = uri.getPathSegments().get(1);

        switch (match) {
            case PRODUCTS_ID: {
                deleted_rows = db.delete(ProductsContract.ProductsEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            }
            case PRICES_ID: {
                deleted_rows = db.delete(ProductsContract.PricesEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            }
            case VOUCHERS_ID: {
                deleted_rows = db.delete(ProductsContract.VouchersEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            }
            case CARTS_ID: {
                deleted_rows = db.delete(ProductsContract.CartsEntry.TABLE_NAME, "_id=?", new String[]{id});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        getContext().getContentResolver().notifyChange(uri, null);

        return deleted_rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int updated_rows;
        String id = uri.getPathSegments().get(1);


        switch (match) {
            case PRODUCTS_ID: {
                updated_rows = db.update(ProductsContract.ProductsEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            }
            case PRICES_ID: {
                updated_rows = db.update(ProductsContract.PricesEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            }
            case VOUCHERS_ID: {
                updated_rows = db.update(ProductsContract.VouchersEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            }
            case CARTS_ID: {
                updated_rows = db.update(ProductsContract.CartsEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return updated_rows;
    }


}
