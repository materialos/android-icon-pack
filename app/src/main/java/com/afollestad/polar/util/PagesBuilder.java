package com.afollestad.polar.util;

import android.app.Fragment;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PagesBuilder implements Iterable<PagesBuilder.Page> {

    private final ArrayList<Page> mPages;

    public PagesBuilder(int expectedSize) {
        mPages = new ArrayList<>(expectedSize);
    }

    public void add(@NonNull Page page) {
        mPages.add(page);
    }

    public Page get(int index) {
        return mPages.get(index);
    }

    public int size() {
        return mPages.size();
    }

    @Override
    public Iterator<Page> iterator() {
        return mPages.iterator();
    }

    public int findPositionForItem(MenuItem item) {
        synchronized (mPages) {
            for (int i = 0; i < size(); i++) {
                if (get(i).drawerId == item.getItemId()) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static class Page {

        @IdRes
        public final int drawerId;
        @DrawableRes
        public final int iconRes;
        @StringRes
        public final int titleRes;
        @NonNull
        public final Fragment fragment;

        public Page(@IdRes int drawerId, @DrawableRes int iconRes, @StringRes int titleRes, @NonNull Fragment fragment) {
            this.drawerId = drawerId;
            this.iconRes = iconRes;
            this.titleRes = titleRes;
            this.fragment = fragment;
        }

        public void addToMenu(Menu menu) {
            MenuItem item = menu.add(Menu.NONE, drawerId, Menu.NONE, titleRes);
            item.setIcon(iconRes);
            item.setCheckable(true);
        }
    }
}