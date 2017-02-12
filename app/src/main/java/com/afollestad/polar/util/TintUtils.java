package com.afollestad.polar.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class TintUtils {

    public static Drawable createTintedDrawable(@NonNull Context context, @DrawableRes int drawable, @ColorInt int color) {
        return createTintedDrawable(ContextCompat.getDrawable(context, drawable), color);
    }

    @CheckResult
    @Nullable
    public static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable == null) return null;
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && drawable instanceof VectorDrawable) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            return drawable;
        }
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    private static void tintImageView(Object target, Field field, int tintColor) throws Exception {
        field.setAccessible(true);
        final ImageView imageView = (ImageView) field.get(target);
        if (imageView == null) return;
        if (imageView.getDrawable() != null)
            imageView.setImageDrawable(createTintedDrawable(imageView.getDrawable(), tintColor));
    }

    public static void themeSearchView(@NonNull Toolbar toolbar, @NonNull SearchView searchView, @ColorInt int tintColor) {
        final Class<?> cls = searchView.getClass();
        try {
            final Field mCollapseIconField = toolbar.getClass().getDeclaredField("mCollapseIcon");
            mCollapseIconField.setAccessible(true);
            final Drawable drawable = (Drawable) mCollapseIconField.get(toolbar);
            if (drawable != null)
                mCollapseIconField.set(toolbar, createTintedDrawable(drawable, tintColor));

            final Field mSearchSrcTextViewField = cls.getDeclaredField("mSearchSrcTextView");
            mSearchSrcTextViewField.setAccessible(true);
            final EditText mSearchSrcTextView = (EditText) mSearchSrcTextViewField.get(searchView);
            mSearchSrcTextView.setTextColor(tintColor);
            mSearchSrcTextView.setHintTextColor(adjustAlpha(tintColor, 0.5f));
            setCursorTint(mSearchSrcTextView, tintColor);

            Field field = cls.getDeclaredField("mSearchButton");
            tintImageView(searchView, field, tintColor);
            field = cls.getDeclaredField("mGoButton");
            tintImageView(searchView, field, tintColor);
            field = cls.getDeclaredField("mCloseButton");
            tintImageView(searchView, field, tintColor);
            field = cls.getDeclaredField("mVoiceButton");
            tintImageView(searchView, field, tintColor);
            field = cls.getDeclaredField("mCollapsedIcon");
            tintImageView(searchView, field, tintColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isColorLight(@ColorInt int color) {
        if (color == Color.BLACK) return false;
        else if (color == Color.WHITE || color == Color.TRANSPARENT) return true;
        final double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.4;
    }

    public static int adjustAlpha(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static void setCursorTint(@NonNull EditText editText, @ColorInt int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0] = createTintedDrawable(drawables[0], color);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = createTintedDrawable(drawables[1], color);
            fCursorDrawable.set(editor, drawables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @ColorInt
    public static int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        color = Color.HSVToColor(hsv);
        return color;
    }

    private TintUtils() {
    }
}
