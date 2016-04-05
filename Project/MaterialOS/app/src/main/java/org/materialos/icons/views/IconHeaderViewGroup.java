package org.materialos.icons.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.materialos.icons.R;

/**
 * @author Aidan Follestad (afollestad)
 */
public class IconHeaderViewGroup extends RelativeLayout {

    private static int mHeight = -1;

    public IconHeaderViewGroup(Context context) {
        super(context);
    }

    public IconHeaderViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconHeaderViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void invalidateHeight() {
        if (mHeight == -1) {
            mHeight = getResources().getDimensionPixelSize(R.dimen.button_height) +
                    (getResources().getDimensionPixelSize(R.dimen.content_inset_half) * 2);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        invalidateHeight();

        View child = getChildAt(0);
        if (child.getMeasuredHeight() <= 0)
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        int top = (mHeight / 2) - (child.getMeasuredHeight() / 2);
        int bottom = top + child.getMeasuredHeight();
        int left = getPaddingStart();
        int right = left + child.getMeasuredWidth();
        child.layout(left, top, right, bottom);

        child = getChildAt(1);
        if (child.getMeasuredHeight() <= 0)
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        top = (mHeight / 2) - (child.getMeasuredHeight() / 2);
        bottom = top + child.getMeasuredHeight();
        right = getMeasuredWidth() - getPaddingEnd();
        left = right - child.getMeasuredWidth();
        child.layout(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        invalidateHeight();
        //noinspection Range
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY));
    }
}
