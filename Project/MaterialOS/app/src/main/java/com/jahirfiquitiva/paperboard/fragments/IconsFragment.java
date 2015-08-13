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
import android.widget.TextView;

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

    private String getUiName(String name) {
        if (name.contains(",")) {
            return name.substring(0, name.indexOf(","));
        } else {
            return name;
        }
    }

    private String getDrawableName(String name) {
        String lowerCase = name.toLowerCase();

        if (lowerCase.contains(",")) {
            int first = lowerCase.indexOf(",") + 1;
            int last = lowerCase.lastIndexOf(",");
            if (lowerCase.lastIndexOf(",") != lowerCase.indexOf(",")) {
                return lowerCase.substring(first, last);
            } else {
                lowerCase = lowerCase.substring(0, lowerCase.indexOf(","));
            }
        }

        String[] words = lowerCase.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.equals("alt")) {
                sb.append("_").append(word);
                if (words.length > i + 1) {
                    sb.append(words[i + 1]);
                }
                i++;
            } else {
                sb.append(word);
            }
        }
        return sb.toString();
    }

    private String getDesignerName(String name) {
        if (name.contains(",")) {
            return name.substring(name.lastIndexOf(",") + 1, name.length());
        } else {
            return null;
        }
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

            holder.title.setText(getUiName(mIconNames[position]));

            holder.icon.startAnimation(anim);
            holder.icon.setImageResource(mThumbs.get(position));
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View dialogIconView = View.inflate(getActivity(), R.layout.dialog_icon, null);
                    ImageView dialogIcon = (ImageView) dialogIconView.findViewById(R.id.dialog_icon_image);
                    dialogIcon.setImageResource(mThumbs.get(position));

                    TextView designer = (TextView) dialogIconView.findViewById(R.id.dialog_icon_designer_text);
                    String designerName = getDesignerName(mIconNames[position]);
                    if (designerName != null) {
                        designer.setText(getString(R.string.designer) + designerName);
                    } else {
                        designer.setVisibility(View.GONE);
                    }

                    String name = getUiName(mIconNames[position]);
                    new MaterialDialog.Builder(getActivity())
                            .customView(dialogIconView, true)
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
            final TextView title;

            IconsHolder(View v) {
                icon = (ImageView) v.findViewById(R.id.icon_img);
                title = (TextView) v.findViewById(R.id.list_item_icon_name_text);
                if (!getResources().getBoolean(R.bool.config_icon_name_displayed)) {
                    title.setVisibility(View.GONE);
                }
            }
        }

    }
}