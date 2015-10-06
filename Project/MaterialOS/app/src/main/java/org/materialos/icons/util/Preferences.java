package org.materialos.icons.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String
            PREFERENCES_NAME = "DASHBOARD_PREFERENCES",
            PREF_INIT_SETUP = "firstrun",
            PREF_SAVED_VERSION = "pref_saved_version";

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

    public int getSavedVersion() {
        return getSharedPreferences().getInt(PREF_SAVED_VERSION, 0);
    }

    public boolean isFirstRun() {
        return getSharedPreferences().getBoolean(PREF_INIT_SETUP, true);
    }

    public boolean isRotateMinute() {
        return getSharedPreferences().getBoolean(ROTATE_MINUTE, false);
    }

    public void setRotateMinute(boolean bool) {
        getSharedPreferences().edit().putBoolean(ROTATE_MINUTE, bool).apply();
    }

    public int getRotateTime() {
        return getSharedPreferences().getInt(ROTATE_TIME, 900000);
    }

    public void setRotateTime(int time) {
        getSharedPreferences().edit().putInt(ROTATE_TIME, time).apply();
    }

    public void setNotFirstrun() {
        getSharedPreferences().edit().putBoolean(PREF_INIT_SETUP, false).apply();
    }

    public void saveVersion() {
        getSharedPreferences().edit().putInt(PREF_SAVED_VERSION, Util.getAppVersionCode(context)).apply();
    }
}