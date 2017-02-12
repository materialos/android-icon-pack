package com.afollestad.polar.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.polar.R;
import com.afollestad.polar.adapters.ApplyAdapter;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.fragments.base.BasePageFragment;
import com.afollestad.polar.util.ApplyUtil;

import butterknife.ButterKnife;

public class ApplyFragment extends BasePageFragment implements ApplyAdapter.SelectionCallback {

    public ApplyFragment() {
    }

    @Override
    public int getTitle() {
        return R.string.apply;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        final RecyclerView recyclerView = ButterKnife.findById(v, android.R.id.list);
        final GridLayoutManager lm = new GridLayoutManager(getActivity(), Config.get().gridWidthApply());
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0 && ApplyUtil.canQuickApply(getActivity()) != null)
                    return Config.get().gridWidthApply();
                return 1;
            }
        });

        final ApplyAdapter mAdapter = new ApplyAdapter(getActivity(), this);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onLauncherSelection(int index, final String title, final String pkg) {
        ApplyUtil.apply(getActivity(), pkg, new ApplyUtil.ApplyCallback() {
            @Override
            public void onNotInstalled() {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.not_installed)
                        .content(Html.fromHtml(getString(R.string.not_installed_prompt, title)))
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(String.format("market://details?id=%s", pkg)));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW)
                                            .setData(Uri.parse(String.format("http://play.google.com/store/apps/details?id=%s", pkg)));
                                    startActivity(intent);
                                }
                            }
                        }).show();
            }
        });
    }
}