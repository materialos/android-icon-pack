package com.afollestad.polar.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.FitCenter;

public class KeepRatio extends FitCenter {

    public KeepRatio(Context context) {
        super(context);
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() > toTransform.getHeight()) {
            outWidth = Math.round(((float) toTransform.getHeight() / outHeight) * toTransform.getWidth());
        } else {
            outHeight = Math.round(((float) toTransform.getWidth() / outWidth) * toTransform.getHeight());
        }

        return super.transform(pool, toTransform, outWidth, outHeight);
    }

    @Override
    public String getId() {
        return "Octopus";
    }
}
