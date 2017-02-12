package com.afollestad.polar.services;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.afollestad.polar.ui.MainActivity;
import com.afollestad.polar.util.WallpaperUtils;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;


/**
 * @author Aidan Follestad (afollestad)
 */
public class PolarWallpaperSource extends RemoteMuzeiArtSource {

    public PolarWallpaperSource() {
        super(PolarWallpaperSource.class.getSimpleName());
    }

    private static final int ROTATE_TIME_MILLIS = 3 * 60 * 60 * 1000; // rotate every 3 hours

    private void setActiveIndex(int index) {
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit().putInt("muzei_index", index).commit();
    }

    private int getActiveIndex() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getInt("muzei_index", -1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setUserCommands(BUILTIN_COMMAND_ID_NEXT_ARTWORK);
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        WallpaperUtils.WallpapersHolder wallpapers;
        try {
            wallpapers = WallpaperUtils.getAll(this, !WallpaperUtils.didExpire(this));
        } catch (Exception e) {
            Log.d(PolarWallpaperSource.class.getSimpleName(), String.format("Failed to retrieve wallpapers for Muzei... %s", e.getMessage()));
            throw new RetryException();
        }

        if (wallpapers == null || wallpapers.length() == 0) {
            Log.d(PolarWallpaperSource.class.getSimpleName(), "No wallpapers were found for Muzei.");
            throw new RetryException();
        }

        int currentActive = getActiveIndex() + 1;
        if (currentActive > wallpapers.length() - 1)
            currentActive = 0;
        setActiveIndex(currentActive);
        WallpaperUtils.Wallpaper currentWallpaper = wallpapers.get(currentActive);

        Log.d(PolarWallpaperSource.class.getSimpleName(), String.format("Publishing artwork to Muzei: %s", currentWallpaper.url));
        final Artwork currentArt = new Artwork.Builder()
                .imageUri(Uri.parse(currentWallpaper.url))
                .title(currentWallpaper.name)
                .byline(currentWallpaper.author)
                .viewIntent(new Intent(getApplicationContext(), MainActivity.class)
                        .setAction(Intent.ACTION_SET_WALLPAPER)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                .token(String.format("%s:%s", currentWallpaper.name, currentWallpaper.author))
                .build();

        publishArtwork(currentArt);
        scheduleUpdate(System.currentTimeMillis() + ROTATE_TIME_MILLIS);
    }
}