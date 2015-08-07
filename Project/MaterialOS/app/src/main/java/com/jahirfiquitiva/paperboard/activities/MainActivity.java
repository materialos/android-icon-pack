package com.jahirfiquitiva.paperboard.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.adapters.ChangelogAdapter;
import com.jahirfiquitiva.paperboard.fragments.ApplyFragment;
import com.jahirfiquitiva.paperboard.fragments.CreditsFragment;
import com.jahirfiquitiva.paperboard.fragments.HomeFragment;
import com.jahirfiquitiva.paperboard.fragments.PreviewsFragment;
import com.jahirfiquitiva.paperboard.fragments.RequestFragment;
import com.jahirfiquitiva.paperboard.fragments.WallpapersFragment;
import com.jahirfiquitiva.paperboard.utilities.Preferences;
import com.jahirfiquitiva.paperboard.utilities.Util;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeader;
import com.mikepenz.materialdrawer.accountswitcher.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.pkmmte.requestmanager.PkRequestManager;
import com.pkmmte.requestmanager.RequestSettings;

import org.materialos.icons.R;


public class MainActivity extends AppCompatActivity {

    private static final boolean WITH_LICENSE_CHECKER = false;
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    public String version;
    private Drawer mDrawer = null;
    private int mCurrentItem = -1;
    private boolean mFirstrun, mEnableFeatures;
    private Preferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Uncomment this for custom themeing
        // setTheme(R.style.CustomTheme);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final String appName = getResources().getString(R.string.app_name);
        String home = getResources().getString(R.string.section_one);
        final String previews = getResources().getString(R.string.section_two);
        final String apply = getResources().getString(R.string.section_three);
        final String wallpapers = getResources().getString(R.string.section_four);
        final String iconRequest = getResources().getString(R.string.section_five);
        final String credits = getResources().getString(R.string.section_six);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .withSelectionFirstLine(getResources().getString(R.string.app_long_name))
                .withSelectionSecondLine("v" + Util.getAppVersion(this))
                .withSavedInstance(savedInstanceState)
                .build();

        mEnableFeatures = mPrefs.isFeaturesEnabled();
        mFirstrun = mPrefs.isFirstRun();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(home).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(1),
                        new PrimaryDrawerItem().withName(previews).withIcon(GoogleMaterial.Icon.gmd_palette).withIdentifier(2),
                        new PrimaryDrawerItem().withName(apply).withIcon(GoogleMaterial.Icon.gmd_style).withIdentifier(3),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(credits).withIdentifier(6)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            switch (drawerItem.getIdentifier()) {
                                case 1:
                                    switchFragment(1, appName, HomeFragment.class);
                                    break;
                                case 2:
                                    switchFragment(2, previews, PreviewsFragment.class);
                                    break;
                                case 3:
                                    switchFragment(3, apply, ApplyFragment.class);
                                    break;
                                case 4:
                                    if (Util.hasNetwork(MainActivity.this)) {
                                        switchFragment(4, wallpapers, WallpapersFragment.class);
                                    } else {
                                        showNotConnectedDialog();
                                    }
                                    break;
                                case 5:
                                    switchFragment(5, iconRequest, RequestFragment.class);
                                    break;
                                case 6:
                                    switchFragment(6, credits, CreditsFragment.class);
                                    break;
                            }

                        } else {
                            return false;
                        }
                        return true;
                    }

                })
                .withSavedInstance(savedInstanceState)
                .build();

        mDrawer.getListView().setVerticalScrollBarEnabled(false);
        runLicenseChecker();

        if (savedInstanceState == null) {
            mCurrentItem = -1;
            mDrawer.setSelectionByIdentifier(1);
        }
    }

    public Drawer getDrawer() {
        return mDrawer;
    }

    public void switchFragment(int itemId, String title, Class<? extends Fragment> fragment) {
        if (mCurrentItem == itemId) {
            // Don't allow re-selection of the currently active item
            return;
        }
        mCurrentItem = itemId;
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, Fragment.instantiate(MainActivity.this, fragment.getName()))
                .commit();

        if (mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
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
                                getResources().getString(R.string.iconpack_designer) +
                                getResources().getString(R.string.share_two) +
                                MARKET_URL + getPackageName();
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, (getResources().getString(R.string.share_title))));
                break;

            case R.id.sendemail:
                StringBuilder emailBuilder = new StringBuilder();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + getResources().getString(R.string.email_id)));
                intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject));

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
                startActivity(Intent.createChooser(intent, (getResources().getString(R.string.send_title))));
                break;

            case R.id.changelog:
                showChangelog();
                break;
        }
        return true;
    }

    private void addItemsToDrawer() {
        final String wallpapers = getResources().getString(R.string.section_four);
        final String iconRequest = getResources().getString(R.string.section_five);
        IDrawerItem walls = new PrimaryDrawerItem().withName(wallpapers).withIcon(GoogleMaterial.Icon.gmd_image).withIdentifier(4);
        IDrawerItem request = new PrimaryDrawerItem().withName(iconRequest).withIcon(GoogleMaterial.Icon.gmd_content_paste).withIdentifier(5);
        if (mEnableFeatures) {
            mDrawer.addItem(walls, 3);
            mDrawer.addItem(request, 4);
        }
    }

    private void runLicenseChecker() {
        if (mFirstrun) {
            if (WITH_LICENSE_CHECKER) {
                checkLicense();
            } else {
                mPrefs.setFeaturesEnabled(true);
                addItemsToDrawer();
                showChangelogDialog();
            }
        } else {
            if (WITH_LICENSE_CHECKER) {
                if (!mEnableFeatures) {
                    showNotLicensedDialog();
                } else {
                    addItemsToDrawer();
                    showChangelogDialog();
                }
            } else {
                addItemsToDrawer();
                showChangelogDialog();
            }
        }
    }

    private void showChangelog() {
        new MaterialDialog.Builder(this)
                .title(R.string.changelog_dialog_title)
                .adapter(new ChangelogAdapter(this, R.array.fullchangelog), null)
                .positiveText(R.string.nice)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mPrefs.setNotFirstrun();
                    }
                }).show();
    }

    private void showChangelogDialog() {
        if (mPrefs.getSavedVersion() != Util.getAppVersion(this))
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
                        int nSelection = mCurrentItem - 1;
                        if (mDrawer != null)
                            mDrawer.setSelection(nSelection);
                    }
                }).show();
    }

    private void checkLicense() {
        String installer = getPackageManager().getInstallerPackageName(getPackageName());
        try {
            if (installer.equals("com.google.android.feedback")
                    || installer.equals("com.android.vending")
                    || installer.equals("com.amazon.venezia")) {
                new MaterialDialog.Builder(this)
                        .title(R.string.license_success_title)
                        .content(R.string.license_success)
                        .positiveText(R.string.close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mEnableFeatures = true;
                                mPrefs.setFeaturesEnabled(true);
                                addItemsToDrawer();
                                showChangelogDialog();
                            }
                        }).show();
            } else {
                showNotLicensedDialog();
            }
        } catch (Exception e) {
            showNotLicensedDialog();
        }
    }

    private void showNotLicensedDialog() {
        mEnableFeatures = false;
        mPrefs.setFeaturesEnabled(false);
        new MaterialDialog.Builder(this)
                .title(R.string.license_failed_title)
                .content(R.string.license_failed)
                .positiveText(R.string.download)
                .negativeText(R.string.exit)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URL + getPackageName()));
                        startActivity(browserIntent);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        finish();
                    }
                })
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }).show();
    }

}
