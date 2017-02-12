package com.afollestad.polar.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.polar.R;
import com.afollestad.polar.ui.IconMoreActivity;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.ui.base.ISelectionMode;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.Utils;
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IconAdapter extends SectionedRecyclerViewAdapter<IconAdapter.MainViewHolder>
        implements View.OnClickListener, View.OnTouchListener {

    public final static int SEARCH_RESULT_LIMIT = 20;

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            if (view.getTag() instanceof String) {
                // Grid item
                final String[] tag = view.getTag().toString().split(":");
                mListener.onClick(view,
                        Integer.parseInt(tag[0]),
                        Integer.parseInt(tag[1]),
                        Integer.parseInt(tag[2]));
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //More button
        if (event.getAction() == MotionEvent.ACTION_UP) {
            final int index = (Integer) v.getTag();
            final DrawableXmlParser.Category category = mFiltered != null ?
                    mFiltered.get(index) : mCategories.get(index);


            int topPadding = ((BaseThemedActivity) mContext).getLastStatusBarInsetHeight();
            float[] pressedLocation = new float[]{
                    event.getRawX(),
                    event.getRawY() - topPadding};

            int[] headerLocation = new int[2];
            ((View) v.getParent()).getLocationOnScreen(headerLocation);

            final Intent intent = new Intent(mContext, IconMoreActivity.class)
                    .putExtra(IconMoreActivity.EXTRA_CATEGORY, category)
                    .putExtra(IconMoreActivity.EXTRA_REVEAL_ANIM_LOCATION, pressedLocation);

            if (mContext instanceof ISelectionMode) {
                intent.putExtra("selection_mode", ((ISelectionMode) mContext).inSelectionMode());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTransitionSection = index;
                mTransitionViews = new SparseArray<>();

                notifyDataSetChanged();
                Utils.waitForLayout(mRecyclerView, new Utils.LayoutCallback<RecyclerView>() {
                    @Override
                    public void onLayout(RecyclerView view) {
                        List<Pair<View, String>> pairs = asList(mTransitionViews);
                        //noinspection unchecked
                        final Bundle activityOptions =
                                ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext,
                                        pairs.toArray(new Pair[pairs.size()])).toBundle();

                        ((Activity) mContext).startActivityForResult(intent, 6969, activityOptions);
                    }
                });
            } else {
                ((Activity) mContext).startActivityForResult(intent, 6969);
            }
        }
        return true;
    }

    public static <C> List<C> asList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    public interface ClickListener {
        void onClick(View view, int section, int relative, int absolute);
    }

    public IconAdapter(Context context, int gridWidth, ClickListener listener, RecyclerView recyclerView) {
        mContext = context;
        mIconsPerSection = gridWidth * 2;
        mListener = listener;
        mCategories = new ArrayList<>();
        mRecyclerView = recyclerView;
    }

    final Context mContext;
    private final RecyclerView mRecyclerView;
    final int mIconsPerSection;
    private final ClickListener mListener;
    final ArrayList<DrawableXmlParser.Category> mCategories;
    ArrayList<DrawableXmlParser.Category> mFiltered;

    private int mTransitionSection = -1;
    SparseArray<Pair<View, String>> mTransitionViews;

    public void filter(String str) {
        if (str == null || str.trim().isEmpty()) {
            mFiltered = null;
            notifyDataSetChanged();
            return;
        }

        str = str.toLowerCase(Locale.getDefault());
        mFiltered = new ArrayList<>();

        for (DrawableXmlParser.Category cat : mCategories) {
            DrawableXmlParser.Category include = null;
            for (DrawableXmlParser.Icon icon : cat.getIcons()) {
                if (mFiltered.size() == SEARCH_RESULT_LIMIT)
                    break; // limit number of search results to reduce computation time
                if (icon.getName().toLowerCase(Locale.getDefault()).contains(str)) {
                    if (include != null) {
                        if (include.getName().equalsIgnoreCase(icon.getCategory().getName())) {
                            include.addItem(icon);
                        } else {
                            mFiltered.add(include);
                            include = null;
                        }
                    }
                    if (include == null) {
                        include = new DrawableXmlParser.Category(icon.getCategory().getName());
                        include.addItem(icon);
                    }
                }
            }
            if (include != null) mFiltered.add(include);
        }
        notifyDataSetChanged();
    }

    public void set(List<DrawableXmlParser.Category> categories) {
        mCategories.clear();
        mCategories.addAll(categories);
        notifyDataSetChanged();
    }

    public static class MainViewHolder extends RecyclerView.ViewHolder {

        public MainViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            moreButton = (Button) itemView.findViewById(R.id.moreButton);
        }

        final ImageView image;
        final TextView title;
        final Button moreButton;
    }

    public DrawableXmlParser.Icon getIcon(int section, int relative) {
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        return category.getIcons().get(relative);
    }

    @Override
    public int getSectionCount() {
        return mFiltered != null ? mFiltered.size() : mCategories.size();
    }

    @Override
    public int getItemCount(int section) {
        int count = mFiltered != null ? mFiltered.get(section).size() : mCategories.get(section).size();
        if (mCategories.size() > 1 && count > mIconsPerSection)
            return mIconsPerSection;
        return count;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                viewType == VIEW_TYPE_HEADER ? R.layout.list_item_icon_header : R.layout.list_item_icon, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindHeaderViewHolder(MainViewHolder holder, int section) {
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        holder.title.setText(category.getName());

        if (mCategories.size() > 1 && category.size() > mIconsPerSection) {
            holder.moreButton.setVisibility(View.VISIBLE);
            holder.moreButton.setTag(section);
            holder.moreButton.setOnTouchListener(this);
            holder.moreButton.setText(holder.itemView.getContext().getString(
                    R.string.more_x, category.size() - mIconsPerSection));
        } else {
            holder.moreButton.setVisibility(View.INVISIBLE);
            holder.moreButton.setTag(null);
            holder.moreButton.setOnClickListener(null);
        }
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int section, int relativePos, int absolutePos) {
        final Context c = holder.itemView.getContext();
        final DrawableXmlParser.Category category = mFiltered != null ?
                mFiltered.get(section) : mCategories.get(section);
        final int res = category.getIcons().get(relativePos).getDrawableId(c);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mTransitionSection == section) {
                final String transitionName = mContext.getString(R.string.transition_name_recyclerview_item) + relativePos;
                holder.itemView.setTransitionName(transitionName);
                mTransitionViews.put(relativePos, new Pair<>(holder.itemView, transitionName));
            } else {
                holder.itemView.setTransitionName("");
            }
        }

        holder.image.setBackground(null);
        holder.image.setImageDrawable(null);

        if (res == 0) {
            holder.image.setBackgroundColor(Color.parseColor("#40000000"));
        } else {
            Glide.with(c)
                    .fromResource()
                    .load(res)
                    .into(holder.image);
        }

        holder.itemView.setTag(String.format(Locale.getDefault(), "%d:%d:%d", section, relativePos, absolutePos));
        holder.itemView.setOnClickListener(this);
    }
}