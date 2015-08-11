package com.jahirfiquitiva.paperboard.fragments;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jahirfiquitiva.paperboard.util.Util;

import org.materialos.icons.R;

import java.util.ArrayList;

public class IconsFragment extends Fragment {

    private static final String INIT_ICON_NAMES_IDS = "iconsNameId";

    private String[] mIconNames;

    public static IconsFragment newInstance(int iconNames) {
        IconsFragment fragment = new IconsFragment();
        Bundle args = new Bundle();
        args.putInt(INIT_ICON_NAMES_IDS, iconNames);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IconAdapter icAdapter = new IconAdapter();
        View view = inflater.inflate(R.layout.grid_icons, container, false);
        GridView gridview = (GridView) view.findViewById(R.id.icons_grid);
        gridview.setColumnWidth(Util.convertToPixel(getActivity(), 72) + Util.convertToPixel(getActivity(), 4));
        gridview.setAdapter(icAdapter);
        return view;
    }

    private String getDrawableName(String name) {
        String partialConvertedText = name.toLowerCase();
        String[] words = partialConvertedText.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == words.length - 1 && word.startsWith("alt")) {
                sb.append("_").append(word);
            } else {
                sb.append(word);
            }
        }
        return sb.toString();
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
                convertView = inflater.inflate(R.layout.list_item_icon, parent, false);
                holder = new IconsHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (IconsHolder) convertView.getTag();
            }

            holder.icon.startAnimation(anim);
            holder.icon.setImageResource(mThumbs.get(position));
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogIconView = View.inflate(getActivity(), R.layout.dialog_icon, null);
                    ImageView dialogIcon = (ImageView) dialogIconView.findViewById(R.id.dialogicon);
                    dialogIcon.setImageResource(mThumbs.get(position));
                    String name = mIconNames[position];
                    new MaterialDialog.Builder(getActivity())
                            .customView(dialogIconView, false)
                            .title(name)
                            .positiveText(R.string.close)
                            .show();
                }
            });

            return convertView;
        }

        private void loadIcon() {
            mThumbs = new ArrayList<>();
            final Resources resources = getResources();
            final String packageName = getActivity().getApplication().getPackageName();
            addIcon(resources, packageName, getArguments().getInt(INIT_ICON_NAMES_IDS, 0));
        }

        private void addIcon(Resources resources, String packageName, int list) {
            mIconNames = resources.getStringArray(list);
            for (String iconName : mIconNames) {
                iconName = getDrawableName(iconName);
                int res = resources.getIdentifier(iconName, "drawable", packageName);
                if (res != 0) {
                    final int thumbRes = resources.getIdentifier(iconName, "drawable", packageName);
                    if (thumbRes != 0)
                        mThumbs.add(thumbRes);
                }
            }
        }

        class IconsHolder {

            final ImageView icon;

            IconsHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.icon_img);
            }
        }

    }
}