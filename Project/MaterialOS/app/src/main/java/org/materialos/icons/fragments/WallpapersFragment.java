package org.materialos.icons.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
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
import org.materialos.icons.adapters.WallpaperAdapter;
import org.materialos.icons.config.Config;
import org.materialos.icons.fragments.base.BasePageFragment;
import org.materialos.icons.ui.MainActivity;
import org.materialos.icons.util.TintUtils;
import org.materialos.icons.util.Utils;
import org.materialos.icons.util.WallpaperUtils;
import org.materialos.icons.viewer.ViewerActivity;

import java.net.UnknownHostException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.materialos.icons.viewer.ViewerActivity.STATE_CURRENT_POSITION;

/**
 * @author Aidan Follestad (afollestad)
 */
public class WallpapersFragment extends BasePageFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    @Bind(android.R.id.list)
    RecyclerView mRecyclerView;
    @Bind(android.R.id.empty)
    TextView mEmpty;
    @Bind(android.R.id.progress)
    View mProgress;

    public static final int RQ_CROPANDSETWALLPAPER = 8585;
    public static final int RQ_VIEWWALLPAPER = 2001;

    private WallpaperAdapter mAdapter;
    WallpaperUtils.WallpapersHolder mWallpapers;
    private String mQueryText;
    private static Toast mToast;

    public static void showToast(Context context, @StringRes int message) {
        showToast(context, context.getString(message));
    }

    public static void showToast(Context context, String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public WallpapersFragment() {
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

    void openViewer(View view, int index) {
        ImageView iv = (ImageView) view.findViewById(R.id.image);

        final Intent intent = new Intent(getActivity(), ViewerActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable("wallpapers", mWallpapers);
        extras.putInt(STATE_CURRENT_POSITION, index);
        intent.putExtras(extras);

        final String transName = "view_" + index;
        ViewCompat.setTransitionName(iv, transName);
        final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), iv, transName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Somehow this works (setting status bar color in both MainActivity and here)
            //to avoid image glitching through on when ViewActivity is first created.
            getActivity().getWindow().setStatusBarColor(
                    DialogUtils.resolveColor(getActivity(), R.attr.colorPrimaryDark));
            View statusBar = getActivity().getWindow().getDecorView().findViewById(android.R.id.statusBarBackground);
            if (statusBar != null) {
                statusBar.post(new Runnable() {
                    @Override
                    public void run() {
                        ActivityCompat.startActivityForResult(getActivity(), intent, RQ_VIEWWALLPAPER, options.toBundle());
                    }
                });
                return;
            }
        }

        ActivityCompat.startActivityForResult(getActivity(), intent, RQ_VIEWWALLPAPER, options.toBundle());
    }

    private void showOptions(final int imageIndex) {
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
        ButterKnife.bind(this, view);
        setBottomPadding(mRecyclerView, Utils.getNavBarHeight(getActivity()), R.dimen.grid_margin);

        mAdapter = new WallpaperAdapter(new WallpaperAdapter.ClickListener() {
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
        ButterKnife.unbind(this);
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
                    if (error instanceof UnknownHostException || (error instanceof BridgeException &&
                            ((BridgeException) error).reason() == BridgeException.REASON_REQUEST_FAILED)) {
                        mEmpty.setText(R.string.unable_to_contact_server);
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

    // Search

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            mAdapter.filter(mQueryText);
            setListShown(true);
        }
    };

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
}