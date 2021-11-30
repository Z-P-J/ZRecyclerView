package com.zpj.recyclerview.layouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class BannerLayouter extends ViewPagerLayouter {

    private static final String TAG = "BannerLayouter";

    private boolean mStart;

    private final Runnable mAutoRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFlinger != null) {
                int dx = -getWidth() - mFirstOffset;
                if (dx == 0) {
                    dx = -getWidth();
                }
                Log.d(TAG, "mAutoRunnable mFirstOffset=" + mFirstOffset + " dx=" + dx + " mTop=" + mTop + " mBottom=" + mBottom);
                mFlinger.startScroll(dx, 0, 500);
            }
            startAutoPlay();
        }
    };

    public void startAutoPlay() {
        mStart = true;
        getRecycler().postDelayed(mAutoRunnable, 3000);
    }

    public void stopAutoPlay() {
        mStart = false;
        getRecycler().removeCallbacks(mAutoRunnable);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        if (mBottom < 0 || mTop > getHeight()) {
            if (mStart) {
                stopAutoPlay();
            }
        } else {
            if (!mStart) {
                startAutoPlay();
            }
        }
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        super.layoutChildren(multiData, recycler, currentPosition);


        if (mStart) {
            return;
        }
        mStart = true;

        if (mFlinger == null) {
            mFlinger = new ViewPagerFlinger(getRecycler().getContext(), multiData);
        }
        startAutoPlay();

    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        Log.d(TAG, "onTouchDown downX=" + downX + " downY=" + downY);
        stopAutoPlay();
        return super.onTouchDown(multiData, downX, downY);
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        boolean result = super.onTouchUp(multiData, velocityX, velocityY);
        startAutoPlay();
        return result;
    }

}
