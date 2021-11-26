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

    @Override
    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getWidth();
    }

    @Override
    public void onTouchDown(MultiData<?> multiData, float downX, float downY) {
        Log.d(TAG, "onTouchDown downX=" + downX + " downY=" + downY);
        if (mFlinger != null) {
            mFlinger.stop();
        } else {
            mFlinger = new BannerFlinger(getRecycler().getContext(), multiData);
        }
    }

    @Override
    public void onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        if (mFlinger != null) {
            mFlinger.fling(velocityX, velocityY);
        }
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
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

            int finalX = this.mScroller.getFinalX();
            this.mScroller.abortAnimation();
            this.mScroller.forceFinished(true);

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
            this.mScroller.startScroll(0, 0, dx, 0, 500);

            this.postOnAnimation();
        }

    }

}
