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
import android.view.Menu;
import android.view.MenuItem;

import com.cruz.sergio.myproteinpricechecker.helper.NetworkUtils;

import static com.cruz.sergio.myproteinpricechecker.TabFragment.viewPager;

public class MainActivity extends AppCompatActivity {
    public static NavigationView mNavigationView;
    DrawerLayout mDrawerLayout;
    static FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Activity mActivity;
    SharedPreferences sharedPrefs;
    Handler mHandler;
    Bundle indexBundle;
    int index = 0;


    @Override
    protected void onStart() {
        super.onStart();
        NetworkUtils.createBroadcast(mActivity);
        //Log.d("Sergio>>>", "Starting MainActivity and createBroadcast");
    }

    @Override
    protected void onPause() {
        super.onPause();
        NetworkUtils.UnregisterBroadcastReceiver(mActivity);
        //Log.d("Sergio>>>", "Pausing MainActivity and UnregisterBroadcastReceiver");
    }

    @Override
    protected void onStop() {
        super.onStop();
        NetworkUtils.UnregisterBroadcastReceiver(mActivity);
        //Log.d("Sergio>>>", "Stoping MainActivity");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Log.d("Sergio>>>", "Resuming MainActivity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtils.UnregisterBroadcastReceiver(mActivity);
        //Log.e("Sergio>>>", "Destroying MainActivity and UnregisterBroadcastReceiver");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mActivity = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

        /**
         *Setup the DrawerLayout and NavigationView
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setItemIconTintList(null);

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */
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



        /**
         * Setup click events on the Navigation View Items.
         */
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


        /**
         * Setup Drawer Toggle on the Toolbar ? triple parallel lines on the left
         */

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);

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
            getFragmentManager().popBackStack();
            //customviewpager.popFromBackStack(true);
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