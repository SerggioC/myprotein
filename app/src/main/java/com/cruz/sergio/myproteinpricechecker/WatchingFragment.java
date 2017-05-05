package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

import static android.database.DatabaseUtils.dumpCursorToString;
import static android.util.DisplayMetrics.DENSITY_HIGH;
import static android.util.DisplayMetrics.DENSITY_LOW;
import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static android.util.DisplayMetrics.DENSITY_XXHIGH;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.ALL_PRODUCT_COLUMNS_PROJECTION;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;
import static java.text.DateFormat.DEFAULT;
import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateTimeInstance;

public class WatchingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    Activity mActivity;
    SwipeRefreshLayout watchingSwipeRefreshLayout;
    static cursorDBAdapter cursorDBAdapter;
    ListView listViewItems;
    static Loader<Cursor> loaderManager;
    public static float scale;
    public int density;
    static String[] imageSizesToLoad;
    ArrayList bitmapArray;
    Timer timer = new Timer();

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView titleView; // ou Product Name
        public final TextView highestPriceView;
        public final TextView lowestPriceView;
        public final TextView currentPriceView;
        public final TextView highestPriceDate;
        public final TextView lowestPriceDate;
        public final TextView currentPriceDate;
        public final ImageSwitcher imageSwitcher;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.item_icon);
            titleView = (TextView) view.findViewById(R.id.item_title_textview);
            highestPriceView = (TextView) view.findViewById(R.id.item_highest_price_textview);
            lowestPriceView = (TextView) view.findViewById(R.id.item_lowest_price_textview);
            currentPriceView = (TextView) view.findViewById(R.id.item_current_price_textview);
            highestPriceDate = (TextView) view.findViewById(R.id.item_highest_price_date);
            lowestPriceDate = (TextView) view.findViewById(R.id.item_lowest_price_date);
            currentPriceDate = (TextView) view.findViewById(R.id.item_current_price_date);
            imageSwitcher = (ImageSwitcher) view.findViewById(R.id.image_switcher);
        }
    }

    public WatchingFragment() {
        //required empty constructor?
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        super.onCreate(savedInstanceState);
        scale = getResources().getDisplayMetrics().density;
        density = getResources().getDisplayMetrics().densityDpi;

        if (density <= DENSITY_LOW) {
            imageSizesToLoad = new String[]{"small", "smallthumb", "thumbnail"};
        } else if (density > DENSITY_LOW && density <= DENSITY_MEDIUM) {
            imageSizesToLoad = new String[]{"smallthumb", "thumbnail"};
        } else if (density > DENSITY_MEDIUM && density <= DENSITY_HIGH) {
            imageSizesToLoad = new String[]{"thumbnail", "smallprod"};
        } else if (density > DENSITY_HIGH && density <= DENSITY_XHIGH) {
            imageSizesToLoad = new String[]{"smallprod", "product", "large"};
        } else if (density > DENSITY_XHIGH && density <= DENSITY_XXHIGH) { //galaxy S5: 480dpi scale = 3x; (70x70)*3 = 210x210;
            imageSizesToLoad = new String[]{"large", "list", "raw"};
        } else {
            imageSizesToLoad = new String[]{"raw", "largeproduct", "quickview"};
        }

        Log.d("Sergio>", this + "onCreate:\ndensity=\n" + density);
        for (int i = 0; i < imageSizesToLoad.length; i++) {
            Log.d("Sergio>", this + "onCreate:\nimageSizesToLoad=\n" + imageSizesToLoad[i]);
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
        Log.i("Sergio>>>", this + " onCreateLoader: " + "\nCONTENT_DIR_TYPE= " + CONTENT_DIR_TYPE + " \nCONTENT_ITEM_TYPE= " + CONTENT_ITEM_TYPE);
        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI;
        Log.d("Sergio>>>", this + " onCreateLoader: " + "\nuri=" + uri);

        //String selection = "WHERE '" + ProductsContract.ProductsEntry.TABLE_NAME + "' = 'qualquercoisa'";
        CursorLoader cursor_loader = new CursorLoader(
                mActivity,
                uri,
                ALL_PRODUCT_COLUMNS_PROJECTION,
                null,
                null,
                ProductsContract.ProductsEntry._ID + " ASC "
        );
        return cursor_loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String cursorToString = dumpCursorToString(data);
        if (cursorToString.length() > 4000) {
            Log.v("Sergio", "cursorToString.length = " + cursorToString.length());
            int chunkCount = cursorToString.length() / 4000;     // integer division
            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                if (max >= cursorToString.length()) {
                    Log.v("Sergio", "chunk " + i + " of " + chunkCount + ":" + cursorToString.substring(4000 * i));
                } else {
                    Log.v("Sergio", "chunk " + i + " of " + chunkCount + ":" + cursorToString.substring(4000 * i, max));
                }
            }
        } else {
            Log.v("Sergio", cursorToString);
        }
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
            //Log.i("Sergio>", this + " bindView: cursor=\n" + dumpCursorToString(cursor));
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.imageSwitcher.removeAllViews();

            String prod_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
            String min_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
            String max_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE));
            String current_price = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE));
            String options_sabor = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1));
            String options_caixa = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2));
            String options_quant = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3));
            String string_array_base_img_uris = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAY_BASE_IMG_URIS));
            String string_array_base_image_URLs = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAY_MP_BASE_IMG_URLS));
            String string_array_img_uris = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGE_URIS));
            String string_array_image_URLs = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMG_URLS));

            long minPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE));
            long maxPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE));
            long actualPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE));

            if (options_sabor == null) options_sabor = "";
            if (options_caixa == null) options_caixa = "";
            if (options_quant == null) options_quant = "";
            if (max_price == null || max_price.equals("0")) max_price = "-";
            if (min_price == null || min_price.equals("0")) min_price = "-";
            if (current_price == null || current_price.equals("0")) current_price = "-";


            Boolean gotPictures = false;
            if (string_array_base_img_uris != null) {
                gotPictures = extractImagesFromBase(viewHolder, string_array_base_img_uris, string_array_base_image_URLs);
            } else if (string_array_img_uris != null) {
                gotPictures = extractImagesFromJSON(viewHolder, string_array_img_uris, string_array_image_URLs);
            }

            if (!gotPictures) { //Se não encontrou imagens nenhumas colocar imagem com no image available
                viewHolder.imageSwitcher.addView(getNewImageView(70));
                ImageView iv = (ImageView) viewHolder.imageSwitcher.getChildAt(0);
                Glide.with(mActivity).load(R.drawable.noimage).into(iv);
            }

            viewHolder.titleView.setText(prod_name + " " + options_sabor + " " + options_caixa + " " + options_quant);
            viewHolder.highestPriceView.setText(max_price);
            viewHolder.lowestPriceView.setText(min_price);
            viewHolder.currentPriceView.setText(current_price);

            viewHolder.highestPriceDate.setText(getMillisecondsToDate(maxPriceDate));
            viewHolder.lowestPriceDate.setText(getMillisecondsToDate(minPriceDate));
            viewHolder.currentPriceDate.setText(getMillisecondsToDate(actualPriceDate));
            //End bindView
        }

        private Boolean extractImagesFromBase(ViewHolder viewHolder, String string_array_base_img_uris, String string_array_base_image_urLs) {
            String[] baseImageSizes = new String[]{"S300", "S480", "S600"};
            ArrayList<File> arrayListImageFiles = new ArrayList<>();
            Boolean gotPictures = false;
            JSONArray jsonArray_img_uris = null;
            if (string_array_base_img_uris != null) {
                try {
                    jsonArray_img_uris = new JSONArray(string_array_base_img_uris);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonArray_img_uris != null) {
                if (jsonArray_img_uris.length() > 0) {
                    String fileNameToLoad = null;
                    for (int i = 0; i < jsonArray_img_uris.length(); i++) {
                        try {
                            String filename = (String) jsonArray_img_uris.get(i);
                            if (filename != null) {
                                for (int k = 0; k < baseImageSizes.length; k++) {
                                    if (filename.contains("_" + baseImageSizes[k] + "_")) {
                                        fileNameToLoad = filename;
                                        gotPictures = true;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (gotPictures) {
                        File img_File = new File(mActivity.getFilesDir(), fileNameToLoad);
                        if (img_File.exists()) {
                            arrayListImageFiles.add(img_File);
                            placeImagesFromFile(viewHolder, arrayListImageFiles);
                        }
                    }

                    Log.i("Sergio>", this + "bindView:\narrayListImageFiles From Base=\n" + arrayListImageFiles);
                }
            }
            // Se as imagem não estiverem guardadas no /data/ folder da app no dispositivo (memória interna)
            // fazer o download a partir da lista de URLs e guardá-las
            if (arrayListImageFiles.size() == 0) {
                if (string_array_base_image_urLs != null) {
                    gotPictures = false;
                    ArrayList<String> arrayListImageURLs = new ArrayList<>();
                    JSONArray jsonArray_image_URLs = null;
                    try {
                        jsonArray_image_URLs = new JSONArray(string_array_base_image_urLs);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonArray_image_URLs != null) {
                        if (jsonArray_image_URLs.length() > 0) {

                            String urlToLoad = null;
                            for (int j = 0; j < jsonArray_image_URLs.length(); j++) {
                                try {
                                    String url = (String) jsonArray_image_URLs.get(j);
                                    if (url != null) {
                                        for (int k = 0; k < baseImageSizes.length; k++) {
                                            if (url.contains("_" + baseImageSizes[k] + "_")) {
                                                urlToLoad = url;
                                                gotPictures = true;
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    gotPictures = false;
                                }
                            }
                            if (gotPictures) {
                                arrayListImageURLs.add(urlToLoad);
                                placeImagesFromURL(viewHolder, arrayListImageURLs);

                                //TODO create image file and save it
                                //saveImagesWithGlide(fileNameToLoad);
                            }
                        }
                    }
                }
            }
            return gotPictures;
        }

        @NonNull
        public Boolean extractImagesFromJSON(ViewHolder viewHolder, String string_array_img_uris, String string_array_image_URLs) {
            ArrayList<File> arrayListImageFiles = new ArrayList<>();
            Boolean gotPictures = false;
            JSONArray jsonArray_img_uris = null;
            if (string_array_img_uris != null) {
                try {
                    jsonArray_img_uris = new JSONArray(string_array_img_uris);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (jsonArray_img_uris != null) {
                if (jsonArray_img_uris.length() > 0) {
                    for (int i = 0; i < jsonArray_img_uris.length(); i++) {
                        JSONArray json_array_i = jsonArray_img_uris.optJSONArray(i);
                        String fileNameToLoad = null;
                        for (int j = 0; j < json_array_i.length(); j++) {
                            try {
                                String filename = (String) json_array_i.get(j);
                                if (filename != null) {
                                    for (int k = 0; k < imageSizesToLoad.length; k++) {
                                        if (filename.contains("_" + imageSizesToLoad[k] + "_")) {
                                            fileNameToLoad = filename;
                                            gotPictures = true;
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (gotPictures) {
                            File img_File = new File(mActivity.getFilesDir(), fileNameToLoad);
                            if (img_File.exists()) {
                                arrayListImageFiles.add(img_File);
                            }
                        }
                    }
                    Log.i("Sergio>", this + "bindView:\narrayListImageFiles=\n" + arrayListImageFiles);

                    if (gotPictures && arrayListImageFiles.size() > 0) {
                        placeImagesFromFile(viewHolder, arrayListImageFiles);
                    }
                }
            }
            // Se as imagem não estiverem guardadas no /data/ folder da app no dispositivo (memória interna)
            // fazer o download a partir da lista de URLs
            if (arrayListImageFiles.size() == 0) {
                if (string_array_image_URLs != null) {
                    gotPictures = false;
                    ArrayList<String> arrayListImageURLs = new ArrayList<>();
                    JSONArray jsonArray_image_URLs = null;
                    try {
                        jsonArray_image_URLs = new JSONArray(string_array_image_URLs);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonArray_image_URLs != null) {
                        if (jsonArray_image_URLs.length() > 0) {
                            for (int i = 0; i < jsonArray_image_URLs.length(); i++) {
                                JSONArray json_array_i = jsonArray_image_URLs.optJSONArray(i);
                                String urlToLoad = null;
                                for (int j = 0; j < json_array_i.length(); j++) {
                                    try {
                                        String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                                        String url = (String) ((JSONObject) json_array_i.get(j)).get("url");
                                        if (url != null) {
                                            for (int k = 0; k < imageSizesToLoad.length; k++) {
                                                if (size.equals(imageSizesToLoad[k])) {
                                                    urlToLoad = url;
                                                    gotPictures = true;
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        gotPictures = false;
                                    }
                                }
                                if (gotPictures) {
                                    arrayListImageURLs.add(urlToLoad);
                                }
                            }
                            if (gotPictures) {
                                placeImagesFromURL(viewHolder, arrayListImageURLs);

                                //TODO create image file and save it
                                //saveImagesWithGlide(fileNameToLoad);
                            }
                        }
                    }
                }
            }
            return gotPictures;
        }

        private void placeImagesFromURL(final ViewHolder viewHolder, final ArrayList<String> arrayListImageURLs) {
            final int size = arrayListImageURLs.size();
            final ArrayList<Bitmap> arrayListImageBitmap = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                final int finalI = i;
                Glide.with(mActivity)
                        .load(arrayListImageURLs.get(i))
                        .asBitmap()
                        .asIs()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(final Bitmap resource, GlideAnimation glideAnimation) {
                                arrayListImageBitmap.add(resource);
                                if (finalI == size - 1) {
                                    bitmapsReady(viewHolder, size, arrayListImageBitmap);
                                }
                            }
                        });
            }
        }

        public void bitmapsReady(final ViewHolder viewHolder, final int size, final ArrayList<Bitmap> arrayListImageBitmap) {
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
            //Set the schedule function and rate
            final int[] currentIndex = {0};
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    //Called every 5000 milliseconds
                    currentIndex[0]++;
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (currentIndex[0] >= size) currentIndex[0] = 0;
                            viewHolder.imageSwitcher.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                        }
                    });
                }
            }, 0, 5000);

            viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentIndex[0]++;
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (currentIndex[0] >= size) currentIndex[0] = 0;
                            viewHolder.imageSwitcher.setImageDrawable(new BitmapDrawable(mActivity.getResources(), arrayListImageBitmap.get(currentIndex[0])));
                        }
                    });
                }
            });
        }

        private void placeImagesFromFile(final ViewHolder viewHolder, final ArrayList<File> arrayListImageFiles) {
            final int size = arrayListImageFiles.size();

            viewHolder.imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                public View makeView() {
                    return getNewImageView(70);
                }
            });
            // Declare in and out animations and load them using AnimationUtils class
            Animation fadeIn = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_in);
            fadeIn.setDuration(1000);
            Animation fadeOut = AnimationUtils.loadAnimation(mActivity, android.R.anim.fade_out);
            fadeOut.setDuration(1000);

            // set the animation type to ImageSwitcher
            viewHolder.imageSwitcher.setInAnimation(fadeIn);
            viewHolder.imageSwitcher.setOutAnimation(fadeOut);

            //Set the schedule function and rate
            final int[] currentIndex = {0};
            final TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (currentIndex[0] >= size) currentIndex[0] = 0;
                            viewHolder.imageSwitcher.setImageURI(Uri.fromFile(arrayListImageFiles.get(currentIndex[0])));
                        }
                    });
                    currentIndex[0]++;
                }
            };
            //Called every 5000 milliseconds with 0 delay
            timer.scheduleAtFixedRate(timerTask, 0, 5000);

            viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentIndex[0]++;
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (currentIndex[0] >= size) currentIndex[0] = 0;
                            viewHolder.imageSwitcher.setImageURI(Uri.fromFile(arrayListImageFiles.get(currentIndex[0])));
                        }
                    });
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

        private void placeImagesWithTransition(ImageView imageview) {
            // create the transition layers
            Drawable[] layers = new Drawable[bitmapArray.size()];
            for (int i = 0; i < bitmapArray.size(); i++) {
                layers[i] = (Drawable) bitmapArray.get(i);
            }
//            layers[0] = new BitmapDrawable(getResources(), firstBitmap);
//            layers[1] = new BitmapDrawable(getResources(), secondBitmap);

            TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
            imageview.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(1000);
        }

        void saveToBitmapArray(String filename) {
            File file = new File(mActivity.getFilesDir(), filename); // Pass getFilesDir() and "filename" to read file
            Glide.with(mActivity).load(file.getPath()).into(new SimpleTarget<GlideDrawable>() {
                @Override
                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                    bitmapArray.add(resource);
                }
            });
        }

        void placeImage(ViewHolder viewHolder, int id, String filename) {
            File file = new File(mActivity.getFilesDir(), filename); // Pass getFilesDir() and "filename" to read file

            ImageView imageView = new ImageView(mActivity);
            imageView.setId(id);
            imageView.setPadding(0, 2, 2, 0);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setAdjustViewBounds(true);
            int widthHeight = (int) (70 * scale + 0.5f);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(widthHeight, widthHeight));
            Glide.with(mActivity).load(file.getPath()).into(imageView);
            viewHolder.imageSwitcher.addView(imageView);
        }
    }

    public String getMillisecondsToDate(long milliseconds) {
        long timeDif = System.currentTimeMillis() - milliseconds;

        if (timeDif <= 3_600_000) { // uma hora atrás
            return TimeUnit.MILLISECONDS.toMinutes(timeDif) + " minutos";
        } else if (timeDif > 3_600_000 && timeDif <= 86_400_000) { // Dentro do dia de hoje até 24h
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " horas";
        } else if (timeDif > 86_400_000 && timeDif < 172_800_000) { // Ontem 24 a 48h
            return "Ontem";
        } else {
            DateFormat dateFormat = getDateTimeInstance(MEDIUM, DEFAULT);
            Date resultdate = new Date(milliseconds);
            return dateFormat.format(resultdate);
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

    public void getWatchingProducts() {
/*        DBHelper dbHelper = new DBHelper(mActivity);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Extract full products_table Table from database (SELECT *)
        Cursor all_products_cursor = db.rawQuery("SELECT * FROM " + ProductsContract.ProductsEntry.TABLE_NAME, null);*/
        //Log.i("Sergio>>>", this + " getWatchingProducts: all_products_cursor\n" + dumpCursorToString(all_products_cursor));

        //if (all_products_cursor.getCount() > 0) {
        if (true) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            DetailsFragment.showCustomToast(mActivity, "Empty DataBase...", R.mipmap.ic_info, R.color.colorPrimaryAlpha, Toast.LENGTH_SHORT);
        }
        //db.close();
        //all_products_cursor.close();
        watchingSwipeRefreshLayout.setRefreshing(false);
    }
}
