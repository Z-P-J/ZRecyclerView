package com.zpj.recyclerview.layouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class ViewPagerLayouter extends InfiniteHorizontalLayouter {

    private static final String TAG = "ViewPagerLayouter";

    @Override
    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getWidth();
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        Log.d(TAG, "onTouchDown downX=" + downX + " downY=" + downY);
        if (mFlinger != null) {
            mFlinger.stop();
        } else {
            mFlinger = new ViewPagerFlinger(getRecycler().getContext(), multiData);
        }
        return true;
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        Log.d(TAG, "onTouchUp velocityX=" + velocityX + " velocityY=" + velocityY);
        if (mFlinger != null) {
            mFlinger.fling(velocityX, velocityY);
        }
        return false;
    }

    @Override
    protected void onDetached() {
        if (mFirstOffset < 0) {
            mFirstOffset = 0;
            mFirstPosition++;
        }
        super.onDetached();
    }

    protected class ViewPagerFlinger extends HorizontalFlinger {


        public ViewPagerFlinger(Context context, MultiData<?> scrollMultiData) {
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
                    onStopOverScroll(scrollMultiData);
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
