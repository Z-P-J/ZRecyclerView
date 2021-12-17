package com.zpj.recyclerview.flinger;

import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;
import com.zpj.recyclerview.layouter.ViewPagerLayouter;

public abstract class ViewPagerFlinger extends HorizontalFlinger {

    private int mCurrentItem = 0;

    public ViewPagerFlinger(AbsLayouter layouter, MultiData<?> multiData) {
        super(layouter, multiData);
    }

    @Override
    public void fling(float velocityX, float velocityY) {

        if (mMultiData != null && mLayouter instanceof ViewPagerLayouter) {
            stop();



            ViewPagerLayouter layouter = (ViewPagerLayouter) mLayouter;
            int tempCurrentItem = layouter.getCurrentItem();
            View current = layouter.findViewByPosition(layouter.getCurrentPosition());
            int position = layouter.getPosition(current);
            int item = position - layouter.getPositionOffset();

            int left = layouter.getDecoratedLeft(current);
            int right = layouter.getDecoratedRight(current);

            this.mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

            int finalX = this.mScroller.getFinalX();

            int dx = 0;
            if (velocityX > 0) {
                if (left + finalX > layouter.getWidth()) {
                    dx = layouter.getWidth() - left;
                    mCurrentItem = item - 1;
                } else if (left < layouter.getWidth() / 2) {
                    dx = -left;
                    mCurrentItem = item;
                } else {
                    dx = layouter.getWidth() - left;
                    mCurrentItem = item - 1;
                }
                if (mCurrentItem < 0) {
                    if (layouter.isInfinite()) {
                        mCurrentItem = mMultiData.getCount() - 1;
                    } else {
                        dx = -left;
                        mCurrentItem = 0;
                    }
                }
            } else {
                if (right + finalX < 0) {
                    dx = -right;
                    mCurrentItem = item + 1;
                } else if (right > layouter.getWidth() / 2) {
                    dx = layouter.getWidth() - right;
                    mCurrentItem = item;
                } else {
                    dx = -right;
                    mCurrentItem = item - 1;
                }
                if (mCurrentItem >= mMultiData.getCount()) {
                    if (layouter.isInfinite()) {
                        mCurrentItem = 0;
                    } else {
                        dx = layouter.getWidth() - right;
                        mCurrentItem = mMultiData.getCount() - 1;
                    }
                }
            }
            if (tempCurrentItem != mCurrentItem) {
                onItemSelected(mCurrentItem);
            }
            scroll(dx, 0, 500);
        }
    }

    protected abstract void onItemSelected(int item);

}
