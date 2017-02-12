package com.afollestad.polar.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.VC;
import com.afollestad.polar.views.SplitButtonsLayout;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.ButterKnife;


public class AboutAdapter extends RecyclerView.Adapter<AboutAdapter.MainViewHolder> implements View.OnClickListener {

    public static class AboutItem {

        public final String coverImage;
        public final String profileImage;
        public final String title;
        public final String description;
        public final String[] buttonNames;
        public final String[] buttonLinks;

        public AboutItem(String coverImage, String profileImage,
                         String title, String description, String[] buttonNames, String[] buttonLinks) {
            this.coverImage = coverImage;
            this.profileImage = profileImage;
            this.title = title;
            this.description = description;
            this.buttonNames = buttonNames;
            this.buttonLinks = buttonLinks;
        }
    }

    public interface OptionsClickListener {
        void onOptionFeedback();

        void onOptionDonate();
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() instanceof String) {
            try {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse((String) view.getTag())));
            } catch (Exception e) {
                Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public AboutAdapter(Activity context, OptionsClickListener cb) {
        mContext = context;

        final Resources r = context.getResources();
        final String[] titles = r.getStringArray(R.array.about_titles);
        final String[] descriptions = r.getStringArray(R.array.about_descriptions);
        final String[] images = r.getStringArray(R.array.about_images);
        final String[] covers = r.getStringArray(R.array.about_covers);

        final String[] buttonNames2d = r.getStringArray(R.array.about_buttons_names);
        final String[][] buttonNames3d = new String[buttonNames2d.length][];
        for (int i = 0; i < buttonNames2d.length; i++)
            buttonNames3d[i] = buttonNames2d[i].split("\\|");

        final String[] buttonLinks2d = r.getStringArray(R.array.about_buttons_links);
        final String[][] buttonLinks3d = new String[buttonLinks2d.length][];
        for (int i = 0; i < buttonLinks2d.length; i++)
            buttonLinks3d[i] = buttonLinks2d[i].split("\\|");

        mItems = new ArrayList<>(titles.length);
        for (int i = 0; i < titles.length; i++) {
            mItems.add(new AboutItem(covers[i], images[i], titles[i], descriptions[i],
                    buttonNames3d[i], buttonLinks3d[i]));
        }

        mOptionCb = cb;
        mOptionsEnabled = Config.get().feedbackEnabled() || Config.get().donationEnabled();
    }

    private final Context mContext;
    private final ArrayList<AboutItem> mItems;
    private final OptionsClickListener mOptionCb;
    private final boolean mOptionsEnabled;

    public static class MainViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public MainViewHolder(View itemView, OptionsClickListener optionsCb) {
            super(itemView);
            cover = ButterKnife.findById(itemView, R.id.cover);
            image = ButterKnife.findById(itemView, R.id.image);
            title = ButterKnife.findById(itemView, R.id.title);
            description = ButterKnife.findById(itemView, R.id.description);
            buttons = ButterKnife.findById(itemView, R.id.buttonsFrame);

            feedbackButton = ButterKnife.findById(itemView, R.id.feedbackButton);
            feedbackImage = ButterKnife.findById(itemView, R.id.feedbackImage);
            donateButton = ButterKnife.findById(itemView, R.id.donateButton);
            donateImage = ButterKnife.findById(itemView, R.id.donateImage);
            mOptionsCb = optionsCb;
            if (feedbackButton != null)
                feedbackButton.setOnClickListener(this);
            if (donateButton != null)
                donateButton.setOnClickListener(this);
        }

        final ImageView cover;
        final ImageView image;
        final TextView title;
        final TextView description;
        final SplitButtonsLayout buttons;

        final View feedbackButton;
        final ImageView feedbackImage;
        final View donateButton;
        final ImageView donateImage;
        private final OptionsClickListener mOptionsCb;

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.feedbackButton) {
                mOptionsCb.onOptionFeedback();
            } else {
                mOptionsCb.onOptionDonate();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mOptionsEnabled) {
            if (position == 0) return -1;
            position--;
        }
        return position;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        @LayoutRes
        int layoutRes = getLayoutResourceForViewType(viewType);
        final View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MainViewHolder(v, mOptionCb);
    }

    @LayoutRes
    private int getLayoutResourceForViewType(int viewType) {
        if (viewType == -1)
            return R.layout.list_item_about_options;
        return R.layout.list_item_about_person;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int index) {
        if (holder.title == null) {
            // Options
            final boolean feedbackEnabled = Config.get().feedbackEnabled();
            final boolean donationEnabled = Config.get().donationEnabled();
            final int accentColor = DialogUtils.resolveColor(mContext, R.attr.colorAccent);

            if (feedbackEnabled) {
                holder.feedbackImage.setImageDrawable(TintUtils.createTintedDrawable(
                        VC.get(R.drawable.ic_action_feedback), accentColor));
            } else {
                ((LinearLayout.LayoutParams) holder.donateButton.getLayoutParams()).weight = 2;
                holder.feedbackButton.setVisibility(View.GONE);
            }
            if (donationEnabled) {
                holder.donateImage.setImageDrawable(TintUtils.createTintedDrawable(
                        VC.get(R.drawable.ic_action_donate), accentColor));
            } else {
                ((LinearLayout.LayoutParams) holder.feedbackButton.getLayoutParams()).weight = 2;
                holder.donateButton.setVisibility(View.GONE);
            }
            return;
        }
        if (mOptionsEnabled)
            index--;

        final AboutItem item = mItems.get(index);
        holder.title.setText(item.title);
        holder.description.setText(Html.fromHtml(item.description));
        holder.description.setMovementMethod(LinkMovementMethod.getInstance());

        Glide.with(mContext)
                .load(item.coverImage)
                .into(holder.cover);
        Glide.with(mContext)
                .load(item.profileImage)
                .into(holder.image);

        if (item.buttonNames.length > 0) {
            holder.buttons.setButtonCount(item.buttonNames.length);
            if (!holder.buttons.hasAllButtons()) {
                if (item.buttonNames.length != item.buttonLinks.length)
                    throw new IllegalStateException("Button names and button links must have the same number of items (item " + index + ")");
                for (int i = 0; i < item.buttonNames.length; i++)
                    holder.buttons.addButton(item.buttonNames[i], item.buttonLinks[i]);
            }
        } else {
            holder.buttons.setVisibility(View.GONE);
        }

        for (int i = 0; i < holder.buttons.getChildCount(); i++)
            holder.buttons.getChildAt(i).setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        int count = mItems.size();
        if (mOptionsEnabled) count++;
        return count;
    }
}