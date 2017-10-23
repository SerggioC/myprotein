package com.cruz.sergio.myproteinpricechecker;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.Alarm;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.util.DisplayMetrics.DENSITY_HIGH;
import static android.util.DisplayMetrics.DENSITY_LOW;
import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static android.util.DisplayMetrics.DENSITY_XXHIGH;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.MAX_NOTIFY_VALUE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.UPDATE_ONSTART;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.density;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.R.id.main_cardview;
import static com.cruz.sergio.myproteinpricechecker.R.id.notify;
import static com.cruz.sergio.myproteinpricechecker.helper.Alarm.updatePricesOnReceive;
import static com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice.LAST_DB_UPDATE_PREF_KEY;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.showCustomSlimToast;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.showCustomToast;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.ALL_PRODUCT_COLUMNS_PROJECTION;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;
import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class WatchingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int LOADER_ID = 0;
    public static final int IMAGE_PERIOD = 5400; // (ms)
    public static DeletedProductListener delete_listener;
    static Loader<Cursor> loaderManager;
    static String[] imageSizesToUse;
    cursorDBAdapter cursorDBAdapter;
    Activity mActivity;
    SwipeRefreshLayout watchingSwipeRefreshLayout;
    Timer timer = new Timer();
    Boolean[] isExpandedArray = null;
    Boolean addedNewProduct = false;
    Boolean[] showPercent = null;
    SharedPreferences defaultSharedPreferences;

    public WatchingFragment() {
        //required empty constructor?
    }

    public static void expandIt(final View view, Boolean isLastItem) {
        view.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = view.getMeasuredHeight();
        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = 1;
        view.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
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
        animation.setDuration((int) (targetHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);

        if (isLastItem) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation arg0) {
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    ListView listViewItems = view.getRootView().findViewById(R.id.watching_listview);
                    listViewItems.smoothScrollToPosition(listViewItems.getMaxScrollAmount());
                }
            });
        }
    }

    public static void collapseIt(final View view) {
        final int initialHeight = view.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        animation.setDuration((int) (initialHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }

    public void setDeleteProductlistener(DeletedProductListener listener) {
        delete_listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mActivity = getActivity();
        super.onCreate(savedInstanceState);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

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

        Alarm alarm_jobservice = new Alarm();
        alarm_jobservice.setUpdateCompleteListener(new Alarm.UpdateCompleteListener() {
            @Override
            public void onUpdateReady(Boolean isReady, Boolean isSingleLine) {
                Log.w("Sergio>", this + "\n" + "onUpdateReady= " + isReady);
                if (WatchingFragment.this.isAdded()) {
                    if (watchingSwipeRefreshLayout != null && !isSingleLine) {
                        watchingSwipeRefreshLayout.setRefreshing(false);
                    }
                    if (isReady || isSingleLine) {
                        redrawListView();
                    }
                }
            }
        });

        SearchFragment sf = new SearchFragment();
        sf.setNewProductListener(new SearchFragment.AddedNewProductListener() {
            @Override
            public void onProductAdded(Boolean addedNew) {
                Log.w("Sergio>", this + "\n" + "addedNewProduct= " + addedNew);
                TabLayout tabLayout = getActivity().findViewById(R.id.tabs);
                TabLayout.Tab tab = tabLayout.getTabAt(MainActivity.TABS.WATCHING);
                tabLayout.setScrollPosition(MainActivity.TABS.WATCHING, 0f, true);
                if (tab != null) tab.select();
                redrawListView();
                addedNewProduct = addedNew;
            }
        });

        MainActivity.notifySettingsChanged = new MainActivity.ChangedNotifySettings() {
            @Override
            public void onNotifySettingsChanged(Boolean hasChanged) {
                if (hasChanged) {
                    redrawListView();
                }
            }
        };

    }

    @Override
    public void onStart() {
        super.onStart();
        if (UPDATE_ONSTART) {
            //FirebaseJobservice.updatePricesOnStart(mActivity, false, false, null);
            updatePricesOnReceive(getActivity(), false, false, null);
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
        ListView listViewItems = rootview.findViewById(R.id.watching_listview);
        listViewItems.addHeaderView(View.inflate(mActivity, R.layout.watch_list_header_view, null));
        listViewItems.setAdapter(cursorDBAdapter);

        watchingSwipeRefreshLayout = rootview.findViewById(R.id.watching_swiperefresh);

        watchingSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        timer.cancel();
                        timer.purge();
                        timer = new Timer();
                        //updatePricesOnStart(mActivity, false, false, null);
                        updatePricesOnReceive(mActivity, false, false, null);
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
        Log.i("Sergio>>>", this + " onCreateLoader:  id= " + id + "\n" +
                "CONTENT_DIR_TYPE= " + CONTENT_DIR_TYPE + "\n" +
                "CONTENT_ITEM_TYPE= " + CONTENT_ITEM_TYPE + "\n" +
                "uri= " + uri);

        //String selection = "WHERE '" + ProductsContract.ProductsEntry.TABLE_NAME + "' = 'qualquercoisa'";
        return new CursorLoader(
                mActivity,
                uri,
                ALL_PRODUCT_COLUMNS_PROJECTION,
                null,
                null,
                ProductsContract.ProductsEntry._ID + " ASC ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        dump_BIGdata_toLog(dumpCursorToString(data));
        if (data.getCount() == 0) {
            showCustomToast(mActivity, "Empty DataBase.\n" +
                            "Add products to track their prices.",
                    R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
            if (watchingSwipeRefreshLayout != null) {
                watchingSwipeRefreshLayout.setRefreshing(false);
            }
        }
        cursorDBAdapter.swapCursor(data);
        if (addedNewProduct) {
            ListView listViewItems = mActivity.findViewById(R.id.watching_listview);
            listViewItems.smoothScrollToPosition(listViewItems.getMaxScrollAmount());
            addedNewProduct = false;
        }
    }

    public void dump_BIGdata_toLog(String data) {
        // String cursorToString = dumpCursorToString((Cursor) data);
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

    private void animateRemoving(CardView mainCardView, CardView under_view, final String prod_name) {

        AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mainCardView, "alpha", 1f, 0f);
        ObjectAnimator transAnim = ObjectAnimator.ofFloat(mainCardView, "translationX", mainCardView.getWidth());
        ObjectAnimator alphaAnim2 = ObjectAnimator.ofFloat(under_view, "alpha", 1f, 0f);
        ObjectAnimator transAnim2 = ObjectAnimator.ofFloat(under_view, "translationX", under_view.getWidth());
        animSet.playTogether(transAnim, alphaAnim, alphaAnim2, transAnim2);
        animSet.setDuration(250);
        animSet.start();
        animSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
//                if (has_deleted) {
//                    showCustomToast(mActivity, prod_name + " " + "deleted from database.",
//                            R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);
//                } else {
//                    showCustomToast(mActivity, "Database updated.\nSome image files could not deleted.",
//                            R.mipmap.ic_warning, R.color.f_color4, Toast.LENGTH_LONG);
//                }

                showCustomToast(mActivity, prod_name + " " + "deleted from database.",
                        R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);

                // Redraw listView
                redrawListView();

                // Reload/Redraw the graph
                delete_listener.onProductDeleted(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

//        mainCardView.animate().translationX(mainCardView.getWidth()).alpha(0).setDuration(200).setListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                // Redraw listView
//                timer.cancel();
//                timer.purge();
//                timer = new Timer();
//                getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//            }
//        });
    }

    private boolean deleteImageFiles(String string_array_images) {
        boolean hasDeleted = false;
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
                    JSONArray json_array_i = jsonArray_imgs.optJSONArray(i);
                    for (int j = 0; j < json_array_i.length(); j++) {
                        try {
                            JSONObject obj_ij = (JSONObject) json_array_i.get(j);
                            if (obj_ij.has("file")) {
                                String file_uri = ((String) obj_ij.get("file"));
                                if (file_uri != null) {
                                    File fdelete = new File(mActivity.getFilesDir(), file_uri);
                                    if (fdelete.exists()) {
                                        hasDeleted = fdelete.delete();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return hasDeleted;
    }

    public String getMillisecondsToDate(long milliseconds) {
        long timeDif = System.currentTimeMillis() - milliseconds;

        if (timeDif < 60_000) { // há menos de 60 segundos atrás
            return "Now";
        } else if (timeDif >= 60_000 && timeDif <= 3_600_000) { // uma hora atrás
            return TimeUnit.MILLISECONDS.toMinutes(timeDif) + " " + "Minutes ago";

        } else if (timeDif > 3_600_000 && timeDif < 7_200_000) { // Dentro de 1h - 2hr
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " " + "Hour ago";

        } else if (timeDif >= 7_200_000 && timeDif <= 86_400_000) { // Dentro do dia de hoje até 24h atrás
            return TimeUnit.MILLISECONDS.toHours(timeDif) + " " + "Hours ago";

        } else if (timeDif > 86_400_000 && timeDif <= 172_800_000) { // Ontem 24 a 48h
            DateFormat df = getTimeInstance(SHORT);
            Date resultDate = new Date(milliseconds);
            return "Yesterday" + " " + df.format(resultDate);

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
                                    outputStream = mActivity.openFileOutput(filename, MODE_PRIVATE);
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

    public void redrawListView() {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
    }

    interface DeletedProductListener {
        void onProductDeleted(Boolean deleted);
    }

    static class ViewHolder {
        final TextView titleView; // ou Product Name
        final TextView highestPriceView;
        final TextView lowestPriceView;
        final TextView currentPriceView;
        final TextView highestPriceDate;
        final TextView lowestPriceDate;
        final TextView info_top;
        final TextView currentInfo;
        final TextView undercard_tv_desc;
        final ImageSwitcher imageSwitcher;
        final LinearLayout ll_current_price;
        final ImageView up_down_icon;
        final CardView main_cardView;
        final CardView under_view;
        final TextView undercard_last_updated;
        final ProgressBar small_pb_undercard;
        final TextView product_brand;
        final ImageView notify_icon;
        final TextView notify_info;
        final LinearLayout llPriceInfo;


        public ViewHolder(View view) {
            titleView = view.findViewById(R.id.item_title_textview);
            highestPriceView = view.findViewById(R.id.item_highest_price_textview);
            lowestPriceView = view.findViewById(R.id.item_lowest_price_textview);
            currentPriceView = view.findViewById(R.id.item_current_price_textview);
            highestPriceDate = view.findViewById(R.id.item_highest_price_date);
            lowestPriceDate = view.findViewById(R.id.item_lowest_price_date);
            info_top = view.findViewById(R.id.info_top);
            currentInfo = view.findViewById(R.id.current_info);
            ll_current_price = view.findViewById(R.id.ll_current_price);
            imageSwitcher = view.findViewById(R.id.image_switcher);
            up_down_icon = view.findViewById(R.id.up_down_arrow);
            under_view = view.findViewById(R.id.under_cardview);
            main_cardView = view.findViewById(main_cardview);
            undercard_tv_desc = view.findViewById(R.id.description_undercard);
            undercard_last_updated = view.findViewById(R.id.last_updated_undercard);
            small_pb_undercard = view.findViewById(R.id.pbar_undercard);
            product_brand = view.findViewById(R.id.product_brand);
            notify_icon = view.findViewById(notify);
            notify_info = view.findViewById(R.id.notifications_info);
            llPriceInfo = view.findViewById(R.id.ll_price_info);
        }
    }

    public class cursorDBAdapter extends CursorAdapter {
        private LayoutInflater cursorItemInflater;

        public cursorDBAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            cursorItemInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // quando convertView != null recicla as views anteriores já existentes
            // mas misturava as imagens do imageSwitcher. As views não visíveis eram recriadas
            // com imageSwitcher das views que desapareciam multiplicando timers e imagens misturando tudo,
            // mas fica mais lento
            // TODO: melhorar performance
            View view;
            if (convertView != null) {
                //view = newView(mContext, mCursor, null);
                view = newView(null, null, null);
                view.setTag(R.id.view_position, position);
                bindView(view, mContext, (Cursor) getItem(position));
            } else {
                view = super.getView(position, convertView, parent);
            }

            View under_view = view.findViewById(R.id.under_cardview);
            expandOrCollapse(under_view, mCursor);

            if (showPercent == null || showPercent.length != mCursor.getCount()) {
                showPercent = new Boolean[mCursor.getCount()];
                for (int i = 0; i < mCursor.getCount(); i++) {
                    showPercent[i] = true;
                }
            }

            view.setTag(R.id.view_position, position);

            return view;
        }

        // The newView method is used to inflate a new view and return it,
        // you don't bind any data to the view at this point.
        @Override
        public View newView(Context context, final Cursor cursor, ViewGroup parent) {
            final View item_root = cursorItemInflater.inflate(R.layout.watching_item_layout, null, false);
            ViewHolder viewHolder = new ViewHolder(item_root);
            item_root.setTag(R.id.viewholder, viewHolder);
            if (cursor != null) {
                item_root.setTag(R.id.view_position, cursor.getPosition());
            }
            return item_root;
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(final View view, Context context, final Cursor cursor) {
            final ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.viewholder);

            final int this_position = view.getTag(R.id.view_position) == null ? -1 : (int) view.getTag(R.id.view_position);
            final int this_product_id = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
            final String prod_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
            final String url = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
            final String string_array_images = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES));
            final String productBrand = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BRAND));
            final Boolean[] show_notifications = {cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS)) == 1};
            final double notify_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE));
            String current_price_string = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE));
            final String currencySymbol = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL));

            final boolean symb_before = current_price_string.indexOf(currencySymbol) == 0;

            final CardView mainCardView = view.findViewById(R.id.main_cardview);
            mainCardView.findViewById(R.id.open_web).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browser);
                }
            });
            mainCardView.findViewById(R.id.add_to_cart).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCustomSlimToast(mActivity, "Add product to virtual cart", Toast.LENGTH_SHORT);
                }
            });

            final Boolean globalNotifications = defaultSharedPreferences.getBoolean("notifications_global_key", true);

            if (globalNotifications) {
                if (show_notifications[0]) {
                    viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications);
                    viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_notifications_15dp), null, null, null);
                    String notifyText = "Notify when price drops.";
                    if (notify_value > 0) {
                        notifyText = "Notify when price reaches " + (symb_before ? (currencySymbol + notify_value) : (notify_value + currencySymbol));
                    }
                    viewHolder.notify_info.append(notifyText);
                } else {
                    viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_none);
                    viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_notifications_none_15dp), null, null, null);
                    viewHolder.notify_info.append("Notifications disabled.");
                }
            } else {
                SpannableStringBuilder ssb1 = new SpannableStringBuilder("Notifications disabled.");
                if (notify_value > 0) {
                    String notifyText2 = "Notify when price reaches " + (symb_before ? (currencySymbol + notify_value) : (notify_value + currencySymbol));
                    ssb1 = new SpannableStringBuilder(notifyText2);
                    ssb1.setSpan(new StrikethroughSpan(), 0, notifyText2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                viewHolder.notify_info.append(ssb1);
                if (show_notifications[0]) {
                    viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_off);
                    viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_notifications_off_15dp), null, null, null);
                } else {
                    viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_none);
                    viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_notifications_none_15dp), null, null, null);

                }
            }

            final android.support.v7.widget.SwitchCompat[] alertSwitch = new android.support.v7.widget.SwitchCompat[1];
            final RadioGroup[] radioGroup = new RadioGroup[1];
            final android.support.v7.widget.AppCompatRadioButton[] radio_every = new AppCompatRadioButton[1];
            final android.support.v7.widget.AppCompatRadioButton[] radio_target = new AppCompatRadioButton[1];
            final android.support.design.widget.TextInputEditText[] alertTextView = new android.support.design.widget.TextInputEditText[1];

            viewHolder.notify_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View notifyIconView) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

                    LayoutInflater inflater = mActivity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.notifications_alert_dialog, null);
                    alertDialogBuilder.setView(dialogView);
                    alertSwitch[0] = dialogView.findViewById(R.id.switch_notify);
                    alertTextView[0] = dialogView.findViewById(R.id.tv_alert_value);
                    radio_every[0] = dialogView.findViewById(R.id.radioButton_every);
                    radio_target[0] = dialogView.findViewById(R.id.radioButton_target);
                    radioGroup[0] = dialogView.findViewById(R.id.radioGroup_notify);
                    final TextView textView1 = dialogView.findViewById(R.id.tv_alert1);

                    alertSwitch[0].setChecked(show_notifications[0]);
                    radioGroup[0].setEnabled(show_notifications[0]);

                    radio_every[0].setEnabled(show_notifications[0]);
                    radio_every[0].setChecked(notify_value == 0 ? true : false);

                    radio_target[0].setEnabled(show_notifications[0]);
                    radio_target[0].setChecked(show_notifications[0] && notify_value > 0 ? true : false);

                    textView1.setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
                    textView1.setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());

                    alertTextView[0].setEnabled(show_notifications[0] && notify_value > 0);
                    alertTextView[0].setActivated(show_notifications[0] && notify_value > 0);
                    alertTextView[0].setText(show_notifications[0] && notify_value > 0 ? String.valueOf(notify_value) : "");

                    alertSwitch[0].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            radioGroup[0].setEnabled(alertSwitch[0].isChecked());
                            radio_every[0].setEnabled(alertSwitch[0].isChecked());
                            radio_target[0].setEnabled(alertSwitch[0].isChecked());
                            alertTextView[0].setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
                            alertTextView[0].setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());
                            alertTextView[0].setText(alertSwitch[0].isChecked() && radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
                            textView1.setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
                            textView1.setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());
                        }
                    });

                    radio_target[0].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertTextView[0].setEnabled(radio_target[0].isChecked());
                            alertTextView[0].setActivated(radio_target[0].isChecked());
                            alertTextView[0].setText(radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
                            textView1.setEnabled(radio_target[0].isChecked());
                            textView1.setActivated(radio_target[0].isChecked());
                        }
                    });

                    radio_every[0].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertTextView[0].setEnabled(radio_target[0].isChecked());
                            alertTextView[0].setActivated(radio_target[0].isChecked());
                            alertTextView[0].setText(radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
                            textView1.setEnabled(radio_target[0].isChecked());
                            textView1.setActivated(radio_target[0].isChecked());
                        }
                    });

                    alertDialogBuilder.setTitle("Notifications");
                    alertDialogBuilder.setIcon(R.mipmap.ic_notification_bell);
                    alertDialogBuilder
                            .setMessage("Edit notification settings for this item.")
                            .setCancelable(true)
                            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    DBHelper dbHelper = new DBHelper(mActivity);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                                    double target_val;
                                    String target_val_str = alertTextView[0].getText().toString();
                                    if (StringUtil.isBlank(target_val_str)) {
                                        target_val = 0;
                                    } else {
                                        try {
                                            target_val = Double.parseDouble(target_val_str);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                            target_val = MAX_NOTIFY_VALUE;
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                            target_val = 0;
                                        }
                                    }

                                    ContentValues contentValues = new ContentValues(2);
                                    contentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE, target_val);
                                    contentValues.put(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS, alertSwitch[0].isChecked() ? 1 : 0);

                                    int update_result = db.update(ProductsContract.ProductsEntry.TABLE_NAME, contentValues, "_ID=" + "'" + this_product_id + "'", null);

                                    if (update_result == 1) {
                                        redrawListView();
                                        showCustomToast(mActivity,
                                                (alertSwitch[0].isChecked() && radio_target[0].isChecked() ?
                                                        "Alert when price reaches " +
                                                                (symb_before ? (currencySymbol + String.valueOf(target_val)) : (String.valueOf(target_val) + currencySymbol)) :
                                                        (alertSwitch[0].isChecked() && radio_every[0].isChecked()) ? "Alert every time price drops." : "Notifications disabled."),
                                                alertSwitch[0].isChecked() ? R.mipmap.ic_ok2 : R.mipmap.ic_warning,
                                                alertSwitch[0].isChecked() ? R.color.f_color2 : R.color.orange,
                                                Toast.LENGTH_LONG);

                                    } else {
                                        showCustomToast(mActivity, "Error updating notifications!",
                                                R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                                    }


                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });


            mainCardView.findViewById(R.id.expand_underview_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCursor != null) {
                        final CardView under_view = view.findViewById(R.id.under_cardview);

                        under_view.findViewById(R.id.update_this_entry).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //UPDATE THIS ITEM ONLY
                                //updatePricesOnStart(mActivity, false, true, String.valueOf(this_product_id));
                                updatePricesOnReceive(mActivity, false, true, String.valueOf(this_product_id));
                                v.setVisibility(View.GONE);
                                viewHolder.small_pb_undercard.setVisibility(View.VISIBLE);
                            }
                        });

                        under_view.findViewById(R.id.delete_entry).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
                                alertDialogBuilder.setTitle("Delete Product?");
                                alertDialogBuilder.setIcon(R.mipmap.ic_error);
                                alertDialogBuilder
                                        .setMessage("Are you sure you want to delete this entry?\nAll logged prices for this product will be lost!")
                                        .setCancelable(true)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                DBHelper dbHelper = new DBHelper(mActivity);
                                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                                int delete_db_entries_result = db.delete(ProductsContract.ProductsEntry.TABLE_NAME, "_ID=" + "'" + this_product_id + "'", null);
                                                deleteImageFiles(string_array_images);

                                                if (delete_db_entries_result == 1) {
                                                    animateRemoving(mainCardView, under_view, prod_name);
                                                } else {
                                                    showCustomToast(mActivity, "Error deleting " + prod_name + " from DataBase!",
                                                            R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                                                }

                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        });

                        Boolean isExpanded = isExpandedArray[this_position];
                        if (isExpanded) {
                            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_expand_more), null, null, null);
                            ((TextView) v).setText("Details");
                            collapseIt(under_view);
                            isExpandedArray[this_position] = false;
                        } else {
                            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_expand_less_black_24dp), null, null, null);
                            ((TextView) v).setText("Close");
                            if (this_position == mCursor.getCount() - 1) {
                                expandIt(under_view, true);
                            } else {
                                expandIt(under_view, false);
                            }
                            isExpandedArray[this_position] = true;
                        }
                    }
                }
            });


            String options_sabor = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1));
            String options_caixa = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2));
            String options_quant = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3));

            double actual_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE));
            long actualPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE));

            String min_price_string = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE));
            long minPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE));
            double min_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE));

            String max_price_string = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE));
            long maxPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE));
            double max_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE));

            long previousPriceDate = cursor.getLong(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE));
            double previous_price_value = cursor.getDouble(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE));


            if (options_sabor == null) options_sabor = "";
            if (options_caixa == null) options_caixa = "";
            if (options_quant == null) options_quant = "";
            if (StringUtil.isBlank(current_price_string)) current_price_string = "N/A";

            String sub_title = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE));
            String webstore_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME));
            String webstore_str = "Webstore";

            SpannableStringBuilder pptList_SSB = new SpannableStringBuilder(webstore_str);
            pptList_SSB.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pptList_SSB.length() - webstore_str.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            pptList_SSB.append(": " + webstore_name + "\n");

            pptList_SSB.append(sub_title);
            pptList_SSB.setSpan(new RelativeSizeSpan(1.1f), pptList_SSB.length() - sub_title.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

            String prod_description = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION));

            if (!StringUtil.isBlank(prod_description)) {

                String prod_benefits = "\n" + "Product benefits" + "\n";
                pptList_SSB.append(prod_benefits);
                pptList_SSB.setSpan(new RelativeSizeSpan(1.1f), pptList_SSB.length() - prod_benefits.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                pptList_SSB.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pptList_SSB.length() - prod_benefits.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

                String[] prod_description_array = prod_description.split("\n");
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tick, null);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                String drawableStr = drawable.toString();
                int pdal = prod_description_array.length - 1;
                for (int i = 0; i < pdal; i++) {
                    pptList_SSB.append(drawableStr);
                    pptList_SSB.setSpan(new ImageSpan(drawable), pptList_SSB.length() - drawableStr.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    pptList_SSB.append(" " + prod_description_array[i] + "\n");
                }
                pptList_SSB.append(drawableStr);
                pptList_SSB.setSpan(new ImageSpan(drawable), pptList_SSB.length() - drawableStr.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                pptList_SSB.append(" " + prod_description_array[pdal]);

            }
            viewHolder.undercard_tv_desc.setText(pptList_SSB);
            SpannableStringBuilder ssbp = new SpannableStringBuilder(getMillisecondsToDate(actualPriceDate));
            ssbp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mActivity, R.color.dark_green)), 0, ssbp.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.undercard_last_updated.append(ssbp);

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

            String str_title = prod_name + " " + options_sabor + " " + options_caixa + " " + options_quant;
            viewHolder.titleView.setText(str_title);
            viewHolder.product_brand.setText(productBrand);
            viewHolder.highestPriceView.setText(max_price_string);
            viewHolder.lowestPriceView.setText(min_price_string);
            viewHolder.currentPriceView.setText(current_price_string);
            viewHolder.highestPriceDate.setText(getMillisecondsToDate(maxPriceDate));
            viewHolder.lowestPriceDate.setText(getMillisecondsToDate(minPriceDate));

            if (this_position == 0) {
                if (getCount() == 1) {
                    ((TextView) mActivity.findViewById(R.id.updated_tv)).setText(getMillisecondsToDate(actualPriceDate));
                } else {
                    SharedPreferences sharedPref = mActivity.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
                    long last_saved_date = sharedPref.getLong(LAST_DB_UPDATE_PREF_KEY, 0);
                    ((TextView) mActivity.findViewById(R.id.updated_tv)).setText(last_saved_date == 0 ? "Never" : getMillisecondsToDate(last_saved_date));
                }

                ImageView global_notifications_imageView = mActivity.findViewById(R.id.ic_global_notifications);
                if (globalNotifications) {
                    global_notifications_imageView.setImageResource(R.drawable.ic_notifications);
                } else {
                    global_notifications_imageView.setImageResource(R.drawable.ic_notifications_off_none);
                }

                global_notifications_imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);

                        LayoutInflater inflater = mActivity.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.notifications_alert_dialog_global, null);
                        alertDialogBuilder.setView(dialogView);
                        final SwitchCompat switchCompat = dialogView.findViewById(R.id.switch_notify_global);
                        switchCompat.setChecked(globalNotifications);
                        alertDialogBuilder.setTitle("Global Notifications");
                        alertDialogBuilder.setIcon(R.mipmap.ic_notification_bell);
                        alertDialogBuilder
                                .setCancelable(true)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        boolean isChecked = switchCompat.isChecked();
                                        if (globalNotifications != isChecked) {
                                            defaultSharedPreferences.edit().putBoolean("notifications_global_key", isChecked).commit();
                                            showCustomToast(mActivity, "Global Notifications are now " + (isChecked ? "Active!" : "Disabled!"),
                                                    isChecked ? R.mipmap.ic_ok2 : R.mipmap.ic_warning,
                                                    isChecked ? R.color.f_color2 : R.color.orange, Toast.LENGTH_LONG);
                                            redrawListView();
                                        }


                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });

            }

            // pode dar erro ao atualizar o preço, ou o produto/opção não estar disponível
            // guardo o preço = 0 nesta situação
            if (actual_price_value != 0) {

                Boolean hasPercent = false;
                String absDiffStr = "";
                String percentStr = "";

                // Descida de preço
                if (actual_price_value < previous_price_value && previous_price_value != 0d) {
                    double diff = previous_price_value - actual_price_value;
                    if (diff > 0d) {
                        absDiffStr = "-" + getAbsDiffString(currencySymbol, symb_before, diff);
                        percentStr = "-" + getPercentString(previous_price_value, diff);
                        hasPercent = true;
                    }

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(showPercent[this_position] ? percentStr : absDiffStr);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));

                    if (globalNotifications && show_notifications[0] && notify_value == 0 ||
                            globalNotifications && show_notifications[0] && notify_value > 0 && actual_price_value <= notify_value) {
                        viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_active);
                    }
                }

                // Subida de preço
                if (actual_price_value > previous_price_value && previous_price_value != 0d) {
                    double diff = actual_price_value - previous_price_value;
                    if (diff > 0d) {
                        absDiffStr = "+" + getAbsDiffString(currencySymbol, symb_before, diff);
                        percentStr = "+" + getPercentString(previous_price_value, diff);
                        hasPercent = true;
                    }
                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(showPercent[this_position] ? percentStr : absDiffStr);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
                }

                // Preço mais alto
                if (actual_price_value >= max_price_value && min_price_value != max_price_value) {
                    previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                    double diff = actual_price_value - previous_price_value;
                    if (diff > 0d) {
                        absDiffStr = "+" + getAbsDiffString(currencySymbol, symb_before, diff);
                        percentStr = "+" + getPercentString(previous_price_value, diff);
                        hasPercent = true;
                    }

                    viewHolder.info_top.setVisibility(View.VISIBLE);
                    viewHolder.info_top.setText("Highest price!");
                    viewHolder.info_top.setTextColor(ContextCompat.getColor(mActivity, R.color.red));

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(showPercent[this_position] ? percentStr : absDiffStr);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
                    viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ll_red_bg));
                }

                // Preço mais baixo
                if (actual_price_value <= min_price_value && min_price_value != max_price_value) {
                    previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                    double diff = previous_price_value - min_price_value;
                    if (diff > 0d) {
                        absDiffStr = "-" + getAbsDiffString(currencySymbol, symb_before, diff);
                        percentStr = "-" + getPercentString(previous_price_value, diff);
                        hasPercent = true;
                    }

                    viewHolder.info_top.setVisibility(View.VISIBLE);
                    viewHolder.info_top.setText("Best price!");
                    viewHolder.info_top.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(showPercent[this_position] ? percentStr : absDiffStr);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));
                    viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ll_green_bg));
                }


                if (hasPercent) {
                    final String finalPercentStr = percentStr;
                    final String finalAbsoluteDiff = absDiffStr;
                    viewHolder.llPriceInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (showPercent[this_position]) {
                                viewHolder.currentInfo.setText(finalAbsoluteDiff);
                                showPercent[this_position] = false;
                            } else {
                                viewHolder.currentInfo.setText(finalPercentStr);
                                showPercent[this_position] = true;
                            }
                        }
                    });
                }


            } else {

                SpannableStringBuilder ssb_title = new SpannableStringBuilder(str_title + " (Not available)");
                ssb_title.setSpan(new ForegroundColorSpan(Color.RED), str_title.length(), ssb_title.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb_title.setSpan(new RelativeSizeSpan(0.9f), str_title.length(), ssb_title.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
                viewHolder.titleView.setText(ssb_title);

//                viewHolder.info_top.setVisibility(View.VISIBLE);
//                viewHolder.info_top.setText("Not available");
//                viewHolder.info_top.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
            }
        }   // End bindView

        String getAbsDiffString(String currencySymbol, boolean symb_before, double diff) {
            return symb_before ? currencySymbol + round(diff) : round(diff) + currencySymbol;
        }

        @NonNull
        String getPercentString(double previous_price_value, double diff) {
            DecimalFormat percentFormater = new DecimalFormat("##.#%");
            return percentFormater.format(diff / previous_price_value);
        }

        public String round(double value) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            return bd.toString();
        }

        public void expandOrCollapse(View view, Cursor cursor) {
            if (isExpandedArray == null || isExpandedArray.length != cursor.getCount()) {
                isExpandedArray = new Boolean[cursor.getCount()];
                for (int i = 0; i < cursor.getCount(); i++) {
                    isExpandedArray[i] = false;
                }
            }
            if (isExpandedArray[cursor.getPosition()]) {
                expandIt(view, false);
                TextView textView = ((LinearLayout) view.getParent()).findViewById(R.id.expand_underview_tv);
                textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mActivity, R.drawable.ic_expand_less_black_24dp), null, null, null);
                textView.setText("Close");
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
                    if (currentIndex[0] >= size) currentIndex[0] = 0;
                    viewHolder.imageSwitcher.setImageURI(Uri.fromFile(arrayListImageFiles.get(currentIndex[0])));
                    currentIndex[0]++;
                }
            };

            TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(runnable);
                }
            };
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(timerTask, 0, IMAGE_PERIOD);

//            // Iniciar primeira apresentação da imagem
//            mActivity.runOnUiThread(runnable);
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
                    if (currentIndex[0] >= size) currentIndex[0] = 0;
                    if (WatchingFragment.this.isVisible()) {
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
                    currentIndex[0]++;
                }
            };
//            mActivity.runOnUiThread(runnable);

            TimerTask timerTask = new TimerTask() {
                public void run() {
                    mActivity.runOnUiThread(runnable);
                }
            };

            //Called every 5400 milliseconds
            timer.scheduleAtFixedRate(timerTask, 0, IMAGE_PERIOD);

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


}
