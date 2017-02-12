package com.afollestad.polar.util;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;

/**
 * @author Aidan Follestad (afollestad)
 */
public class VC {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static void init(Fragment context) {
        mContext = context.getActivity();
    }

    public static void destroy() {
        mContext = null;
    }

    @Nullable
    public static Drawable get(@DrawableRes int iconRes) {
        if (iconRes == 0) return null;
        if (mContext == null) return null;
        try {
            return VectorDrawableCompat.create(mContext.getResources(), iconRes, null);
        } catch (Throwable t) {
            try {
                return ContextCompat.getDrawable(mContext, iconRes);
            } catch (Throwable t2) {
                return null;
            }
        }
    }

    private VC() {
    }
}