package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.util.DisplayMetrics.DENSITY_HIGH;
import static android.util.DisplayMetrics.DENSITY_LOW;
import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static android.util.DisplayMetrics.DENSITY_XXHIGH;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.UPDATE_ONSTART;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.density;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.TabFragment.tabLayout;
import static com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice.updatePricesOnStart;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.ALL_PRODUCT_COLUMNS_PROJECTION;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;
import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class WatchingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    Activity mActivity;
    SwipeRefreshLayout watchingSwipeRefreshLayout;
    static cursorDBAdapter cursorDBAdapter;
    ListView listViewItems;
    static Loader<Cursor> loaderManager;
    static String[] imageSizesToUse;
    Timer timer = new Timer();
    Boolean[] isExpandedArray = null;


    public static class ViewHolder {
        public final TextView titleView; // ou Product Name
        public final TextView highestPriceView;
        public final TextView lowestPriceView;
        public final TextView currentPriceView;
        public final TextView highestPriceDate;
        public final TextView lowestPriceDate;
        public final TextView currentInfo;
        public final ImageSwitcher imageSwitcher;
        public final LinearLayout ll_current_price;
        public final ImageView up_down_icon;


        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.item_title_textview);
            highestPriceView = (TextView) view.findViewById(R.id.item_highest_price_textview);
            lowestPriceView = (TextView) view.findViewById(R.id.item_lowest_price_textview);
            currentPriceView = (TextView) view.findViewById(R.id.item_current_price_textview);
            highestPriceDate = (TextView) view.findViewById(R.id.item_highest_price_date);
            lowestPriceDate = (TextView) view.findViewById(R.id.item_lowest_price_date);
            currentInfo = (TextView) view.findViewById(R.id.current_info);
            ll_current_price = (LinearLayout) view.findViewById(R.id.ll_current_price);
            imageSwitcher = (ImageSwitcher) view.findViewById(R.id.image_switcher);
            up_down_icon = (ImageView) view.findViewById(R.id.up_down_arrow);
        }
    }

    public WatchingFragment() {
        //required empty constructor?
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        super.onCreate(savedInstanceState);

        if (density <= DENSITY_LOW) {
            imageSizesToUse = new String[]{"50x50", "60x60", "70x70"};
        } else if (density > DENSITY_LOW && density <= DENSITY_MEDIUM) {
            imageSizesToUse = new String[]{"60x60", "70x70"};
        } else if (density > DENSITY_MEDIUM && density <= DENSITY_HIGH) {
            imageSizesToUse = new String[]{"70x70", "100x100"};
        } else if (density > DENSITY_HIGH && density <= DENSITY_XHIGH) {
            imageSizesToUse = new String[]{"100x100", "130x130", "180x180"};
        } else if (density > DENSITY_XHIGH && density <= DENSITY_XXHIGH) { //galaxy S5: 480dpi scale = 3x; (70x70)*3 = 210x210;
            imageSizesToUse = new String[]{"180x180", "200x200", "270x270"};
        } else {
            imageSizesToUse = new String[]{"270x270", "300x300", "350x350"};
        }

        FirebaseJobservice jobservice = new FirebaseJobservice();
        jobservice.setUpdateCompleteListener(new FirebaseJobservice.UpdateCompleteListener() {
            @Override
            public void onUpdateReady(Boolean isReady) {
                Log.w("Sergio>", this + "\n" + "onUpdateReady= " + isReady);
                if (WatchingFragment.this.isAdded()) {
                    if (isReady) {
                        timer.cancel();
                        timer.purge();
                        timer = new Timer();
                        getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
                    }
                    if (watchingSwipeRefreshLayout != null) {
                        watchingSwipeRefreshLayout.setRefreshing(false);
                    }
                }

            }
        });

        DetailsFragment detailsFragment = new DetailsFragment();
        detailsFragment.setNewProductListener(new DetailsFragment.AddedNewProductListener() {
            @Override
            public void onProductAdded(Boolean addedNew) {
                Log.w("Sergio>", this + "\n" + "addedNewProduct= " + addedNew);
                timer.cancel();
                timer.purge();
                timer = new Timer();
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                tabLayout.setScrollPosition(0, 0f, true);
                tab.select();
                getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (UPDATE_ONSTART) {
            updatePricesOnStart(mActivity, false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.watching_fragment, null);
        loaderManager = getLoaderManager().initLoader(LOADER_ID, null, this);

        cursorDBAdapter = new cursorDBAdapter(mActivity, null, 0);
        listViewItems = (ListView) rootview.findViewById(R.id.watching_listview);
        listViewItems.setAdapter(cursorDBAdapter);
        listViewItems.addHeaderView(View.inflate(mActivity, R.layout.watch_list_header_view, null));
        listViewItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Log.i("Sergio>>>", this + "\n" +
                            "cursor get position = " + cursor.getPosition() + "\n" +
                            "list item position = " + position);

                    Boolean isExpanded = isExpandedArray[cursor.getPosition()];
                    View under_view = view.findViewById(R.id.under_cardview);
                    if (isExpanded) {
                        collapseIt(under_view);
                        isExpandedArray[cursor.getPosition()] = false;
                    } else {
                        expandIt(under_view);
                        isExpandedArray[cursor.getPosition()] = true;
                    }

                }
            }
        });

        watchingSwipeRefreshLayout = (SwipeRefreshLayout) rootview.findViewById(R.id.watching_swiperefresh);
        watchingSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        timer.cancel();
                        timer.purge();
                        timer = new Timer();
                        updatePricesOnStart(mActivity, false);
                    }
                }
        );

        return rootview;
    }

    public static void expandIt(final View view) {
        Log.i("Sergio>", " expandIt\nview= " + view.getHeight() + " " + view.getVisibility() + " " + view.getLayoutParams().height);
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1 ? LinearLayout.LayoutParams.WRAP_CONTENT : (int) (targetHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(a);
    }

    public static void collapseIt(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
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

//        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI.buildUpon()
//                .appendPath(ProductsContract.ProductsEntry.TABLE_NAME)
//                .build();

        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI;
        Log.i("Sergio>>>", this + " onCreateLoader:  id= " + id + "\n" +
                "CONTENT_DIR_TYPE= " + CONTENT_DIR_TYPE + "\n" +
                "CONTENT_ITEM_TYPE= " + CONTENT_ITEM_TYPE + "\n" +
                "uri= " + uri);

        //String selection = "WHERE '" + ProductsContract.ProductsEntry.TABLE_NAME + "' = 'qualquercoisa'";
        CursorLoader cursor_loader = new CursorLoader(
                mActivity,
                uri,
                ALL_PRODUCT_COLUMNS_PROJECTION,
                null,
                null,
                ProductsContract.ProductsEntry._ID + " ASC ");
        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

//        dump_BIGdata_toLog(dumpCursorToString(data));

        if (data.getCount() == 0) {
            DetailsFragment.showCustomToast(mActivity, "Empty DataBase! Add products to track their prices.",
                    R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_SHORT);
            if (watchingSwipeRefreshLayout != null) {
                watchingSwipeRefreshLayout.setRefreshing(false);
            }
        }
        cursorDBAdapter.swapCursor(data);
    }

    public void dump_BIGdata_toLog(String data) {
        // String cursorToString = dumpCursorToString(data);
        if (data.length() > 4000) {
            Log.v("Sergio", "data.length = " + data.length());
            int chunkCount = data.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= data.length()) {
                    Log.v("Sergio", "chunk " + i + " of " + chunkCount + ":" + data.substring(4000 * i));
                } else {
                    Log.v("Sergio", "chunk " + i + " of " + chunkCount + ":" + data.substring(4000 * i, max));
                }
            }
        } else {
            Log.v("Sergio", data);
        }
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
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // quando convertView != null recicla as views anteriores já existentes
            // mas misturava as imagens do imageSwitcher. As views não visíveis eram recriadas
            // com imageSwitcher das views que desapareciam multiplicando timers e imagens misturando tudo,
            // mas fica mais lento
            // TODO: melhorar performance
            View view;
            if (convertView != null) {
                //view = newView(mContext, mCursor, null);
                view = newView(null, null, null);
                bindView(view, mContext, (Cursor) getItem(position));
            } else {
                view = super.getView(position, convertView, parent);
            }
            expandOrCollapse(view, mCursor);

            return view;
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View item_root = cursorItemInflater.inflate(R.layout.watching_item_layout2, null, false);
            ViewHolder viewHolder = new ViewHolder(item_root);
            item_root.setTag(R.id.viewholder, viewHolder);
            return item_root;
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            String prod_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
            String min_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
            String max_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE));
            String current_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE));
            String options_sabor = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1));
            String options_caixa = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2));
            String options_quant = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3));
            String string_array_images = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES));
            long minPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE));
            long maxPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE));
            long actualPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE));
            long previousPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE));
            double actual_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE));
            double min_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE));
            double max_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE));
            double previous_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE));


            if (options_sabor == null) options_sabor = "";
            if (options_caixa == null) options_caixa = "";
            if (options_quant == null) options_quant = "";
            if (max_price == null || max_price.equals("0")) max_price = "-";
            if (min_price == null || min_price.equals("0")) min_price = "-";
            if (current_price == null || current_price.equals("0")) current_price = "-";

            ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.viewholder);

            Boolean gotPictures = null;
            if (string_array_images != null && CACHE_IMAGES) {
                gotPictures = extractImagesFromJSON_Cache(viewHolder, string_array_images);
                if (!gotPictures) {
                    gotPictures = extractImagesFromJSON_URL(viewHolder, string_array_images);
                }
            } else if (string_array_images != null && !CACHE_IMAGES) {
                gotPictures = extractImagesFromJSON_URL(viewHolder, string_array_images);
            }

            //Se não encontrou imagens nenhumas colocar imagem "no image available"
            if (!gotPictures && viewHolder.imageSwitcher.getChildCount() < 2) {
                viewHolder.imageSwitcher.addView(getNewImageView(70));
                ImageView iv = (ImageView) viewHolder.imageSwitcher.getChildAt(0);
                Glide.with(mActivity).load(R.drawable.noimage).asBitmap().format(PREFER_ARGB_8888).asIs().dontTransform().into(iv);
            }

            viewHolder.titleView.setText(prod_name + " " + options_sabor + " " + options_caixa + " " + options_quant);
            viewHolder.highestPriceView.setText(max_price);
            viewHolder.lowestPriceView.setText(min_price);
            viewHolder.currentPriceView.setText(current_price);
            viewHolder.highestPriceDate.setText(getMillisecondsToDate(maxPriceDate));
            viewHolder.lowestPriceDate.setText(getMillisecondsToDate(minPriceDate));
            ((TextView) mActivity.findViewById(R.id.updated_tv)).setText(getMillisecondsToDate(actualPriceDate));

            if (actual_price_value < previous_price_value && previous_price_value != 0d) {
                double diff = actual_price_value - previous_price_value;
                String str_diff = diff < 0 ? diff + "" : "";
                viewHolder.currentInfo.setVisibility(View.VISIBLE);
                viewHolder.currentInfo.setText(str_diff);
                viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));
            }
            if (actual_price_value > previous_price_value && previous_price_value != 0d) {
                double diff = actual_price_value - previous_price_value;
                String str_diff = diff > 0 ? diff + "" : "";
                viewHolder.currentInfo.setVisibility(View.VISIBLE);
                viewHolder.currentInfo.setText(str_diff);
                viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
            }
            if (actual_price_value >= max_price_value && min_price_value != max_price_value) {
                previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                double diff = actual_price_value - previous_price_value;
                String str_diff = diff > 0d ? "" + diff : "";
                viewHolder.currentInfo.setVisibility(View.VISIBLE);
                viewHolder.currentInfo.setText("Highest price! " + str_diff);
                viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
            }
            if (actual_price_value <= min_price_value && min_price_value != max_price_value) {
                previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                double diff = min_price_value - actual_price_value;
                String str_diff = diff != 0d ? "" + diff : "";
                viewHolder.currentInfo.setVisibility(View.VISIBLE);
                viewHolder.currentInfo.setText("Best price! " + str_diff);
                viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));
                viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ll_green_bg));
            }
            Log.i("Sergio>", this + " bindView\n" +
                    "previous_price_value= " + previous_price_value + "\n" +
                    "actual_price_value= " + actual_price_value + "\n" +
                    "min_price_value= " + min_price_value + "\n" +
                    "max_price_value= " + max_price_value);
        }   // End bindView

        public void expandOrCollapse(View view, Cursor cursor) {
            if (isExpandedArray == null || isExpandedArray.length != cursor.getCount()) {
                isExpandedArray = new Boolean[cursor.getCount()];
                for (int i = 0; i < cursor.getCount(); i++) {
                    isExpandedArray[i] = false;
                }
            }
            if (isExpandedArray[cursor.getPosition()]) {
                expandIt(view.findViewById(R.id.under_cardview));
                expandIt(view.findViewById(R.id.under_cardview));
            }
        }

        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            Log.w("Sergio>", this + "onContentChanged");
        }

        private Boolean extractImagesFromJSON_URL(final ViewHolder viewHolder, String string_array_images) {
            ArrayList<String> arrayListImageURLs = new ArrayList<>();
            Boolean gotPictures = false;
            Boolean gotPicturesURL_List = false;
            JSONArray jsonArray_imgs = null;
            if (string_array_images != null) {
                try {
                    jsonArray_imgs = new JSONArray(string_array_images);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonArray_imgs != null) {
                if (jsonArray_imgs.length() > 0) {
                    for (int i = 0; i < jsonArray_imgs.length(); i++) {
                        String img_url_ToUse = null;
                        JSONArray json_array_i = jsonArray_imgs.optJSONArray(i);
                        for (int j = 0; j < json_array_i.length(); j++) {
                            try {
                                String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                                String img_url = ((String) ((JSONObject) json_array_i.get(j)).get("url")).replace("\\", "");
                                if (img_url != null) {
                                    for (int k = 0; k < imageSizesToUse.length; k++) {
                                        if (size.equals(imageSizesToUse[k])) {
                                            img_url_ToUse = img_url;
                                            gotPictures = true;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (gotPictures) {
                            arrayListImageURLs.add(img_url_ToUse);
                        }
                    }
                    if (gotPictures && arrayListImageURLs.size() > 0) {
                        gotPicturesURL_List = true;
                        //placeImagesFromURL(viewHolder, arrayListImageURLs);
                        placeimageURLs(viewHolder, arrayListImageURLs);

                    }
                }
            }
            return gotPicturesURL_List;
        }

        @NonNull
        private Boolean extractImagesFromJSON_Cache(ViewHolder viewHolder, String string_array_images) {
            ArrayList<File> arrayListImageFiles = new ArrayList<>();
            Boolean gotPictures = false;
            Boolean gotPicturesFileList = false;
            JSONArray jsonArray_imgs = null;
            if (string_array_images != null) {
                try {
                    jsonArray_imgs = new JSONArray(string_array_images);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonArray_imgs != null) {
                if (jsonArray_imgs.length() > 0) {
                    for (int i = 0; i < jsonArray_imgs.length(); i++) {
                        String file_uri_ToUse = null;
                        JSONArray json_array_i = jsonArray_imgs.optJSONArray(i);
                        for (int j = 0; j < json_array_i.length(); j++) {
                            try {
                                JSONObject obj_ij = (JSONObject) json_array_i.get(j);
                                if (obj_ij.has("file")) {
                                    String size = (String) obj_ij.get("size");
                                    String file_uri = ((String) obj_ij.get("file"));
                                    if (file_uri != null) {
                                        for (int k = 0; k < imageSizesToUse.length; k++) {
                                            if (size.equals(imageSizesToUse[k])) {
                                                file_uri_ToUse = file_uri;
                                                gotPictures = true;
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (gotPictures) {
                            File img_File = new File(mActivity.getFilesDir(), file_uri_ToUse);
                            if (img_File.exists()) {
                                arrayListImageFiles.add(img_File);
                            }
                        }
                    }
                    if (gotPictures && arrayListImageFiles.size() > 0) {
                        gotPicturesFileList = true;
                        placeImagesFromFile(viewHolder, arrayListImageFiles);
                    }
                }
            }
            return gotPicturesFileList;
        }

        private void placeImagesFromFile(final ViewHolder viewHolder, final ArrayList<File> arrayListImageFiles) {
            final int size = arrayListImageFiles.size();

            viewHolder.imageSwitcher.removeAllViews();
            viewHolder.imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                public View makeView() {
                    return getNewImageView(70);
                }
            });
            // Declare in and out animations and load them using AnimationUtils class
            Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
            fadeIn.setDuration(1200);
            Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
            fadeOut.setDuration(1200);
            // set the animation type to ImageSwitcher
            viewHolder.imageSwitcher.setInAnimation(fadeIn);
            viewHolder.imageSwitcher.setOutAnimation(fadeOut);

            final int[] currentIndex = {0};
            final Runnable runnable = new Runnable() {
                public void run() {
                    currentIndex[0]++;
                    if (currentIndex[0] >= size) currentIndex[0] = 0;
                    viewHolder.imageSwitcher.setImageURI(Uri.fromFile(arrayListImageFiles.get(currentIndex[0])));
                }
            };
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(runnable);
                }
            };

            Timer timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 0, 5400);

            viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.runOnUiThread(runnable);
                }
            });

        }

        private void placeimageURLs(final ViewHolder viewHolder, final ArrayList<String> arrayListImageURLs) {
            viewHolder.imageSwitcher.removeAllViews();
            viewHolder.imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                public View makeView() {
                    return getNewImageView(70);
                }
            });
            // Declare in and out animations and load them using AnimationUtils class
            final Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
            fadeIn.setDuration(1200);
            Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
            fadeOut.setDuration(1200);
            // set the animation type to ImageSwitcher
            viewHolder.imageSwitcher.setInAnimation(fadeIn);
            viewHolder.imageSwitcher.setOutAnimation(fadeOut);

            final int size = arrayListImageURLs.size();
            //Set the schedule function and rate
            final int[] currentIndex = {0};
            final Runnable runnable = new Runnable() {
                public void run() {
                    currentIndex[0]++;
                    if (currentIndex[0] >= size) currentIndex[0] = 0;
                    Glide.with(mActivity)
                            .load(arrayListImageURLs.get(currentIndex[0]))
                            .asBitmap()
                            .asIs()
                            .format(PREFER_ARGB_8888)
                            .dontTransform()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    viewHolder.imageSwitcher.setImageDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    viewHolder.imageSwitcher.setImageResource(R.drawable.noimage);
                                }
                            });
                }
            };
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(runnable);
                }
            };
            //Called every 5400 milliseconds
            timer.scheduleAtFixedRate(timerTask, 0, 5400);

            viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.runOnUiThread(runnable);
                }
            });

        }

        private void placeImagesFromURL(final ViewHolder viewHolder, final ArrayList<String> arrayListImageURLs) {
            final int size = arrayListImageURLs.size();
            final ArrayList<Bitmap> arrayListImageBitmap = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                final int final_i = i;
                Glide.with(mActivity)
                        .load(arrayListImageURLs.get(i))
                        .asBitmap()
                        .asIs()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .skipMemoryCache(true)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                arrayListImageBitmap.add(bitmap);
                                if (final_i == size - 1) {
                                    bitmapsReady(viewHolder, arrayListImageBitmap);
                                }
                            }
                        });
            }
        }

        private void bitmapsReady(final ViewHolder viewHolder, final ArrayList<Bitmap> arrayListImageBitmap) {
            viewHolder.imageSwitcher.removeAllViews();
            viewHolder.imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                public View makeView() {
                    return getNewImageView(70);
                }
            });
            // Declare in and out animations and load them using AnimationUtils class
            Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
            fadeIn.setDuration(1200);
            Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
            fadeOut.setDuration(1200);
            // set the animation type to ImageSwitcher
            viewHolder.imageSwitcher.setInAnimation(fadeIn);
            viewHolder.imageSwitcher.setOutAnimation(fadeOut);

            final int size = arrayListImageBitmap.size();
            //Set the schedule function and rate
            final int[] currentIndex = {0};
            final Runnable runnable = new Runnable() {
                public void run() {
                    currentIndex[0]++;
                    if (currentIndex[0] >= size) currentIndex[0] = 0;
                    viewHolder.imageSwitcher.setImageDrawable(
                            new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                }
            };
            TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(runnable);
                }
            };
            //Called every 5400 milliseconds
            timer.scheduleAtFixedRate(timerTask, 0, 5400);

            viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.runOnUiThread(runnable);
                }
            });
        }

        @NonNull
        private View getNewImageView(int pixels_widthHeight) {
            ImageView imageView = new ImageView(mActivity);
            imageView.setPadding(0, 2, 2, 0);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setAdjustViewBounds(true);
            int widthHeight = (int) (pixels_widthHeight * scale + 0.5f);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(widthHeight, widthHeight));
            return imageView;
        }

    }

    public String getMillisecondsToDate(long milliseconds) {
        long timeDif = System.currentTimeMillis() - milliseconds;

        if (timeDif < 60_000) { // há menos de 60 segundos atrás
            return "Agora";
        } else if (timeDif >= 60_000 && timeDif <= 3_600_000) { // uma hora atrás
            return TimeUnit.MILLISECONDS.toMinutes(timeDif) + " Minutos atrás";

        } else if (timeDif > 3_600_000 && timeDif < 7_200_000) { // Dentro de 1h - 2hr
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " Hora atrás";

        } else if (timeDif >= 7_200_000 && timeDif <= 86_400_000) { // Dentro do dia de hoje até 24h atrás
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " Horas atrás";

        } else if (timeDif > 86_400_000 && timeDif <= 172_800_000) { // Ontem 24 a 48h
            DateFormat df = getTimeInstance(SHORT);
            Date resultDate = new Date(milliseconds);
            return "Ontem " + df.format(resultDate);

        } else {
            String pattern;
            if (timeDif < TimeUnit.DAYS.toMillis(365L)) {
                pattern = "dd MMM kk:mm";
            } else {
                pattern = "dd MMM yy kk:mm";
            }
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern); // dia Mês 14:55 p.ex.
            DateFormat dateFormat = getDateTimeInstance(SHORT, SHORT);
            Date resultdate = new Date(milliseconds);
            return sdf.format(resultdate);
        }

    }

    private void saveImagesWithGlide(String imageURL, final String filename) {
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


}
