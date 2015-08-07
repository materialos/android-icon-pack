package com.jahirfiquitiva.paperboard.utilities;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class PaletteTransformation implements Transformation {

    private static final PaletteTransformation INSTANCE = new PaletteTransformation();
    private static final Map<Bitmap, Palette> CACHE = new WeakHashMap<>();

    private static Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    public static abstract class PaletteCallback implements Callback {

        private final WeakReference<ImageView> mImageView;

        public PaletteCallback(@NonNull ImageView imageView) {
            mImageView = new WeakReference<>(imageView);
        }

        protected abstract void onSuccess(Palette palette);

        @Override
        public final void onSuccess() {
            if (getImageView() == null)
                return;
            final Bitmap bitmap = ((BitmapDrawable) getImageView().getDrawable()).getBitmap();
            final Palette palette = getPalette(bitmap);
            onSuccess(palette);

        }

        private ImageView getImageView() {
            return mImageView.get();
        }

    }

    public static PaletteTransformation instance() {
        return INSTANCE;
    }

    @Override
    public final Bitmap transform(Bitmap source) {
        final Palette palette = new Palette.Builder(source).generate();
        CACHE.put(source, palette);
        return source;
    }

    @Override
    public String key() {
        return "";
    }

    private PaletteTransformation() {
    }
}