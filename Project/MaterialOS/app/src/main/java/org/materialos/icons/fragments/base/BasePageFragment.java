package org.materialos.icons.fragments.base;

import android.support.annotation.DimenRes;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.assent.AssentFragment;
import org.materialos.icons.ui.base.BaseThemedActivity;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BasePageFragment extends AssentFragment {

    private boolean isVisible;

    @StringRes
    protected abstract int getTitle();

    public void updateTitle() {
        if (getActivity() != null)
            getActivity().setTitle(getTitle());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisible = isVisibleToUser;
        if (isVisibleToUser)
            updateTitle();
    }

    protected void invalidateOptionsMenu() {
        if (isVisible && getActivity() != null && !getActivity().isFinishing())
            getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null)
            BaseThemedActivity.themeMenu(getActivity(), menu);
    }

    protected void setBottomMargin(View view, int margin, @DimenRes int defaultMargin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        lp.bottomMargin = (defaultMargin != 0 ? getResources().getDimensionPixelSize(defaultMargin) : 0) + margin;
        view.setLayoutParams(lp);
    }

    protected void setBottomPadding(View view, int padding, @DimenRes int defaultPadding) {
        view.setPadding(view.getPaddingLeft(),
                view.getPaddingTop(),
                view.getPaddingRight(),
                (defaultPadding != 0 ? getResources().getDimensionPixelSize(defaultPadding) : 0) + padding);
    }

//    /**
//     * Applies window insets apart from the top inset to a ViewGroup's direct children, if they have
//     * fitsSystemWindows set.
//     * <p/>
//     * Must be called in/after onViewCreated
//     */
//    protected void applyInsets(ViewGroup viewGroup) {
//        for (int i = 0; i < viewGroup.getChildCount(); i++) {
//            View child = viewGroup.getChildAt(i);
//            if (child.getFitsSystemWindows()) {
//                applyInsetsToView(child);
//            }
//        }
//    }
//
//    protected void applyInsetsToViewMargin(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
//                    //Ignore fitsSystemWindows
//                    return insets;
//                }
//            });
//        }
//        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//        layoutParams.bottomMargin += ((MainActivity) getActivity()).getBottomInset();
//        view.setLayoutParams(layoutParams);
//    }
//
//    /**
//     * Applies any window insets apart from the top inset to the view.
//     * <p/>
//     * Must be called in/after onViewCreated
//     */
//    protected void applyInsetsToView(View view) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
//                @Override
//                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
//                    //Ignore fitsSystemWindows
//                    return insets;
//                }
//            });
//        }
//        view.setPaddingRelative(view.getPaddingStart(), view.getPaddingTop(),
//                view.getPaddingEnd(), view.getPaddingBottom() + ((MainActivity) getActivity()).getBottomInset());
//    }
}