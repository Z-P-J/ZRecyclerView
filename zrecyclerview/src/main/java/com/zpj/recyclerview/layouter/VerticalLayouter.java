package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

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
//        int availableSpace = -dy + anchorTop;
//        if (anchorTop > 0) {
//            availableSpace += anchorTop;
//        }

        int left = 0;
        int top = anchorTop;
        int right = getLayoutManager().getWidth();
        int bottom = anchorTop;

//        boolean isFull = currentPosition == mPositionOffset + multiData.getCount() - 1;
        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
//            View view = recycler.getViewForPosition(currentPosition--);
//            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
//            params.setMultiData(multiData);
//            getLayoutManager().addView(view, 0);
//            getLayoutManager().measureChild(view, 0, 0);

            Log.e(TAG, "scrollVerticallyBy decoratedTop currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition--, 0, recycler, multiData);

            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            top = bottom - measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            bottom = top;
        }
//        Log.e(TAG, "scrollVerticallyBy dy=" + dy + " availableSpace=" + availableSpace + " anchorTop=" + anchorTop + " return=" + Math.min(-dy, -dy - availableSpace));
        mTop = top;
//        return Math.min(-dy, -dy - availableSpace - anchorTop);
        return availableSpace;
//        return Math.min(-dy, -dy - availableSpace + anchorTop);
//        if (anchorTop < 0) {
//            Math.min(-dy, -dy - availableSpace - anchorTop);
//        } else {
//            Math.min(-dy, Math.min(-dy - availableSpace, -dy - availableSpace - anchorTop))
//        }
//        return Math.min(-dy, Math.min(-dy - availableSpace, -dy - availableSpace - anchorTop));
//        return Math.min(-dy, isFull ? -dy - availableSpace : -dy - availableSpace - anchorTop);
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
//        int availableSpace = dy;
//        if (anchorBottom < getLayoutManager().getHeight()) {
//            availableSpace += getLayoutManager().getHeight() - anchorBottom;
//        }

//        int availableSpace = dy + getLayoutManager().getHeight() - anchorBottom;
//        int temp = availableSpace;

        int left = 0;
        int top = anchorBottom;
        int right = getLayoutManager().getWidth();
        int bottom = anchorBottom;
        Log.e(TAG, "fillVerticalBottom anchorBottom=" + anchorBottom + " height=" + getLayoutManager().getHeight());
//        Log.e(TAG, "fillVerticalBottom availableSpace=" + availableSpace + " dy=" + dy + " currentPosition=" + currentPosition);

        while (availableSpace > 0 && currentPosition < mPositionOffset + multiData.getCount()) {
//            View view = recycler.getViewForPosition(currentPosition++);
//            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
//            params.setMultiData(multiData);
//            getLayoutManager().addView(view);
//            getLayoutManager().measureChild(view, 0, 0);
            Log.e(TAG, "fillVerticalBottom currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = addViewAndMeasure(currentPosition++, recycler, multiData);
            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            bottom = top + measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            top = bottom;
        }
        mBottom = bottom;
//        Log.e(TAG, "fillVerticalBottom dy=" + dy + " availableSpace=" + availableSpace + " return=" + Math.min(dy, temp - availableSpace));
//        return Math.min(dy, dy - availableSpace + (anchorBottom - getLayoutManager().getHeight()));
//        return Math.min(dy, temp - availableSpace);
        return availableSpace;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
