package org.materialos.icons.activities;

import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.materialos.icons.R;
import org.materialos.icons.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DetailedWallpaperActivity extends AppCompatActivity {

    private final Target mWallPaperTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        WallpaperManager wm = WallpaperManager.getInstance(DetailedWallpaperActivity.this);
                        wm.setBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            showNoPicDialog();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };
    public String mWall;
    private String mSaveWallLocation, mPicName, mDialogContent;
    private final Target mWallCropTarget = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ImageView wall = (ImageView) findViewById(R.id.bigwall);
                        Uri wallUri = getLocalBitmapUri(wall);
                        if (wallUri != null) {
                            Intent setWall = new Intent(Intent.ACTION_ATTACH_DATA);
                            setWall.setDataAndType(wallUri, "image/*");
                            setWall.putExtra("png", "image/*");
                            startActivityForResult(Intent.createChooser(setWall, getString(R.string.set_as)), 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            showNoPicDialog();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            postponeEnterTransition();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_wallpaper);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(Util.convertToPixel(this,4));
        }
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_ab_detailed_wallpaper);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSaveWallLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + getResources().getString(R.string.walls_save_location);
        mPicName = getResources().getString(R.string.walls_prefix_name);

        mDialogContent = getResources().getString(R.string.download_done) + mSaveWallLocation;

        //TODO: This.
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isfirstrun", true);

        if (isFirstRun) {
            File folder = new File(mSaveWallLocation);
            if (!folder.exists())
                folder.mkdirs();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                    .putBoolean("isfirstrun", false).commit();

        }

        mImageView = (ImageView) findViewById(R.id.bigwall);
        mWall = getIntent().getStringExtra("wall");
        Picasso.with(this)
                .load(mWall)
                .into(mImageView, new Callback.EmptyCallback() {
                            @Override
                            public void onSuccess() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                    startPostponedEnterTransition();
                            }
                        }
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_walls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.download:
                File file = new File(mSaveWallLocation, mPicName + convertWallName(mWall) + ".png");
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                try {
                    FileOutputStream ostream = new FileOutputStream(file);
                    ((BitmapDrawable)mImageView.getDrawable()).getBitmap()
                            .compress(Bitmap.CompressFormat.PNG, -1, ostream);
                    ostream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showSaveCompletedDialog();
                break;

            case R.id.apply:
                showSetWallDialog();
                break;

            case android.R.id.home:
                finish();

        }
        return true;
    }

    private String convertWallName(String link) {
        return (link
                .replaceAll("png", "")                   // Deletes png extension
                .replaceAll("jpg", "")                   // Deletes jpg extension
                .replaceAll("jpeg", "")                  // Deletes jpeg extension
                .replaceAll("bmp", "")                   // Deletes bmp extension
                .replaceAll("[^a-zA-Z0-9\\p{Z}]", "")    // Remove all special characters and symbols
                .replaceFirst("^[0-9]+(?!$)", "")        // Remove all leading numbers unless they're all numbers
                .replaceAll("\\p{Z}", "_"))              // Replace all kinds of spaces with underscores
                .replaceAll(getResources().getString(R.string.replace_one), "")
                .replaceAll(getResources().getString(R.string.replace_two), "")
                .replaceAll(getResources().getString(R.string.replace_three), "")
                .replaceAll(getResources().getString(R.string.replace_four), "")
                .replaceAll(getResources().getString(R.string.replace_five), "")
                .replaceAll(getResources().getString(R.string.replace_six), "")
                .replaceAll(getResources().getString(R.string.replace_seven), "");

    }

    public void showSaveCompletedDialog() {
        new MaterialDialog.Builder(this)
                .content(mDialogContent)
                .negativeText(android.R.string.ok)
                .show();
    }

    public void showSetWallDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.set_wall_title)
                .content(R.string.set_wall_content)
                .positiveText(R.string.set_it)
                .neutralText(R.string.crop_wall)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        showSettingWallDialog(false);
                        Picasso.with(DetailedWallpaperActivity.this)
                                .load(mWall)
                                .into(mWallPaperTarget);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        Picasso.with(DetailedWallpaperActivity.this)
                                .load(mWall)
                                .into(mWallCropTarget);
                    }
                }).show();
    }

    //TODO: What in the world is this waiting?
    public void showSettingWallDialog(boolean indeterminate) {
        if (indeterminate) {
            new MaterialDialog.Builder(this)
                    .title(R.string.setting_wall_title)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .show();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.setting_wall_title)
                    .content(R.string.please_wait)
                    .progress(false, 60)
                    .showListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            final MaterialDialog dialog = (MaterialDialog) dialogInterface;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while (dialog.getCurrentProgress() != dialog.getMaxProgress()) {
                                        if (dialog.isCancelled())
                                            break;
                                        try {
                                            Thread.sleep(50);
                                        } catch (InterruptedException e) {
                                            break;
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.incrementProgress(1);
                                            }
                                        });
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.setTitle(getString(R.string.done));
                                            dialog.setContent(getString(R.string.set_as_wall_done));
                                            dialog.setActionButton(DialogAction.NEGATIVE, R.string.close);
                                        }
                                    });
                                }
                            }).start();
                        }
                    }).show();
        }
    }

    private void showNoPicDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(R.string.wall_error)
                .positiveText(android.R.string.ok)
                .show();
    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable)
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        else
            return null;
        Uri bmpUri = null;
        try {
            File file = new File(mSaveWallLocation, mPicName + convertWallName(mWall) + ".png");
            file.getParentFile().mkdirs();
            file.delete();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
