package com.cruz.sergio.myproteinpricechecker;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.DBHelper;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.CACHE_IMAGES;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.MAX_NOTIFY_VALUE;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.PREFERENCE_FILE_NAME;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.scale;
import static com.cruz.sergio.myproteinpricechecker.R.id.main_cardview;
import static com.cruz.sergio.myproteinpricechecker.R.id.notify;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.IMAGE_PERIOD;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.delete_listener;
import static com.cruz.sergio.myproteinpricechecker.WatchingFragment.imageSizesToUse;
import static com.cruz.sergio.myproteinpricechecker.helper.Alarm.updatePricesOnReceive;
import static com.cruz.sergio.myproteinpricechecker.helper.FirebaseJobservice.LAST_DB_UPDATE_PREF_KEY;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.getMillisecondsToDate;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomSlimToast;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomToast;

/**
 * Created by Sergio on 04/01/2018.
 */

public class WatchingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
   private final Context mContext;
   private Cursor mCursor;
   private Boolean[] showPercent = null;
   private Boolean globalNotifications = null;
   SharedPreferences defaultSharedPreferences;
   Boolean[] isExpandedArray = null;


   private static final int VIEW_TYPE_HEADER = 0;
   private static final int VIEW_TYPE_ITEM = 1;

   public WatchingAdapter(Context context, RedrawRecyclerViewHandler handler) {
      this.mContext = context;
      this.redrawHandler = handler;
   }


   public interface RedrawRecyclerViewHandler {
      void onRedrawRecyclerView(Boolean redraw);
   }

   RedrawRecyclerViewHandler redrawHandler;


   /**
    * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
    * an item.
    * <p>
    * This new ViewHolder should be constructed with a new View that can represent the items
    * of the given type. You can either create a new View manually or inflate it from an XML
    * layout file.
    * <p>
    * The new ViewHolder will be used to display items of the adapter using
    * {@link #onBindViewHolder(RecyclerView.ViewHolder, int)}. Since it will be re-used to display
    * different items in the data set, it is a good idea to cache references to sub views of
    * the View to avoid unnecessary {@link View#findViewById(int)} calls.
    *
    * @param viewGroup   The ViewGroup into which the new View will be added after it is bound to
    *                 an adapter position.
    * @param viewType The view type of the new View.
    * @return A new ViewHolder that holds a View of the given view type.
    * @see #getItemViewType(int)
    * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
    */
   @Override
   public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
      defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      globalNotifications = defaultSharedPreferences.getBoolean("notifications_global_key", true);
      RecyclerView.ViewHolder viewHolder;

      switch (viewType) {
         case VIEW_TYPE_HEADER: {
            View view = LayoutInflater.from(mContext).inflate( R.layout.watch_list_header_view, viewGroup, false);
            view.setFocusable(true);
            viewHolder = new HeaderViewHolder(view);
            break;
         }
         case VIEW_TYPE_ITEM: {
            View view = LayoutInflater.from(mContext).inflate(R.layout.watching_item_layout, viewGroup, false);
            view.setFocusable(true);
            viewHolder = new WatchingViewHolder(view);
            break;
         }
         default: {
            int layoutId = R.layout.watching_item_layout;
            View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);
            view.setFocusable(true);
            viewHolder = new WatchingViewHolder(view);
            Log.w("Sergio>", this + " onCreateViewHolder\nwrong ViewType or no viewType given. Returning VIEW_TYPE_ITEM");
            break;
         }
      }

      if (showPercent == null || showPercent.length != mCursor.getCount()) {
         showPercent = new Boolean[mCursor.getCount()];
         for (int i = 0; i < mCursor.getCount(); i++) {
            showPercent[i] = true;
         }
      }


      return viewHolder;
   }

   /**
    * Called by RecyclerView to display the data at the specified position. This method should
    * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
    * position.
    * <p>
    * Note that unlike {@link ListView}, RecyclerView will not call this method
    * again if the position of the item changes in the data set unless the item itself is
    * invalidated or the new position cannot be determined. For this reason, you should only
    * use the <code>position</code> parameter while acquiring the related data item inside
    * this method and should not keep a copy of it. If you need the position of an item later
    * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
    * have the updated adapter position.
    * <p>
    * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int)} instead if Adapter can
    * handle efficient partial bind.
    *
    * @param holder   The ViewHolder which should be updated to represent the contents of the
    *                     item at the given position in the data set.
    * @param position The position of the item within the adapter's data set.
    */
   @Override
   public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      mCursor.moveToPosition(position);

      if (getItemViewType(position) == VIEW_TYPE_HEADER) {
         // Casting to the desired viewHolder
         HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
         BindHeaderView(headerViewHolder, position);
         return;
      }

      // Casting to the desired viewHolder
      WatchingViewHolder viewHolder = (WatchingViewHolder) holder;

      expandOrCollapse(viewHolder.under_cardview, viewHolder.expand_underview_tv, mCursor);

      //final int position = view.getTag(R.id.view_position) == null ? -1 : (int) view.getTag(R.id.view_position);
      //final int this_product_id = mCursor.getInt(ProductsContract.ProductsEntry._ID_INDEX);

      final String prod_name = mCursor.getString(mCursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME));
      final String string_array_images = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES_INDEX);
      final String productBrand = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BRAND_INDEX);
      final Boolean[] show_notifications = {mCursor.getInt(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS_INDEX) == 1};
      final double notify_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE_INDEX);
      String current_price_string = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_INDEX);
      final String currencySymbol = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL_INDEX);

      final boolean symb_before = current_price_string.indexOf(currencySymbol) == 0;

      if (globalNotifications) {
         if (show_notifications[0]) {
            viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications);
            viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_15dp), null, null, null);
            String notifyText = "Notify when price drops.";
            if (notify_value > 0) {
               notifyText = "Notify when price reaches " + (symb_before ? (currencySymbol + notify_value) : (notify_value + currencySymbol));
            }
            viewHolder.notify_info.setText(notifyText);
         } else {
            viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_none);
            viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_none_15dp), null, null, null);
            viewHolder.notify_info.setText("Notifications disabled.");
         }
      } else {
         SpannableStringBuilder ssb1 = new SpannableStringBuilder("Notifications disabled.");
         if (notify_value > 0) {
            String notifyText2 = "Notify when price reaches " + (symb_before ? (currencySymbol + notify_value) : (notify_value + currencySymbol));
            ssb1 = new SpannableStringBuilder(notifyText2);
            ssb1.setSpan(new StrikethroughSpan(), 0, notifyText2.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
         }
         viewHolder.notify_info.setText(ssb1);
         if (show_notifications[0]) {
            viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_off);
            viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_off_15dp), null, null, null);
         } else {
            viewHolder.notify_icon.setImageResource(R.drawable.ic_notifications_none);
            viewHolder.notify_info.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_notifications_none_15dp), null, null, null);

         }
      }
      

      String options_sabor = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME1_INDEX);
      String options_caixa = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME2_INDEX);
      String options_quant = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MP_OPTIONS_NAME3_INDEX);

      double actual_price_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_VALUE_INDEX);
      long actualPriceDate = mCursor.getLong(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE_INDEX);

      String min_price_string = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_INDEX);
      long minPriceDate = mCursor.getLong(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_DATE_INDEX);
      double min_price_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_MIN_PRICE_VALUE_INDEX);

      String max_price_string = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_INDEX);
      long maxPriceDate = mCursor.getLong(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_DATE_INDEX);
      double max_price_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_MAX_PRICE_VALUE_INDEX);

      long previousPriceDate = mCursor.getLong(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_DATE_INDEX);
      double previous_price_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_PREVIOUS_PRICE_VALUE_INDEX);


      if (options_sabor == null) options_sabor = "";
      if (options_caixa == null) options_caixa = "";
      if (options_quant == null) options_quant = "";
      if (StringUtil.isBlank(current_price_string)) current_price_string = "N/A";

      String sub_title = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_PRODUCT_SUBTITLE_INDEX);
      String webstore_name = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_WEBSTORE_NAME_INDEX);
      String webstore_str = "Webstore";

      SpannableStringBuilder pptList_SSB = new SpannableStringBuilder(webstore_str);
      pptList_SSB.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pptList_SSB.length() - webstore_str.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
      pptList_SSB.append(": " + webstore_name + "\n");

      pptList_SSB.append(sub_title);
      pptList_SSB.setSpan(new RelativeSizeSpan(1.1f), pptList_SSB.length() - sub_title.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

      String prod_description = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_PRODUCT_DESCRIPTION_INDEX);

      if (!StringUtil.isBlank(prod_description)) {

         String prod_benefits = "\n" + "Product benefits" + "\n";
         pptList_SSB.append(prod_benefits);
         pptList_SSB.setSpan(new RelativeSizeSpan(1.1f), pptList_SSB.length() - prod_benefits.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
         pptList_SSB.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), pptList_SSB.length() - prod_benefits.length(), pptList_SSB.length(), SPAN_EXCLUSIVE_EXCLUSIVE);

         String[] prod_description_array = prod_description.split("\n");
         Drawable drawable = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.tick, null);
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
      SpannableStringBuilder ssbp = new SpannableStringBuilder("Last updated: " + getMillisecondsToDate(actualPriceDate));
      ssbp.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.dark_green)), 0, ssbp.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
      viewHolder.undercard_last_updated.setText(ssbp);

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
         Glide.with(mContext).load(R.drawable.noimage).asBitmap().format(PREFER_ARGB_8888).asIs().dontTransform().into(iv);
      }

      String str_title = prod_name + " " + options_sabor + " " + options_caixa + " " + options_quant;
      viewHolder.titleView.setText(str_title);
      viewHolder.product_brand.setText(productBrand);
      viewHolder.highestPriceView.setText(max_price_string);
      viewHolder.lowestPriceView.setText(min_price_string);
      viewHolder.currentPriceView.setText(current_price_string);
      viewHolder.highestPriceDate.setText(getMillisecondsToDate(maxPriceDate));
      viewHolder.lowestPriceDate.setText(getMillisecondsToDate(minPriceDate));


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
            viewHolder.currentInfo.setText(showPercent[position] ? percentStr : absDiffStr);
            viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mContext, R.color.dark_green));
            viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.down_arrow));

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
            viewHolder.currentInfo.setText(showPercent[position] ? percentStr : absDiffStr);
            viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.up_arrow));
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
            viewHolder.info_top.setTextColor(ContextCompat.getColor(mContext, R.color.red));

            viewHolder.currentInfo.setVisibility(View.VISIBLE);
            viewHolder.currentInfo.setText(showPercent[position] ? percentStr : absDiffStr);
            viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.up_arrow));
            viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ll_red_bg));
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
            viewHolder.info_top.setTextColor(ContextCompat.getColor(mContext, R.color.dark_green));

            viewHolder.currentInfo.setVisibility(View.VISIBLE);
            viewHolder.currentInfo.setText(showPercent[position] ? percentStr : absDiffStr);
            viewHolder.currentInfo.setTextColor(ContextCompat.getColor(mContext, R.color.dark_green));
            viewHolder.up_down_icon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.down_arrow));
            viewHolder.ll_current_price.setBackground(ContextCompat.getDrawable(mContext, R.drawable.ll_green_bg));
         }


         if (hasPercent) {
            final String finalPercentStr = percentStr;
            final String finalAbsoluteDiff = absDiffStr;
            viewHolder.llPriceInfo.setOnClickListener(v -> {
               if (showPercent[position]) {
                  viewHolder.currentInfo.setText(finalAbsoluteDiff);
                  showPercent[position] = false;
               } else {
                  viewHolder.currentInfo.setText(finalPercentStr);
                  showPercent[position] = true;
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
//                viewHolder.info_top.setTextColor(ContextCompat.getColor(mContext, R.color.red));
      }
   }

   private void BindHeaderView(HeaderViewHolder viewHolder, int position) {

         if (getItemCount() == 1) {
            mCursor.moveToPosition(position);
            long actualPriceDate = mCursor.getLong(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_DATE_INDEX);
            viewHolder.updated_tv.setText(getMillisecondsToDate(actualPriceDate));
         } else {
            SharedPreferences sharedPref = mContext.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE);
            long last_saved_date = sharedPref.getLong(LAST_DB_UPDATE_PREF_KEY, 0);
            viewHolder.updated_tv.setText(last_saved_date == 0 ? "Never" : getMillisecondsToDate(last_saved_date));
         }

         if (globalNotifications) {
            viewHolder.ic_global_notifications.setImageResource(R.drawable.ic_notifications);
         } else {
            viewHolder.ic_global_notifications.setImageResource(R.drawable.ic_notifications_off_none);
         }

      viewHolder.ic_global_notifications.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

            LayoutInflater inflater = LayoutInflater.from(mContext);
            View dialogView = inflater.inflate(R.layout.notifications_alert_dialog_global, null);
            alertDialogBuilder.setView(dialogView);
            final SwitchCompat switchCompat = dialogView.findViewById(R.id.switch_notify_global);
            switchCompat.setChecked(globalNotifications);
            alertDialogBuilder.setTitle("Global Notifications");
            alertDialogBuilder.setIcon(R.mipmap.ic_notification_bell);
            alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, id) -> {
                   boolean isChecked = switchCompat.isChecked();
                   if (globalNotifications != isChecked) {
                      defaultSharedPreferences.edit().putBoolean("notifications_global_key", isChecked).commit();
                      showCustomToast(mContext, "Global Notifications are now " + (isChecked ? "Active!" : "Disabled!"),
                          isChecked ? R.mipmap.ic_ok2 : R.mipmap.ic_warning,
                          isChecked ? R.color.f_color2 : R.color.orange, Toast.LENGTH_LONG);
                      //redrawListView();
                      redrawHandler.onRedrawRecyclerView(true);
                      notifyDataSetChanged();
                   }


                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
         });



   }

   @Override
   public int getItemViewType(int position) {
      if (position == 0) {
         return VIEW_TYPE_HEADER;
      } else {
         return VIEW_TYPE_ITEM;
      }
   }

   /**
    * Returns the total number of items in the data set held by the adapter.
    *
    * @return The total number of items in this adapter.
    */
   @Override
   public int getItemCount() {
      if (mCursor == null) return 0;
      return mCursor.getCount();
   }

   public void swapCursor(Cursor cursor) {
      this.mCursor = cursor;
      notifyDataSetChanged();
   }




   class HeaderViewHolder extends RecyclerView.ViewHolder {
      final TextView updated_tv;
      final ImageView ic_global_notifications;


      public HeaderViewHolder(View itemView) {
         super(itemView);
         updated_tv = itemView.findViewById(R.id.updated_tv);
         ic_global_notifications = itemView.findViewById(R.id.ic_global_notifications);
      }

   }

   public class WatchingViewHolder extends RecyclerView.ViewHolder {
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
      final TextView undercard_last_updated;
      final ProgressBar small_pb_undercard;
      final TextView product_brand;
      final ImageView notify_icon;
      final TextView notify_info;
      final LinearLayout llPriceInfo;
      final TextView expand_underview_tv;
      final ImageView update_this_entry_icon;
      final android.support.v7.widget.CardView under_cardview;
      final ImageView delete_entry;

      public WatchingViewHolder(View itemView) {
         super(itemView);
         titleView = itemView.findViewById(R.id.item_title_textview);
         highestPriceView = itemView.findViewById(R.id.item_highest_price_textview);
         lowestPriceView = itemView.findViewById(R.id.item_lowest_price_textview);
         currentPriceView = itemView.findViewById(R.id.item_current_price_textview);
         highestPriceDate = itemView.findViewById(R.id.item_highest_price_date);
         lowestPriceDate = itemView.findViewById(R.id.item_lowest_price_date);
         info_top = itemView.findViewById(R.id.info_top);
         currentInfo = itemView.findViewById(R.id.current_info);
         ll_current_price = itemView.findViewById(R.id.ll_current_price);
         imageSwitcher = itemView.findViewById(R.id.image_switcher);
         up_down_icon = itemView.findViewById(R.id.up_down_arrow);
         main_cardView = itemView.findViewById(main_cardview);
         undercard_tv_desc = itemView.findViewById(R.id.description_undercard);
         undercard_last_updated = itemView.findViewById(R.id.last_updated_undercard);
         small_pb_undercard = itemView.findViewById(R.id.pbar_undercard);
         product_brand = itemView.findViewById(R.id.product_brand);
         notify_icon = itemView.findViewById(notify);
         notify_info = itemView.findViewById(R.id.notifications_info);
         llPriceInfo = itemView.findViewById(R.id.ll_price_info);
         expand_underview_tv = itemView.findViewById(R.id.expand_underview_tv);
         update_this_entry_icon = itemView.findViewById(R.id.update_this_entry);
         under_cardview = itemView.findViewById(R.id.under_cardview);
         delete_entry = itemView.findViewById(R.id.delete_entry);


         itemView.findViewById(R.id.open_web).setOnClickListener(v -> {
            mCursor.moveToPosition(getAdapterPosition());
            String url = mCursor.getString(mCursor.getColumnIndex(ProductsContract.ProductsEntry.COLUMN_PRODUCT_BASE_URL));
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            v.getContext().startActivity(browser);
         });

         itemView.findViewById(R.id.add_to_cart).setOnClickListener(v ->
             showCustomSlimToast(mContext, "Add product to virtual cart\nTODO", Toast.LENGTH_SHORT));
         
         NotificationClickHandler();

         UnderViewClickHandler();

      }

      //
      // Handling clicks in the hidden cardview
      //
      void UnderViewClickHandler() {
         expand_underview_tv.setOnClickListener(v -> {

         if (mCursor != null) {

            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            String this_product_id = String.valueOf(mCursor.getInt(ProductsContract.ProductsEntry._ID_INDEX));

            //UPDATE THIS ITEM ONLY
            update_this_entry_icon.setOnClickListener(v1 -> {

               //updatePricesOnStart(mContext, false, true, String.valueOf(this_product_id));
               updatePricesOnReceive(mContext, false, true, this_product_id);
               v1.setVisibility(View.GONE);
               small_pb_undercard.setVisibility(View.VISIBLE);
            });

            delete_entry.setOnClickListener(v12 -> {

               AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
               alertDialogBuilder.setTitle("Delete Product?");
               alertDialogBuilder.setIcon(R.mipmap.ic_error);
               alertDialogBuilder
                   .setMessage("Are you sure you want to delete this entry?\nAll logged prices for this product will be lost!")
                   .setCancelable(true)
                   .setPositiveButton("Yes", (dialog, id) -> {
                      DBHelper dbHelper = new DBHelper(mContext);
                      SQLiteDatabase db = dbHelper.getWritableDatabase();
                      int delete_db_entries_result = db.delete(ProductsContract.ProductsEntry.TABLE_NAME, "_ID=" + "'" + this_product_id + "'", null);
                      db.close();

                      String string_array_images = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_ARRAYLIST_IMAGES_INDEX);
                      deleteImageFiles(string_array_images);

                      final String prod_name = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_PRODUCT_NAME_INDEX);

                      if (delete_db_entries_result == 1) {
                         animateRemoving(main_cardView, under_cardview, prod_name);
                      } else {
                         showCustomToast(mContext, "Error deleting " + prod_name + " from DataBase!",
                             R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                      }

                   })
                   .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

               AlertDialog alertDialog = alertDialogBuilder.create();
               alertDialog.show();
            });

            Boolean isExpanded = isExpandedArray[position];
            if (isExpanded) {
               ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_expand_more), null, null, null);
               ((TextView) v).setText("Details");
               collapseIt(under_cardview);
               isExpandedArray[position] = false;
            } else {
               ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_expand_less_black_24dp), null, null, null);
               ((TextView) v).setText("Close");
               if (position == mCursor.getCount() - 1) {
                  expandIt(under_cardview, true);
               } else {
                  expandIt(under_cardview, false);
               }
               isExpandedArray[position] = true;
            }
         }
      });
      }


      //
      // Alert Dialog for notifications
      //
      void NotificationClickHandler() {

         final SwitchCompat[] alertSwitch = new SwitchCompat[1];
         final RadioGroup[] radioGroup = new RadioGroup[1];
         final AppCompatRadioButton[] radio_every = new AppCompatRadioButton[1];
         final AppCompatRadioButton[] radio_target = new AppCompatRadioButton[1];
         final android.support.design.widget.TextInputEditText[] alertTextView = new android.support.design.widget.TextInputEditText[1];

         notify_icon.setOnClickListener(notifyIconView -> {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            Boolean show_notifications = mCursor.getInt(ProductsContract.ProductsEntry.COLUMN_NOTIFICATIONS_INDEX) == 1;
            double notify_value = mCursor.getDouble(ProductsContract.ProductsEntry.COLUMN_NOTIFY_VALUE_INDEX);
            String current_price_string = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_ACTUAL_PRICE_INDEX);
            String currencySymbol = mCursor.getString(ProductsContract.ProductsEntry.COLUMN_MP_CURRENCY_SYMBOL_INDEX);
            boolean symb_before = current_price_string.indexOf(currencySymbol) == 0;

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.notifications_alert_dialog, null);

            int pad = (int) (10 * scale + 0.5f);
            dialogView.setPadding(pad, 0, pad, 0);
            alertDialogBuilder.setView(dialogView);
            alertSwitch[0] = dialogView.findViewById(R.id.switch_notify);
            alertTextView[0] = dialogView.findViewById(R.id.tv_alert_value);
            radio_every[0] = dialogView.findViewById(R.id.radioButton_every);
            radio_target[0] = dialogView.findViewById(R.id.radioButton_target);
            radioGroup[0] = dialogView.findViewById(R.id.radioGroup_notify);
            final TextView textView1 = dialogView.findViewById(R.id.tv_alert1);

            alertSwitch[0].setChecked(show_notifications);
            radioGroup[0].setEnabled(show_notifications);

            radio_every[0].setEnabled(show_notifications);
            radio_every[0].setChecked(notify_value == 0 ? true : false);

            radio_target[0].setEnabled(show_notifications);
            radio_target[0].setChecked(show_notifications && notify_value > 0 ? true : false);

            textView1.setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
            textView1.setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());

            alertTextView[0].setEnabled(show_notifications && notify_value > 0);
            alertTextView[0].setActivated(show_notifications && notify_value > 0);
            alertTextView[0].setText(show_notifications && notify_value > 0 ? String.valueOf(notify_value) : "");

            alertSwitch[0].setOnClickListener(v -> {
               radioGroup[0].setEnabled(alertSwitch[0].isChecked());
               radio_every[0].setEnabled(alertSwitch[0].isChecked());
               radio_target[0].setEnabled(alertSwitch[0].isChecked());
               alertTextView[0].setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
               alertTextView[0].setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());
               alertTextView[0].setText(alertSwitch[0].isChecked() && radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
               textView1.setEnabled(alertSwitch[0].isChecked() && radio_target[0].isChecked());
               textView1.setActivated(alertSwitch[0].isChecked() && radio_target[0].isChecked());
            });

            radio_target[0].setOnClickListener(v -> {
               alertTextView[0].setEnabled(radio_target[0].isChecked());
               alertTextView[0].setActivated(radio_target[0].isChecked());
               alertTextView[0].setText(radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
               textView1.setEnabled(radio_target[0].isChecked());
               textView1.setActivated(radio_target[0].isChecked());
            });

            radio_every[0].setOnClickListener(v -> {
               alertTextView[0].setEnabled(radio_target[0].isChecked());
               alertTextView[0].setActivated(radio_target[0].isChecked());
               alertTextView[0].setText(radio_target[0].isChecked() ? String.valueOf(notify_value) : "");
               textView1.setEnabled(radio_target[0].isChecked());
               textView1.setActivated(radio_target[0].isChecked());
            });

            alertDialogBuilder.setTitle("Notifications");
            alertDialogBuilder.setIcon(R.mipmap.ic_notification_bell);
            alertDialogBuilder
                .setMessage("Edit notification settings for this item.")
                .setCancelable(true)
                .setPositiveButton("Save", (dialog, id) -> {
                   DBHelper dbHelper = new DBHelper(mContext);
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

                   int update_result = db.update(ProductsContract.ProductsEntry.TABLE_NAME, contentValues, "_ID=" + "'" + adapterPosition + "'", null);

                   if (update_result == 1) {
                      redrawHandler.onRedrawRecyclerView(true);
                      notifyDataSetChanged();

                      showCustomToast(mContext,
                          (alertSwitch[0].isChecked() && radio_target[0].isChecked() ?
                              "Alert when price reaches " +
                                  (symb_before ? (currencySymbol + String.valueOf(target_val)) : (String.valueOf(target_val) + currencySymbol)) :
                              (alertSwitch[0].isChecked() && radio_every[0].isChecked()) ? "Alert every time price drops." : "Notifications disabled."),
                          alertSwitch[0].isChecked() ? R.mipmap.ic_ok2 : R.mipmap.ic_warning,
                          alertSwitch[0].isChecked() ? R.color.f_color2 : R.color.orange,
                          Toast.LENGTH_LONG);

                   } else {
                      showCustomToast(mContext, "Error updating notifications!",
                          R.mipmap.ic_error, R.color.red, Toast.LENGTH_LONG);
                   }


                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
         });
      }


      
   }

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


   public void expandOrCollapse(View view, TextView expand_underview_tv, Cursor cursor) {
      int itemCount = getItemCount();
      if (isExpandedArray == null || isExpandedArray.length != itemCount) {
         isExpandedArray = new Boolean[itemCount];
         for (int i = 0; i < itemCount; i++) {
            isExpandedArray[i] = false;
         }
      }
      if (isExpandedArray[cursor.getPosition()]) {
         expandIt(view, false);
         //TextView textView = ((LinearLayout) view.getParent()).findViewById(R.id.expand_underview_tv);
         expand_underview_tv.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_expand_less_black_24dp), null, null, null);
         expand_underview_tv.setText("Close");
      }
   }


