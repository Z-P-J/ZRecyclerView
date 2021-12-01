package com.zpj.recyclerview.flinger;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerViewHelper;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;

public abstract class AbsFlinger implements Flinger, Runnable, Interpolator {

    public static final Interpolator sScrollInterpolator = new FastOutSlowInInterpolator();

    protected final AbsLayouter mLayouter;
    protected final MultiData<?> mMultiData;
    protected final OverScroller mScroller;

    private int mLastX;
    private int mLastY;

    private Interpolator mInterpolator;

    public AbsFlinger(AbsLayouter layouter, MultiData<?> multiData) {
        this.mLayouter = layouter;
        this.mMultiData = multiData;
        this.mScroller = new OverScroller(layouter.getContext(), this);
    }

    @Override
    public void run() {
        if (mMultiData == null) {
            stop();
            return;
        }
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            int dx = mLastX - x;
            int dy = mLastY - y;

            if (onComputeScroll(dx, dy)) {
                mLastX = x;
                mLastY = y;
                postOnAnimation();
            } else {
                onFinished();
            }
        } else {
            onFinished();
        }
    }

    @Override
    public float getInterpolation(float input) {
        if (mInterpolator == null) {
            return RecyclerViewHelper.getInterpolator().getInterpolation(input);
        }
        return mInterpolator.getInterpolation(input);
    }

    @Override
    public void postOnAnimation() {
        mLayouter.getRecycler().removeCallbacks(this);
        ViewCompat.postOnAnimation(mLayouter.getRecycler().getRecyclerView(), this);
    }



    @Override
    public void fling(float velocityX, float velocityY) {
        if (mMultiData == null) {
            return;
        }
        stop();
        mInterpolator = null;
        this.mScroller.fling(0, 0, (int) velocityX, (int) velocityY,
                Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        this.postOnAnimation();
    }

    @Override
    public void scroll(int dx, int dy) {
        scroll(dx, dy, Math.max(250, calculateTimeForScrolling(dx)));
    }

    @Override
    public void scroll(int dx, int dy, int duration) {
        if (mMultiData == null) {
            return;
        }
        stop();
        mInterpolator = null;
        mInterpolator = sScrollInterpolator;
        this.mScroller.startScroll(0, 0, dx, dy, duration);
        this.postOnAnimation();
    }

    @Override
    public boolean isStop() {
        return this.mScroller.isFinished();
    }

    @Override
    public void stop() {
        mLayouter.getRecycler().removeCallbacks(this);
        this.mScroller.forceFinished(true);
        this.mLastX = 0;
        this.mLastY = 0;
        onStopped();
    }

    @Override
    public void onFinished() {

    }

    @Override
    public void onStopped() {

    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    protected float calculateSpeedPerPixel() {
        return 25f / mLayouter.getContext().getResources().getDisplayMetrics().densityDpi;
    }

    protected int calculateTimeForScrolling(int dx) {
        return (int) Math.ceil(Math.abs(dx) * calculateSpeedPerPixel());
    }

}
