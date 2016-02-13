package org.materialos.icons.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.polar.R;
import org.materialos.icons.util.DrawableXmlParser;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class IconMoreAdapter extends RecyclerView.Adapter<IconMoreAdapter.MainViewHolder> {

    private final int mIconsInAnimation;

    public interface ClickListener {
        void onClick(View view, int index);
    }

    public IconMoreAdapter(ClickListener listener, int gridWidth, Context context) {
        mListener = listener;
        mContext = context;
        mIcons = new ArrayList<>();
        mIconsInAnimation = gridWidth * 2;

        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return mIcons.get(position).getUniqueId();
    }

    final Context mContext;
    final ClickListener mListener;
    private final List<DrawableXmlParser.Icon> mIcons;

    public void set(List<DrawableXmlParser.Icon> icons) {
        mIcons.clear();
        mIcons.addAll(icons);
        notifyDataSetChanged();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        public MainViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(v, getAdapterPosition());
                }
            });
        }

        final ImageView image;
    }

    public DrawableXmlParser.Icon getIcon(int index) {
        return mIcons.get(index);
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_icon, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        final Context c = holder.itemView.getContext();
        final int res = mIcons.get(position).getDrawableId(c);

        if (position < mIconsInAnimation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = mContext.getString(R.string.transition_name_recyclerview_item) + position;
            holder.itemView.setTransitionName(transitionName);
        }

        if (res == 0) {
            holder.image.setBackgroundColor(Color.parseColor("#40000000"));
        } else {
            holder.image.setBackground(null);
            Glide.with(c)
                    .fromResource()
                    .load(res)
                    .into(holder.image);
        }
    }
}