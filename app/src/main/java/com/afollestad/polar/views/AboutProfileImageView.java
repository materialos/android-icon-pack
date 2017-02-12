package com.afollestad.polar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.makeramen.roundedimageview.RoundedImageView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AboutProfileImageView extends RoundedImageView {

    public AboutProfileImageView(Context context) {
        super(context);
    }

    public AboutProfileImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AboutProfileImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getParent() != null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
            params.topMargin = getMeasuredHeight() / 2;
            setLayoutParams(params);
        }
    }
}
