package com.afollestad.polar.adapters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.polar.R;
import com.afollestad.polar.fragments.KustomFragment;
import com.afollestad.polar.kustom.KustomUtil;
import com.afollestad.polar.util.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * @author Frank Monza (fmonza)
 */
public class KustomAdapter extends RecyclerView.Adapter<KustomAdapter.KustomVH> {

    private final static String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=%s";

    public final static int SEARCH_RESULT_LIMIT = 10;

    private final Object LOCK = new Object();

    private final boolean mKustomInstalled;
    private final String mFolder;

    private Drawable mWallpaper;
    private ArrayList<KustomFragment.PreviewItem> mPreviews;
    private ArrayList<KustomFragment.PreviewItem> mPreviewsFiltered;

    public KustomAdapter(Context context, final @NonNull @KustomUtil.KustomDir String folder) {
        mFolder = folder;
        mKustomInstalled = Utils.isPkgInstalled(context, KustomUtil.getPkgByFolder(folder));
    }

    public void setPreviews(ArrayList<KustomFragment.PreviewItem> previewFiles, Drawable wallpaper) {
        this.mPreviews = previewFiles;
        this.mWallpaper = wallpaper;
        notifyDataSetChanged();
    }

    public void filter(String name) {
        if (name == null || name.trim().isEmpty()) {
            synchronized (LOCK) {
                if (mPreviewsFiltered != null) {
                    mPreviewsFiltered.clear();
                    mPreviewsFiltered = null;
                }
            }
        }
        else {
            synchronized (LOCK) {
                mPreviewsFiltered = new ArrayList<>();
                name = name.toLowerCase(Locale.getDefault());
                for (int i = 0; i < mPreviews.size(); i++) {
                    if (mPreviewsFiltered.size() == SEARCH_RESULT_LIMIT)
                        break;
                    if (mPreviews.get(i).title
                            .toLowerCase(Locale.getDefault())
                            .contains(name)) {
                        mPreviewsFiltered.add(mPreviews.get(i));
                    }
                }
                if (mPreviewsFiltered.size() == 0) {
                    mPreviewsFiltered = null;
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && !mKustomInstalled)
            return 1;
        return 0;
    }

    @Override
    public KustomVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType == 1 ?
                R.layout.list_item_kustom_header : R.layout.list_item_kustom, parent, false);
        return new KustomVH(view, mFolder, viewType == 1);
    }

    @Override
    public void onBindViewHolder(KustomVH holder, int position) {
        if (holder.image != null) {
            if (getItemViewType(0) == 1) position--;
            final KustomFragment.PreviewItem preview = mPreviewsFiltered != null ?
                    mPreviewsFiltered.get(position) : mPreviews.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(new File(preview.previewPath))
                    .into(holder.image);
            holder.background.setImageDrawable(mWallpaper);
            holder.name.setText(preview.title);
            holder.file = preview.fileName;
        } else {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            lp.setFullSpan(true);
            holder.itemView.setLayoutParams(lp);
        }
        if (holder.title != null) {
            holder.title.setText(KustomUtil.getInstallMsg(KustomUtil.getPkgByFolder(mFolder)));
        }
    }

    @Override
    public int getItemCount() {
        if (mPreviewsFiltered != null)
            return mPreviewsFiltered.size() + (!mKustomInstalled ? 1 : 0);
        else if (mPreviews != null && mPreviews.size() > 0)
            return mPreviews.size() + (!mKustomInstalled ? 1 : 0);
        else return 0;
    }

    public static class KustomVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final String folder;
        String file;
        final boolean showInstaller;
        final ImageView background;
        final ImageView image;
        final TextView name;
        final TextView title;
        final CardView card;

        public KustomVH(View itemView, @NonNull String folder, boolean showInstaller) {
            super(itemView);
            this.folder = folder;
            this.showInstaller = showInstaller;
            background = ButterKnife.findById(itemView, R.id.background);
            image = ButterKnife.findById(itemView, R.id.image);
            name = ButterKnife.findById(itemView, R.id.name);
            card = ButterKnife.findById(itemView, R.id.card);
            title = ButterKnife.findById(itemView, R.id.title);
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final Context c = view.getContext();
            if (showInstaller) {
                String pkg = KustomUtil.getPkgByFolder(folder);
                if (!Utils.isPkgInstalled(c, pkg)) {
                    Toast.makeText(c, R.string.kustom_already_installed, Toast.LENGTH_SHORT).show();
                } else {
                    c.startActivity(new Intent(Intent.ACTION_VIEW)
                            .setData(Uri.parse(String.format(GOOGLE_PLAY_URL, pkg))));
                }
            } else {
                Intent i = new Intent();
                i.setComponent(new ComponentName(KustomUtil.getPkgByFolder(folder),
                        KustomUtil.getEditorActivityByFolder(folder)));
                i.setData(new Uri.Builder()
                        .scheme("kfile")
                        .authority(String.format("%s.kustomprovider", c.getPackageName()))
                        .appendPath(folder)
                        .appendPath(file)
                        .build());
                c.startActivity(i);
            }
        }
    }
}