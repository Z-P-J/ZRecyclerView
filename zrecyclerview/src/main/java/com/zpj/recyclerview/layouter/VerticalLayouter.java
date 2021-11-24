package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class VerticalLayouter extends AbsLayouter {

    private static final String TAG = "VerticalLayouter";

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
        int right = getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            Log.e(TAG, "scrollVerticallyBy decoratedTop currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition--, 0, recycler, multiData);

            int measuredHeight= getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            top = bottom - measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            bottom = top;
        }
        mTop = top;
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = getWidth();
        int bottom = anchorBottom;
        Log.e(TAG, "fillVerticalBottom anchorBottom=" + anchorBottom + " height=" + getHeight());

        while (availableSpace > 0 && currentPosition < mPositionOffset + multiData.getCount()) {
            Log.e(TAG, "fillVerticalBottom currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition++, recycler, multiData);
            int measuredHeight= getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            bottom = top + measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            top = bottom;
        }
        mBottom = bottom;
        return availableSpace;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