//   public class cursorDBAdapter extends CursorAdapter {
//      private LayoutInflater cursorItemInflater;
//
//      public cursorDBAdapter(Context context, Cursor c, int flags) {
//         super(context, c, flags);
//         cursorItemInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//      }
//
//      @Override
//      public int getCount() {
//         return super.getCount();
//      }
//
//      @Override
//      public View getView(final int position, View convertView, ViewGroup parent) {
//
//         // quando convertView != null recicla as views anteriores já existentes
//         // mas misturava as imagens do imageSwitcher. As views não visíveis eram recriadas
//         // com imageSwitcher das views que desapareciam multiplicando timers e imagens misturando tudo,
//         // mas fica mais lento
//         // TODO: melhorar performance
//
//         if (showPercent == null || showPercent.length != mCursor.getCount()) {
//            showPercent = new Boolean[mCursor.getCount()];
//            for (int i = 0; i < mCursor.getCount(); i++) {
//               showPercent[i] = true;
//            }
//         }
//
//         View view;
//         if (convertView != null) {
//            //view = newView(mContext, mCursor, null);
//            view = newView(null, null, null);
//            view.setTag(R.id.view_position, position);
//            bindView(view, mContext, (Cursor) getItem(position));
//         } else {
//            view = super.getView(position, convertView, parent);
//         }
//
//         View under_view = view.findViewById(R.id.under_cardview);
//         expandOrCollapse(under_view, mCursor);
//
//
//         view.setTag(R.id.view_position, position);
//
//         return view;
//      }
//
//      // The newView method is used to inflate a new view and return it,
//      // you don't bind any data to the view at this point.
//      @Override
//      public View newView(Context context, final Cursor cursor, ViewGroup parent) {
//         final View item_root = cursorItemInflater.inflate(R.layout.watching_item_layout, null, false);
//         ViewHolder viewHolder = new ViewHolder(item_root);
//         item_root.setTag(R.id.viewholder, viewHolder);
//         if (cursor != null) {
//            item_root.setTag(R.id.view_position, cursor.getPosition());
//         }
//         return item_root;
//      }
//
//      // The bindView method is used to bind all data to a given view
//      // such as setting the text on a TextView.
//      @Override
//      public void bindView(final View view, Context context, final Cursor cursor) {
//         final ViewHolder viewHolder = (ViewHolder) view.getTag(R.id.viewholder);
//
//
//      }   // End bindView
//
//
//
//
//      @Override
//      protected void onContentChanged() {
//         super.onContentChanged();
//         Log.w("Sergio>", this + "onContentChanged");
//      }
//
//
//
//   }

   private Boolean extractImagesFromJSON_URL(final WatchingViewHolder viewHolder, String string_array_images) {
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
   private Boolean extractImagesFromJSON_Cache(WatchingViewHolder viewHolder, String string_array_images) {
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
                  File img_File = new File(mContext.getFilesDir(), file_uri_ToUse);
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

   private void placeImagesFromFile(final WatchingViewHolder viewHolder, final ArrayList<File> arrayListImageFiles) {
      final int size = arrayListImageFiles.size();

      viewHolder.imageSwitcher.removeAllViews();
      viewHolder.imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
         public View makeView() {
            return getNewImageView(70);
         }
      });
      // Declare in and out animations and load them using AnimationUtils class
      Animation fadeIn = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
      fadeIn.setDuration(1200);
      Animation fadeOut = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
      fadeOut.setDuration(1200);
      // set the animation type to ImageSwitcher
      viewHolder.imageSwitcher.setInAnimation(fadeIn);
      viewHolder.imageSwitcher.setOutAnimation(fadeOut);

      final int[] currentIndex = {0};

      final Runnable runnable = () -> {
         if (currentIndex[0] >= size) currentIndex[0] = 0;
         viewHolder.imageSwitcher.setImageURI(Uri.fromFile(arrayListImageFiles.get(currentIndex[0])));
         currentIndex[0]++;
      };

      TimerTask timerTask = new TimerTask() {
         public void run() {
            ((Activity) mContext).runOnUiThread(runnable);
         }
      };
      Timer timer = new Timer();
      timer.scheduleAtFixedRate(timerTask, 0, IMAGE_PERIOD);

//            // Iniciar primeira apresentação da imagem
//            mContext.runOnUiThread(runnable);
      viewHolder.imageSwitcher.setOnClickListener(v ->
          ((Activity) mContext).runOnUiThread(runnable));

   }

   private void placeimageURLs(final WatchingViewHolder viewHolder, final ArrayList<String> arrayListImageURLs) {
      viewHolder.imageSwitcher.removeAllViews();
      viewHolder.imageSwitcher.setFactory(() -> getNewImageView(70));
      // Declare in and out animations and load them using AnimationUtils class
      final Animation fadeIn = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
      fadeIn.setDuration(1200);
      Animation fadeOut = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
      fadeOut.setDuration(1200);
      // set the animation type to ImageSwitcher
      viewHolder.imageSwitcher.setInAnimation(fadeIn);
      viewHolder.imageSwitcher.setOutAnimation(fadeOut);

      final int size = arrayListImageURLs.size();
      //Set the schedule function and rate
      final int[] currentIndex = {0};
      final Runnable runnable = () -> {
         if (currentIndex[0] >= size) currentIndex[0] = 0;

         if (viewHolder.imageSwitcher.getRootView().isShown()) {
            Glide.with(mContext)
                .load(arrayListImageURLs.get(currentIndex[0]))
                .asBitmap()
                .asIs()
                .format(PREFER_ARGB_8888)
                .dontTransform()
                .into(new SimpleTarget<Bitmap>() {
                   @Override
                   public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                      viewHolder.imageSwitcher.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
                   }

                   @Override
                   public void onLoadFailed(Exception e, Drawable errorDrawable) {
                      viewHolder.imageSwitcher.setImageResource(R.drawable.noimage);
                   }
                });
         }
         currentIndex[0]++;
      };

      TimerTask timerTask = new TimerTask() {
         public void run() {
            ((Activity) mContext).runOnUiThread(runnable);
         }
      };

      //Called every 5400 milliseconds
      WatchingFragment.timer.scheduleAtFixedRate(timerTask, 0, IMAGE_PERIOD);

      viewHolder.imageSwitcher.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            ((Activity) mContext).runOnUiThread(runnable);
         }
      });

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
               android.support.v7.widget.RecyclerView recyclerView = view.getRootView().findViewById(R.id.watching_recyclerview);
               recyclerView.smoothScrollToPosition(recyclerView.getMeasuredHeight());
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
   @NonNull
   private View getNewImageView(int pixels_widthHeight) {
      ImageView imageView = new ImageView(mContext);
      imageView.setPadding(0, 2, 2, 0);
      imageView.setScaleType(ImageView.ScaleType.CENTER);
      imageView.setAdjustViewBounds(true);
      int widthHeight = (int) (pixels_widthHeight * scale + 0.5f);
      imageView.setLayoutParams(new FrameLayout.LayoutParams(widthHeight, widthHeight));
      return imageView;
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
//                    showCustomToast(mContext, prod_name + " " + "deleted from database.",
//                            R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);
//                } else {
//                    showCustomToast(mContext, "Database updated.\nSome image files could not deleted.",
//                            R.mipmap.ic_warning, R.color.f_color4, Toast.LENGTH_LONG);
//                }

            showCustomToast(mContext, prod_name + " " + "deleted from database.",
                R.mipmap.ic_ok2, R.color.green, Toast.LENGTH_LONG);

            // Redraw listView
            //redrawListView();
            redrawHandler.onRedrawRecyclerView(true);
            notifyDataSetChanged();

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
                           File fdelete = new File(mContext.getFilesDir(), file_uri);
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
}
