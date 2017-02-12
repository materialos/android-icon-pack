package com.afollestad.polar.zooper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.FileNotFoundException;

public class TemplateProvider extends ContentProvider {

    public int delete(@NonNull Uri paramUri, String paramString, String[] paramArrayOfString) {
        return 0;
    }

    public String getType(@NonNull Uri paramUri) {
        return null;
    }

    public Uri insert(@NonNull Uri paramUri, ContentValues paramContentValues) {
        return null;
    }

    public boolean onCreate() {
        return false;
    }

    public AssetFileDescriptor openAssetFile(@NonNull Uri paramUri, @NonNull String paramString)
            throws FileNotFoundException {
        if (paramUri.getPathSegments().size() > 0)
            try {
                if (getContext() == null) return null;
                final String name = paramUri.getPath().substring(1);
                return getContext().getAssets().openFd(name);
            } catch (Throwable localThrowable) {
                return null;
            }
        return null;
    }

    public Cursor query(@NonNull Uri paramUri, String[] paramArrayOfString1, String paramString1, String[] paramArrayOfString2, String paramString2) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"string"});
        try {
            if (getContext() == null) return cursor;
            final String path = paramUri.getPath().substring(1);
            final String[] items = getContext().getAssets().list(path);
            for (String s : items) {
                cursor.newRow().add(s);
                cursor.moveToNext();
            }
            cursor.moveToFirst();
        } catch (Exception e) {
            cursor.close();
            throw new RuntimeException(e);
        }
        return cursor;
    }

    public int update(@NonNull Uri paramUri, ContentValues paramContentValues, String paramString, String[] paramArrayOfString) {
        return 0;
    }
}
