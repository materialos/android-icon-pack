package com.afollestad.polar.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.KustomAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.kustom.KustomUtil;
import com.afollestad.polar.ui.MainActivity;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author Frank Monza (fmonza)
 */
public class KustomFragment extends BasePageFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static String ARG_FOLDER = "folder";

    @BindView(android.R.id.list)
    RecyclerView mRecyclerView;
    @BindView(android.R.id.empty)
    TextView mEmpty;
    @BindView(android.R.id.progress)
    View mProgress;

    private KustomAdapter mAdapter;
    private String mQueryText;
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.filter(mQueryText);
            setListShown(true);
        }
    };
    private ArrayList<PreviewItem> mPreviews;
    private Drawable mWallpaper;
    private Unbinder unbinder;

    public KustomFragment() {
    }

    public static KustomFragment newInstance(@KustomUtil.KustomDir String folder) {
        KustomFragment myFragment = new KustomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FOLDER, folder);
        myFragment.setArguments(args);
        return myFragment;
    }

    @Override
    public int getTitle() {
        if (KustomUtil.FOLDER_WIDGETS.equals(getFolder()))
            return R.string.kwgt;
        else return R.string.klwp;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.kustom, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(getString(getSearchHintRes()));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if (getActivity() != null) {
            final MainActivity act = (MainActivity) getActivity();
            TintUtils.themeSearchView(act.getToolbar(), mSearchView, DialogUtils.resolveColor(act, R.attr.tab_icon_color));
        }
    }

    private void setListShown(boolean shown) {
        final View v = getView();
        if (v != null) {
            mRecyclerView.setVisibility(shown ?
                    View.VISIBLE : View.GONE);
            mProgress.setVisibility(shown ?
                    View.GONE : View.VISIBLE);
            mEmpty.setVisibility(shown && mAdapter.getItemCount() == 0 ?
                    View.VISIBLE : View.GONE);
        }
    }

    @SuppressWarnings({"unchecked", "WrongConstant"})
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        final int kustomGridWidth = Config.get().gridWidthKustom();
        mAdapter = new KustomAdapter(getActivity(), getFolder());
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                kustomGridWidth, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        KustomUtil.getPreviews(getActivity(), new KustomUtil.PreviewCallback() {
            @Override
            public void onPreviewsLoaded(ArrayList<PreviewItem> previews, Drawable wallpaper, Exception error) {
                if (getActivity() == null || getActivity().isFinishing() || !isAdded())
                    return;
                else if (error != null) {
                    error.printStackTrace();
                    setListShown(true);
                    mAdapter.setPreviews(null, null);
                    mEmpty.setVisibility(View.VISIBLE);
                    if (error.getMessage().trim().isEmpty())
                        mEmpty.setText(error.toString());
                    else mEmpty.setText(error.getMessage());
                    return;
                }
                mPreviews = previews;
                mWallpaper = wallpaper;
                mAdapter.setPreviews(mPreviews, mWallpaper);
            }
        }, getFolder());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null && getActivity().isFinishing()) {
            Utils.wipe(KustomUtil.getKustomPreviewCache(getActivity()));
            mWallpaper = null;
            mPreviews = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mQueryText = newText;
        mRecyclerView.postDelayed(searchRunnable, 400);
        return false;
    }

    @Override
    public boolean onClose() {
        mRecyclerView.removeCallbacks(searchRunnable);
        mQueryText = null;
        mAdapter.filter(null);
        setListShown(true);
        return false;
    }

    private int getSearchHintRes() {
        if (KustomUtil.FOLDER_WIDGETS.equals(getFolder())) {
            return R.string.search_widgets;
        } else return R.string.search_wallpapers;
    }

    private String getFolder() {
        return getArguments().getString(ARG_FOLDER);
    }

    public static class PreviewItem implements Serializable {
        public final String folder;
        public final String fileName;
        public final String previewPath;
        public final String title;

        public PreviewItem(JSONObject info, String folder, String fileName, String previewPath)
                throws JSONException {
            this.folder = folder;
            this.fileName = fileName;
            this.previewPath = previewPath;
            this.title = info.getString("title");
        }

    }
}