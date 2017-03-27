package com.cruz.sergio.myproteinpricechecker;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.TheMenuItem.lastMenuItem;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.mNavigationView;

public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    Bundle extras;
    MenuItem menuItem;
    int[] tab_icons;
    String[] tab_text;
    int numberOfTabs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getArguments();
        tab_text = new String[] {
                getString(R.string.tab_text_watching),
                getString(R.string.tab_text_search),
                getString(R.string.tab_text_graph),
                getString(R.string.tab_text_voucher)
        };
        tab_icons = new int[] {
                R.drawable.ic_view_statelist,
                R.drawable.ic_search_statelist,
                R.drawable.ic_graph_statelist,
                R.drawable.ic_voucher_statelist
        };
        numberOfTabs = tab_text.length;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*** Inflate tab_layout and setup Views. */
        View tabLayout = inflater.inflate(R.layout.tab_layout, null);
        TabFragment.tabLayout = (TabLayout) tabLayout.findViewById(R.id.tabs);
        viewPager = (ViewPager) tabLayout.findViewById(R.id.viewpager);

        /*** Set an Adapter for the View Pager */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        //Manter o conteudo das tabs ao passar de uma para a outra
        viewPager.setOffscreenPageLimit(numberOfTabs);

        /**
         * This is a workaround,
         * The setupWithViewPager dose't works without the runnable.
         * Maybe a Support Library Bug.
         */
        TabFragment.tabLayout.post(new Runnable() {
            @Override
            public void run() {
                TabFragment.tabLayout.setupWithViewPager(viewPager);

                for (int i = 0; i < numberOfTabs; i++) {
                    View one_tab = LayoutInflater.from(getActivity()).inflate(R.layout.one_tab_layout, null);
                    TabFragment.tabLayout.getTabAt(i).setCustomView(one_tab);
                    TextView textView = (TextView) one_tab.findViewById(R.id.tabText);
                    textView.setText(tab_text[i]);
                    ImageView imageView = (ImageView) one_tab.findViewById(R.id.tabImage);
                    imageView.setImageResource(tab_icons[i]);
                }

                if (extras != null) {
                    int index = extras.getInt("index");
                    viewPager.setCurrentItem(index);
                }
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Menu menulateral = mNavigationView.getMenu();
                switch (position) {
                    case 0:
                        menuItem = menulateral.findItem(R.id.nav_item_watching);
                        break;
                    case 1:
                        menuItem = menulateral.findItem(R.id.nav_item_search);
                        break;
                    case 2:
                        menuItem = menulateral.findItem(R.id.nav_item_graphs);
                        break;
                    case 3:
                        menuItem = menulateral.findItem(R.id.nav_item_voucher);
                        break;
                }
                lastMenuItem.setChecked(false);
                menuItem.setChecked(true);
                lastMenuItem = menuItem;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return tabLayout;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        //Return fragment with respect to Position
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new WatchingFragment();
                case 1:
                    return new SearchFragment();
                case 2:
                    return new GraphsFragment();
                case 3:
                    return new VoucherFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return numberOfTabs;
        }

        // This method returns the title of the tab according to the position.
        @Override
        public CharSequence getPageTitle(int position) {
//            switch (position) {
//                case 0:
//                    return "Watching";
//                case 1:
//                    return "Search";
//                case 2:
//                    return "Graphs";
//            }
            return null;
        }
    }

}
