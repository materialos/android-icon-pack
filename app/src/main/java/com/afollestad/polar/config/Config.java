package com.afollestad.polar.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.polar.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class Config implements IConfig {

    private Config(@Nullable Context context) {
        mR = null;
        mContext = context;
        if (context != null)
            mR = context.getResources();
    }

    private static Config mConfig;
    private Context mContext;
    private Resources mR;

    public static void init(@NonNull Context context) {
        mConfig = new Config(context);
    }

    public static void setContext(Context context) {
        if (mConfig != null) {
            mConfig.mContext = context;
            if (context != null)
                mConfig.mR = context.getResources();
        }
    }

    private void destroy() {
        mContext = null;
        mR = null;
    }

    public static void deinit() {
        if (mConfig != null) {
            mConfig.destroy();
            mConfig = null;
        }
    }

    @NonNull
    public static IConfig get() {
        if (mConfig == null)
            return new Config(null); // shouldn't ever happen, but avoid crashes
        return mConfig;
    }

    // Getters

    private SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public boolean allowThemeSwitching() {
        return mR != null && mR.getBoolean(R.bool.allow_theme_switching);
    }

    @Override
    public boolean darkTheme() {
        if (!Config.get().allowThemeSwitching())
            darkTheme(darkThemeDefault());
        return prefs().getBoolean("config_dark_theme", darkThemeDefault());
    }

    @Override
    public void darkTheme(boolean enabled) {
        prefs().edit().putBoolean("config_dark_theme", enabled).commit();
    }

    @Override
    public boolean darkThemeDefault() {
        return mR != null && mR.getBoolean(R.bool.dark_theme_default);
    }

    @Override
    public boolean navDrawerModeEnabled() {
        return !(mR == null || mContext == null) &&
                prefs().getBoolean("nav_drawer_mode", mR.getBoolean(R.bool.nav_drawer_mode_default));
    }

    @Override
    public void navDrawerModeEnabled(boolean enabled) {
        if (mR == null || mContext == null) return;
        prefs().edit().putBoolean("nav_drawer_mode", enabled).commit();
    }

    @Override
    public boolean navDrawerModeAllowSwitch() {
        return mR != null && mR.getBoolean(R.bool.allow_nav_drawer_mode_switch);
    }

    @Override
    public boolean homepageEnabled() {
        return mR == null || mR.getBoolean(R.bool.homepage_enabled);
    }

    @Nullable
    @Override
    public String wallpapersJsonUrl() {
        if (mR == null) return null;
        return mR.getString(R.string.wallpapers_json_url);
    }

    @Override
    public boolean wallpapersAllowDownload() {
        return mR != null && mR.getBoolean(R.bool.wallpapers_allow_download);
    }

    @Override
    public boolean wallpapersEnabled() {
        String url = wallpapersJsonUrl();
        return url != null && !url.trim().isEmpty();
    }

    @Override
    public boolean zooperEnabled() {
        return mR != null && mR.getBoolean(R.bool.enable_zooper_page);
    }

    @Override
    public boolean kustomWidgetEnabled() {
        return mR != null && mR.getBoolean(R.bool.enable_kustom_widgets_page);
    }

    @Override
    public boolean kustomWallpaperEnabled() {
        return mR != null && mR.getBoolean(R.bool.enable_kustom_wallpapers_page);
    }

    @Nullable
    @Override
    public String iconRequestEmail() {
        if (mR == null) return null;
        return mR.getString(R.string.icon_request_email);
    }

    @Override
    public boolean iconRequestEnabled() {
        final String requestEmail = iconRequestEmail();
        return requestEmail != null && !requestEmail.trim().isEmpty();
    }

    @Nullable
    @Override
    public String feedbackEmail() {
        if (mR == null) return null;
        return mR.getString(R.string.feedback_email);
    }

    @Nullable
    @Override
    public String feedbackSubjectLine() {
        if (mR == null || mContext == null) return null;
        return mR.getString(R.string.feedback_subject_line, mContext.getString(R.string.app_name));
    }

    @Override
    public boolean feedbackEnabled() {
        final String feedbackEmail = feedbackEmail();
        return feedbackEmail != null && !feedbackEmail.trim().isEmpty();
    }

    @Nullable
    @Override
    public String donationLicenseKey() {
        if (mR == null) return null;
        return mR.getString(R.string.donate_license_key);
    }

    @Override
    public boolean donationEnabled() {
        final String licenseKey = donationLicenseKey();
        return licenseKey != null && !licenseKey.trim().isEmpty();
    }

    @Nullable
    @Override
    public String[] donateOptionsNames() {
        if (mR == null) return null;
        return mR.getStringArray(R.array.donate_option_names);
    }

    @Nullable
    @Override
    public String[] donateOptionsIds() {
        if (mR == null) return null;
        return mR.getStringArray(R.array.donate_option_ids);
    }

    @Nullable
    @Override
    public String licensingPublicKey() {
        if (mR == null) return null;
        return mR.getString(R.string.licensing_public_key);
    }

    @Override
    public boolean persistSelectedPage() {
        return mR == null || mR.getBoolean(R.bool.persist_selected_page);
    }

    @Override
    public boolean changelogEnabled() {
        return mR != null && mR.getBoolean(R.bool.changelog_enabled);
    }

    @Override
    public int gridWidthWallpaper() {
        if (mR == null) return 2;
        return mR.getInteger(R.integer.wallpaper_grid_width);
    }

    @Override
    public int gridWidthApply() {
        if (mR == null) return 3;
        return mR.getInteger(R.integer.apply_grid_width);
    }

    @Override
    public int gridWidthIcons() {
        if (mR == null) return 4;
        return mR.getInteger(R.integer.icon_grid_width);
    }

    @Override
    public int gridWidthRequests() {
        if (mR == null) return 3;
        return mR.getInteger(R.integer.requests_grid_width);
    }

    @Override
    public int gridWidthZooper() {
        if (mR == null) return 2;
        return mR.getInteger(R.integer.zooper_grid_width);
    }

    @Override
    public int gridWidthKustom() {
        if (mR == null) return 2;
        return mR.getInteger(R.integer.kustom_grid_width);
    }

    @Override
    public int iconRequestMaxCount() {
        if (mR == null) return -1;
        return mR.getInteger(R.integer.icon_request_maxcount);
    }

    @Override
    public String polarBackendHost() {
        if (mR == null) return null;
        return mR.getString(R.string.polar_backend_host);
    }

    @Override
    public String polarBackendApiKey() {
        if (mR == null) return null;
        return mR.getString(R.string.polar_backend_apikey);
    }
}