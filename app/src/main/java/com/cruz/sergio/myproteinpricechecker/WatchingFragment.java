package com.cruz.sergio.myproteinpricechecker;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v7.widget.CardView;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
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
import static com.cruz.sergio.myproteinpricechecker.R.id.main_cardview;
import static com.cruz.sergio.myproteinpricechecker.TabFragment.tabLayout;
import static com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice.updatePricesOnStart;
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
    public static final int IMAGE_PERIOD = 5400;
    Activity mActivity;
    SwipeRefreshLayout watchingSwipeRefreshLayout;
    static cursorDBAdapter cursorDBAdapter;
    static ListView listViewItems;
    static Loader<Cursor> loaderManager;
    static String[] imageSizesToUse;
    Timer timer = new Timer();
    Boolean[] isExpandedArray = null;
    Boolean addedNewProduct = false;

    public static class ViewHolder {
        public final TextView titleView; // ou Product Name
        public final TextView highestPriceView;
        public final TextView lowestPriceView;
        public final TextView currentPriceView;
        public final TextView highestPriceDate;
        public final TextView lowestPriceDate;
        public final TextView info_top;
        public final TextView currentInfo;
        public final TextView undercard_tv_desc;
        public final ImageSwitcher imageSwitcher;
        public final LinearLayout ll_current_price;
        public final ImageView up_down_icon;
        public final CardView main_cardView;
        public final CardView under_view;

        public ViewHolder(View view) {
            titleView = (TextView) view.findViewById(R.id.item_title_textview);
            highestPriceView = (TextView) view.findViewById(R.id.item_highest_price_textview);
            lowestPriceView = (TextView) view.findViewById(R.id.item_lowest_price_textview);
            currentPriceView = (TextView) view.findViewById(R.id.item_current_price_textview);
            highestPriceDate = (TextView) view.findViewById(R.id.item_highest_price_date);
            lowestPriceDate = (TextView) view.findViewById(R.id.item_lowest_price_date);
            info_top = (TextView) view.findViewById(R.id.info_top);
            currentInfo = (TextView) view.findViewById(R.id.current_info);
            ll_current_price = (LinearLayout) view.findViewById(R.id.ll_current_price);
            imageSwitcher = (ImageSwitcher) view.findViewById(R.id.image_switcher);
            up_down_icon = (ImageView) view.findViewById(R.id.up_down_arrow);
            under_view = (CardView) view.findViewById(R.id.under_cardview);
            main_cardView = (CardView) view.findViewById(main_cardview);
            undercard_tv_desc = (TextView) view.findViewById(R.id.description_undercard);
        }
    }

    public WatchingFragment() {
        //required empty constructor?
    }

    public static DeletedProductListener delete_listener;

    interface DeletedProductListener {
        void onProductDeleted(Boolean deleted);
    }

    public void setDeleteProductlistener(DeletedProductListener listener) {
        this.delete_listener = listener;
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

        SearchFragment sf = new SearchFragment();
        sf.setNewProductListener(new SearchFragment.AddedNewProductListener() {
            @Override
            public void onProductAdded(Boolean addedNew) {
                Log.w("Sergio>", this + "\n" + "addedNewProduct= " + addedNew);
                timer.cancel();
                timer.purge();
                timer = new Timer();
                TabLayout.Tab tab = tabLayout.getTabAt(MainActivity.TAB_IDS.WATCHING);
                tabLayout.setScrollPosition(MainActivity.TAB_IDS.WATCHING, 0f, true);
                tab.select();
                getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
                addedNewProduct = addedNew;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (UPDATE_ONSTART) {
            FirebaseJobservice.updatePricesOnStart(mActivity, false);
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
        listViewItems.addHeaderView(View.inflate(mActivity, R.layout.watch_list_header_view, null));
        listViewItems.setAdapter(cursorDBAdapter);

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
            showCustomToast(mActivity, "Empty DataBase.\n" +
                            "Add products to track their prices.",
                    R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
            if (watchingSwipeRefreshLayout != null) {
                watchingSwipeRefreshLayout.setRefreshing(false);
            }
        }
        cursorDBAdapter.swapCursor(data);
        if (addedNewProduct) {
            listViewItems.smoothScrollToPosition(listViewItems.getMaxScrollAmount());
            addedNewProduct = false;
        }
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
            ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.viewholder);

            final int this_position = view.getTag(R.id.view_position) == null ? -1 : (int) view.getTag(R.id.view_position);
            final int this_product_id = cursor.getInt(cursor.getColumnIndex(ProductsContract.ProductsEntry._ID));
            final String prod_name = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
            final String url = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
            final String string_array_images = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES));

            final CardView mainCardView = (CardView) view.findViewById(R.id.main_cardview);

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

            mainCardView.findViewById(R.id.notify).setTag(false);
            mainCardView.findViewById(R.id.notify).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((Boolean) v.getTag()) {
                        ((ImageView) v).setImageResource(R.drawable.ic_notifications_none);
                        v.setTag(false);
                    } else {
                        ((ImageView) v).setImageResource(R.drawable.ic_notifications);
                        v.setTag(true);
                    }
                }
            });

            mainCardView.findViewById(R.id.expand_underview_tv).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCursor != null) {
                        final CardView under_view = (CardView) view.findViewById(R.id.under_cardview);

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


//                                final Dialog dialogBuilder = new Dialog(mActivity);
//
//                                dialogBuilder.setContentView(R.layout.alert_dialog_layout);
//                                ((ImageView) dialogBuilder.findViewById(R.id.dialog_icon))
//                                        .setImageResource(R.mipmap.ic_error);
//                                ((TextView) dialogBuilder.findViewById(R.id.dialog_title))
//                                        .setText("Delete Product?");
//                                ((TextView) dialogBuilder.findViewById(R.id.dialog_message))
//                                        .setText("Are you sure you want to delete this entry?\n" +
//                                                "All logged prices for this product will be lost!");
//                                (dialogBuilder.findViewById(R.id.dialog_cancel)).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        dialogBuilder.dismiss();
//                                    }
//                                });
//                                (dialogBuilder.findViewById(R.id.dialog_ok)).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        DBHelper dbHelper = new DBHelper(mActivity);
//                                        SQLiteDatabase db = dbHelper.getWritableDatabase();
//                                        int delete_result = db.delete(ProductsContract.ProductsEntry.TABLE_NAME, "_ID=" + "'" + this_product_id + "'", null);
//
//                                        NetworkUtils.showCustomSlimToast(mActivity, "Delete entry position" + this_position + "\n" +
//                                                "DB product _ID = " + this_product_id + "\n" +
//                                                "delete_result= " + delete_result, Toast.LENGTH_LONG);
//                                    }
//                                });
//
//                                dialogBuilder.show();

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

            String current_price_string = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE));
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

            String currency_symb = cursor.getString(cursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL));
            boolean symb_after = currency_symb.indexOf(" ") == 0;

            if (options_sabor == null) options_sabor = "";
            if (options_caixa == null) options_caixa = "";
            if (options_quant == null) options_quant = "";
            if (current_price_string == null || current_price_string.equals("")) current_price_string = "N/A";

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
            viewHolder.highestPriceView.setText(max_price_string);
            viewHolder.lowestPriceView.setText(min_price_string);
            viewHolder.currentPriceView.setText(current_price_string);
            viewHolder.highestPriceDate.setText(getMillisecondsToDate(maxPriceDate));
            viewHolder.lowestPriceDate.setText(getMillisecondsToDate(minPriceDate));
            ((TextView) mActivity.findViewById(R.id.updated_tv)).setText(getMillisecondsToDate(actualPriceDate));

            // pode dar erro ao atualizar o preço, ou o produto/opção não estar disponível
            // guardo o preço = 0 nesta situação
            if (actual_price_value != 0) {

                // Decida de preço
                if (actual_price_value < previous_price_value && previous_price_value != 0d) {
                    double diff = previous_price_value - actual_price_value;
                    String str_diff = "";
                    if (diff > 0d && symb_after) {
                        str_diff = "-" + round(diff) + currency_symb;
                    } else if (diff > 0d && !symb_after) {
                        str_diff = "-" + currency_symb + round(diff);
                    }

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(str_diff);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));
                }

                // Subida de preço
                if (actual_price_value > previous_price_value && previous_price_value != 0d) {
                    double diff = actual_price_value - previous_price_value;
                    String str_diff = "";
                    if (diff > 0d && symb_after) {
                        str_diff = "+" + round(diff) + currency_symb;
                    } else if (diff > 0d && !symb_after) {
                        str_diff = "+" + currency_symb + round(diff);
                    }

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(str_diff);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
                }

                // Preço mais alto
                if (actual_price_value >= max_price_value && min_price_value != max_price_value) {
                    previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                    double diff = actual_price_value - previous_price_value;
                    String str_diff = "";
                    if (diff > 0d && symb_after) {
                        str_diff = "+" + round(diff) + currency_symb;
                    } else if (diff > 0d && !symb_after) {
                        str_diff = "+" + currency_symb + round(diff);
                    }

                    viewHolder.info_top.setVisibility(View.VISIBLE);
                    viewHolder.info_top.setText("Highest price!");
                    viewHolder.info_top.setTextColor(ContextCompat.getColor(mActivity, R.color.red));

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(str_diff);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.red));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.up_arrow));
                    viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ll_red_bg));
                }

                // Preço mais baixo
                if (actual_price_value <= min_price_value && min_price_value != max_price_value) {
                    previous_price_value = previous_price_value == 0d ? actual_price_value : previous_price_value;
                    double diff = previous_price_value - min_price_value;
                    String str_diff = "";
                    if (diff > 0d && symb_after) {
                        str_diff = "-" + round(diff) + currency_symb;
                    } else if (diff > 0d && !symb_after) {
                        str_diff = "-" + currency_symb + round(diff);
                    }

                    viewHolder.info_top.setVisibility(View.VISIBLE);
                    viewHolder.info_top.setText("Best price!");
                    viewHolder.info_top.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));

                    viewHolder.currentInfo.setVisibility(View.VISIBLE);
                    viewHolder.currentInfo.setText(str_diff);
                    viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mActivity, R.color.dark_green));
                    viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.down_arrow));
                    viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mActivity, R.drawable.ll_green_bg));
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
                TextView textView = (TextView) ((LinearLayout) view.getParent()).findViewById(R.id.expand_underview_tv);
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
                timer.cancel();
                timer.purge();
                timer = new Timer();
                getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);

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
