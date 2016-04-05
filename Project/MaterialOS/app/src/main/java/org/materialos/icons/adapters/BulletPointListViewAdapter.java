package org.materialos.icons.adapters;

/**
 * @author Aidan Follestad (afollestad)
 */

import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.materialos.icons.R;

import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BulletPointListViewAdapter extends BaseAdapter {

    private final CharSequence[] mItems;

    public BulletPointListViewAdapter(@NonNull Context context, @ArrayRes int items) {
        mItems = context.getResources().getTextArray(items);
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.length : 0;
    }

    @Override
    public Object getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_bullet, parent, false);
        }
        TextView title = ButterKnife.findById(convertView, R.id.title);
        title.setText(Html.fromHtml(mItems[position].toString()));
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
