package org.materialos.icons.adapters;

import android.content.Context;
import android.content.Intent;
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
import org.materialos.icons.util.Utils;
import com.bumptech.glide.Glide;

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

    private final int[] mWidgetPreviews;
    private final String[] mWidgetNames;
    private ArrayList<Integer> mFilteredPreviews;
    private ArrayList<String> mFilteredNames;

    public ZooperAdapter(Context context) {
        mWidgetPreviews = Utils.resolveResourceIds(context, R.array.zooper_widget_previews);
        mWidgetNames = context.getResources().getStringArray(R.array.zooper_widget_names);
        if (mWidgetPreviews.length != mWidgetNames.length)
            throw new IllegalStateException("Zooper widget previews and names arrays must have the same number of items.");
    }

    public void filter(String name) {
        if (name == null || name.trim().isEmpty()) {
            synchronized (mWidgetNames) {
                mFilteredPreviews.clear();
                mFilteredPreviews = null;
                mFilteredNames.clear();
                mFilteredNames = null;
                return;
            }
        }

        synchronized (mWidgetNames) {
            mFilteredPreviews = new ArrayList<>();
            mFilteredNames = new ArrayList<>();
            name = name.toLowerCase(Locale.getDefault());
            for (int i = 0; i < mWidgetNames.length; i++) {
                if (mFilteredNames.size() == SEARCH_RESULT_LIMIT)
                    break;
                if (mWidgetNames[i].toLowerCase(Locale.getDefault())
                        .contains(name)) {
                    mFilteredNames.add(mWidgetNames[i]);
                    mFilteredPreviews.add(mWidgetPreviews[i]);
                }
            }
            if (mFilteredNames.size() == 0) {
                mFilteredNames = null;
                mFilteredPreviews = null;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
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
            position--;
            Glide.with(holder.itemView.getContext())
                    .load(mWidgetPreviews[position])
                    .into(holder.image);
            if (position < mWidgetNames.length) {
                holder.name.setText(mWidgetNames[position]);
                holder.name.setVisibility(View.VISIBLE);
            } else holder.name.setVisibility(View.GONE);
        } else {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            lp.setFullSpan(true);
            holder.itemView.setLayoutParams(lp);

        }
    }

    @Override
    public int getItemCount() {
        return mWidgetPreviews.length + 1;
    }

    public static class ZooperVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView image;
        final TextView name;
        final CardView card;

        public ZooperVH(View itemView, boolean listenForClick) {
            super(itemView);
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