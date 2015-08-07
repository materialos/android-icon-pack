package com.jahirfiquitiva.paperboard.adapters;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.graphics.Palette;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.jahirfiquitiva.paperboard.fragments.WallpapersFragment;
import com.jahirfiquitiva.paperboard.utilities.PaletteTransformation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import org.materialos.icons.R;

import static com.jahirfiquitiva.paperboard.utilities.PaletteTransformation.PaletteCallback;

public class WallsGridAdapter extends BaseAdapter {

    private final ArrayList<HashMap<String, String>> data;
    private final Context context;
    private final int numColumns;
    private boolean usePalette = true;

    public WallsGridAdapter(Context context, ArrayList<HashMap<String, String>> arraylist, int numColumns) {
        super();
        this.context = context;
        this.numColumns = numColumns;
        data = arraylist;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Animation anim = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        HashMap<String, String> jsondata = data.get(position);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int imageWidth = (width / numColumns);

        final WallsHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_wallpaper, parent, false);
            holder = new WallsHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (WallsHolder) convertView.getTag();

        }

        holder.name.setText(jsondata.get(WallpapersFragment.NAME));
        final String wallurl = jsondata.get(WallpapersFragment.WALL);
        holder.wall.startAnimation(anim);

        //noinspection SuspiciousNameCombination
        Picasso.with(context)
                .load(wallurl)
                .resize(imageWidth, imageWidth)
                .centerCrop()
                .noFade()
                .transform(PaletteTransformation.instance())
                .into(holder.wall,
                        new PaletteCallback(holder.wall) {
                            @Override
                            public void onSuccess(Palette palette) {
                                holder.progressBar.setVisibility(View.GONE);
                                if (usePalette) {
                                    if (palette != null) {
                                        Palette.Swatch wallSwatch = palette.getVibrantSwatch();
                                        if (wallSwatch != null) {
                                            holder.titleBg.setBackgroundColor(wallSwatch.getRgb());
                                            holder.titleBg.setAlpha(1);
                                            holder.name.setTextColor(wallSwatch.getTitleTextColor());
                                            holder.name.setAlpha(1);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onError() {
                            }
                        });

        return convertView;
    }

    class WallsHolder {

        final ImageView wall;
        final TextView name;
        final ProgressBar progressBar;
        final LinearLayout titleBg;
        final MaterialRippleLayout content;

        WallsHolder(View v) {
            wall = (ImageView) v.findViewById(R.id.wall);
            name = (TextView) v.findViewById(R.id.name);
            progressBar = (ProgressBar) v.findViewById(R.id.progress);
            titleBg = (LinearLayout) v.findViewById(R.id.titlebg);
            content = (MaterialRippleLayout) v.findViewById(R.id.walls_ripple);
        }
    }
}
