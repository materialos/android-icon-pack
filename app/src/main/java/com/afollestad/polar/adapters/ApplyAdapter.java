package com.afollestad.polar.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.util.ApplyUtil;
import com.afollestad.polar.util.TintUtils;

import java.util.Arrays;
import java.util.Comparator;

import butterknife.ButterKnife;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ApplyAdapter extends RecyclerView.Adapter<ApplyAdapter.ApplyVH> {

    public static class Launcher {

        @DrawableRes
        public final int icon;
        public final String title;
        public final String pkg;
        @ColorInt
        public final int color;
        @ColorInt
        public final int colorDark;
        public final boolean isInstalled;

        public Launcher(Context context, @DrawableRes int icon, String title, String pkg, @ColorInt int color) {
            this.icon = icon;
            this.title = title;
            this.pkg = pkg;
            this.color = color;
            this.colorDark = TintUtils.darkenColor(this.color);
            this.isInstalled = checkInstalled(context);
        }

        private boolean checkInstalled(Context context) {
            PackageManager packageManager = context.getPackageManager();
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(pkg, 0);
                return applicationInfo != null;
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        }
    }

    public static class LauncherSorter implements Comparator<Launcher> {

        private static int booleanCompare(boolean lhs, boolean rhs) {
            return lhs == rhs ? 0 : lhs ? 1 : -1;
        }

        @Override
        public int compare(Launcher lhs, Launcher rhs) {
            int installed = booleanCompare(rhs.isInstalled, lhs.isInstalled);
            if (installed == 0)
                return lhs.title.compareTo(rhs.title);
            return installed;
        }
    }

    public interface SelectionCallback {

        void onLauncherSelection(int index, String title, String pkg);
    }

    private Launcher[] mLaunchers;
    private final Context mContext;
    private final SelectionCallback mCallback;
    private Launcher mQuickApplyLauncher;

    public ApplyAdapter(Context context, SelectionCallback callback) {
        this.mContext = context;
        this.mCallback = callback;

        final int[] icons;
        TypedArray a = null;
        try {
            a = context.getResources().obtainTypedArray(R.array.launcher_icons);
            icons = new int[a.length()];
            for (int i = 0; i < a.length(); i++)
                icons[i] = a.getResourceId(i, -1);
        } finally {
            if (a != null)
                a.recycle();
        }

        final String[] titles = context.getResources().getStringArray(R.array.launcher_names);
        final String[] packages = context.getResources().getStringArray(R.array.launcher_packages);

        final String[] colorHexes = context.getResources().getStringArray(R.array.launcher_colors_primary);
        final int[] colors = new int[colorHexes.length];
        for (int i = 0; i < colorHexes.length; i++)
            colors[i] = Color.parseColor(colorHexes[i]);

        this.mLaunchers = new Launcher[titles.length];
        for (int i = 0; i < titles.length; i++)
            this.mLaunchers[i] = new Launcher(context, icons[i], titles[i], packages[i], colors[i]);
        Arrays.sort(this.mLaunchers, new LauncherSorter());
        setHasStableIds(false);

        final String quickApplyPkg = ApplyUtil.canQuickApply(mContext);
        if (quickApplyPkg != null) {
            for (Launcher launcher : mLaunchers) {
                final String pkg = launcher.pkg;
                if (pkg.contains("|")) {
                    final String[] splitPkg = pkg.split("\\|");
                    for (String sp : splitPkg) {
                        if (sp.equalsIgnoreCase(quickApplyPkg)) {
                            mQuickApplyLauncher = launcher;
                            break;
                        }
                    }
                    if (mQuickApplyLauncher != null) break;
                } else if (pkg.equalsIgnoreCase(quickApplyPkg)) {
                    mQuickApplyLauncher = launcher;
                    break;
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mQuickApplyLauncher != null && position == 0)
            return 1;
        return 0;
    }

    @Override
    public ApplyVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 1 ? R.layout.list_item_quickapply :
                        R.layout.list_item_apply, parent, false);
        return new ApplyVH(v);
    }

    @Override
    public void onBindViewHolder(ApplyVH holder, int position) {
        if (getItemViewType(position) == 1) {
            holder.icon.setColorFilter(DialogUtils.resolveColor(mContext, android.R.attr.textColorSecondary), PorterDuff.Mode.SRC_IN);
            return;
        }

        if (mQuickApplyLauncher != null)
            position--;
        final Launcher launcher = mLaunchers[position];
        holder.title.setText(launcher.title);
        holder.icon.setBackgroundColor(launcher.color);
        holder.title.setBackgroundColor(launcher.colorDark);
        holder.icon.setImageResource(launcher.icon);
        holder.card.setCardBackgroundColor(launcher.color);
    }

    @Override
    public int getItemCount() {
        int count = mLaunchers.length;
        if (mQuickApplyLauncher != null)
            count++;
        return count;
    }

    public class ApplyVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        final CardView card;
        final ImageView icon;
        final TextView title;

        public ApplyVH(View itemView) {
            super(itemView);
            this.card = ButterKnife.findById(itemView, R.id.card);
            this.icon = ButterKnife.findById(itemView, R.id.icon);
            this.title = ButterKnife.findById(itemView, R.id.title);
            if (this.card != null)
                this.card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mCallback != null) {
                int position = getAdapterPosition();
                final Launcher launcher;
                if (mQuickApplyLauncher != null && position == 0) {
                    launcher = mQuickApplyLauncher;
                } else if (mQuickApplyLauncher != null) {
                    launcher = mLaunchers[position - 1];
                } else {
                    launcher = mLaunchers[position];
                }
                String pkg = launcher.pkg;
                if (pkg.contains("|"))
                    pkg = pkg.split("\\|")[0];
                mCallback.onLauncherSelection(position, launcher.title, pkg);
            }
        }
    }
}