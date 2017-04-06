package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import static android.database.DatabaseUtils.dumpCursorToString;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.ALL_PRODUCT_COLUMNS_PROJECTION;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;


public class WatchingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    Activity mActivity;
    SwipeRefreshLayout watchingSwipeRefreshLayout;
    static cursorDBAdapter cursorDBAdapter;
    ListView listViewItems;
    static Loader<Cursor> loaderManager;

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView titleView; // ou Product Name
        public final TextView highestPriceView;
        public final TextView lowestPriceView;
        public final TextView currentPriceView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.item_icon);
            titleView = (TextView) view.findViewById(R.id.item_title_textview);
            highestPriceView = (TextView) view.findViewById(R.id.item_highest_price_textview);
            lowestPriceView = (TextView) view.findViewById(R.id.item_lowest_price_textview);
            currentPriceView = (TextView) view.findViewById(R.id.item_current_price_textview);
        }
    }

    public WatchingFragment() {
        //required empty constructor?
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.watching_fragment, null);
        loaderManager = getLoaderManager().initLoader(LOADER_ID, null, this);


/*        DBHelper dbHelper = new DBHelper(mActivity);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor all_products_cursor = db.rawQuery("SELECT * FROM '" + ProductsContract.ProductsEntry.TABLE_NAME + "'", null);
        Log.w("Sergio>>>", this + " onCreateView: all_products_cursor=\n" + dumpCursorToString(all_products_cursor));
*/
        cursorDBAdapter = new cursorDBAdapter(mActivity, null, 0);
        listViewItems = (ListView) rootview.findViewById(R.id.watching_listview);
        listViewItems.setAdapter(cursorDBAdapter);
        listViewItems.addHeaderView(View.inflate(mActivity, R.layout.watch_list_header_view, null));
        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //((Callback) mActivity).onItemSelected("Altamente");
                    Log.i("Sergio>>>", this + " onItemClick: callback dumpCursorToString= " + dumpCursorToString(cursor));
                }
            }
        });

        watchingSwipeRefreshLayout = (SwipeRefreshLayout) rootview.findViewById(R.id.watching_swiperefresh);
        watchingSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getWatchingProducts();
                    }
                }
        );
        return rootview;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("Sergio>>>", this + " onCreateLoader: id= " + id);
//
//        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI.buildUpon()
//                .appendPath(ProductsContract.ProductsEntry.TABLE_NAME)
//                .build();
        Log.i("Sergio>>>", this + " onCreateLoader: " +
                "\nCONTENT_DIR_TYPE= " + CONTENT_DIR_TYPE +
                " \nCONTENT_ITEM_TYPE= " + CONTENT_ITEM_TYPE);

        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI;
        Log.d("Sergio>>>", this + " onCreateLoader: " +
                "\nuri=" + uri);

        //String selection = "WHERE '" + ProductsContract.ProductsEntry.TABLE_NAME + "' = 'qualquercoisa'";
        CursorLoader cursor_loader = new CursorLoader(
                mActivity,
                uri,
                ALL_PRODUCT_COLUMNS_PROJECTION,
                null,
                null,
                ProductsContract.ProductsEntry._ID + " ASC "
        );
        Log.d("Sergio>>>", this + " onCreateLoader: cursor_loader= " + cursor_loader);

        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("Sergio>>>", this + " onLoadFinished: dumpCursorToString " +
                "\nCursor data= " + dumpCursorToString(data));
        cursorDBAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorDBAdapter.swapCursor(null);
    }

    public class cursorDBAdapter extends CursorAdapter {
        private LayoutInflater cursorItemInflater;

        public cursorDBAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            cursorItemInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Log.d("Sergio>>>", this + " cursorDBAdapter: cursorItemInflater \n" + cursorItemInflater);
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            Log.d("Sergio>>>", this + " newView: cursor= " + cursor + " parent= " + parent);
            View item_root = cursorItemInflater.inflate(R.layout.watching_item_layout, parent, false);
            ViewHolder viewHolder = new ViewHolder(item_root);
            item_root.setTag(viewHolder);
            return item_root;
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            //Log.i("Sergio>", this + " bindView: cursor=\n" + dumpCursorToString(cursor));

            String prod_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
            String img_url = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_BASE_IMG_URL));
            String max_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE));
            String min_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
            String current_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
            String options_sabor = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1));
            String options_caixa = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2));
            String options_quant = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3));
            if (options_sabor == null) options_sabor = "";
            if (options_caixa == null) options_caixa = "";
            if (options_quant == null) options_quant = "";
            if (max_price == null || max_price.equals("0")) max_price = "-";
            if (min_price == null || min_price.equals("0")) min_price = "-";
            if (current_price == null || current_price.equals("0")) current_price = "-";

            if (img_url != null) {
                Glide.with(mActivity).load(img_url).into(viewHolder.iconView);
            } else {
                Glide.with(mActivity).load(R.drawable.noimage).into(viewHolder.iconView);
            }
            viewHolder.titleView.setText(prod_name + " " + options_sabor + " " + options_caixa + " " + options_quant);
            viewHolder.highestPriceView.setText(max_price);
            viewHolder.lowestPriceView.setText(min_price);
            viewHolder.currentPriceView.setText(current_price);

            Log.i("Sergio>>>", this + " bindView: prod_name= " + prod_name);
        }
    }

    public void getWatchingProducts() {
/*        DBHelper dbHelper = new DBHelper(mActivity);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Extract full products_table Table from database (SELECT *)
        Cursor all_products_cursor = db.rawQuery("SELECT * FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);*/
        //Log.i("Sergio>>>", this + " getWatchingProducts: all_products_cursor\n" + dumpCursorToString(all_products_cursor));

        //if (all_products_cursor.getCount() > 0) {
        if (true) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            DetailsFragment.showCustomToast(mActivity, "Empty DataBase...", R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_SHORT);
        }
        //db.close();
        //all_products_cursor.close();
        watchingSwipeRefreshLayout.setRefreshing(false);
    }
}
