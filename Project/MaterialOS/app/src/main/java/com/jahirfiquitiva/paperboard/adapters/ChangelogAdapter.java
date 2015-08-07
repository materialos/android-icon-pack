package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.materialos.icons.R;

public class ChangelogAdapter extends BaseAdapter {

    private final Context mContext;
    private final String[][] mChangelog;

    public ChangelogAdapter(Context context, int rootArray) {

        // Save the context
        mContext = context;

        // Populate the two-dimensional array
        TypedArray typedArray = mContext.getResources().obtainTypedArray(rootArray);
        mChangelog = new String[typedArray.length()][];
        for (int i = 0; i < typedArray.length(); i++) {
            int id = typedArray.getResourceId(i, 0);
            if (id > 0) {
                mChangelog[i] = mContext.getResources().getStringArray(id);
            }
        }
        typedArray.recycle();
    }

    @Override
    public int getCount() {
        return mChangelog.length;
    }

    @Override
    public String[] getItem(int position) {
        return mChangelog[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.dialog_changelog_content, parent, false);
        }

        TextView versionName = (TextView) convertView.findViewById(R.id.changelog_versionname);
        TextView versionContent = (TextView) convertView.findViewById(R.id.changelog_versioncontent);

        String nameStr = mChangelog[position][0];
        String contentStr = "";

        for (int i = 1; i < mChangelog[position].length; i++) {
            if (i > 1) {
                // No need for new line on the first item
                contentStr += "\n";
            }
            contentStr += "\u2022 ";
            contentStr += mChangelog[position][i];
        }

        versionName.setText(nameStr);
        versionContent.setText(contentStr);

        return convertView;
    }
}
