package com.afollestad.polar.ui.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.assent.AssentActivity;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.Utils;
import com.afollestad.polar.util.VC;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseThemedActivity extends AssentActivity {

    private boolean mLastDarkTheme = false;

    public static void themeMenu(Context context, Menu menu) {
        final int tintColor = DialogUtils.resolveColor(context, R.attr.toolbar_icons_color);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null)
                item.setIcon(TintUtils.createTintedDrawable(item.getIcon(), tintColor));
        }
    }

    public abstract Toolbar getToolbar();

    public int getLastStatusBarInsetHeight() {
        return 0;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Config.init(this);
        VC.init(this);
        mLastDarkTheme = darkTheme();
        setTheme(getCurrentTheme());
        super.onCreate(savedInstanceState);

        if (Config.get().navDrawerModeEnabled()) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                //TODO: Get insets working on KitKat when drawer is used.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                    DialogUtils.resolveColor(this, R.attr.colorPrimary));
            setTaskDescription(td);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !DialogUtils.resolveBoolean(this, R.attr.disable_auto_light_status_bar)) {
            final View decorView = getWindow().getDecorView();
            final boolean lightStatusEnabled = DialogUtils.resolveBoolean(this, R.attr.force_light_status_bar) ||
                    TintUtils.isColorLight(DialogUtils.resolveColor(this, R.attr.colorPrimaryDark));
            final int systemUiVisibility = decorView.getSystemUiVisibility();
            if (lightStatusEnabled) {
                decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    @SuppressLint("PrivateResource")
    @Override
    protected void onStart() {
        super.onStart();
        final Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            final int titleColor = DialogUtils.resolveColor(this, R.attr.toolbar_title_color);
            final int iconColor = DialogUtils.resolveColor(this, R.attr.toolbar_icons_color);
            toolbar.setTitleTextColor(titleColor);
            Utils.setOverflowButtonColor(this, iconColor);

            if (TintUtils.isColorLight(titleColor)) {
                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
            } else {
                toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VC.init(this);
        Config.setContext(this);
        if (mLastDarkTheme != darkTheme())
            recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        themeMenu(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected boolean isTranslucent() {
        return false;
    }

    @StyleRes
    private int getCurrentTheme() {
        if (isTranslucent()) {
            if (!mLastDarkTheme)
                return R.style.AppTheme_Light_Translucent;
            return R.style.AppTheme_Dark_Translucent;
        } else {
            if (!mLastDarkTheme)
                return R.style.AppTheme_Light;
            return R.style.AppTheme_Dark;
        }
    }

    protected final void darkTheme(boolean newValue) {
        Config.get().darkTheme(newValue);
    }

    protected final boolean darkTheme() {
        return Config.get().darkTheme();
    }
}