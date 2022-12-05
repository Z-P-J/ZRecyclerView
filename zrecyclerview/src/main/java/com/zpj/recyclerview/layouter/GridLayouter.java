package com.zpj.recyclerview.layouter;

import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.StateMultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.AnchorInfo;
import com.zpj.recyclerview.core.Scene;

public class GridLayouter extends AbsLayouter {

    private static final String TAG = "GridLayouter";

    private int mSpanCount;

    public GridLayouter() {
        this(2);
    }

    public GridLayouter(@IntRange(from = 1) int spanCount) {
        mSpanCount = spanCount;
    }

    public void setSpanCount(int spanCount) {
        this.mSpanCount = spanCount;
    }

    public int getSpanCount() {
        return mSpanCount;
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
    protected int fillVerticalTop(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y;
        int right = 0;
        int bottom = top;

        MultiData<?> multiData = scene.getMultiData();
        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = scene.addViewAndMeasure(positionOffset, 0);

            int childHeight = scene.getDecoratedMeasuredHeight(view);
            top = bottom - childHeight;
            right = scene.getWidth();
            scene.layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (scene.getWidth() - scene.getPaddingLeft() - scene.getPaddingRight()) / mSpanCount;
            int childHeight = 0;

            while (availableSpace > 0 && currentPosition >= positionOffset) {
                int posInLine = (currentPosition - positionOffset) % mSpanCount;
                right = (posInLine + 1) * childWidth;
                left = right - childWidth;

                View view = scene.addView(currentPosition--, 0);
                scene.measureChild(view, scene.getWidth() - childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = scene.getDecoratedMeasuredHeight(view);
                }
                top = bottom - childHeight;

                scene.layoutDecorated(view, left, top, right, bottom);

                if (posInLine == 0) {
                    bottom = top;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        scene.setTop(top);
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y;
        int right = 0;
        int bottom = top;

        int itemCount = scene.getItemCount();
        MultiData<?> multiData = scene.getMultiData();
        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = scene.addViewAndMeasure(positionOffset);

            int childHeight = scene.getDecoratedMeasuredHeight(view);
            bottom = top + childHeight;
            right = scene.getWidth();
            scene.layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (scene.getWidth() - scene.getPaddingLeft() - scene.getPaddingRight()) / mSpanCount;
            int childHeight = 0;
            while (availableSpace > 0 && currentPosition < itemCount + positionOffset) {

                int posInLine = (currentPosition - positionOffset) % mSpanCount;
                left = posInLine * childWidth;
                right = left + childWidth;

                View view = scene.addView(currentPosition++);
                scene.measureChild(view, scene.getWidth() - childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = scene.getDecoratedMeasuredHeight(view);
                }
                bottom = top + childHeight;

                Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
                scene.layoutDecorated(view, left, top, right, bottom);

                if (posInLine == mSpanCount - 1 || currentPosition == itemCount + positionOffset) {
                    top = bottom;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        scene.setBottom(bottom);
        return availableSpace;
    }
    
}
