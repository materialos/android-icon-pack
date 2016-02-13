package org.materialos.icons.zooper;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.bridge.BridgeUtil;
import com.afollestad.polar.R;
import org.materialos.icons.dialogs.ProgressDialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ZooperUtil {

    public static final String FOLDER_ICONSETS = "IconSets";
    public static final String FOLDER_FONTS = "fonts";
    public static final String FOLDER_BITMAPS = "bitmaps";

    private static final int BUFFER_SIZE = 2048;

    private static void LOG(@NonNull String msg, @Nullable Object... args) {
        if (args != null)
            msg = String.format(msg, args);
        Log.d("ZooperUtil", msg);
    }

    @StringDef({FOLDER_ICONSETS, FOLDER_FONTS, FOLDER_BITMAPS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ZooperDir {
    }

    @NonNull
    public static File getZooperWidgetDir() {
        return new File(Environment.getExternalStorageDirectory(), "ZooperWidget");
    }

    @NonNull
    public static File getZooperWidgetDir(@NonNull @ZooperDir String folder) {
        return new File(getZooperWidgetDir(), folder);
    }

    public static boolean checkInstalled(@NonNull Context context, @NonNull @ZooperDir String folder) {
        final AssetManager assetManager = context.getAssets();
        final String[] files;
        try {
            files = assetManager.list(folder);
        } catch (IOException e) {
            // Assume this means the folder doesn't exist, which means we don't need to install it.
            // So, say it is installed.
            return true;
        }
        for (String filename : files) {
            final File file = new File(getZooperWidgetDir(folder), filename);
            if (!file.exists()) return false;
        }
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void install(@NonNull Context context, @NonNull @ZooperDir String folder) throws IOException {
        getZooperWidgetDir(folder).mkdirs();
        final AssetManager assetManager = context.getAssets();
        final String[] files = assetManager.list(folder);
        for (String filename : files) {
            final File dest = new File(getZooperWidgetDir(folder), filename);
            LOG("Installing %s -> %s", filename, dest.getAbsolutePath());
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(String.format("%s%s%s", folder, File.separator, filename));
                out = new FileOutputStream(dest);
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1)
                    out.write(buffer, 0, read);
                out.flush();
            } finally {
                BridgeUtil.closeQuietly(in);
                BridgeUtil.closeQuietly(out);
            }
        }
    }

    public interface CheckResult {
        void onCheckResult(boolean fontsInstalled, boolean iconsetsInstalled, boolean bitmapsInstalled);
    }

    private static void post(@Nullable Activity target, @NonNull Runnable runnable) {
        if (target == null || target.isFinishing()) return;
        target.runOnUiThread(runnable);
    }

    public static void checkInstalled(@NonNull final Activity context, @NonNull final CheckResult result) {
        final ProgressDialogFragment dialog = ProgressDialogFragment.show((AppCompatActivity) context, R.string.please_wait);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean fontsInstalled = checkInstalled(context, FOLDER_FONTS);
                if (context.isFinishing()) {
                    dialog.dismiss();
                    return;
                }
                final boolean iconsetsInstalled = checkInstalled(context, FOLDER_ICONSETS);
                if (context.isFinishing()) {
                    dialog.dismiss();
                    return;
                }
                final boolean bitmapsInstalled = checkInstalled(context, FOLDER_BITMAPS);
                post(context, new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        result.onCheckResult(fontsInstalled, iconsetsInstalled, bitmapsInstalled);
                    }
                });
            }
        }).start();
    }

    public interface InstallResult {
        void onInstallResult(Exception e);
    }

    public static void install(@NonNull final Activity context, final boolean fonts, final boolean iconsets,
                               final boolean bitmaps, @NonNull final InstallResult result) {
        final ProgressDialogFragment dialog = ProgressDialogFragment.show((AppCompatActivity) context, R.string.please_wait);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (fonts) {
                    dialog.setContent(R.string.installing_fonts);
                    try {
                        install(context, FOLDER_FONTS);
                    } catch (final IOException e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                result.onInstallResult(new Exception("Failed to install fonts.", e));
                            }
                        });
                        return;
                    }
                }
                if (context.isFinishing()) {
                    dialog.dismiss();
                    return;
                }
                if (iconsets) {
                    dialog.setContent(R.string.installing_iconsets);
                    try {
                        install(context, FOLDER_ICONSETS);
                    } catch (final IOException e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                result.onInstallResult(new Exception("Failed to install iconsets.", e));
                            }
                        });
                        return;
                    }
                }
                if (context.isFinishing()) {
                    dialog.dismiss();
                    return;
                }
                if (bitmaps) {
                    dialog.setContent(R.string.installing_bitmaps);
                    try {
                        install(context, FOLDER_BITMAPS);
                    } catch (final IOException e) {
                        dialog.dismiss();
                        e.printStackTrace();
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                result.onInstallResult(new Exception("Failed to install bitmaps.", e));
                            }
                        });
                        return;
                    }
                }
                post(context, new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        result.onInstallResult(null);
                    }
                });
            }
        }).start();
    }

    private ZooperUtil() {
    }
}