package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.StateMultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.scene.GridScene;

public class GridLayouter extends AbsLayouter<GridScene> {

    private static final String TAG = "GridLayouter";

//    private int mSpanCount;
//
//    public GridLayouter(int spanCount) {
//        this.mSpanCount = Math.max(spanCount, 1);
//    }
//
//    public void setSpanCount(int spanCount) {
//        this.mSpanCount = spanCount;
//    }

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

        int positionOffset = mScene.getPositionOffset();
        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = mScene.addViewAndMeasure(positionOffset, 0);

            int childHeight = getDecoratedMeasuredHeight(view);
            top = bottom - childHeight;
            right = getRecyclerWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int spanCount = mScene.getSpanCount();
            int childWidth = (getRecyclerWidth() - mScene.getPaddingLeft() - mScene.getPaddingRight()) / spanCount;
            int childHeight = 0;

            while (availableSpace > 0 && currentPosition >= positionOffset) {
                int posInLine = (currentPosition - positionOffset) % spanCount;
                right = (posInLine + 1) * childWidth;
                left = right - childWidth;

                View view = mScene.addView(currentPosition--, 0);
                measureChild(view, getRecyclerWidth() - childWidth, 0);

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
        mScene.setTop(top);
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int positionOffset = mScene.getPositionOffset();
        int itemCount = mScene.getItemCount();
        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = mScene.addViewAndMeasure(positionOffset);

            int childHeight = getDecoratedMeasuredHeight(view);
            bottom = top + childHeight;
            right = getRecyclerWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int spanCount = mScene.getSpanCount();
            int childWidth = (getRecyclerWidth() - mScene.getPaddingLeft() - mScene.getPaddingRight()) / spanCount;
            int childHeight = 0;
            while (availableSpace > 0 && currentPosition < itemCount + positionOffset) {

                int posInLine = (currentPosition - positionOffset) % spanCount;
                left = posInLine * childWidth;
                right = left + childWidth;

                View view = mScene.addView(currentPosition++);
                measureChild(view, getRecyclerWidth() - childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = getDecoratedMeasuredHeight(view);
                }
                bottom = top + childHeight;

                Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
                layoutDecorated(view, left, top, right, bottom);

                if (posInLine == spanCount - 1 || currentPosition == itemCount + positionOffset) {
                    top = bottom;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        mScene.setBottom(bottom);
        return availableSpace;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return 0;
    }

}
