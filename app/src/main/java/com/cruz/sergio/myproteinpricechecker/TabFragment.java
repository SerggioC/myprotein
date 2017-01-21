package com.cruz.sergio.myproteinpricechecker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3;
    Bundle extras;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getArguments();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        View tabLayout = inflater.inflate(R.layout.tab_layout, null);
        TabFragment.tabLayout = (TabLayout) tabLayout.findViewById(R.id.tabs);
        viewPager = (ViewPager) tabLayout.findViewById(R.id.viewpager);

        /**
         *Set an Adapter for the View Pager
         */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */

        TabFragment.tabLayout.post(new Runnable() {
            @Override
            public void run() {
                TabFragment.tabLayout.setupWithViewPager(viewPager);

                if (extras != null) {
                    int index = extras.getInt("index");
                    viewPager.setCurrentItem(index);
                    Log.i("Sergio>>>", "getItem: position= " + index);
                }

            }
        });



        return tabLayout;

    }


    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }


        /**
         * Return fragment with respect to Position
         */
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new WatchingFragment();
                case 1:
                    return new SearchFragment();
                case 2:
                    return new GraphsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        /**
         * This method returns the title of the tab according to the position.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Watching";
                case 1:
                    return "Search";
                case 2:
                    return "Graphs";
            }
            return null;
        }
    }

}
