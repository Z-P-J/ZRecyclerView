package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.StateMultiData;

public class GridLayouter extends AbsLayouter {

    private static final String TAG = "GridLayouter";

    private int mSpanCount;

    public GridLayouter(int spanCount) {
        this.mSpanCount = Math.max(spanCount, 1);
    }

    public void setSpanCount(int spanCount) {
        this.mSpanCount = spanCount;
        if (getLayoutHelper() != null) {
            getLayoutHelper().requestLayout();
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = addViewAndMeasure(mPositionOffset, 0, multiData);

            int childHeight = getDecoratedMeasuredHeight(view);
            top = bottom - childHeight;
            right = getWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / mSpanCount;
            int childHeight = 0;

            while (availableSpace > 0 && currentPosition >= mPositionOffset) {
                int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
                right = (posInLine + 1) * childWidth;
                left = right - childWidth;

                View view = addView(currentPosition--, 0, multiData);
                measureChild(view, getWidth() - childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = getDecoratedMeasuredHeight(view);
                }
                top = bottom - childHeight;

                layoutDecorated(view, left, top, right, bottom);

                if (posInLine == 0) {
                    bottom = top;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        mTop = top;
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = addViewAndMeasure(mPositionOffset, multiData);

            int childHeight = getDecoratedMeasuredHeight(view);
            bottom = top + childHeight;
            right = getWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / mSpanCount;
            int childHeight = 0;
            while (availableSpace > 0 && currentPosition < getCount(multiData) + mPositionOffset) {

                int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
                left = posInLine * childWidth;
                right = left + childWidth;

                View view = addView(currentPosition++, multiData);
                measureChild(view, getWidth() - childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = getDecoratedMeasuredHeight(view);
                }
                bottom = top + childHeight;

                Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
                layoutDecorated(view, left, top, right, bottom);

                if (posInLine == mSpanCount - 1 || currentPosition == getCount(multiData) + mPositionOffset) {
                    top = bottom;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        mBottom = bottom;
        return availableSpace;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return 0;
    }

}
