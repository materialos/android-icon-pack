package com.afollestad.polar.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.afollestad.polar.fragments.ZooperFragment;
import com.afollestad.polar.util.Utils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ZooperAdapter extends RecyclerView.Adapter<ZooperAdapter.ZooperVH> {

    private final static String GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=%s";
    private final static String ZOOPER_PRO_PKG = "org.zooper.zwpro";
    public final static int SEARCH_RESULT_LIMIT = 10;

    private final Object LOCK = new Object();

    private String[] mWidgetNames;
    private final boolean mZooperInstalled;

    private Drawable mWallpaper;
    private ArrayList<ZooperFragment.PreviewItem> mPreviews;
    private ArrayList<ZooperFragment.PreviewItem> mPreviewsFiltered;
    private ArrayList<String> mFilteredNames;

    public ZooperAdapter(Context context) {
        mWidgetNames = context.getResources().getStringArray(R.array.zooper_widget_names);
        mZooperInstalled = Utils.isPkgInstalled(context, ZOOPER_PRO_PKG);
    }

    public void setPreviews(ArrayList<ZooperFragment.PreviewItem> previewFiles, Drawable wallpaper) {
        if (previewFiles == null)
            previewFiles = new ArrayList<>();
        this.mPreviews = previewFiles;
        if (mWidgetNames == null || mWidgetNames.length == 0) {
            mWidgetNames = new String[previewFiles.size()];
            for (int i = 0; i < previewFiles.size(); i++)
                mWidgetNames[i] = previewFiles.get(i).name;
        }
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
                if (mFilteredNames != null) {
                    mFilteredNames.clear();
                    mFilteredNames = null;
                }
                return;
            }
        }

        synchronized (LOCK) {
            mPreviewsFiltered = new ArrayList<>();
            mFilteredNames = new ArrayList<>();
            name = name.toLowerCase(Locale.getDefault());
            for (int i = 0; i < mWidgetNames.length; i++) {
                if (mFilteredNames.size() == SEARCH_RESULT_LIMIT)
                    break;
                if (mWidgetNames[i].toLowerCase(Locale.getDefault())
                        .contains(name)) {
                    mFilteredNames.add(mWidgetNames[i]);
                    mPreviewsFiltered.add(mPreviews.get(i));
                }
            }
            if (mFilteredNames.size() == 0) {
                mFilteredNames = null;
                mPreviewsFiltered = null;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && !mZooperInstalled)
            return 1;
        return 0;
    }

    @Override
    public ZooperVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType == 1 ?
                R.layout.list_item_zooper_header : R.layout.list_item_zooper, parent, false);
        return new ZooperVH(view, viewType == 1);
    }

    @Override
    public void onBindViewHolder(ZooperVH holder, int position) {
        if (holder.image != null) {
            if (getItemViewType(0) == 1) position--;
            final ZooperFragment.PreviewItem preview = mPreviewsFiltered != null ?
                    mPreviewsFiltered.get(position) : mPreviews.get(position);
            Glide.with(holder.itemView.getContext())
                    .load(new File(preview.path))
                    .into(holder.image);
            holder.background.setImageDrawable(mWallpaper);
            if (position < mWidgetNames.length) {
                holder.name.setText(mWidgetNames[position]);
                holder.name.setVisibility(View.VISIBLE);
            } else holder.name.setText(preview.name);
        } else {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            lp.setFullSpan(true);
            holder.itemView.setLayoutParams(lp);

        }
    }

    @Override
    public int getItemCount() {
        if (mPreviewsFiltered != null)
            return mPreviewsFiltered.size() + (!mZooperInstalled ? 1 : 0);
        else if (mPreviews != null && mPreviews.size() > 0)
            return mPreviews.size() + (!mZooperInstalled ? 1 : 0);
        else return 0;
    }

    public static class ZooperVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView background;
        final ImageView image;
        final TextView name;
        final CardView card;

        public ZooperVH(View itemView, boolean listenForClick) {
            super(itemView);
            background = ButterKnife.findById(itemView, R.id.background);
            image = ButterKnife.findById(itemView, R.id.image);
            name = ButterKnife.findById(itemView, R.id.name);
            card = ButterKnife.findById(itemView, R.id.card);
            if (listenForClick)
                card.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final Context c = view.getContext();
            if (Utils.isPkgInstalled(c, ZOOPER_PRO_PKG)) {
                Toast.makeText(c, R.string.zooper_already_installed, Toast.LENGTH_SHORT).show();
            } else {
                c.startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(String.format(GOOGLE_PLAY_URL, ZOOPER_PRO_PKG))));
            }
        }
    }
}