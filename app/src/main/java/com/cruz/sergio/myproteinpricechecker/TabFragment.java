package com.cruz.sergio.myproteinpricechecker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.cruz.sergio.myproteinpricechecker.MainActivity.GETNEWS_ONSTART;
import static com.cruz.sergio.myproteinpricechecker.MainActivity.TheMenuItem.lastMenuItem;

public class TabFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    Bundle extras;
    MenuItem menuItem;
    int[] tab_icons;
    int[] tab_icons_selected;
    String[] tab_text;
    int numberOfTabs;
    int lastPosition = 0;
    List<TextView> tab_textViews;
    List<ImageView> tab_imageViews;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        extras = getArguments();
        tab_text = new String[]{
                getString(R.string.tab_news),
                getString(R.string.tab_text_watching),
                getString(R.string.tab_text_search),
                getString(R.string.tab_text_graph),
                getString(R.string.tab_text_voucher)
        };
        tab_icons = new int[]{
                R.drawable.ic_news_statelist,
                R.drawable.ic_view_statelist,
                R.drawable.ic_search_statelist,
                R.drawable.ic_graph_statelist,
                R.drawable.ic_voucher_statelist
        };

        tab_icons_selected = new int[]{
                R.drawable.ic_news_pressed,
                R.mipmap.ic_menu_view_pressed,
                R.drawable.ic_search_selected2,
                R.mipmap.ic_graph_selected,
                R.mipmap.ic_voucher_selected
        };

        numberOfTabs = tab_text.length;
        tab_textViews = new ArrayList<>(numberOfTabs);
        tab_imageViews = new ArrayList<>(numberOfTabs);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /*** Inflate tab_layout and setup Views. */
        View view = inflater.inflate(R.layout.tab_layout, null);
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewpager);

        /*** Set an Adapter for the View Pager */
        viewPager.setAdapter(new MyTabAdapter(getChildFragmentManager()));
        //Manter o conteúdo das tabs ao passar de uma para a outra
        viewPager.setOffscreenPageLimit(numberOfTabs);

        /**
         * This is a workaround,
         * The setupWithViewPager dose't works without the runnable.
         * Maybe a Support Library Bug.
         */
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);

                Context context = getActivity().getApplicationContext();

                for (int i = 0; i < numberOfTabs; i++) {
                    View one_tab = LayoutInflater.from(context).inflate(R.layout.one_tab_layout, null);
                    tabLayout.getTabAt(i).setCustomView(one_tab);
                    TextView textView = one_tab.findViewById(R.id.tabText);
                    textView.setText(tab_text[i]);
                    tab_textViews.add(textView);

                    ImageView imageView = one_tab.findViewById(R.id.tabImage);
                    imageView.setImageResource(tab_icons[i]);
                    tab_imageViews.add(imageView);

                }
                tab_textViews.get(0).setTextColor(ContextCompat.getColor(getActivity(), R.color.orange));
                tab_textViews.get(0).setTypeface(Typeface.DEFAULT_BOLD);
                tab_imageViews.get(0).setImageResource(tab_icons_selected[0]);

                if (extras != null) {
                    int index = extras.getInt("index");
                    viewPager.setCurrentItem(index);
                }

                if (!GETNEWS_ONSTART) {
                    TabLayout.Tab tab = tabLayout.getTabAt(MainActivity.TABS.WATCHING);
                    tabLayout.setScrollPosition(MainActivity.TABS.WATCHING, 0f, true);
                    if (tab != null) tab.select();
                }

            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Menu menulateral = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu();
                switch (position) {
                    case 0:
                        menuItem = menulateral.findItem(R.id.nav_item_news);
                        break;
                    case 1:
                        menuItem = menulateral.findItem(R.id.nav_item_watching);
                        break;
                    case 2:
                        menuItem = menulateral.findItem(R.id.nav_item_search);
                        break;
                    case 3:
                        menuItem = menulateral.findItem(R.id.nav_item_graphs);
                        break;
                    case 4:
                        menuItem = menulateral.findItem(R.id.nav_item_voucher);
                        break;
                }

                tab_textViews.get(lastPosition).setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                tab_textViews.get(lastPosition).setTypeface(Typeface.DEFAULT);
                tab_imageViews.get(lastPosition).setImageResource(tab_icons[lastPosition]);
                tab_textViews.get(position).setTextColor(ContextCompat.getColor(getActivity(), R.color.orange));
                tab_textViews.get(position).setTypeface(Typeface.DEFAULT_BOLD);
                tab_imageViews.get(position).setImageResource(tab_icons_selected[position]);
                lastPosition = position;
                lastMenuItem.setChecked(false);
                menuItem.setChecked(true);
                lastMenuItem = menuItem;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        return view;
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class MyTabAdapter extends FragmentPagerAdapter {

        MyTabAdapter(FragmentManager fm) {
            super(fm);
        }

        //Return fragment with respect to Position
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new NewsFragment();
                case 1:
                    return new WatchingFragment();
                case 2:
                    return new SearchFragment();
                case 3:
                    return new GraphsFragment();
                case 4:
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
