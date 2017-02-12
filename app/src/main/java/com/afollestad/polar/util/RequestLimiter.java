package com.afollestad.polar.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressLint("CommitPrefEdits")
public final class RequestLimiter {

    private final static String UPDATE_TIME = "[prl-ut]";
    private final static String SENT_COUNT = "[prl-sc]";

    private final int mLimitInterval;
    private final SharedPreferences mPrefs;

    private RequestLimiter(int limitInterval, SharedPreferences prefs) {
        mLimitInterval = limitInterval;
        mPrefs = prefs;
    }

    public static RequestLimiter get(Context context) {
        return new RequestLimiter(
                context.getResources().getInteger(R.integer.icon_request_limit_interval),
                context.getSharedPreferences("[prl]", Context.MODE_PRIVATE));
    }

    public static long sToMs(long s) {
        return s * 1000;
    }

    public static long msToS(long ms) {
        return ms / 1000;
    }

    public static void log(@NonNull String msg, @Nullable Object... args) {
        if (!BuildConfig.DEBUG) return;
        if (args != null)
            msg = String.format(Locale.getDefault(), msg, args);
        Log.d("PolarRequestLimiter", msg);
    }

    public long intervalMs() {
        return sToMs(mLimitInterval);
    }

    public void update(int sentCount) {
        if (mLimitInterval <= 0) return;
        final int newSentCount = mPrefs.getInt(SENT_COUNT, 0) + sentCount;
        log("Updating sent count to %d.", newSentCount);
        SharedPreferences.Editor editor = mPrefs.edit()
                .putInt(SENT_COUNT, newSentCount);
        if (newSentCount == sentCount) {
            log("First request in the current interval, setting update time to now.");
            editor.putLong(UPDATE_TIME, System.currentTimeMillis());
        }
        editor.commit();
    }

    public int allow(int allowedCount) {
        if (mLimitInterval <= 0) {
            // No limit interval set, disable limiting.
            return NO_LIMIT;
        }
        final long lastUpdate = mPrefs.getLong(UPDATE_TIME, -1);
        if (lastUpdate == -1) {
            // No previously recorded update time, first time sending a request.
            return allowedCount;
        }
        final long now = System.currentTimeMillis();
        final long nextAllowedTime = lastUpdate + sToMs(mLimitInterval);
        boolean pastInterval = now >= nextAllowedTime;
        if (pastInterval) {
            log("Past the previous interval! Resetting update time and sent count.");
            mPrefs.edit().remove(UPDATE_TIME).remove(SENT_COUNT).commit();
            return allowedCount;
        } else {
            final int sentCount = mPrefs.getInt(SENT_COUNT, 0);
            if (sentCount < allowedCount) {
                log("The sent count (%d) hasn't reached the allowed count (%d) yet. Icon request is allowed!",
                        sentCount, allowedCount);
                return allowedCount - sentCount;
            } else {
                log("An icon request is not allowed... wait %d more seconds.",
                        msToS(nextAllowedTime - now));
                return WAIT;
            }
        }
    }

    public static int NO_LIMIT = -1;
    public static int WAIT = -2;

    private final static long MS_IN_SECOND = 1000;
    private final static long MS_IN_MINUTE = MS_IN_SECOND * 60;
    private final static long MS_IN_HOUR = MS_IN_MINUTE * 60;
    private final static long MS_IN_DAY = MS_IN_HOUR * 24;
    private final static long MS_IN_WEEK = MS_IN_DAY * 7;
    private final static long MS_IN_MONTH = MS_IN_WEEK * 4;
    private final static long MS_IN_YEAR = MS_IN_MONTH * 12;

    @SuppressLint("DefaultLocale")
    public String remainingIntervalString() {
        final long lastUpdate = mPrefs.getLong(UPDATE_TIME, -1);
        final long now = System.currentTimeMillis();
        final long nextAllowedTime = lastUpdate + sToMs(mLimitInterval);
        final long diff = nextAllowedTime - now;

        String unit;
        long value;

        if (diff < MS_IN_MINUTE) {
            // Less than a minute, unit in seconds
            value = diff / MS_IN_SECOND;
            unit = "second";
        } else if (diff < MS_IN_HOUR) {
            // Less than an hour, unit in minute and seconds
            return String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(diff),
                    TimeUnit.MILLISECONDS.toSeconds(diff) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(diff))
            );
        } else if (diff < MS_IN_DAY) {
            // Less than a day, unit in hours
            value = diff / MS_IN_HOUR;
            unit = "hour";
        } else if (diff < MS_IN_WEEK) {
            // Less than a week, unit in days
            value = diff / MS_IN_DAY;
            unit = "day";
        } else if (diff < MS_IN_MONTH) {
            // Less than a month, unit in weeks
            value = diff / MS_IN_WEEK;
            unit = "week";
        } else if (diff < MS_IN_YEAR) {
            // Less than a year, unit in months
            value = diff / MS_IN_MONTH;
            unit = "month";
        } else {
            // Else, unit in years
            value = diff / MS_IN_YEAR;
            unit = "year";
        }

        return String.format("%d %s%s", value, unit, value > 1 ? "s" : "");
    }

    public static boolean needed(@Nullable Context context) {
        return context != null && context.getResources().getInteger(R.integer.icon_request_limit_interval) > 0;
    }
}