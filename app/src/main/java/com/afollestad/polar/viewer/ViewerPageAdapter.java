package com.afollestad.polar.viewer;

import android.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.polar.util.WallpaperUtils;


/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewerPageAdapter extends FragmentStatePagerAdapter {

    private final int thumbHeight;
    private final int thumbWidth;
    public int mCurrentPage;
    private final WallpaperUtils.WallpapersHolder mWallpapers;

    public ViewerPageAdapter(AppCompatActivity context, int initialOffset, WallpaperUtils.WallpapersHolder wallpapers, int thumbWidth, int thumbHeight) {
        super(context.getFragmentManager());
        mCurrentPage = initialOffset;
        mWallpapers = wallpapers;
        this.thumbWidth = thumbWidth;
        this.thumbHeight = thumbHeight;
    }

    @Override
    public Fragment getItem(int position) {
        return ViewerPageFragment.create(mWallpapers.get(position), position, thumbWidth, thumbHeight)
                .setIsActive(mCurrentPage == position);
    }

    @Override
    public int getCount() {
        return mWallpapers.length();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}