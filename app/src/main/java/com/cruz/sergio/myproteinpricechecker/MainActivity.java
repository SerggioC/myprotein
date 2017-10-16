package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cruz.sergio.myproteinpricechecker.helper.Alarm;
import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;
import com.cruz.sergio.myproteinpricechecker.helper.ProzisDomain;

import static com.cruz.sergio.myproteinpricechecker.TabFragment.tabLayout;
import static com.cruz.sergio.myproteinpricechecker.TabFragment.viewPager;
import static com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils.UnregisterBroadcastReceiver;

public class MainActivity extends AppCompatActivity {
    public static final String PREFERENCE_FILE_NAME = "wpt_preferences_file";
    public static final int SETTINGS_REQUEST_CODE = 5;
    public static final String CHANGED_NOTIFY_SETTINGS_REF = "notify_settings_ref";
    public static NavigationView mNavigationView;
    public static Boolean GETNEWS_ONSTART;
    public static Boolean CACHE_IMAGES;
    public static Boolean UPDATE_ONSTART;
    public static int MAX_NOTIFY_VALUE = 1_000_000_000;
    public static float scale;
    public static int density;
    public static boolean detailsActivityIsActive;
    public static ChangedNotifySettings notifySettingsChanged;
    static FragmentManager mFragmentManager;
    DrawerLayout mDrawerLayout;
    FragmentTransaction mFragmentTransaction;
    Activity mActivity;
    Handler mHandler;
    Bundle indexBundle;
    int index = 0;

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkUtils.createBroadcast(mActivity);
    }

    String getCurrencyCode(String symbol) {
//    http://www.xe.com/symbols.php

        String currencyCode = null;
//        switch (symbol) {
//            case "£": {
//                currencyCode = "GBP";
//            }
//            break;
//            case 2:
//                currencyCode = "February";
//                break;
//            case 3:
//                currencyCode = "March";
//                break;
//            case 4:
//                currencyCode = "April";
//                break;
//            case 5:
//                currencyCode = "May";
//                break;
//            case 6:
//                currencyCode = "June";
//                break;
//            case 7:
//                currencyCode = "July";
//                break;
//            case 8:
//                currencyCode = "August";
//                break;
//            case 9:
//                currencyCode = "September";
//                break;
//            case 10:
//                currencyCode = "October";
//                break;
//            case 11:
//                currencyCode = "November";
//                break;
//            case 12:
//                currencyCode = "December";
//                break;
//            default:
//                currencyCode = null;
//                break;
//        }

        return currencyCode;
    }

    @Override
    protected void onPause() {
        UnregisterBroadcastReceiver(mActivity);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        Alarm alarm = new Alarm();
        alarm.setAlarm(this);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        CACHE_IMAGES = sharedPrefs.getBoolean("cache_images", false);
        UPDATE_ONSTART = sharedPrefs.getBoolean("update_on_start", false);
        GETNEWS_ONSTART = sharedPrefs.getBoolean("getnews_on_start", false);

        String prz_country = ProzisDomain.getProzisWebLocation(sharedPrefs.getString("prz_website_location", "pt")).toLowerCase();
        SharedPreferences.Editor sharedPrefEditor = mActivity.getSharedPreferences(PREFERENCE_FILE_NAME, MODE_PRIVATE).edit();
        sharedPrefEditor.putString("prz_website_location", prz_country).commit();


        /* THIS IS WORKING
//*
//*        StartFirebase.createJobDispatcher(this);
//*
*/
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
                    case R.id.nav_item_news:
                        index = 0;
                        break;
                    case R.id.nav_item_watching:
                        index = 1;
                        break;
                    case R.id.nav_item_search:
                        index = 2;
                        break;
                    case R.id.nav_item_graphs:
                        index = 3;
                        break;
                    case R.id.nav_item_voucher:
                        index = 4;
                        break;
                    case R.id.nav_item_settings:
                        Intent intent = new Intent(mActivity, SettingsActivity.class);
                        intent.putExtra("menuId", TheMenuItem.lastMenuItem.getItemId());
                        startActivity(intent);
                        break;
                    case R.id.nav_item_fav:
//                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//                        fragmentTransaction.replace(R.id.containerView, new DetailsFragment()).commit();
                        startActivity(new Intent(mActivity, DetailsActivityMyprotein.class));
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
//        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
//        lp.setMargins(0, 0, 0, 0);
//        toolbar.getChildAt(0).setLayoutParams(lp);


        // Menu icons are inflated just as they were with actionbar
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

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
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(
                    mActivity,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out).toBundle();
            startActivityForResult(new Intent(mActivity, SettingsActivity.class), SETTINGS_REQUEST_CODE, bundle);

            //startActivity(new Intent(mActivity, SettingsActivity.class));
        }

        if (id == R.id.action_search) {
            TabLayout.Tab tab = tabLayout.getTabAt(TAB_IDS.SEARCH);
            tab.select();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Boolean changedNotify = data.getExtras().getBoolean(CHANGED_NOTIFY_SETTINGS_REF);
            if (changedNotify) {
                notifySettingsChanged.onNotifySettingsChanged(changedNotify);
            }
        }
    }

    public interface ChangedNotifySettings {
        void onNotifySettingsChanged(Boolean hasChanged);
    }

    public static final class TAB_IDS {
        public static final int NEWS = 0;
        public static final int WATCHING = 1;
        public static final int SEARCH = 2;
        public static final int GRAPHS = 3;
        public static final int VOUCHERS = 4;
        public static final int CARTS = 5;
    }

    public static class TheMenuItem {
        static MenuItem lastMenuItem;

        TheMenuItem(MenuItem lastMenuItem) {
            this.lastMenuItem = lastMenuItem;
        }
    }

}