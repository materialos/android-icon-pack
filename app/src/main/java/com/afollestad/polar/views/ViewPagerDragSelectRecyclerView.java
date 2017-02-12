package com.afollestad.polar.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ViewPagerDragSelectRecyclerView extends DragSelectRecyclerView {

    public ViewPagerDragSelectRecyclerView(Context context) {
        super(context);
    }

    public ViewPagerDragSelectRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewPagerDragSelectRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null)
            getParent().requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(event);
    }
}
