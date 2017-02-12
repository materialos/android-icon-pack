package com.afollestad.polar.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class Utils {

    public static void showError(Context context, Exception e) {
        e.printStackTrace();
        new MaterialDialog.Builder(context)
                .title(R.string.error)
                .content(e.getMessage())
                .positiveText(android.R.string.ok)
                .show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static int wipe(File dir) {
        if (!dir.exists()) return 0;
        int count = 1;
        if (dir.isDirectory()) {
            File[] contents = dir.listFiles();
            if (contents != null && contents.length > 0) {
                for (File fi : contents)
                    count += wipe(fi);
            }
        }
        dir.delete();
        return count;
    }

    public static boolean isPkgInstalled(@NonNull Context context, @NonNull String targetPackage) {
        final PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            installed = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA) != null;
        } catch (Throwable ignored) {
        }
        return installed;
    }

//    public static int getStatusBarHeight(Context context) {
//        final Resources r = context.getResources();
//        int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0)
//            return r.getDimensionPixelSize(resourceId);
//        return 0;
//    }
//
//    public static int getNavBarHeight(Activity context) {
//        if (context == null || context.isFinishing()) {
//            return 0;
//        } else if (!context.getResources().getBoolean(R.bool.translucent_nav)) {
//            // Translucent nav is disabled
//            return 0;
//        }
//        final Resources r = context.getResources();
//        int resourceId = r.getIdentifier("navigation_bar_height", "dimen", "android");
//        if (resourceId > 0)
//            return r.getDimensionPixelSize(resourceId);
//        return 0;
//    }
//
//    public static int[] resolveResourceIds(@NonNull Context context, @ArrayRes int integerArray) {
//        TypedArray ar = context.getResources().obtainTypedArray(integerArray);
//        int len = ar.length();
//        int[] resIds = new int[len];
//        for (int i = 0; i < len; i++)
//            resIds[i] = ar.getResourceId(i, 0);
//        ar.recycle();
//        return resIds;
//    }
//
//    @Size(2)
//    public static int[] getScreenDimensions(Activity activity) {
//        final Display display = activity.getWindowManager().getDefaultDisplay();
//        final Point size = new Point();
//        display.getSize(size);
//        return new int[]{size.x, size.y};
//    }

    public static <VT extends View> void waitForLayout(@NonNull final VT view, @NonNull final LayoutCallback<VT> cb) {
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    cb.onLayout(view);
                }
            });
        }
    }

    public static void setOverflowButtonColor(@NonNull Activity activity, final @ColorInt int color) {
        @SuppressLint("PrivateResource")
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) return;
                final AppCompatImageView overflow = (AppCompatImageView) outViews.get(0);
                overflow.setImageDrawable(TintUtils.createTintedDrawable(overflow.getDrawable(), color));
                removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    public static Drawable createCardSelector(Context context) {
        final int accentColor = DialogUtils.resolveColor(context, R.attr.colorAccent);
        final boolean darkTheme = Config.get().darkTheme();
        final int activated = TintUtils.adjustAlpha(accentColor, darkTheme ? 0.5f : 0.3f);
        final int pressed = TintUtils.adjustAlpha(accentColor, darkTheme ? 0.75f : 0.6f);

        final StateListDrawable baseSelector = new StateListDrawable();
        baseSelector.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(activated));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(accentColor),
                    baseSelector, new ColorDrawable(Color.WHITE));
        }

        baseSelector.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        baseSelector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressed));
        return baseSelector;
    }

//    public static void recycleQuietely(@Nullable Bitmap bitmap) {
//        if (bitmap == null || bitmap.isRecycled()) return;
//        bitmap.recycle();
//    }

    public static void copy(InputStream is, OutputStream os) throws Exception {
        byte[] buffer = new byte[2048];
        int read;
        while ((read = is.read(buffer)) != -1)
            os.write(buffer, 0, read);
        os.flush();
    }

    public static String removeExtension(String name) {
        if (name.startsWith(".")) return name;
        int dot = name.lastIndexOf('.');
        if (dot == -1) return name;
        return name.substring(0, dot);
    }

    public interface LayoutCallback<VT extends View> {
        void onLayout(VT view);
    }
}