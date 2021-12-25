package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
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

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    protected int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = addViewAndMeasure(mPositionOffset, 0, recycler, multiData);

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

                View view = addViewAndMeasure(currentPosition--, 0, recycler, multiData);
//                measureChild(view, childWidth, 0);

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
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = addViewAndMeasure(mPositionOffset, recycler, multiData);

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

                View view = addViewAndMeasure(currentPosition++, recycler, multiData);
//                measureChild(view, childWidth, 0);

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
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
