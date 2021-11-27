package com.zpj.recyclerview.layouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.zpj.recyclerview.MultiData;

public class BannerLayouter extends InfiniteHorizontalLayouter {

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
            startAuto();
        }
    };

    public void startAuto() {
        mStart = true;
        getRecycler().postDelayed(mAutoRunnable, 3000);
    }

    public void stopAuto() {
        mStart = false;
        getRecycler().removeCallbacks(mAutoRunnable);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        if (mBottom < 0 || mTop > getHeight()) {
            if (mStart) {
                stopAuto();
            }
        } else {
            if (!mStart) {
                startAuto();
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
            mFlinger = new BannerFlinger(getRecycler().getContext(), multiData);
        }
        startAuto();

    }

    @Override
    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getWidth();
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        Log.d(TAG, "onTouchDown downX=" + downX + " downY=" + downY);
        stopAuto();
        if (mFlinger != null) {
            mFlinger.stop();
        } else {
            mFlinger = new BannerFlinger(getRecycler().getContext(), multiData);
        }
        return true;
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        if (mFlinger != null) {
            mFlinger.fling(velocityX, velocityY);
        }
        startAuto();
        return false;
    }

    protected class BannerFlinger extends HorizontalFlinger {


        public BannerFlinger(Context context, MultiData<?> scrollMultiData) {
            super(context, scrollMultiData);
        }

        @Override
        public void run() {
            if (scrollMultiData == null) {
                stop();
                return;
            }
            if (mScroller.computeScrollOffset()) {
                int x = mScroller.getCurrX();
                int dx = mLastFlingX - x;
                Log.d(TAG, "run dx=" + dx);
                if (dx == 0 && !mScroller.isFinished()) {
                    postOnAnimation();
                    return;
                }

                int consumed = scrollHorizontallyBy(dx, RecyclerViewHelper.getRecycler(getLayoutManager().getRecycler()), scrollMultiData);

                Log.d(TAG, "run dx=" + dx + " consumed=" + consumed);
                if (consumed != dx) {
                    stop();
                    if (scrollMultiData != null) {
                        onStopOverScroll(scrollMultiData);
                    }
                    return;
                }
                mLastFlingX = x;
                postOnAnimation();

            }
        }

        @Override
        public void fling(float velocityX, float velocityY) {
            if (scrollMultiData == null) {
                return;
            }
            this.mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

            int finalX = this.mScroller.getFinalX();

            int dx = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getMultiData(view) == scrollMultiData) {
                    int left = getDecoratedLeft(view);
                    int right = getDecoratedRight(view);
                    Log.d(TAG, "fling left=" + left + " right=" + right);
                    if (velocityX > 0) {
                        if (left + finalX > 0) {
                            dx = -left;
                        } else {
                            if (right < getWidth() / 2) {
                                dx = -right;
                            } else {
                                dx = -left;
                            }
                        }
                    } else {
                        if (right + finalX < 0) {
                            dx = -right;
                        } else if (right > getWidth() / 2) {
                            dx = getWidth() - right;
                        } else {
                            dx = -right;
                        }
                    }
                    Log.d(TAG, "fling velocityX=" + velocityX + " finalX=" + finalX);

                    break;
                }
            }

            Log.d(TAG, "fling dx=" + dx);
            startScroll(dx, 0, 500);
        }

    }

}
