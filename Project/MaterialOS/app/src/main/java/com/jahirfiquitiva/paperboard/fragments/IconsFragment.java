package com.jahirfiquitiva.paperboard.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.balysv.materialripple.MaterialRippleLayout;

import java.util.ArrayList;
import java.util.Locale;

import org.materialos.icons.R;

public class IconsFragment extends Fragment {

    private String[] iconsnames;
    public IconAdapter icAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        icAdapter = new IconAdapter();
        View view = inflater.inflate(R.layout.icons_grid, container, false);
        GridView gridview = (GridView) view.findViewById(R.id.icons_grid);
        gridview.setColumnWidth(convertToPixel(72) + convertToPixel(4));
        gridview.setAdapter(icAdapter);
        return view;
    }

    public static IconsFragment newInstance(int iconsArray) {
        IconsFragment fragment = new IconsFragment();
        Bundle args = new Bundle();
        args.putInt("iconsArrayId", iconsArray);
        fragment.setArguments(args);
        return fragment;
    }

    private class IconAdapter extends BaseAdapter {
        private ArrayList<Integer> mThumbs;

        public IconAdapter() {
            loadIcon();
        }

        @Override
        public int getCount() {
            return mThumbs.size();
        }

        @Override
        public Object getItem(int position) {
            return mThumbs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            IconsHolder holder;
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(R.layout.item_icon, parent, false);
                holder = new IconsHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (IconsHolder) convertView.getTag();
            }

            holder.icon.startAnimation(anim);
            holder.icon.setImageResource(mThumbs.get(position));
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogIconView = View.inflate(getActivity(), R.layout.dialog_icon, null);
                    ImageView dialogIcon = (ImageView) dialogIconView.findViewById(R.id.dialogicon);
                    dialogIcon.setImageResource(mThumbs.get(position));
                    String name = iconsnames[position].toLowerCase(Locale.getDefault());
                    new MaterialDialog.Builder(getActivity())
                            .customView(dialogIconView, false)
                            .title(convertText(name))
                            .positiveText(R.string.close)
                            .show();
                }
            });

            return convertView;
        }

        class IconsHolder {

            final ImageView icon;
            final MaterialRippleLayout content;

            IconsHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.icon_img);
                content = (MaterialRippleLayout) v.findViewById(R.id.icons_ripple);
            }
        }

        private void loadIcon() {
            mThumbs = new ArrayList<>();
            final Resources resources = getResources();
            final String packageName = getActivity().getApplication().getPackageName();
            addIcon(resources, packageName, getArguments().getInt("iconsArrayId", 0));
        }

        private void addIcon(Resources resources, String packageName, int list) {
            iconsnames = resources.getStringArray(list);
            for (String extra : iconsnames) {
                int res = resources.getIdentifier(extra, "drawable", packageName);
                if (res != 0) {
                    final int thumbRes = resources.getIdentifier(extra, "drawable", packageName);
                    if (thumbRes != 0)
                        mThumbs.add(thumbRes);
                }
            }
        }

    }

    private int convertToPixel(int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getActivity().getResources().getDisplayMetrics());
        return (int) px;
    }

    private String convertText(String name) {
        String partialConvertedText = name.replaceAll("_", " ");
        String[] text = partialConvertedText.split("\\s+");
        StringBuilder sb = new StringBuilder();
        if (text[0].length() > 0) {
            sb.append(Character.toUpperCase(text[0].charAt(0))).append(text[0].subSequence(1, text[0].length()).toString().toLowerCase());
            for (int i = 1; i < text.length; i++) {
                sb.append(" ");
                sb.append(Character.toUpperCase(text[i].charAt(0))).append(text[i].subSequence(1, text[i].length()).toString().toLowerCase());
            }
        }
        return sb.toString();
    }
}