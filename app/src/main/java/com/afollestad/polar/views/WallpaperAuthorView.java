package com.afollestad.polar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.afollestad.polar.util.WallpaperUtils;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperAuthorView extends TextView {

    public WallpaperAuthorView(Context context) {
        super(context);
    }

    public WallpaperAuthorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperAuthorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private WallpaperUtils.Wallpaper mWallpaper;

    public void setWallpaper(WallpaperUtils.Wallpaper viewHolder) {
        mWallpaper = viewHolder;
    }

    @Override
    public void setTextColor(int color) {
        setTextColor(color, true);
    }

    public void setTextColor(int color, boolean cache) {
        super.setTextColor(color);
        if (cache && mWallpaper != null)
            mWallpaper.setPaletteAuthorColor(color);
    }
}
