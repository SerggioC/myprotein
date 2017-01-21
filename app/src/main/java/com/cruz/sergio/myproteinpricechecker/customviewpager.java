package com.cruz.sergio.myproteinpricechecker;

import android.content.Context;
import android.support.v4.view.ViewPager;

import java.util.Stack;

/*****
 * Project MyProteinPriceChecker
 * Package com.cruz.sergio.myproteinpricechecker
 * Created by Sergio on 16/01/2017 04:16
 ******/

public class customviewpager extends ViewPager {

    private static Stack<Integer> stack = new Stack<>();

    public customviewpager(Context context) {
        super(context);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        stack.push(getCurrentItem());
        super.setCurrentItem(item, smoothScroll);
    }

    public int popFromBackStack(boolean smoothScroll) {
        if (stack.size() > 0) {
            super.setCurrentItem(stack.pop(), smoothScroll);
            return getCurrentItem();
        } else return -1;
    }
}