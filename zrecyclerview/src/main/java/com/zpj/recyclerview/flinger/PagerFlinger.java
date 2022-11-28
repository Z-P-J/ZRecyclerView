package com.zpj.recyclerview.flinger;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;
import com.zpj.recyclerview.layouter.LayoutHelper;
import com.zpj.recyclerview.layouter.PagerLayouter;

public abstract class PagerFlinger extends HorizontalFlinger {

    public PagerFlinger(AbsLayouter layouter, MultiData<?> multiData) {
        super(layouter, multiData);
    }

    @Override
    public void fling(float velocityX, float velocityY) {
        if (mMultiData != null && mLayouter instanceof PagerLayouter) {
            stop();

            PagerLayouter layouter = (PagerLayouter) mLayouter;
            LayoutHelper helper = layouter.getLayoutHelper();
            int tempCurrentItem = layouter.getCurrentItem();
            View current = helper.findViewByPosition(layouter.getCurrentPosition());
            int position = helper.getPosition(current);
            int item = position - layouter.getPositionOffset();

            int left = helper.getDecoratedLeft(current);
            int right = helper.getDecoratedRight(current);

            this.mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

            int finalX = this.mScroller.getFinalX();

            int dx;
            int currentItem;
            if (velocityX > 0) {
                if (left + finalX > layouter.getWidth()) {
                    dx = layouter.getWidth() - left;
                    currentItem = item - 1;
                } else if (left < layouter.getWidth() / 2) {
                    dx = -left;
                    currentItem = item;
                } else {
                    dx = layouter.getWidth() - left;
                    currentItem = item - 1;
                }
                if (currentItem < 0) {
                    if (layouter.isInfinite()) {
                        currentItem = mLayouter.getCount(mMultiData) - 1;
                    } else {
                        dx = -left;
                        currentItem = 0;
                    }
                }
            } else {
                if (right + finalX < 0) {
                    dx = -right;
                    currentItem = item + 1;
                } else if (right > layouter.getWidth() / 2) {
                    dx = layouter.getWidth() - right;
                    currentItem = item;
                } else {
                    dx = -right;
                    currentItem = item - 1;
                }
                if (currentItem >= mLayouter.getCount(mMultiData)) {
                    if (layouter.isInfinite()) {
                        currentItem = 0;
                    } else {
                        dx = layouter.getWidth() - right;
                        currentItem = mLayouter.getCount(mMultiData) - 1;
                    }
                }
            }
            if (tempCurrentItem != currentItem) {
                onItemSelected(currentItem);
            }

            int count = 0;
            for (int i = 0; i < layouter.getChildCount(); i++) {
                View child = layouter.getChildAt(i);
                if (layouter.getMultiData(child) == mMultiData) {
                    count++;
                }
            }
            Log.d("ViewPagerFlinger", "fling count=" + count);

            super.scroll(dx, 0, 500);
        }
    }

    protected abstract void onItemSelected(int item);

}
