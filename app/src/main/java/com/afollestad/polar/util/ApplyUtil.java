package com.afollestad.polar.util;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class ApplyUtil {

    @IntDef({UNKNOWN, APEX, NOVA, AVIATE, ADW, ACTION, SMART, NEXT, GO, HOLO, SOLO, KK, ATOM, INSPIRE, CMTE, LGHOME})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Launcher {
    }

    public static final int UNKNOWN = -1;
    public static final int APEX = 0;
    public static final int NOVA = 1;
    public static final int AVIATE = 2;
    public static final int ADW = 3;
    public static final int ACTION = 4;
    public static final int SMART = 5;
    public static final int NEXT = 6;
    public static final int GO = 7;
    public static final int HOLO = 8;
    public static final int SOLO = 9;
    public static final int KK = 10;
    public static final int ATOM = 11;
    public static final int INSPIRE = 12;
    public static final int CMTE = 13;
    public static final int LGHOME = 14;

    @Launcher
    public static int launcherIdFromPkg(String pkg) {
        if (pkg == null) return UNKNOWN;
        switch (pkg) {
            case "com.anddoes.launcher":
                return APEX;
            case "com.teslacoilsw.launcher":
                return NOVA;
            case "com.tul.aviate":
                return AVIATE;
            case "org.adwfreak.launcher":
            case "org.adw.launcher":
                return ADW;
            case "com.actionlauncher.playstore":
            case "com.chrislacy.actionlauncher.pro":
                return ACTION;
            case "ginlemon.flowerpro":
            case "ginlemon.flowerfree":
                return SMART;
            case "com.gtp.nextlauncher":
                return NEXT;
            case "com.gau.go.launcherex":
                return GO;
            case "com.mobint.hololauncher.hd":
            case "com.mobint.hololauncher":
                return HOLO;
            case "home.solo.launcher.free":
                return SOLO;
            case "com.kk.launcher":
                return KK;
            case "com.dlto.atom.launcher":
                return ATOM;
            case "com.bam.android.inspirelauncher":
                return INSPIRE;
            case "com.cyngn.theme.chooser":
            case "org.cyanogenmod.theme.chooser":
                return CMTE;
            case "com.lge.launcher2":
                return LGHOME;
            default:
                return UNKNOWN;
        }
    }

    public interface ApplyCallback {
        void onNotInstalled();
    }

    public static void apply(@NonNull final Context context, @NonNull String launcherPkg, @NonNull ApplyCallback cb) {
        final int id = launcherIdFromPkg(launcherPkg);
        if (id == -1) throw new RuntimeException("Unsupported launcher: " + launcherPkg);
        apply(context, id, cb);
    }

    @SuppressLint("SwitchIntDef")
    public static void apply(@NonNull final Context context, @Launcher final int launcher, @NonNull final ApplyCallback cb) {
        switch (launcher) {
            case GO:
                new MaterialDialog.Builder(context)
                        .title(R.string.go_launcher)
                        .content(Html.fromHtml(context.getString(R.string.go_launcher_notice)))
                        .positiveText(android.R.string.ok)
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                applyFinish(context, launcher, cb);
                            }
                        }).show();
                break;
            default:
                applyFinish(context, launcher, cb);
                break;
        }
    }

    private static void applyFinish(@NonNull final Context context, @Launcher int launcher, @NonNull ApplyCallback cb) {
        final String ACTION_APPLY_ICON_THEME = "com.teslacoilsw.launcher.APPLY_ICON_THEME";
        final String NOVA_PACKAGE = "com.teslacoilsw.launcher";
        final String EXTRA_ICON_THEME_PACKAGE = "com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE";
        final String EXTRA_ICON_THEME_TYPE = "com.teslacoilsw.launcher.extra.ICON_THEME_TYPE";
        final String APEX_ACTION_SET_THEME = "com.anddoes.launcher.SET_THEME";
        final String APEX_EXTRA_PACKAGE_NAME = "com.anddoes.launcher.THEME_PACKAGE_NAME";
        final String AVIATE_ACTION_SET_THEME = "com.tul.aviate.SET_THEME";
        final String AVIATE_EXTRA_PACKAGE_NAME = "THEME_PACKAGE";
        final String ACTION_APPLY_SOLO_THEME = "home.solo.launcher.free.APPLY_THEME";
        final String SOLO_EXTRA_APPLY_THEME_NAME = "home.solo.launcher.free.extra.NAME";
        final String SOLO_EXTRA_APPLY_THEME_PACKAGE = "home.solo.launcher.free.extra.PACKAGE";
        final String SOLO_LAUNCHER_PACKAGENAME = "home.solo.launcher.free";
        final String SOLO_LAUNCHER_CLASSNAME = "home.solo.launcher.free.Launcher";

        final Resources res = context.getResources();
        final PackageManager pm = context.getPackageManager();

        try {
            switch (launcher) {
                case APEX: {
                    Intent apex = new Intent(APEX_ACTION_SET_THEME)
                            .putExtra(APEX_EXTRA_PACKAGE_NAME, BuildConfig.APPLICATION_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(apex);
                    Toast.makeText(context, R.string.finish_apply, Toast.LENGTH_LONG).show();
                    break;
                }
                case NOVA: {
                    Intent nova = new Intent(ACTION_APPLY_ICON_THEME)
                            .setPackage(NOVA_PACKAGE)
                            .putExtra(EXTRA_ICON_THEME_TYPE, "GO")
                            .putExtra(EXTRA_ICON_THEME_PACKAGE, BuildConfig.APPLICATION_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(nova);
                    break;
                }
                case AVIATE: {
                    Intent intent = new Intent(AVIATE_ACTION_SET_THEME)
                            .putExtra(AVIATE_EXTRA_PACKAGE_NAME, BuildConfig.APPLICATION_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    break;
                }
                case ADW: {
                    Intent adw = new Intent("org.adw.launcher.SET_THEME")
                            .putExtra("org.adw.launcher.theme.NAME", BuildConfig.APPLICATION_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(adw);
                    break;
                }
                case ACTION: {
                    Intent al = pm.getLaunchIntentForPackage(
                            "com.actionlauncher.playstore");
                    if (al != null) {
                        al.putExtra("apply_icon_pack", BuildConfig.APPLICATION_ID)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(al);
                    } else {
                        al = pm.getLaunchIntentForPackage(
                                "com.chrislacy.actionlauncher.pro");
                        if (al != null) {
                            al.putExtra("apply_icon_pack", BuildConfig.APPLICATION_ID)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(al);
                        } else {
                            throw new ActivityNotFoundException();
                        }
                    }
                    break;
                }
                case SMART: {
                    Intent smart = context.getPackageManager().getLaunchIntentForPackage("ginlemon.flowerfree");
                    Intent smartpro = context.getPackageManager().getLaunchIntentForPackage("ginlemon.flowerpro");
                    if (smart != null) {
                        Intent smartlauncherIntent = new Intent("ginlemon.smartlauncher.setGSLTHEME");
                        smartlauncherIntent.putExtra("package", BuildConfig.APPLICATION_ID);
                        context.startActivity(smartlauncherIntent);
                    } else if (smartpro != null) {
                        Intent smartlauncherIntent = new Intent("ginlemon.smartlauncher.setGSLTHEME");
                        smartlauncherIntent.putExtra("package", BuildConfig.APPLICATION_ID);
                        context.startActivity(smartlauncherIntent);
                    } else {
                        throw new ActivityNotFoundException();
                    }
                    break;
                }
                case NEXT: {
                    Intent nextApply = pm.getLaunchIntentForPackage(
                            "com.gtp.nextlauncher");
                    if (nextApply != null) {
                        Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction")
                                .putExtra("type", 1)
                                .putExtra("pkgname", BuildConfig.APPLICATION_ID)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(nextApply);
                        context.sendBroadcast(go);
                        Toast.makeText(context, R.string.finish_apply, Toast.LENGTH_LONG).show();
                    } else {
                        throw new ActivityNotFoundException();
                    }
                    break;
                }
                case GO: {
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.gau.go.launcherex");
                    if (intent != null) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction")
                                        .putExtra("type", 1)
                                        .putExtra("pkgname", context.getPackageName());
                                context.sendBroadcast(go);
                            }
                        }, 250);
                    } else {
                        throw new ActivityNotFoundException();
                    }
                    context.startActivity(intent);
                    break;
                }
                case HOLO:
                    Intent holo = new Intent(Intent.ACTION_MAIN)
                            .setComponent(new ComponentName("com.mobint.hololauncher.hd",
                                    "com.mobint.hololauncher.SettingsActivity"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(holo);
                        Toast.makeText(context, R.string.finish_holo_apply, Toast.LENGTH_LONG).show();
                    } catch (ActivityNotFoundException e) {
                        holo.setComponent(new ComponentName("com.mobint.hololauncher",
                                "com.mobint.hololauncher.SettingsActivity"));
                        context.startActivity(holo);
                        Toast.makeText(context, R.string.finish_holo_apply, Toast.LENGTH_LONG).show();
                    }
                    break;
                case SOLO:
                    context.startActivity(new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(new ComponentName(SOLO_LAUNCHER_PACKAGENAME, SOLO_LAUNCHER_CLASSNAME)));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent solo = new Intent(ACTION_APPLY_SOLO_THEME)
                                    .putExtra(SOLO_EXTRA_APPLY_THEME_PACKAGE, BuildConfig.APPLICATION_ID)
                                    .putExtra(SOLO_EXTRA_APPLY_THEME_NAME, res.getString(R.string.app_name));
                            context.sendBroadcast(solo);
                        }
                    }, 250);
                    break;
                case KK:
                    Intent kkApply = context.getPackageManager().getLaunchIntentForPackage("com.kk.launcher");
                    if (kkApply != null) {
                        Intent kk = new Intent("com.gridappsinc.launcher.action.THEME")
                                .putExtra("com.kk.launcher.theme.EXTRA_NAME", "theme_name")
                                .putExtra("com.kk.launcher.theme.EXTRA_PKG", BuildConfig.APPLICATION_ID);
                        context.startActivity(kkApply);
                        context.sendBroadcast(kk);
                    } else {
                        throw new ActivityNotFoundException();
                    }
                    break;
                case ATOM:
                    Intent atom = new Intent("com.dlto.atom.launcher.intent.action.ACTION_VIEW_THEME_SETTINGS")
                            .setPackage("com.dlto.atom.launcher")
                            .putExtra("packageName", BuildConfig.APPLICATION_ID)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(atom);
                    break;
                case INSPIRE:
                    Intent inspireMain = context.getPackageManager().getLaunchIntentForPackage("com.bam.android.inspirelauncher");
                    if (inspireMain != null) {
                        Intent inspire = new Intent("com.bam.android.inspirelauncher.action.ACTION_SET_THEME")
                                .putExtra("theme_name", BuildConfig.APPLICATION_ID);
                        context.startActivity(inspireMain);
                        context.sendBroadcast(inspire);
                    } else {
                        throw new ActivityNotFoundException();
                    }
                    break;
                case CMTE:
                    try {
                        Intent cmteMain = new Intent(Intent.ACTION_MAIN);
                        cmteMain.setClassName("org.cyanogenmod.theme.chooser", "org.cyanogenmod.theme.chooser.ChooserActivity");
                        cmteMain.putExtra("pkgName", BuildConfig.APPLICATION_ID);
                        context.startActivity(cmteMain);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, R.string.cmte_unavailable, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case LGHOME:
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName("com.lge.launcher2", "com.lge.launcher2.homesettings.HomeSettingsPrefActivity"));
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, R.string.lghome_unavailable, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case UNKNOWN:
                    break;
            }
        } catch (Throwable t) {
            cb.onNotInstalled();
        }
    }

    @NonNull
    private static String getDefaultLauncher(@NonNull Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }

    @Nullable
    @SuppressWarnings("ResourceType")
    public static String canQuickApply(@NonNull Context context) {
        final String pkg = getDefaultLauncher(context);
        if (launcherIdFromPkg(pkg) == -1) return null;
        return pkg;
    }
}