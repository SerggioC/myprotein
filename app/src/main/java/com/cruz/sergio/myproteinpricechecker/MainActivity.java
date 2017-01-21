package com.cruz.sergio.myproteinpricechecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import static com.cruz.sergio.myproteinpricechecker.TabFragment.viewPager;

public class MainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    Activity mActivity;
    SharedPreferences sharedPrefs;
    Handler mHandler;
    Bundle indexBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        mActivity = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity); //TODO settings

        /**
         *Setup the DrawerLayout and NavigationView
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

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

        /**
         * Setup click events on the Navigation View Items.
         */
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                Log.i("Sergio>>>", "tabFragment.isAdded(?) " + tabFragment.isAdded());

                if (menuItem.getItemId() == R.id.nav_item_watching) {
                    int index = 0;
                    if (tabFragment.isAdded()) {
                        viewPager.setCurrentItem(index);
                    } else {
                        indexBundle.putInt("index", index);
                        tabFragment.getArguments().putAll(indexBundle);
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.containerView, tabFragment);
                        fragmentTransaction.commit();
                        Log.i("Sergio>>>", "onNavigationItemSelected: " + indexBundle);
                    }
                }

                if (menuItem.getItemId() == R.id.nav_item_search) {
                    int index = 1;
                    if (tabFragment.isAdded()) {
                        viewPager.setCurrentItem(index);
                    } else {
                        indexBundle.putInt("index", index);
                        tabFragment.getArguments().putAll(indexBundle);
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.containerView, tabFragment);
                        fragmentTransaction.commit();
                    }

//                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
//                    fragmentTransaction.replace(R.id.containerView, new TabFragment()).commit();
                }

                if (menuItem.getItemId() == R.id.nav_item_graphs) {
                    int index = 2;
                    if (tabFragment.isAdded()) {
                        viewPager.setCurrentItem(index);
                    } else {
                        indexBundle.putInt("index", index);
                        tabFragment.getArguments().putAll(indexBundle);
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.containerView, tabFragment);
                        fragmentTransaction.commit();
                    }
                }

                if (menuItem.getItemId() == R.id.nav_item_settings) {
                    startActivity(new Intent(mActivity, SettingsActivity.class));
                }

                if (menuItem.getItemId() == R.id.nav_item_fav) {
                    FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.containerView, new SentFragment()).commit();

                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

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


}