package com.afollestad.polar.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.WallpaperAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.ui.MainActivity;
import com.afollestad.polar.util.TintUtils;
import com.afollestad.polar.util.WallpaperUtils;
import com.afollestad.polar.viewer.ViewerActivity;

import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.afollestad.polar.viewer.ViewerActivity.STATE_CURRENT_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpapersFragment extends BasePageFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    public static final int RQ_CROPANDSETWALLPAPER = 8585;
    public static final int RQ_VIEWWALLPAPER = 2001;
    private static Toast mToast;

    @BindView(android.R.id.list)
    RecyclerView mRecyclerView;
    @BindView(android.R.id.empty)
    TextView mEmpty;
    @BindView(android.R.id.progress)
    View mProgress;
    WallpaperUtils.WallpapersHolder mWallpapers;

    private WallpaperAdapter mAdapter;
    private String mQueryText;
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.filter(mQueryText);
            setListShown(true);
        }
    };
    private Unbinder unbinder;

    public WallpapersFragment() {
    }

    public static void showToast(Context context, @StringRes int message) {
        showToast(context, context.getString(message));
    }

    public static void showToast(Context context, String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public int getTitle() {
        return R.string.wallpapers;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("wallpapers", mWallpapers);
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
        inflater.inflate(R.menu.wallpapers, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem mSearchItem = menu.findItem(R.id.search);
        SearchView mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        mSearchView.setQueryHint(getString(R.string.search_wallpapers));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setOnCloseListener(this);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if (getActivity() != null) {
            final MainActivity act = (MainActivity) getActivity();
            TintUtils.themeSearchView(act.getToolbar(), mSearchView, DialogUtils.resolveColor(act, R.attr.tab_icon_color));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reload) {
            mWallpapers = null;
            load(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openViewer(View view, int index) {
        ImageView iv = (ImageView) view.findViewById(R.id.image);

        final Intent intent = new Intent(getActivity(), ViewerActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("wallpapers", mWallpapers);
        extras.putInt(STATE_CURRENT_POSITION, index);
        extras.putInt(ViewerActivity.EXTRA_WIDTH, iv.getWidth());
        extras.putInt(ViewerActivity.EXTRA_HEIGHT, iv.getHeight());
        intent.putExtras(extras);

        final String transName = "view_" + index;
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), iv, transName);
        ActivityCompat.startActivityForResult(getActivity(), intent, RQ_VIEWWALLPAPER, options.toBundle());
    }

    private void showOptions(final int imageIndex) {
        if (!Config.get().wallpapersAllowDownload()) {
            final WallpaperUtils.Wallpaper wallpaper = mWallpapers.get(imageIndex);
            WallpaperUtils.download(getActivity(), wallpaper, true);
            return;
        }

        new MaterialDialog.Builder(getActivity())
                .title(R.string.wallpaper)
                .items(R.array.wallpaper_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, final int i, CharSequence charSequence) {
                        final WallpaperUtils.Wallpaper wallpaper = mWallpapers.get(imageIndex);
                        WallpaperUtils.download(getActivity(), wallpaper, i == 0);
                    }
                }).show();
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        mAdapter = new WallpaperAdapter(getActivity(), new WallpaperAdapter.ClickListener() {
            @Override
            public boolean onClick(View view, int index, boolean longPress) {
                if (longPress) {
                    showOptions(index);
                    return true;
                } else {
                    openViewer(view, index);
                    return false;
                }
            }
        });
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                Config.get().gridWidthWallpaper(), StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        if (savedInstanceState != null)
            mWallpapers = (WallpaperUtils.WallpapersHolder) savedInstanceState.getSerializable("wallpapers");
        if (getActivity() != null) load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        WallpaperUtils.resetOptionCache(true);
    }

    public void load() {
        load(!WallpaperUtils.didExpire(getActivity()));
    }

    private void load(boolean allowCached) {
        if (allowCached && mWallpapers != null) {
            mAdapter.set(mWallpapers);
            setListShown(true);
            return;
        }
        setListShown(false);
        mAdapter.clear();
        Bridge.config().logging(true);
        WallpaperUtils.getAll(getActivity(), allowCached, new WallpaperUtils.WallpapersCallback() {
            @Override
            public void onRetrievedWallpapers(WallpaperUtils.WallpapersHolder wallpapers, Exception error, boolean cancelled) {
                if (error != null) {
                    if (error instanceof BridgeException) {
                        BridgeException e = (BridgeException) error;
                        if (e.reason() == BridgeException.REASON_REQUEST_FAILED)
                            mEmpty.setText(R.string.unable_to_contact_server);
                        else if (e.reason() == BridgeException.REASON_REQUEST_TIMEOUT ||
                                (e.underlyingException() != null && e.underlyingException() instanceof SocketTimeoutException))
                            mEmpty.setText(R.string.unable_to_contact_server);
                        else mEmpty.setText(e.getMessage());
                    } else {
                        mEmpty.setText(error.getMessage());
                    }
                } else {
                    mEmpty.setText(cancelled ? R.string.request_cancelled : R.string.no_wallpapers);
                    mWallpapers = wallpapers;
                    mAdapter.set(mWallpapers);
                }
                setListShown(true);
            }
        });
    }

    // Search

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            if (mAdapter != null)
                WallpaperUtils.saveDb(getActivity(), mAdapter.getWallpapers());
            if (getActivity().isFinishing()) {
                Bridge.cancelAll()
                        .tag(WallpapersFragment.class.getName())
                        .commit();
            }
        }
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

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
}