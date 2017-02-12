package com.afollestad.polar.fragments.base;

import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;

import com.afollestad.assent.AssentFragment;
import com.afollestad.polar.ui.base.BaseThemedActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BasePageFragment extends AssentFragment {

    @StringRes
    protected abstract int getTitle();

    public void updateTitle() {
        if (getActivity() != null)
            getActivity().setTitle(getTitle());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null)
            BaseThemedActivity.themeMenu(getActivity(), menu);
    }
}