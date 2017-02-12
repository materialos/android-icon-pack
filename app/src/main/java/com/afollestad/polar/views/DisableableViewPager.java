package com.afollestad.polar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pluscubed.insetsdispatcher.view.InsetsDispatcherViewPager;

/**
 * @author Aidan Follestad (afollestad)
 */
public class DisableableViewPager extends InsetsDispatcherViewPager {

    private boolean isPagingEnabled = true;

    public DisableableViewPager(Context context) {
        super(context);
    }

    public DisableableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

}