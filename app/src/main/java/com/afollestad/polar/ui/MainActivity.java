package com.afollestad.polar.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import com.afollestad.bridge.Bridge;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.MainPagerAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.dialogs.ChangelogDialog;
import com.afollestad.polar.dialogs.InvalidLicenseDialog;
import com.afollestad.polar.fragments.AboutFragment;
import com.afollestad.polar.fragments.ApplyFragment;
import com.afollestad.polar.fragments.HomeFragment;
import com.afollestad.polar.fragments.IconsFragment;
import com.afollestad.polar.fragments.KustomFragment;
import com.afollestad.polar.fragments.RequestsFragment;
import com.afollestad.polar.fragments.WallpapersFragment;
import com.afollestad.polar.fragments.ZooperFragment;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.kustom.KustomUtil;
import com.afollestad.polar.ui.base.BaseDonateActivity;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.LicensingUtils;
import com.afollestad.polar.util.PagesBuilder;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.Utils;
import com.afollestad.polar.util.VC;
import com.afollestad.polar.util.WallpaperUtils;
import com.afollestad.polar.views.DisableableViewPager;
import com.google.android.vending.licensing.Policy;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.afollestad.polar.fragments.WallpapersFragment.RQ_CROPANDSETWALLPAPER;
import static com.afollestad.polar.fragments.WallpapersFragment.RQ_VIEWWALLPAPER;
import static com.afollestad.polar.viewer.ViewerActivity.STATE_CURRENT_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class MainActivity extends BaseDonateActivity implements
        LicensingUtils.LicensingCallback, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @BindView(R.id.tabs)
    TabLayout mTabs;

    @Nullable
    @BindView(R.id.navigation_view)
    NavigationView mNavView;
    @Nullable
    @BindView(R.id.drawer)
    DrawerLayout mDrawer;

    @BindView(R.id.pager)
    DisableableViewPager mPager;

    @Nullable
    @BindView(R.id.app_bar)
    LinearLayout mAppBarLinear;

    int mDrawerModeTopInset;

    private PagesBuilder mPages;

    private boolean isReentering;
    private int reenterPos;

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        final boolean useNavDrawer = Config.get().navDrawerModeEnabled();
        if (useNavDrawer)
            setContentView(R.layout.activity_main_drawer);
        else
            setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        setupPages();
        setupPager();
        if (useNavDrawer)
            setupNavDrawer();
        else
            setupTabs();

        // Restore last selected page, tab/nav-drawer-item
        if (Config.get().persistSelectedPage()) {
            int lastPage = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getInt("last_selected_page", 0);
            if (lastPage > mPager.getAdapter().getCount() - 1) lastPage = 0;
            mPager.setCurrentItem(lastPage);
            if (mNavView != null) invalidateNavViewSelection(lastPage);
        }
        dispatchFragmentUpdateTitle(!useNavDrawer);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(new SharedElementCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if (isReentering) {
                        WallpapersFragment frag = (WallpapersFragment) getFragmentManager().findFragmentByTag("page:" + mPager.getCurrentItem());
                        final RecyclerView recyclerView = frag.getRecyclerView();
                        View item = recyclerView.findViewWithTag("view_" + reenterPos);
                        View image = item.findViewById(R.id.image);

                        names.clear();
                        names.add(image.getTransitionName());
                        sharedElements.clear();
                        sharedElements.put(image.getTransitionName(), image);

                        isReentering = false;
                    }
                }
            });
        }

        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        if (Intent.ACTION_SET_WALLPAPER.equals(intent.getAction())) {
            for (int i = 0; i < mPages.size(); i++) {
                PagesBuilder.Page page = mPages.get(i);
                if (page.drawerId == R.id.drawer_wallpapers) {
                    mPager.setCurrentItem(i);
                    break;
                }
            }
        }
    }

    private void setupPages() {
        mPages = new PagesBuilder(7);
        if (Config.get().homepageEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_home, R.drawable.tab_home, R.string.home, new HomeFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_icons, R.drawable.tab_icons, R.string.icons, new IconsFragment()));
        if (Config.get().wallpapersEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_wallpapers, R.drawable.tab_wallpapers, R.string.wallpapers, new WallpapersFragment()));
        if (Config.get().iconRequestEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_requestIcons, R.drawable.tab_requests, R.string.request_icons, new RequestsFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_apply, R.drawable.tab_apply, R.string.apply, new ApplyFragment()));
        if (Config.get().kustomWidgetEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_kwgt, R.drawable.tab_kwgt, R.string.kwgt, KustomFragment.newInstance(KustomUtil.FOLDER_WIDGETS)));
        if (Config.get().kustomWallpaperEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_klwp, R.drawable.tab_klwp, R.string.klwp, KustomFragment.newInstance(KustomUtil.FOLDER_WALLPAPERS)));
        if (Config.get().zooperEnabled())
            mPages.add(new PagesBuilder.Page(R.id.drawer_zooper, R.drawable.tab_zooper, R.string.zooper, new ZooperFragment()));
        mPages.add(new PagesBuilder.Page(R.id.drawer_about, R.drawable.tab_about, R.string.about, new AboutFragment()));
    }

    public boolean retryLicenseCheck() {
        return LicensingUtils.check(this, this);
    }

    @Override
    public void onLicensingResult(boolean allow, int reason) {
        if (allow)
            showChangelogIfNecessary(true);
        else InvalidLicenseDialog.show(this, reason == Policy.RETRY);
    }

    @Override
    public void onLicensingError(int errorCode) {
        Utils.showError(this, new Exception("License checking error occurred, make sure everything is setup correctly. Error code: " + errorCode));
    }

    public void showChangelogIfNecessary(boolean licenseAllowed) {
        if (!Config.get().changelogEnabled()) {
            retryLicenseCheck();
        } else if (licenseAllowed || retryLicenseCheck()) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final int currentVersion = BuildConfig.VERSION_CODE;
            if (currentVersion != prefs.getInt("changelog_version", -1)) {
                prefs.edit().putInt("changelog_version", currentVersion).apply();
                ChangelogDialog.show(this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        if (!Config.get().changelogEnabled())
            menu.findItem(R.id.changelog).setVisible(false);

        MenuItem darkTheme = menu.findItem(R.id.darkTheme);
        if (!Config.get().allowThemeSwitching())
            darkTheme.setVisible(false);
        else darkTheme.setChecked(darkTheme());

        MenuItem navDrawerMode = menu.findItem(R.id.navDrawerMode);
        if (Config.get().navDrawerModeAllowSwitch()) {
            navDrawerMode.setVisible(true);
            navDrawerMode.setChecked(Config.get().navDrawerModeEnabled());
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.changelog) {
            ChangelogDialog.show(this);
            return true;
        } else if (item.getItemId() == R.id.darkTheme) {
            darkTheme(!darkTheme());
            mToolbar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 500);
            return true;
        } else if (item.getItemId() == R.id.navDrawerMode) {
            item.setChecked(!item.isChecked());
            Config.get().navDrawerModeEnabled(item.isChecked());
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupNavDrawer() {
        assert mNavView != null;
        assert mDrawer != null;
        mNavView.getMenu().clear();
        for (PagesBuilder.Page page : mPages)
            page.addToMenu(mNavView.getMenu());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDrawer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    //TODO: Check if NavigationView needs bottom padding
                    WindowInsets drawerLayoutInsets = insets.replaceSystemWindowInsets(
                            insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            0
                    );
                    mDrawerModeTopInset = drawerLayoutInsets.getSystemWindowInsetTop();
                    ((DrawerLayout) v).setChildInsets(drawerLayoutInsets,
                            drawerLayoutInsets.getSystemWindowInsetTop() > 0);
                    return insets;
                }
            });
        }

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable menuIcon = VC.get(R.drawable.ic_action_menu);
        menuIcon = TintUtils.createTintedDrawable(menuIcon, DialogUtils.resolveColor(this, R.attr.tab_icon_color));
        getSupportActionBar().setHomeAsUpIndicator(menuIcon);

        mDrawer.addDrawerListener(new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open, R.string.drawer_close));
        mDrawer.setStatusBarBackgroundColor(DialogUtils.resolveColor(this, R.attr.colorPrimaryDark));
        mNavView.setNavigationItemSelectedListener(this);

        final ColorDrawable navBg = (ColorDrawable) mNavView.getBackground();
        final int selectedIconText = DialogUtils.resolveColor(this, R.attr.colorAccent);
        int iconColor;
        int titleColor;
        int selectedBg;
        if (TintUtils.isColorLight(navBg.getColor())) {
            iconColor = ContextCompat.getColor(this, R.color.navigationview_normalicon_light);
            titleColor = ContextCompat.getColor(this, R.color.navigationview_normaltext_light);
            selectedBg = ContextCompat.getColor(this, R.color.navigationview_selectedbg_light);
        } else {
            iconColor = ContextCompat.getColor(this, R.color.navigationview_normalicon_dark);
            titleColor = ContextCompat.getColor(this, R.color.navigationview_normaltext_dark);
            selectedBg = ContextCompat.getColor(this, R.color.navigationview_selectedbg_dark);
        }

        final ColorStateList iconSl = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{iconColor, selectedIconText});
        final ColorStateList textSl = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked},
                        new int[]{android.R.attr.state_checked}
                },
                new int[]{titleColor, selectedIconText});
        mNavView.setItemTextColor(textSl);
        mNavView.setItemIconTintList(iconSl);

        StateListDrawable bgDrawable = new StateListDrawable();
        bgDrawable.addState(new int[]{android.R.attr.state_checked}, new ColorDrawable(selectedBg));
        mNavView.setItemBackground(bgDrawable);

        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                dispatchFragmentUpdateTitle(false);
                invalidateNavViewSelection(position);
            }
        });

        mToolbar.setContentInsetsRelative(getResources().getDimensionPixelSize(R.dimen.second_keyline), 0);
    }

    void invalidateNavViewSelection(int position) {
        assert mNavView != null;
        final int selectedId = mPages.get(position).drawerId;
        mNavView.post(new Runnable() {
            @Override
            public void run() {
                mNavView.setCheckedItem(selectedId);
            }
        });
    }

    @Override
    public int getLastStatusBarInsetHeight() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }

        boolean useNavDrawer = Config.get().navDrawerModeEnabled();
        if (useNavDrawer) {
            return mDrawerModeTopInset;
        } else {
            return findViewById(R.id.root).getPaddingTop();
        }
    }

    private void setupPager() {
        mPager.setAdapter(new MainPagerAdapter(getFragmentManager(), mPages));
        mPager.setOffscreenPageLimit(mPages.size() - 1);
        // Paging is only enabled in tab mode
        mPager.setPagingEnabled(!Config.get().navDrawerModeEnabled());
    }

    private void setupTabs() {
        assert mTabs != null;
        mTabs.setTabMode(mPages.size() > 6 ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
        mTabs.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabs) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                dispatchFragmentUpdateTitle(false);
            }
        });

        for (PagesBuilder.Page page : mPages)
            addTab(page.iconRes);
        mTabs.setSelectedTabIndicatorColor(DialogUtils.resolveColor(this, R.attr.tab_indicator_color));
    }

    void dispatchFragmentUpdateTitle(final boolean checkTabsLocation) {
        //First set the presumed title, then let fragment do anything specific.
        setTitle(mPages.get(mPager.getCurrentItem()).titleRes);

        mPager.post(new Runnable() {
            @Override
            public void run() {
                final BasePageFragment frag = (BasePageFragment) getFragmentManager().findFragmentByTag("page:" + mPager.getCurrentItem());
                if (frag != null) frag.updateTitle();

                if (checkTabsLocation) {
                    moveTabsIfNeeded();
                }
            }
        });
    }

    void moveTabsIfNeeded() {
        final CharSequence currentTitle = getTitle();

        String longestTitle = null;
        for (PagesBuilder.Page page : mPages) {
            String title = getString(page.titleRes);
            if (longestTitle == null || title.length() > longestTitle.length()) {
                longestTitle = title;
            }
        }
        setTitle(longestTitle);

        if (mTabs != null) {
            ViewTreeObserver vto = mToolbar.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {
                    if (mToolbar.isTitleTruncated() && mTabs.getParent() == mToolbar) {
                        mToolbar.removeView(mTabs);
                        //noinspection ConstantConditions
                        mAppBarLinear.addView(mTabs);
                    }

                    setTitle(currentTitle);

                    mToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ((DrawerLayout) findViewById(R.id.drawer)).closeDrawers();
        final int index = mPages.findPositionForItem(item);
        if (index > -1)
            mPager.setCurrentItem(index, false);
        return false;
    }

    private void addTab(@DrawableRes int icon) {
        assert mTabs != null;
        TabLayout.Tab tab = mTabs.newTab().setIcon(VC.get(icon));
        if (tab.getIcon() != null) {
            Drawable tintedIcon = DrawableCompat.wrap(tab.getIcon());
            DrawableCompat.setTint(tintedIcon, DialogUtils.resolveColor(this, R.attr.tab_icon_color));
            tab.setIcon(tintedIcon);
        }
        mTabs.addTab(tab);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Config.get().persistSelectedPage()) {
            PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                    .edit().putInt("last_selected_page", mPager.getCurrentItem()).commit();
        }
        if (isFinishing()) {
            Config.deinit();
            Bridge.destroy();
            DrawableXmlParser.cleanup();
            LicensingUtils.cleanup();
            VC.destroy();
            Utils.wipe(getExternalCacheDir());
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager != null) {
            FragmentManager fm = getFragmentManager();
            Fragment current = fm.findFragmentByTag("page:" + mPager.getCurrentItem());
            if (current != null && current instanceof RequestsFragment &&
                    ((RequestsFragment) current).onBackPressed()) {
                return;
            }
        }
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_CROPANDSETWALLPAPER) {
            WallpapersFragment.showToast(this, R.string.wallpaper_set);
            WallpaperUtils.resetOptionCache(true);
        } else if (requestCode == RQ_VIEWWALLPAPER) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDrawer != null) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
                mDrawer.setStatusBarBackgroundColor(DialogUtils.resolveColor(this, R.attr.colorPrimaryDark));
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        isReentering = true;
        reenterPos = data.getIntExtra(STATE_CURRENT_POSITION, 0);

        WallpapersFragment frag = (WallpapersFragment) getFragmentManager().findFragmentByTag("page:" + mPager.getCurrentItem());
        final RecyclerView recyclerView = frag.getRecyclerView();
        if (recyclerView != null) {
            postponeEnterTransition();
            recyclerView.scrollToPosition(reenterPos);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                            startPostponedEnterTransition();
                            return true;
                        }
                    });
                }
            });
        }
    }
}