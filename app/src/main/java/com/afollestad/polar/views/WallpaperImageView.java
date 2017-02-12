package com.afollestad.polar.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperImageView extends SquareImageView {

    public WallpaperImageView(Context context) {
        super(context);
    }

    public WallpaperImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WallpaperImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WallpaperImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Nullable
    private ProgressBar mProgressBar;

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mProgressBar != null)
            mProgressBar.setVisibility(View.GONE);
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }
}