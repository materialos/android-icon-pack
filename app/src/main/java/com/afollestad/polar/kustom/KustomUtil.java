package com.afollestad.polar.kustom;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.annotation.StringRes;

import com.afollestad.bridge.BridgeUtil;
import com.afollestad.polar.R;
import com.afollestad.polar.fragments.KustomFragment;
import com.afollestad.polar.util.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Frank Monza (fmonza)
 */
public class KustomUtil {

    public static final String FOLDER_WALLPAPERS = "wallpapers";
    public static final String FOLDER_WIDGETS = "widgets";

    private final static String PKG_KLWP = "org.kustom.wallpaper";
    private final static String PKG_KWGT = "org.kustom.widget";

    private final static String EDITOR_KLWP = "org.kustom.lib.editor.WpAdvancedEditorActivity";
    private final static String EDITOR_KWGT = "org.kustom.widget.picker.WidgetPicker";


    private KustomUtil() {
    }

    @StringRes
    public static int getInstallMsg(String pkg) {
        if (PKG_KWGT.equals(pkg)) return R.string.made_for_kwgt;
        return R.string.made_for_klwp;
    }

    public static String getPkgByFolder(@NonNull @KustomDir String folder) {
        return FOLDER_WIDGETS.equals(folder) ? PKG_KWGT : PKG_KLWP;
    }

    public static String getEditorActivityByFolder(@NonNull @KustomDir String folder) {
        return FOLDER_WIDGETS.equals(folder) ? EDITOR_KWGT : EDITOR_KLWP;
    }

    private static void post(@Nullable Activity target, @NonNull Runnable runnable) {
        if (target == null || target.isFinishing()) return;
        target.runOnUiThread(runnable);
    }

    public static File getKustomPreviewCache(@NonNull Context context) {
        return new File(context.getExternalCacheDir(), "KustomPreviews");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void getPreviews(final Activity context, final PreviewCallback cb, final @NonNull @KustomDir String folder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AssetManager am = context.getAssets();
                    final String[] templates = am.list(folder);

                    if (templates == null || templates.length == 0) {
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                cb.onPreviewsLoaded(null, null, null);
                            }
                        });
                        return;
                    }

                    final File cacheDir = getKustomPreviewCache(context);
                    Utils.wipe(cacheDir);
                    cacheDir.mkdirs();
                    final ArrayList<KustomFragment.PreviewItem> results = new ArrayList<>();

                    for (String file : templates) {
                        final File kFileCache = new File(cacheDir, file);
                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            is = am.open(folder + "/" + file);
                            os = new FileOutputStream(kFileCache);
                            Utils.copy(is, os);
                            BridgeUtil.closeQuietly(is);
                            BridgeUtil.closeQuietly(os);

                            if (kFileCache.exists()) {
                                final String widgetName = Utils.removeExtension(kFileCache.getName());
                                final ZipFile zipFile = new ZipFile(kFileCache);
                                final Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
                                JSONObject info = null;
                                File webpFile = null;
                                ZipEntry entry;
                                while ((entry = entryEnum.nextElement()) != null) {
                                    if (entry.getName().endsWith("preset_thumb_portrait.jpg")) {
                                        webpFile = new File(cacheDir, widgetName + ".webp");
                                        InputStream zis = null;
                                        OutputStream pos = null;
                                        try {
                                            zis = zipFile.getInputStream(entry);
                                            pos = new FileOutputStream(webpFile);
                                            Utils.copy(zis, pos);
                                        } finally {
                                            BridgeUtil.closeQuietly(zis);
                                            BridgeUtil.closeQuietly(pos);
                                        }
                                    }
                                    if (entry.getName().endsWith("preset.json")) {
                                        InputStream zis = null;
                                        try {
                                            zis = zipFile.getInputStream(entry);
                                            BufferedReader streamReader = new BufferedReader(new InputStreamReader(zis, "UTF-8"));
                                            StringBuilder responseStrBuilder = new StringBuilder();
                                            String inputStr;
                                            while ((inputStr = streamReader.readLine()) != null)
                                                responseStrBuilder.append(inputStr);
                                            JSONObject preset = new JSONObject(responseStrBuilder.toString());
                                            info = preset.getJSONObject("preset_info");
                                        } finally {
                                            BridgeUtil.closeQuietly(zis);
                                            BridgeUtil.closeQuietly(os);
                                        }
                                    }
                                    if (info != null && webpFile != null) {
                                        results.add(new KustomFragment.PreviewItem(info, folder,
                                                kFileCache.getName(), webpFile.getAbsolutePath()));
                                        break;
                                    }
                                }
                            }
                        } catch (final Exception e) {
                            if (cb != null) {
                                post(context, new Runnable() {
                                    @Override
                                    public void run() {
                                        cb.onPreviewsLoaded(null, null, e);
                                    }
                                });
                            }
                            break;
                        } finally {
                            kFileCache.delete();
                            BridgeUtil.closeQuietly(is);
                            BridgeUtil.closeQuietly(os);
                        }
                    }

                    if (cb != null) {
                        final Drawable wallpaperDrawable = WallpaperManager.getInstance(context).getDrawable();
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                cb.onPreviewsLoaded(results, wallpaperDrawable, null);
                            }
                        });
                    }
                } catch (final Exception e) {
                    if (cb != null) {
                        post(context, new Runnable() {
                            @Override
                            public void run() {
                                cb.onPreviewsLoaded(null, null, e);
                            }
                        });
                    }
                }
            }
        }).start();
    }

    @StringDef({FOLDER_WALLPAPERS, FOLDER_WIDGETS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface KustomDir {
    }

    public interface PreviewCallback {
        void onPreviewsLoaded(ArrayList<KustomFragment.PreviewItem> previews, Drawable wallpaper, Exception error);
    }
}