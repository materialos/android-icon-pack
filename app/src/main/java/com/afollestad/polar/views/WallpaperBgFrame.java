package com.afollestad.polar.views;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.polar.util.WallpaperUtils;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperBgFrame extends LinearLayout {

    public WallpaperBgFrame(Context context) {
        super(context);
    }

    public WallpaperBgFrame(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperBgFrame(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private WallpaperUtils.Wallpaper mWallpaper;

    public void setWallpaper(WallpaperUtils.Wallpaper wallpaper) {
        mWallpaper = wallpaper;
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        setBackgroundColor(color, true);
    }

    public void setBackgroundColor(@ColorInt int color, boolean cache) {
        super.setBackgroundColor(color);
        ViewGroup parent = (ViewGroup) getParent();
        CardView card = (CardView) parent.getParent();
        card.setCardBackgroundColor(color);
        if (cache && mWallpaper != null)
            mWallpaper.setPaletteBgColor(color);
    }
}
