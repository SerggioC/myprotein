package com.cruz.sergio.myproteinpricechecker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;
import android.util.Log;

import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Created by Sergio on 01/05/2017.
 */

public class WatchingCursorLoader extends AsyncTaskLoader<Cursor> {
    final ForceLoadContentObserver mObserver;

    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    Cursor mCursor;
    CancellationSignal mCancellationSignal;

    /* Runs on a worker thread */
    @Override
    public Cursor loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        try {
            //Cursor cursor = ContentResolverCompat.query(getContext().getContentResolver(), mUri, mProjection, mSelection, mSelectionArgs, mSortOrder, mCancellationSignal);

            DBHelper dbHelper = new DBHelper(getContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor wCursor = db.rawQuery("SELECT * FROM " +
                    ProductsContract.ProductsEntry.TABLE_NAME + " JOIN " + ProductsContract.PricesEntry.TABLE_NAME + " ON " +
                    ProductsContract.ProductsEntry.TABLE_NAME + "." + ProductsContract.ProductsEntry._ID + " = " +
                    ProductsContract.PricesEntry.TABLE_NAME + "." + ProductsContract.PricesEntry.COLUMN_ID_PRODUCTS
                    , null);

            String query = "SELECT * FROM products_table JOIN ";

            Log.i("Sergio>", this + "loadInBackground:\nwCursor=\n" + wCursor);

            if (wCursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    wCursor.getCount();
                    wCursor.registerContentObserver(mObserver);
                } catch (RuntimeException ex) {
                    wCursor.close();
                    throw ex;
                }
            }
            return wCursor;
        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();

        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Creates an empty unspecified CursorLoader.  You must follow this with
     * calls to {@link #setUri(Uri)}, {@link #setSelection(String)}, etc
     * to specify the query to perform.
     */
    public WatchingCursorLoader(Context context) {
        super(context);
        mObserver = new ForceLoadContentObserver();
    }

    public WatchingCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context);
        mObserver = new ForceLoadContentObserver();
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
        }
        mCursor = null;
    }

    public Uri getUri() {
        return mUri;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public String[] getProjection() {
        return mProjection;
    }

    public void setProjection(String[] projection) {
        mProjection = projection;
    }

    public String getSelection() {
        return mSelection;
    }

    public void setSelection(String selection) {
        mSelection = selection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public void setSelectionArgs(String[] selectionArgs) {
        mSelectionArgs = selectionArgs;
    }

    public String getSortOrder() {
        return mSortOrder;
    }

    public void setSortOrder(String sortOrder) {
        mSortOrder = sortOrder;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix);
        writer.print("mUri=");
        writer.println(mUri);
        writer.print(prefix);
        writer.print("mProjection=");
        writer.println(Arrays.toString(mProjection));
        writer.print(prefix);
        writer.print("mSelection=");
        writer.println(mSelection);
        writer.print(prefix);
        writer.print("mSelectionArgs=");
        writer.println(Arrays.toString(mSelectionArgs));
        writer.print(prefix);
        writer.print("mSortOrder=");
        writer.println(mSortOrder);
        writer.print(prefix);
        writer.print("mCursor=");
        writer.println(mCursor);
    }

}
