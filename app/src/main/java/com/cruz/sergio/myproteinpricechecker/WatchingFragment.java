package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
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
    public static float scale;
    public static int density;
    static String[] imageSizesToUse;
    ArrayList bitmapArray;
    Timer timer = new Timer();
    TimerTask[] array_timerTask = null;
    String uri_url;

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

        if (CACHE_IMAGES) {
            uri_url = "uri";
        } else {
            uri_url = "url";
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

//        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI.buildUpon()
//                .appendPath(ProductsContract.ProductsEntry.TABLE_NAME)
//                .build();

        Uri uri = ProductsContract.ProductsEntry.CONTENT_URI;
        Log.i("Sergio>>>", this + " onCreateLoader:  id= " + id + "\nCONTENT_DIR_TYPE= " + CONTENT_DIR_TYPE + " \nCONTENT_ITEM_TYPE= " + CONTENT_ITEM_TYPE + "\nuri=" + uri);

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
        private ArrayList<ArrayList<String>> arrayArray_imgURI;

        public cursorDBAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            cursorItemInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            // quando convertView != null recicla as views anteriores já existentes
            // mas misturava as imagens do imageSwitcher
            // as views não visíveis eram recriadas com imageSwitcher das views que desapareciam
            // multiplicando timers e imagens misturando tudo
            // mas fica mais lento
            // TODO: melhorar performance
            if (convertView != null) {
                //view = newView(mContext, mCursor, null);
                view = newView(null, null, null);
                bindView(view, mContext, (Cursor) getItem(position));
            }

            return view;

        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View item_root = cursorItemInflater.inflate(R.layout.watching_item_layout, null, false);

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
            Log.d("Sergio>", this + "\nbindView:\nstring_array_images=\n" + string_array_images);

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
            viewHolder.currentPriceDate.setText(getMillisecondsToDate(actualPriceDate));
        }   // End bindView

        private Boolean extractImagesFromJSON_URL(ViewHolder viewHolder, String string_array_images) {
            ArrayList<String> arrayListImageURLs = new ArrayList<>();
            Boolean gotPictures = false;
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
                                String file_uri = (String) ((JSONObject) json_array_i.get(j)).get("uri");
                                if (file_uri != null) {
                                    for (int k = 0; k < imageSizesToUse.length; k++) {
                                        if (size.equals(imageSizesToUse[k])) {
                                            img_url_ToUse = file_uri;
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
                        placeImagesFromURL(viewHolder, arrayListImageURLs);
                    }
                }
            }
            return gotPictures;
        }

        @NonNull
        private Boolean extractImagesFromJSON_Cache(ViewHolder viewHolder, String string_array_images) {
            ArrayList<File> arrayListImageFiles = new ArrayList<>();
            Boolean gotPictures = false;
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
                                String size = (String) ((JSONObject) json_array_i.get(j)).get("size");
                                String file_uri = ((String) ((JSONObject) json_array_i.get(j)).get("uri")).replace("\\", "");
                                if (file_uri != null) {
                                    for (int k = 0; k < imageSizesToUse.length; k++) {
                                        if (size.equals(imageSizesToUse[k])) {
                                            file_uri_ToUse = file_uri;
                                            gotPictures = true;
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
                        placeImagesFromFile(viewHolder, arrayListImageFiles);
                    }
                }
            }
            return gotPictures;
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
                            }
                        });
            }
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

        if (timeDif <= 3_600_000) { // uma hora atrás
            return TimeUnit.MILLISECONDS.toMinutes(timeDif) + " Minutos atrás";

        } else if (timeDif > 3_600_000 && timeDif <= 86_400_000) { // Dentro do dia de hoje até 24h atrás
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " Horas atrás";

        } else if (timeDif > 86_400_000 && timeDif <= 172_800_000) { // Ontem 24 a 48h
            DateFormat df = getTimeInstance(SHORT);
            Date resultDate = new Date(milliseconds);
            return "Ontem " + df.format(resultDate);

        } else {
            DateFormat dateFormat = getDateTimeInstance(SHORT, SHORT);
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
