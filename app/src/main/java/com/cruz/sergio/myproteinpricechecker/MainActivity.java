package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;
import com.cruz.sergio.myproteinpricechecker.helper.StartFirebase;

import static com.cruz.sergio.myproteinpricechecker.TabFragment.viewPager;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UnregisterBroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    public static NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    static FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Activity mActivity;
    Handler mHandler;
    Bundle indexBundle;
    int index = 0;
    public static String DETAILS_FRAGMENT_TAG = "DETAILS_FRAGMENT";
    Boolean addedNewProduct = false;
    public static Boolean CACHE_IMAGES = true;
    public static Boolean UPDATE_ONSTART = true;

    public static Boolean BC_Registered = false;
    public static float scale;
    public static int density;

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        BC_Registered = NetworkUtils.createBroadcast(mActivity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UnregisterBroadcastReceiver(mActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        CACHE_IMAGES = sharedPrefs.getBoolean("cache_images", true);
        UPDATE_ONSTART = sharedPrefs.getBoolean("update_on_start", true);

        StartFirebase.createJobDispatcher(this);

        setContentView(R.layout.activity_main);
        scale = getResources().getDisplayMetrics().density;
        density = getResources().getDisplayMetrics().densityDpi;
        mHandler = new Handler();

        /** Setup the DrawerLayout and NavigationView **/
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setItemIconTintList(null);

        /** Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment*/
        final TabFragment tabFragment = new TabFragment();
        indexBundle = new Bundle();
        indexBundle.putInt("index", 0);
        tabFragment.setArguments(indexBundle);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView, tabFragment).commit();

        MenuItem firstMenuItem = mNavigationView.getMenu().findItem(R.id.nav_item_watching);
        firstMenuItem.setChecked(true);
        TheMenuItem.lastMenuItem = firstMenuItem;

        /** Setup click events on the Navigation View Items. **/
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                TheMenuItem.lastMenuItem.setChecked(false);
                menuItem.setChecked(true);

                int menu_Item_Id = menuItem.getItemId();

                switch (menu_Item_Id) {
                    case R.id.nav_item_watching:
                        index = 0;
                        break;
                    case R.id.nav_item_search:
                        index = 1;
                        break;
                    case R.id.nav_item_graphs:
                        index = 2;
                        break;
                    case R.id.nav_item_voucher:
                        index = 3;
                        break;
                    case R.id.nav_item_settings:
                        Intent intent = new Intent(mActivity, SettingsActivity.class);
                        intent.putExtra("menuId", TheMenuItem.lastMenuItem.getItemId());
                        startActivity(intent);
                        break;
                    case R.id.nav_item_fav:
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.containerView, new DetailsFragment()).commit();
                        break;
                }

                if (tabFragment.isAdded()) {
                    viewPager.setCurrentItem(index);
                } else {
                    indexBundle.putInt("index", index);
                    tabFragment.getArguments().putAll(indexBundle);
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, tabFragment);
                    fragmentTransaction.commit();
                }
                TheMenuItem.lastMenuItem = menuItem;

                return true;
            }

        });

        /** Setup Drawer Toggle on the Toolbar ? triple parallel lines on the left **/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Menu icons are inflated just as they were with actionbar
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerToggle.syncState();

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        int count = getFragmentManager().getBackStackEntryCount();
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mNavigationView);

        if (drawerOpen) {
            mDrawerLayout.closeDrawers();
        } else if (!drawerOpen || count == 0) {
            super.onBackPressed();
        } else {
            Log.i("Sergio>", this + "onBackPressed:\naddedNewProduct= " + addedNewProduct);
            Log.i("Sergio>", this + "onBackPressed:\ngetFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG) != null && addedNewProduct=\n" + getFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG) + " added= " + addedNewProduct);
            getFragmentManager().popBackStack();
            if (getFragmentManager().findFragmentByTag(DETAILS_FRAGMENT_TAG) != null && addedNewProduct) {
                WatchingFragment.loaderManager.forceLoad();
                Log.i("Sergio>", this + "onBackPressed:\naddedNewProduct= " + addedNewProduct);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(mActivity, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    public static class TheMenuItem {
        static MenuItem lastMenuItem;

        TheMenuItem(MenuItem lastMenuItem) {
            this.lastMenuItem = lastMenuItem;
        }
    }


}