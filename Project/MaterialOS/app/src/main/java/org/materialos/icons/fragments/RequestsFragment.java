package org.materialos.icons.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.assent.Assent;
import com.afollestad.assent.AssentCallback;
import com.afollestad.assent.PermissionResultSet;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.afollestad.iconrequest.App;
import com.afollestad.iconrequest.AppsLoadCallback;
import com.afollestad.iconrequest.AppsSelectionListener;
import com.afollestad.iconrequest.IconRequest;
import com.afollestad.iconrequest.RequestSendCallback;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import org.materialos.icons.adapters.RequestsAdapter;
import org.materialos.icons.config.Config;
import org.materialos.icons.fragments.base.BasePageFragment;
import org.materialos.icons.ui.MainActivity;
import org.materialos.icons.util.TintUtils;
import org.materialos.icons.util.Utils;
import org.materialos.icons.views.DisableableViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class RequestsFragment extends BasePageFragment implements
        AppsLoadCallback, AppsSelectionListener, RequestSendCallback, AssentCallback,
        DragSelectRecyclerViewAdapter.SelectionListener, RequestsAdapter.SelectionChangedListener {

    private static final Object LOCK = new Object();

    private final static int PERM_RQ = 69;

    private RequestsAdapter mAdapter;
    private MaterialDialog mDialog;

    private int mInitialSelection = -1;
    private boolean mAppsLoaded = false;

    @Bind(android.R.id.list)
    DragSelectRecyclerView list;
    @Bind(android.R.id.progress)
    View progress;
    @Bind(R.id.progressText)
    TextView progressText;
    @Bind(android.R.id.empty)
    TextView emptyText;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    DisableableViewPager mPager;

    public RequestsFragment() {
    }

    @Override
    public int getTitle() {
        return R.string.request_icons;
    }

    public boolean onBackPressed() {
        if (mAdapter != null) {
            if (mAdapter.getSelectedCount() > 0) {
                if (IconRequest.get() != null) {
                    IconRequest.get().unselectAllApps();
                    mAdapter.clearSelected();
                }
                mAdapter.notifyDataSetChanged();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void updateTitle() {
        synchronized (LOCK) {
            MainActivity act = (MainActivity) getActivity();
            if (act != null) {
                if (fab == null) {
                    act.setTitle(R.string.request_icons);
                    invalidateOptionsMenu();
                    return;
                }

                final int numSelected = mAdapter != null ? mAdapter.getSelectedCount() : 0;
                if (numSelected == 0) {
                    act.setTitle(R.string.request_icons);
                } else {
                    act.setTitle(getString(R.string.request_icons_x, numSelected));
                }

                if (!fab.isShown() && numSelected > 0)
                    fab.show();
                else if (fab.isShown() && numSelected == 0)
                    fab.hide();
                // Work around for the icon sometimes being invisible?
                fab.setImageResource(R.drawable.ic_action_apply);
                // Update toolbar items
                invalidateOptionsMenu();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requesticons, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cab_requests, menu);
        super.onCreateOptionsMenu(menu, inflater);
        synchronized (LOCK) {
            MenuItem selectAll = menu.findItem(R.id.selectAll);
            try {
                final Activity act = getActivity();
                final int tintColor = DialogUtils.resolveColor(act, R.attr.toolbar_icons_color);
                if (mAdapter == null || mAdapter.getSelectedCount() == 0)
                    selectAll.setIcon(TintUtils.createTintedDrawable(act, R.drawable.ic_action_selectall, tintColor));
                else
                    selectAll.setIcon(TintUtils.createTintedDrawable(act, R.drawable.ic_action_close, tintColor));
            } catch (Throwable e) {
                e.printStackTrace();
                selectAll.setVisible(false);
            }
            selectAll.setVisible(mAppsLoaded);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        synchronized (LOCK) {
            if (item.getItemId() == R.id.selectAll) {
                final IconRequest ir = IconRequest.get();
                if (ir != null) {
                    if (mAdapter.getSelectedCount() == 0) {
                        ir.selectAllApps();
                        for (int i = 0; i < mAdapter.getItemCount(); i++)
                            mAdapter.setSelected(i, true);
                    } else {
                        ir.unselectAllApps();
                        mAdapter.clearSelected();
                    }
                    mAdapter.notifyDataSetChanged();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        final int offset = Utils.getNavBarHeight(getActivity());
        setBottomMargin(fab, offset, R.dimen.fab_bottom_margin);
        setBottomMargin(progressText, offset, 0);
        setBottomPadding(list, offset, R.dimen.fab_bottom_margin_list);
        setBottomMargin(emptyText, Utils.getNavBarHeight(getActivity()), R.dimen.nav_drawer_item_hor_pad);

        GridLayoutManager lm = new GridLayoutManager(getActivity(), Config.get().gridWidthRequests());
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0)
                    return Config.get().gridWidthRequests();
                return 1;
            }
        });

        mAdapter = new RequestsAdapter(this);
        mAdapter.setSelectionListener(this);

        list.setLayoutManager(lm);
        list.setAdapter(mAdapter);

        emptyText.setText(R.string.no_apps);
        emptyText.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText(R.string.loading_filter);
        list.setVisibility(View.GONE);

        mPager = (DisableableViewPager) getActivity().findViewById(R.id.pager);
        if (!Config.get().navDrawerModeEnabled()) {
            // Swiping is only enabled in nav drawer mode, so no need to run this code in nav drawer mode
            list.setFingerListener(new DragSelectRecyclerView.FingerListener() {
                @Override
                public void onDragSelectFingerAction(boolean dragActive) {
                    mPager.setPagingEnabled(!dragActive);
                }
            });
        }

        emptyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Assent.requestPermissions(RequestsFragment.this, PERM_RQ, Assent.WRITE_EXTERNAL_STORAGE);
            }
        });

        if (savedInstanceState != null) {
            IconRequest.restoreInstanceState(getActivity(), savedInstanceState, this, this, this);
            final IconRequest ir = IconRequest.get();
            if (ir != null && ir.isAppsLoaded()) {
                mAdapter.setApps(ir.getApps());
                mAdapter.restoreInstanceState(savedInstanceState);
                emptyText.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
        if (getActivity() != null)
            ((MainActivity) getActivity()).showChangelogIfNecessary(false);
    }

    @Override
    public void onPermissionResult(PermissionResultSet permissionResultSet) {
        if (permissionResultSet.isGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
            onClickFab();
        } else {
            Toast.makeText(getActivity(), R.string.write_storage_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
        IconRequest.saveInstanceState(outState);
    }

    @SuppressLint("StringFormatInvalid")
    private void reload() {
        synchronized (LOCK) {
            if (IconRequest.get() == null) {
                final File saveFolder = new File(Environment.getExternalStorageDirectory(), getString(R.string.app_name));
                Utils.wipe(new File(saveFolder, "files"));
                IconRequest.start(getActivity())
                        .toEmail(getString(R.string.icon_request_email))
                        .withSubject(String.format("%s %s", getString(R.string.app_name), getString(R.string.icon_request)))
                        .saveDir(saveFolder)
                        .loadCallback(this)
                        .selectionCallback(this)
                        .sendCallback(this)
                        .withFooter(getString(R.string.x_version_x, getString(R.string.app_name), BuildConfig.VERSION_NAME))
                        .includeDeviceInfo(true)
                        .build();
            }
            if (!IconRequest.get().isAppsLoaded())
                IconRequest.get().loadApps();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized (LOCK) {
            if (getActivity() != null && getActivity().isFinishing())
                IconRequest.cleanup();
            if (mDialog != null)
                mDialog.dismiss();
        }
    }

    // Icon Requests

    @Override
    public void onLoadingFilter() {
        if (progressText == null) return;
        mAppsLoaded = false;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                emptyText.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                list.setVisibility(View.GONE);
                progressText.setText(R.string.loading_filter);
            }
        });
    }

    @Override
    public void onAppsLoadProgress(final int percent) {
        if (progressText == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || getActivity() == null) return;
                // Percent isn't used here since it happens so fast anyways
                progressText.setText(R.string.loading);
            }
        });
    }

    @Override
    public void onAppsLoaded(ArrayList<App> arrayList, Exception e) {
        synchronized (LOCK) {
            if (progressText == null || IconRequest.get() == null) return;
            mAppsLoaded = true;
            progressText.post(new Runnable() {
                @Override
                public void run() {
                    if (IconRequest.get() == null) return;
                    invalidateOptionsMenu();
                    mAdapter.setApps(IconRequest.get().getApps());
                    mAdapter.notifyDataSetChanged();
                    emptyText.setVisibility(mAdapter.getItemCount() == 0 ?
                            View.VISIBLE : View.GONE);
                    progress.setVisibility(View.GONE);
                    list.setVisibility(mAdapter.getItemCount() == 0 ?
                            View.GONE : View.VISIBLE);
                }
            });
        }
    }

    // Apps selection listener

    @Override
    public void onAppSelectionChanged(int count) {
        if (count == 0)
            fab.hide();
        else fab.show();
    }

    // Request send listener

    @Override
    public void onRequestPreparing() {
        if (getActivity() == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                mDialog = new MaterialDialog.Builder(getActivity())
                        .content(R.string.preparing_icon_request)
                        .progress(true, -1)
                        .cancelable(false)
                        .show();
            }
        });
    }

    @Override
    public void onRequestError(Exception e) {
        mDialog.dismiss();
        Utils.showError(getActivity(), e);
    }

    @Override
    public void onRequestSent() {
        if (getActivity() == null) return;
        progressText.post(new Runnable() {
            @Override
            public void run() {
                mDialog.dismiss();
                fab.hide();
                IconRequest.get().unselectAllApps();
                mAdapter.clearSelected();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @OnClick(R.id.fab)
    public void onClickFab() {
        if (!Config.get().iconRequestEnabled()) {
            Utils.showError(getActivity(), new Exception("The developer has not set an email for icon requests yet."));
            return;
        } else if (!Assent.isPermissionGranted(Assent.WRITE_EXTERNAL_STORAGE)) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.permission_needed)
                    .content(Html.fromHtml(getString(R.string.permission_needed_desc, getString(R.string.app_name))))
                    .positiveText(android.R.string.ok)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Assent.requestPermissions(RequestsFragment.this, PERM_RQ, Assent.WRITE_EXTERNAL_STORAGE);
                        }
                    }).show();
            return;
        }

        synchronized (LOCK) {
            final IconRequest ir = IconRequest.get();
            if (getActivity() == null || ir == null) return;
            final List<App> apps = ir.getApps();
            if (apps != null) {
                for (int i = 0; i < apps.size(); i++) {
                    if (mAdapter.isIndexSelected(i + 1))
                        ir.selectApp(apps.get(i));
                    else ir.unselectApp(apps.get(i));
                }
            }
            ir.send();
        }
    }

    @Override
    public void onDragSelectionChanged(int count) {
        updateTitle();
        invalidateOptionsMenu();
    }

    @Override
    public void onClick(int index, boolean longClick) {
        if (longClick) {
            if (mAdapter.isIndexSelected(index)) return;
            mInitialSelection = index;
            list.setDragSelectActive(true, index);
        } else {
            if (index == mInitialSelection) {
                list.setDragSelectActive(false, -1);
                mInitialSelection = -1;
            }
            mAdapter.toggleSelected(index);
        }
    }
}