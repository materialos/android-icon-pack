package com.jahirfiquitiva.paperboard.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String
            PREFERENCES_NAME = "DASHBOARD_PREFERENCES",
            ENABLE_FEATURES = "enable_features",
            FIRSTRUN = "firstrun";

    private static final String
            ROTATE_MINUTE = "rotate_time_minute",
            ROTATE_TIME = "muzei_rotate_time";

    private final Context context;

    public Preferences(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        return getSharedPreferences().getBoolean(FIRSTRUN, true);
    }

    public boolean isFeaturesEnabled() {
        return getSharedPreferences().getBoolean(ENABLE_FEATURES, true);
    }

    public boolean isRotateMinute() {
        return getSharedPreferences().getBoolean(ROTATE_MINUTE, false);
    }

    public int getRotateTime() {
        return getSharedPreferences().getInt(ROTATE_TIME, 900000);
    }

    public void setFeaturesEnabled(boolean bool) {
        getSharedPreferences().edit().putBoolean(ENABLE_FEATURES, bool).apply();
    }

    public void setNotFirstrun() {
        getSharedPreferences().edit().putBoolean(FIRSTRUN, false).apply();
    }

    public void setRotateTime(int time) {
        getSharedPreferences().edit().putInt(ROTATE_TIME, time).apply();
    }

    public void setRotateMinute(boolean bool) {
        getSharedPreferences().edit().putBoolean(ROTATE_MINUTE, bool).apply();
    }
}