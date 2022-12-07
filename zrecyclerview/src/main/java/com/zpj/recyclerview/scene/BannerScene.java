package com.zpj.recyclerview.scene;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.PagerLayouter;

public class BannerScene extends PagerScene {

    private static final String TAG = "BannerScene";

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
                    Log.d(TAG, "mAutoRunnable dx=" + dx + " mTop=" + getTop() + " mBottom=" + getBottom());
                    mFlinger.fling(dx * 10, 0);
                }
                startAutoPlay();
            } else {
                stopAutoPlay();
            }
        }
    };

    public BannerScene(MultiData<?> multiData) {
        this(multiData, new PagerLayouter(true));
    }

    public BannerScene(MultiData<?> multiData, PagerLayouter layouter) {
        super(multiData, layouter);
    }


//    public BannerScene() {
//        super();
//        mIsInfinite = true;
//    }

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
    public void layoutChildren() {
        super.layoutChildren();
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
    public boolean onTouchDown(MotionEvent event) {
        if (!canHandleTouch(event)) {
            return false;
        }
        stopAutoPlay();
        return super.onTouchDown(event);
    }

    @Override
    public boolean onTouchUp(MotionEvent event, float velocityX, float velocityY) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        boolean result = super.onTouchUp(event, velocityX, velocityY);
        startAutoPlay();
        return result;
    }

}
