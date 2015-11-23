package org.materialos.icons.activities;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;

import org.materialos.icons.BuildConfig;
import org.materialos.icons.R;
import org.materialos.icons.fragments.AboutFragment;
import org.materialos.icons.fragments.ApplyFragment;
import org.materialos.icons.fragments.ChangelogDialogFragment;
import org.materialos.icons.fragments.HomeFragment;
import org.materialos.icons.fragments.IconsFragment;
import org.materialos.icons.fragments.RequestFragment;
import org.materialos.icons.fragments.WallpapersFragment;
import org.materialos.icons.util.Preferences;
import org.materialos.icons.util.Util;

import java.lang.reflect.Field;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {
    public static final int DRAWER_ITEM_HOME = 1234;
    public static final int DRAWER_ITEM_ICONS = 6434;
    public static final int DRAWER_ITEM_APPLY = 9650;
    public static final int DRAWER_ITEM_WALLPAPER = 3462;
    public static final int DRAWER_ITEM_REQUEST = 1284;
    public static final int DRAWER_ITEM_ABOUT = 3255;
    public static final int DRAWER_ITEM_CHANGELOG = 1337;
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    private Drawer mDrawer = null;
    private int mCurrentSelectedPosition = -1;
    private Preferences mPrefs;
    private Toolbar mToolbar;
    private TextView mToolbarCenterTitle;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grab a reference to the manager and store it in a variable. This helps make code shorter.
        PkRequestManager requestManager = PkRequestManager.getInstance(this);
        requestManager.setDebugging(false);
        // Set your custom settings. Email address is required! Everything else is set to default.
        requestManager.setSettings(new RequestSettings.Builder()
                .addEmailAddress(getResources().getString(R.string.email_id))
                .emailSubject(getResources().getString(R.string.email_request_subject))
                .emailPrecontent(getResources().getString(R.string.request_precontent))
                .saveLocation(Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.request_save_location))
                .build());
        requestManager.loadAppsIfEmptyAsync();

        mPrefs = new Preferences(MainActivity.this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarCenterTitle = (TextView) mToolbar.findViewById(R.id.app_bar_title);
        setSupportActionBar(mToolbar);

        final String appName = getString(R.string.app_name);
        final String home = getString(R.string.home);
        final String previews = getString(R.string.icons);
        final String apply = getString(R.string.apply);
        final String wallpapers = getString(R.string.wallpapers);
        final String iconRequest = getString(R.string.icon_request);
        final String credits = getString(R.string.about);
        final String changelog = getString(R.string.changelog);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withSelectionFirstLine(appName)
                .withSelectionSecondLine("v" + Util.getAppVersionName(this))
                .withSavedInstance(savedInstanceState)
                .build();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(home).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(DRAWER_ITEM_HOME),
                        new PrimaryDrawerItem().withName(previews).withIcon(GoogleMaterial.Icon.gmd_palette).withIdentifier(DRAWER_ITEM_ICONS),
                        new PrimaryDrawerItem().withName(wallpapers).withIcon(GoogleMaterial.Icon.gmd_image).withIdentifier(DRAWER_ITEM_WALLPAPER),
                        new PrimaryDrawerItem().withName(apply).withIcon(GoogleMaterial.Icon.gmd_labels).withIdentifier(DRAWER_ITEM_APPLY),
                        new PrimaryDrawerItem().withName(iconRequest).withIcon(GoogleMaterial.Icon.gmd_paste).withIdentifier(DRAWER_ITEM_REQUEST),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(changelog).withIcon(GoogleMaterial.Icon.gmd_trending_up).withIdentifier(DRAWER_ITEM_CHANGELOG).withSelectable(false),
                        new PrimaryDrawerItem().withName(credits).withIcon(GoogleMaterial.Icon.gmd_info).withIdentifier(DRAWER_ITEM_ABOUT)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                        if (iDrawerItem != null) {
                            switch (iDrawerItem.getIdentifier()) {
                                case DRAWER_ITEM_HOME:
                                    switchFragment(DRAWER_ITEM_HOME, appName, HomeFragment.class);
                                    break;
                                case DRAWER_ITEM_ICONS:
                                    switchFragment(DRAWER_ITEM_ICONS, previews, IconsFragment.class);
                                    break;
                                case DRAWER_ITEM_APPLY:
                                    switchFragment(DRAWER_ITEM_APPLY, apply, ApplyFragment.class);
                                    break;
                                case DRAWER_ITEM_WALLPAPER:
                                    if (Util.hasNetwork(MainActivity.this)) {
                                        switchFragment(DRAWER_ITEM_WALLPAPER, wallpapers, WallpapersFragment.class);
                                    } else {
                                        showNotConnectedDialog();
                                    }
                                    break;
                                case DRAWER_ITEM_REQUEST:
                                    switchFragment(DRAWER_ITEM_REQUEST, iconRequest, RequestFragment.class);
                                    break;
                                case DRAWER_ITEM_CHANGELOG:
                                    showChangelog();
                                    break;
                                case DRAWER_ITEM_ABOUT:
                                    switchFragment(DRAWER_ITEM_ABOUT, credits, AboutFragment.class);
                                    break;
                            }
                            invalidateOptionsMenu();
                        } else {
                            return false;
                        }
                        return true;
                    }

                })
                .withSavedInstance(savedInstanceState)
                .build();

        if (mPrefs.isFirstRun()) {
            showChangelogDialogIfNeeded();
        }

        if (savedInstanceState == null) {
            mCurrentSelectedPosition = 0;
            mDrawer.setSelection(DRAWER_ITEM_HOME);
        } else {
            mCurrentSelectedPosition = mDrawer.getCurrentSelectedPosition();
            updateToolbarElevation(mDrawer.getCurrentSelection());
            updateForHome(mDrawer.getCurrentSelection());
        }
    }

    public void setCenterTitle(int resId) {
        mToolbarCenterTitle.setText(resId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mDrawer.setSelectionAtPosition(mCurrentSelectedPosition);
    }

    public Drawer getDrawer() {
        return mDrawer;
    }

    public void switchFragment(int itemId, String title, Class<? extends Fragment> fragment) {

        mDrawer.getRecyclerView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mDrawer.isDrawerOpen()) {
                    mDrawer.closeDrawer();
                }
            }
        }, 50);


        updateToolbarElevation(itemId);
        updateForHome(itemId);

        if (mCurrentSelectedPosition == mDrawer.getPosition(itemId)) {
            // Don't allow re-selection of the currently active item
            return;
        }

        mCurrentSelectedPosition = mDrawer.getPosition(itemId);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);

        getFragmentManager().beginTransaction()
                .replace(R.id.main, Fragment.instantiate(MainActivity.this, fragment.getName()))
                .commit();


    }

    private void updateToolbarElevation(int itemId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (itemId == DRAWER_ITEM_HOME || itemId == DRAWER_ITEM_ICONS) {
                mToolbar.setElevation(0);
            } else {
                mToolbar.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
            }
        }
    }

    private void updateForHome(int itemId) {
        if (itemId == DRAWER_ITEM_HOME) {
            getWindow().getDecorView().setBackgroundResource(R.drawable.launch_screen);
            mToolbarCenterTitle.setText(R.string.welcome_title);
        } else {
            getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(this, R.color.background_material_light));
            mToolbarCenterTitle.setText("");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = mDrawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        for (int i = 0; i < menu.size(); i++) {
            final boolean home = mDrawer.getCurrentSelection() == DRAWER_ITEM_HOME;
            MenuItemCompat.setShowAsAction(menu.getItem(i), home ?
                    MenuItemCompat.SHOW_AS_ACTION_NEVER :
                    MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
            menu.getItem(i).getIcon().setColorFilter(ContextCompat.getColor(this, home ? R.color.md_black_1000 : R.color.md_white_1000),
                    PorterDuff.Mode.SRC_IN);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.share:
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody =
                        getResources().getString(R.string.share_one) +
                                getResources().getString(R.string.share_name) +
                                getResources().getString(R.string.share_two) +
                                MARKET_URL + getPackageName();
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, (getResources().getString(R.string.share_via))));
                break;

            case R.id.sendemail:
                StringBuilder emailBuilder = new StringBuilder();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getString(R.string.email_id)));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

                emailBuilder.append("\n \n \nOS Version: ").append(System.getProperty("os.version")).append("(").append(Build.VERSION.INCREMENTAL).append(")");
                emailBuilder.append("\nOS API Level: ").append(Build.VERSION.SDK_INT);
                emailBuilder.append("\nDevice: ").append(Build.DEVICE);
                emailBuilder.append("\nManufacturer: ").append(Build.MANUFACTURER);
                emailBuilder.append("\nModel (and Product): ").append(Build.MODEL).append(" (").append(Build.PRODUCT).append(")");
                PackageInfo appInfo = null;
                try {
                    appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                assert appInfo != null;
                emailBuilder.append("\nApp Version Name: ").append(appInfo.versionName);
                emailBuilder.append("\nApp Version Code: ").append(appInfo.versionCode);

                intent.putExtra(Intent.EXTRA_TEXT, emailBuilder.toString());
                startActivity(Intent.createChooser(intent, (getResources().getString(R.string.send_via))));
                break;
            case R.id.rate:
                Intent rate = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + getPackageName()));
                startActivity(rate);
                break;
        }
        return true;
    }

    private void showChangelog() {
        new ChangelogDialogFragment().show(getFragmentManager(), "changelog_dialog");
        mPrefs.setNotFirstrun();
    }

    private void showChangelogDialogIfNeeded() {
        if (mPrefs.getSavedVersion() < Util.getAppVersionCode(this))
            showChangelog();
        mPrefs.saveVersion();
    }

    private void showNotConnectedDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.no_conn_title)
                .content(R.string.no_conn_content)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        int nSelection = mCurrentSelectedPosition - 1;
                        if (mDrawer != null)
                            mDrawer.setSelectionAtPosition(nSelection);
                    }
                }).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setIconsVisibleInOverflow(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    public void setIconsVisibleInOverflow(final Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Field field = menu.getClass().getDeclaredField("mOptionalIconsVisible");
                field.setAccessible(true);
                field.setBoolean(menu, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
