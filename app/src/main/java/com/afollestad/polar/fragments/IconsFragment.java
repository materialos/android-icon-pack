package com.afollestad.polar.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.afollestad.bridge.BridgeUtil;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.IconAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.dialogs.IconDetailsDialog;
import com.afollestad.polar.dialogs.ProgressDialogFragment;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;
import com.afollestad.polar.ui.base.ISelectionMode;
import com.afollestad.polar.util.DrawableXmlParser;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.TimerTask;

import butterknife.ButterKnife;

public class IconsFragment extends BasePageFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    IconAdapter mAdapter;
    RecyclerView mRecyclerView;

    public IconsFragment() {
    }

    public static IconsFragment create(boolean selectionMode) {
        IconsFragment frag = new IconsFragment();
        Bundle args = new Bundle();
        args.putBoolean("selection_mode", selectionMode);
        frag.setArguments(args);
        return frag;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void selectItem(final Activity context, Fragment context2, final DrawableXmlParser.Icon icon) {
        final Bitmap bmp;
        if (icon.getDrawableId(context) != 0) {
            //noinspection ConstantConditions
            bmp = ((BitmapDrawable) ResourcesCompat.getDrawable(context.getResources(),
                    icon.getDrawableId(context), null)).getBitmap();
        } else {
            return;
        }
        if (context instanceof ISelectionMode && ((ISelectionMode) context).inSelectionMode()) {
            if (((ISelectionMode) context).allowResourceResult()) {
                Intent.ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(context, icon.getDrawableId(context));
                context.setResult(Activity.RESULT_OK, new Intent()
                        .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource));
                context.finish();
                return;
            }
            final ProgressDialogFragment progress = ProgressDialogFragment.show((AppCompatActivity) context, R.string.please_wait);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File dest = context.getCacheDir();
                    dest.mkdirs();
                    dest = new File(dest, icon.getName() + ".png");
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(dest);
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, os);

                        Uri uri = FileProvider.getUriForFile(
                                context,
                                BuildConfig.APPLICATION_ID + ".fileProvider",
                                dest);
                        context.setResult(Activity.RESULT_OK, new Intent()
                                .putExtra(Intent.EXTRA_STREAM, uri)
                                .setData(uri)
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                        context.finish();
                    } catch (NullPointerException npe) {
                        dest.delete();
                        progress.dismiss();
                        if (!context.isFinishing()) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showError(context, new Exception("An error occurred while retrieving the icon. Tell the designer of this icon pack to make sure the FileProvider at the bottom of AndroidManifest.xml is using the correct app ID."));
                                }
                            });
                        }
                    } catch (final Exception e) {
                        dest.delete();
                        progress.dismiss();
                        if (!context.isFinishing()) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showError(context, e);
                                }
                            });
                        }
                    } finally {
                        progress.dismiss();
                        BridgeUtil.closeQuietly(os);
                    }
                }
            }).start();
        } else {
            FragmentManager fm;
            if (context2 != null) fm = context2.getChildFragmentManager();
            else fm = context.getFragmentManager();
            IconDetailsDialog.create(bmp, icon).show(fm, "ICON_DETAILS_DIALOG");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.icons, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(getString(R.string.search_icons));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if (getActivity() != null) {
            final BaseThemedActivity act = (BaseThemedActivity) getActivity();
            TintUtils.themeSearchView(act.getToolbar(), mSearchView, DialogUtils.resolveColor(act, R.attr.tab_icon_color));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        TextView emptyView = (TextView) v.findViewById(android.R.id.empty);
        emptyView.setText(R.string.no_results);

        final int gridWidth = Config.get().gridWidthIcons();
        mRecyclerView = ButterKnife.findById(v, android.R.id.list);

        mAdapter = new IconAdapter(getActivity(), gridWidth, new IconAdapter.ClickListener() {
            @Override
            public void onClick(View view, int section, int relative, int absolute) {
                selectItem(getActivity(), IconsFragment.this, mAdapter.getIcon(section, relative));
            }
        }, mRecyclerView);

        final GridLayoutManager lm = new GridLayoutManager(getActivity(), gridWidth);
        mAdapter.setLayoutManager(lm);
        mRecyclerView.setLayoutManager(lm);
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    void setListShown(boolean shown) {
        final View v = getView();
        if (v != null) {
            v.findViewById(android.R.id.list).setVisibility(shown ?
                    View.VISIBLE : View.GONE);
            v.findViewById(android.R.id.progress).setVisibility(shown ?
                    View.GONE : View.VISIBLE);
            v.findViewById(android.R.id.empty).setVisibility(shown && mAdapter.getItemCount() == 0 ?
                    View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getTitle() {
        return R.string.icons;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null) load();
    }

    private void load() {
        if (mAdapter.getItemCount() > 0) return;
        setListShown(false);
        final Handler mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int id = R.xml.drawable;
                int drawableDashboard = getResources().getIdentifier("drawable_dashboard", "xml", getActivity().getPackageName());
                if (drawableDashboard != 0)
                    id = drawableDashboard;
                final List<DrawableXmlParser.Category> categories = DrawableXmlParser.parse(getActivity(), id);
                mHandler.post(new TimerTask() {
                    @Override
                    public void run() {
                        mAdapter.set(categories);
                        setListShown(true);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        mAdapter.filter(s);
        setListShown(true);
        return false;
    }

    @Override
    public boolean onClose() {
        mAdapter.filter(null);
        setListShown(true);
        return false;
    }
}