package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class BannerLayouter extends PagerLayouter {

    private static final String TAG = "BannerLayouter";

    private boolean mStart;

    private boolean mAutoPlay = true;
    private int mAutoPlayDuration = 3000;

    private final Runnable mAutoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAutoPlay) {
                if (mFlinger != null) {
                    View current = findViewByPosition(getCurrentPosition());
                    if (current == null) {
                        onDetached();
                        return;
                    }
                    int dx = getDecoratedLeft(current) - getWidth();
                    Log.d(TAG, "mAutoRunnable dx=" + dx + " mTop=" + mScene.getTop() + " mBottom=" + mScene.getBottom());
                    mFlinger.fling(dx * 10, 0);
                }
                startAutoPlay();
            } else {
                stopAutoPlay();
            }
        }
    };


    public BannerLayouter() {
        mIsInfinite = true;
    }

    public void setAutoPlayDuration(int mAutoPlayDuration) {
        this.mAutoPlayDuration = mAutoPlayDuration;
    }

    public boolean isAutoPlay() {
        return mAutoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.mAutoPlay = autoPlay;
        if (autoPlay && !mStart) {
            startAutoPlay();
        }
    }

    public void toggleAutoPlay() {
        setAutoPlay(!this.mAutoPlay);
    }

    public void startAutoPlay() {
        if (mAutoPlay) {
            mStart = true;
            getRecycler().postDelayed(mAutoRunnable, mAutoPlayDuration);
        }
    }

    public void stopAutoPlay() {
        getRecycler().removeCallbacks(mAutoRunnable);
        mStart = false;
    }

    @Override
    public void layoutChildren(MultiData<?> multiData) {
        super.layoutChildren(multiData);
        if (mAutoPlay) {
            if (mStart) {
                return;
            }
            mStart = true;
            startAutoPlay();
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (!mStart) {
            startAutoPlay();
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (mStart) {
            stopAutoPlay();
        }
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY, MotionEvent event) {
        if (!canHandleTouch(downX, downY)) {
            return false;
        }
        Log.d(TAG, "onTouchDown downX=" + downX + " downY=" + downY);
        stopAutoPlay();
        return super.onTouchDown(multiData, downX, downY, event);
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY, MotionEvent event) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        boolean result = super.onTouchUp(multiData, velocityX, velocityY, event);
        startAutoPlay();
        return result;
    }

}
