package com.afollestad.polar.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Prevents the click listener from being called when the user drags on the image view, and doesn't immediately let go.
 *
 * @author Aidan Follestad (afollestad)
 */
public class NoDragImageView extends ImageView {

    public NoDragImageView(Context context) {
        super(context);
    }

    public NoDragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoDragImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public NoDragImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private float mDownX;
    private float mDownY;
    private boolean isOnClick;
    private OnClickListener mListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = ev.getX();
                mDownY = ev.getY();
                isOnClick = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isOnClick && mListener != null)
                    mListener.onClick(this);
                break;
            case MotionEvent.ACTION_MOVE:
                final float SCROLL_THRESHOLD = 10;
                if (isOnClick && (Math.abs(mDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(mDownY - ev.getY()) > SCROLL_THRESHOLD))
                    isOnClick = false;
                break;
            default:
                break;
        }
        return true;
    }
}