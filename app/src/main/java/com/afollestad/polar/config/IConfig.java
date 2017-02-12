package com.afollestad.polar.config;

import android.support.annotation.Nullable;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface IConfig {

    boolean allowThemeSwitching();

    boolean darkTheme();

    void darkTheme(boolean enabled);

    boolean darkThemeDefault();

    boolean navDrawerModeEnabled();

    void navDrawerModeEnabled(boolean enabled);

    boolean navDrawerModeAllowSwitch();

    boolean homepageEnabled();

    @Nullable
    String wallpapersJsonUrl();

    boolean wallpapersAllowDownload();

    boolean wallpapersEnabled();

    boolean zooperEnabled();

    boolean kustomWidgetEnabled();

    boolean kustomWallpaperEnabled();

    @Nullable
    String iconRequestEmail();

    boolean iconRequestEnabled();

    @Nullable
    String feedbackEmail();

    @Nullable
    String feedbackSubjectLine();

    boolean feedbackEnabled();

    @Nullable
    String donationLicenseKey();

    boolean donationEnabled();

    @Nullable
    String[] donateOptionsNames();

    @Nullable
    String[] donateOptionsIds();

    @Nullable
    String licensingPublicKey();

    boolean persistSelectedPage();

    boolean changelogEnabled();

    int gridWidthWallpaper();

    int gridWidthApply();

    int gridWidthIcons();

    int gridWidthRequests();

    int gridWidthZooper();

    int gridWidthKustom();

    int iconRequestMaxCount();

    String polarBackendHost();

    String polarBackendApiKey();
}