package com.zpj.recyclerview.flinger;

import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerViewHelper;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.zpj.recyclerview.core.Scene;

public abstract class AbsFlinger implements Flinger, Runnable, Interpolator {

    public static final Interpolator sScrollInterpolator = new FastOutSlowInInterpolator();

    @NonNull
    protected final Scene mScene;
    protected final OverScroller mScroller;

    private int mLastX;
    private int mLastY;

    private Interpolator mInterpolator;

    public AbsFlinger(@NonNull Scene scene) {
        mScene = scene;
        this.mScroller = new OverScroller(scene.getContext(), this);
    }

    private boolean flag = true;

    @Override
    public void run() {
        if (flag) {
            flag = false;
            mScene.getLayoutHelper().startInterceptRequestLayout();
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
                finish();
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
        mScene.removeCallbacks(this);
        mScene.postOnAnimation(this);
    }



    @Override
    public void fling(float velocityX, float velocityY) {
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
        mLastX = 0;
        mLastY = 0;
        if (mScroller.isFinished()) {
            return;
        }
        mScene.removeCallbacks(this);
        mScroller.forceFinished(true);
        onStopped();
    }

    protected void finish() {
        mScene.removeCallbacks(this);
        mScroller.forceFinished(true);
        mLastX = 0;
        mLastY = 0;
        onFinished();
    }

    @Override
    public void onFinished() {
        if (!flag) {
            flag = true;
            mScene.getLayoutHelper().stopInterceptRequestLayout();
        }
        mScene.onFlingFinished();
    }

    @Override
    public void onStopped() {
        if (!flag) {
            flag = true;
            mScene.getLayoutHelper().stopInterceptRequestLayout();
        }
        mScene.onFlingStopped();
    }

    public void setInterpolator(Interpolator mInterpolator) {
        this.mInterpolator = mInterpolator;
    }

    protected float calculateSpeedPerPixel() {
        return 25f / mScene.getContext().getResources().getDisplayMetrics().densityDpi;
    }

    protected int calculateTimeForScrolling(int dx) {
        return (int) Math.ceil(Math.abs(dx) * calculateSpeedPerPixel());
    }

}
