package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cruz.sergio.myproteinpricechecker.helper.Alarm;
import com.cruz.sergio.myproteinpricechecker.helper.ProductsContract;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;

import static android.content.Context.MODE_PRIVATE;
import static android.database.DatabaseUtils.dumpCursorToString;
import static android.util.DisplayMetrics.DENSITY_HIGH;
import static android.util.DisplayMetrics.DENSITY_LOW;
import static android.util.DisplayMetrics.DENSITY_MEDIUM;
import static android.util.DisplayMetrics.DENSITY_XHIGH;
import static android.util.DisplayMetrics.DENSITY_XXHIGH;
import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.UPDATE_ONSTART;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.density;
import static com.cruz.sergio.myproteinpricechecker.helper.Alarm.updatePricesOnReceive;
import static com.cruz.sergio.myproteinpricechecker.helper.MPUtils.showCustomToast;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.ALL_PRODUCT_COLUMNS_PROJECTION;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_DIR_TYPE;
import static com.cruz.sergio.myproteinpricechecker.helper.ProductsContract.ProductsEntry.CONTENT_ITEM_TYPE;

public class WatchingFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, WatchingAdapter.RedrawRecyclerViewHandler {
   public static final int LOADER_ID = 0;
   public static final int IMAGE_PERIOD = 5400; // (ms)
   public static DeletedProductListener delete_listener;
   static String[] imageSizesToUse;
   WatchingAdapter adapter;
   Activity mActivity;
   SwipeRefreshLayout watchingSwipeRefreshLayout;
   public static Timer timer = new Timer();
   boolean addedNewProduct = false;
   SharedPreferences defaultSharedPreferences;
   Loader<Cursor> loaderManager;

   public WatchingFragment() {
      //required empty constructor
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
         imageSizesToUse = new String[]{"60x60", "70x70", "100x100"};
      } else if (density > DENSITY_MEDIUM && density <= DENSITY_HIGH) {
         imageSizesToUse = new String[]{"70x70", "100x100", "130x130"};
      } else if (density > DENSITY_HIGH && density <= DENSITY_XHIGH) {
         imageSizesToUse = new String[]{"100x100", "130x130", "180x180", "200x200"};
      } else if (density > DENSITY_XHIGH && density <= DENSITY_XXHIGH) { //galaxy S5: 480dpi scale = 3x; (70x70)*3 = 210x210;
         imageSizesToUse = new String[]{"180x180", "200x200", "270x270", "300x300"};
      } else {
         imageSizesToUse = new String[]{"270x270", "300x300", "350x350", "480x480"};
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

      MainActivity.notifySettingsChanged = hasChanged -> {
         if (hasChanged) {
            redrawListView();
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

      adapter = new WatchingAdapter(this.getContext(), this);

      android.support.v7.widget.RecyclerView watching_recyclerView = rootview.findViewById(R.id.watching_recyclerview);
      watching_recyclerView.setAdapter(adapter);
      watching_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
      watching_recyclerView.setHasFixedSize(true);

      loaderManager = getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);
      //loaderManager.forceLoad();

      watchingSwipeRefreshLayout = rootview.findViewById(R.id.watching_swiperefresh);

      watchingSwipeRefreshLayout.setOnRefreshListener(() -> {
             timer.cancel();
             timer.purge();
             timer = new Timer();
             //updatePricesOnStart(mActivity, false, false, null);
             updatePricesOnReceive(mActivity, false, false, null);
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
        dump_BIGdata_toLog(dumpCursorToString(data));
      if (data.getCount() == 0) {
         showCustomToast(mActivity, "Empty DataBase.\n" +
                 "Add products to track their prices.",
             R.mipmap.ic_info, R.color.colorPrimaryDarker, Toast.LENGTH_SHORT);
         if (watchingSwipeRefreshLayout != null) {
            watchingSwipeRefreshLayout.setRefreshing(false);
         }
      }
      adapter.swapCursor(data);
      if (addedNewProduct) {
         android.support.v7.widget.RecyclerView recyclerView = mActivity.findViewById(R.id.watching_recyclerview);
         recyclerView.smoothScrollToPosition(recyclerView.getMeasuredHeight());
         addedNewProduct = false;
      }
   }


   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      adapter.swapCursor(null);
   }

   public void redrawListView() {
      timer.cancel();
      timer.purge();
      timer = new Timer();
      getLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
      getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, WatchingFragment.this);
      //loaderManager.forceLoad();
   }

   @Override
   public void onRedrawRecyclerView(Boolean redraw) {
      Log.i("Sergio>", this + " onRedrawRecyclerView\nredraw= " + redraw);
      if (redraw) redrawListView();
   }

   interface DeletedProductListener {
      void onProductDeleted(Boolean deleted);
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
}
