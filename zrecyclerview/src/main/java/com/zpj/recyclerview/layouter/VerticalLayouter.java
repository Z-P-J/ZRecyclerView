package com.zpj.recyclerview.layouter;

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
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        int left = 0;
        int top = anchorTop;
        int right = getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            Log.e(TAG, "scrollVerticallyBy decoratedTop currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition--, 0, multiData);

            int measuredHeight= getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            top = bottom - measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            bottom = top;
        }
        mScene.setTop(top);
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = getWidth();
        int bottom = anchorBottom;
        Log.e(TAG, "fillVerticalBottom anchorBottom=" + anchorBottom + " height=" + getHeight());

        while (availableSpace > 0 && currentPosition < mPositionOffset + getCount(multiData)) {
            Log.e(TAG, "fillVerticalBottom currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition++, multiData);
            int measuredHeight= getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            bottom = top + measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            top = bottom;
        }
        mScene.setBottom(bottom);
        return availableSpace;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return 0;
    }

}
