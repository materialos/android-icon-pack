package com.afollestad.polar.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.afollestad.assent.PermissionResultSet;
import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Callback;
import com.afollestad.bridge.Request;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.afollestad.bridge.annotations.Body;
import com.afollestad.bridge.annotations.ContentType;
import com.afollestad.inquiry.Inquiry;
import com.afollestad.inquiry.annotations.Column;
import com.afollestad.inquiry.callbacks.RunCallback;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.fragments.WallpapersFragment;

import java.io.File;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpaperUtils {

    public static final String TABLE_NAME = "polar_wallpapers";
    public static final String DATABASE_NAME = "data_cache";
    public static final int DATABASE_VERSION = 1;

    public interface WallpapersCallback {
        void onRetrievedWallpapers(WallpapersHolder wallpapers, Exception error, boolean cancelled);
    }

    @ContentType("application/json")
    public static class WallpapersHolder implements Serializable {

        public Wallpaper get(int index) {
            return wallpapers[index];
        }

        public int length() {
            return wallpapers != null ? wallpapers.length : 0;
        }

        @Body
        public Wallpaper[] wallpapers;

        public WallpapersHolder() {
        }

        public WallpapersHolder(Wallpaper[] wallpapers) {
            this.wallpapers = wallpapers;
        }
    }

    @ContentType("application/json")
    public static class Wallpaper implements Serializable {

        public Wallpaper() {
        }

        @Column(primaryKey = true, notNull = true, autoIncrement = true)
        public long _id;
        @Body
        @Column
        public String author;
        @Body
        @Column
        public String url;
        @Body
        @Column
        public String name;
        @Body
        @Column
        public String thumbnail;

        public String getListingImageUrl() {
            return thumbnail != null ? thumbnail : url;
        }

        @Column
        private int paletteNameColor;
        @Column
        private int paletteAuthorColor;
        @Column
        private int paletteBgColor;

        public void setPaletteNameColor(@ColorInt int color) {
            this.paletteNameColor = color;
        }

        @ColorInt
        public int getPaletteNameColor() {
            return paletteNameColor;
        }

        public void setPaletteAuthorColor(@ColorInt int color) {
            this.paletteAuthorColor = color;
        }

        @ColorInt
        public int getPaletteAuthorColor() {
            return paletteAuthorColor;
        }

        public void setPaletteBgColor(@ColorInt int color) {
            this.paletteBgColor = color;
        }

        @ColorInt
        public int getPaletteBgColor() {
            return paletteBgColor;
        }

        public boolean isPaletteComplete() {
            return paletteNameColor != 0 && paletteAuthorColor != 0 && paletteBgColor != 0;
        }
    }

    @SuppressLint("CommitPrefEdits")
    public static boolean didExpire(Context context) {
        final long NOW = System.currentTimeMillis();
        final String UPDATE_TIME_KEY = "wallpaper_last_update_time";
        final String LAST_UPDATE_VERSION_KEY = "wallpaper_last_update_version";
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final long LAST_UPDATE = prefs.getLong(UPDATE_TIME_KEY, -1);
        final long INTERVAL = 24 * 60 * 60 * 1000; // every 24 hours

        if (LAST_UPDATE == -1 || NOW >= (LAST_UPDATE + INTERVAL)) {
            Log.d("WallpaperUtils", "Cache invalid: never updated, or it's been 24 hours since last update.");
            // Never updated before, or it's been at least 24 hours
            prefs.edit().putLong(UPDATE_TIME_KEY, NOW).commit();
            return true;
        } else if (prefs.getInt(LAST_UPDATE_VERSION_KEY, -1) != BuildConfig.VERSION_CODE) {
            Log.d("WallpaperUtils", "App was updated, wallpapers cache is invalid.");
            prefs.edit().putInt(LAST_UPDATE_VERSION_KEY, BuildConfig.VERSION_CODE).commit();
            return true;
        }

        Log.d("WallpaperUtils", "Cache is still valid.");
        return false;
    }

    public static WallpapersHolder getAll(final Context context, boolean allowCached) throws Exception {
        final String iname = "get_walldb_instance";
        Inquiry.newInstance(context, DATABASE_NAME)
                .databaseVersion(DATABASE_VERSION)
                .instanceName(iname)
                .build();
        try {
            if (allowCached) {
                Wallpaper[] cache = Inquiry.get(iname).selectFrom(TABLE_NAME, Wallpaper.class).all();
                if (cache != null && cache.length > 0) {
                    Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from cache.", cache.length));
                    return new WallpapersHolder(cache);
                }
            } else {
                Inquiry.get(iname).deleteFrom(TABLE_NAME, Wallpaper.class).run();
            }
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        try {
            WallpapersHolder holder = Bridge.get(context.getString(R.string.wallpapers_json_url))
                    .tag(WallpapersFragment.class.getName())
                    .asClass(WallpapersHolder.class);
            if (holder == null)
                throw new Exception("No wallpapers returned.");
            Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from web.", holder.length()));
            if (holder.length() > 0) {
                try {
                    Inquiry.get(iname).insertInto(TABLE_NAME, Wallpaper.class)
                            .values(holder.wallpapers)
                            .run();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return holder;
        } catch (Exception e1) {
            Log.d("WallpaperUtils", String.format("Failed to load wallpapers... %s", e1.getMessage()));
            throw e1;
        } finally {
            Inquiry.destroy(iname);
        }
    }

    public static void saveDb(@Nullable final Context context, @Nullable final WallpapersHolder holder) {
        if (context == null || holder == null || holder.length() == 0) return;
        final String iname = "save_walldb_instance";
        Inquiry.newInstance(context, DATABASE_NAME)
                .databaseVersion(DATABASE_VERSION)
                .instanceName(iname)
                .build();
        try {
            Inquiry.get(iname).deleteFrom(TABLE_NAME, Wallpaper.class).run();
            Inquiry.get(iname).insertInto(TABLE_NAME, Wallpaper.class)
                    .values(holder.wallpapers)
                    .run(new RunCallback<Long[]>() {
                        @Override
                        public void result(Long[] changed) {
                            Inquiry.destroy(iname);
                        }
                    });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void getAll(final Context context, boolean allowCached, final WallpapersCallback callback) {
        final String iname = "save_walldb_instance2";
        Inquiry.newInstance(context, DATABASE_NAME)
                .databaseVersion(DATABASE_VERSION)
                .instanceName(iname)
                .build();
        try {
            if (allowCached) {
                Wallpaper[] cache = Inquiry.get(iname).selectFrom(TABLE_NAME, Wallpaper.class).all();
                if (cache != null && cache.length > 0) {
                    Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from cache.", cache.length));
                    callback.onRetrievedWallpapers(new WallpapersHolder(cache), null, false);
                    return;
                }
            } else {
                Inquiry.get(iname).deleteFrom(TABLE_NAME, Wallpaper.class).run();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Bridge.get(context.getString(R.string.wallpapers_json_url))
                .tag(WallpapersFragment.class.getName())
                .asClass(WallpapersHolder.class, new ResponseConvertCallback<WallpapersHolder>() {
                    @Override
                    public void onResponse(@NonNull Response response, @Nullable WallpapersHolder holder, @Nullable BridgeException e) {
                        if (e != null) {
                            callback.onRetrievedWallpapers(null, e, e.reason() == BridgeException.REASON_REQUEST_CANCELLED);
                        } else {
                            if (holder == null) {
                                callback.onRetrievedWallpapers(null, new Exception("No wallpapers returned."), false);
                                return;
                            }
                            try {
                                for (Wallpaper wallpaper : holder.wallpapers) {
                                    if (wallpaper.name == null)
                                        wallpaper.name = "";
                                    if (wallpaper.author == null)
                                        wallpaper.author = "";
                                }

                                Log.d("WallpaperUtils", String.format("Loaded %d wallpapers from web.", holder.length()));
                                if (holder.length() > 0) {
                                    try {
                                        Inquiry.get(iname).insertInto(TABLE_NAME, Wallpaper.class)
                                                .values(holder.wallpapers)
                                                .run();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }
                                }
                                callback.onRetrievedWallpapers(holder, null, false);
                            } catch (Throwable e1) {
                                Log.d("WallpaperUtils", String.format("Failed to load wallpapers... %s", e1.getMessage()));
                                if (e1 instanceof Exception)
                                    callback.onRetrievedWallpapers(null, (Exception) e1, false);
                            } finally {
                                Inquiry.destroy(iname);
                            }
                        }
                    }
                });
    }

    private static Activity mContextCache;
    private static Wallpaper mWallpaperCache;
    private static boolean mApplyCache;
    private static File mFileCache;
    private static Toast mToast;

    private static void showToast(Context context, @StringRes int msg) {
        showToast(context, context.getString(msg));
    }

    private static void showToast(Context context, String msg) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void download(final Activity context, final Wallpaper wallpaper, final boolean apply) {
        mContextCache = context;
        mWallpaperCache = wallpaper;
        mApplyCache = apply;

        if (!Assent.isPermissionGranted(Assent.WRITE_EXTERNAL_STORAGE) && !apply) {
            Assent.requestPermissions(new AssentCallback() {
                @Override
                public void onPermissionResult(PermissionResultSet permissionResultSet) {
                    if (permissionResultSet.isGranted(Assent.WRITE_EXTERNAL_STORAGE))
                        download(mContextCache, mWallpaperCache, mApplyCache);
                    else
                        Toast.makeText(context, R.string.write_storage_permission_denied, Toast.LENGTH_LONG).show();
                }
            }, 69, Assent.WRITE_EXTERNAL_STORAGE);
            return;
        }

        File saveFolder;
        final String name;
        final String extension = wallpaper.url.toLowerCase(Locale.getDefault()).endsWith(".png") ? "png" : "jpg";

        if (apply) {
            // Crop/Apply
            saveFolder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? context.getCacheDir() : context.getExternalCacheDir();
            name = String.format("%s_%s_wallpaper.%s",
                    wallpaper.name.replace(" ", "_"),
                    wallpaper.author.replace(" ", "_"),
                    extension);
        } else {
            // Save
            saveFolder = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.app_name));
            name = String.format("%s_%s.%s",
                    wallpaper.name.replace(" ", "_"),
                    wallpaper.author.replace(" ", "_"),
                    extension);
        }

        //noinspection ResultOfMethodCallIgnored
        saveFolder.mkdirs();
        mFileCache = new File(saveFolder, name);

        if (!mFileCache.exists()) {
            final MaterialDialog dialog = new MaterialDialog.Builder(context)
                    .content(R.string.downloading_wallpaper)
                    .progress(true, -1)
                    .cancelable(true)
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            if (mContextCache != null && !mContextCache.isFinishing())
                                showToast(mContextCache, R.string.download_cancelled);
                            Bridge.cancelAll()
                                    .tag(WallpaperUtils.class.getName())
                                    .commit();
                        }
                    }).show();
            Bridge.get(wallpaper.url)
                    .tag(WallpaperUtils.class.getName())
                    .request(new Callback() {
                        @Override
                        public void response(Request request, Response response, BridgeException e) {
                            if (e != null) {
                                dialog.dismiss();
                                if (e.reason() == BridgeException.REASON_REQUEST_CANCELLED) return;
                                Utils.showError(context, e);
                            } else {
                                try {
                                    response.asFile(mFileCache);
                                    finishOption(mContextCache, apply, dialog);
                                } catch (BridgeException e1) {
                                    dialog.dismiss();
                                    Utils.showError(context, e1);
                                }
                            }
                        }
                    });
        } else {
            finishOption(context, apply, null);
        }
    }

    private static void finishOption(final Activity context, boolean apply, @Nullable final MaterialDialog dialog) {
        if (!apply) {
            MediaScannerConnection.scanFile(context,
                    new String[]{mFileCache.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("WallpaperScan", "Scanned " + path + ":");
                            Log.i("WallpaperScan", "-> uri = " + uri);
                        }
                    });
        }

        if (apply) {
            // Apply
            if (dialog != null)
                dialog.dismiss();
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context,
                        BuildConfig.APPLICATION_ID + ".fileProvider",
                        mFileCache);
            } else {
                uri = Uri.fromFile(mFileCache);
            }
            final Intent intent = new Intent(Intent.ACTION_ATTACH_DATA)
                    .setDataAndType(uri, "image/*")
                    .putExtra("mimeType", "image/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.set_wallpaper_using)));
        } else {
            // Save
            if (dialog != null)
                dialog.dismiss();
            showToast(context, context.getString(R.string.saved_to_x, mFileCache.getAbsolutePath()));
            resetOptionCache(false);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void resetOptionCache(boolean delete) {
        mContextCache = null;
        mWallpaperCache = null;
        mApplyCache = false;
        if (delete && mFileCache != null) {
            mFileCache.delete();
            final File[] contents = mFileCache.getParentFile().listFiles();
            if (contents != null && contents.length > 0)
                mFileCache.getParentFile().delete();
        }
    }

    private WallpaperUtils() {
    }
}