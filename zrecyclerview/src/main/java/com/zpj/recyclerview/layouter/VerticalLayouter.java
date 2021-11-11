package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
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
    protected int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {
        int availableSpace = -dy;

        int left = 0;
        int top = anchorTop;
        int right = getLayoutManager().getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition--);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view, 0);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            top = bottom - measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            bottom = top;
        }
        mTop = top;
        return Math.min(-dy, -dy - availableSpace - anchorTop);
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {
        int availableSpace = dy;

        int left = 0;
        int top = anchorBottom;
        int right = getLayoutManager().getWidth();
        int bottom = anchorBottom;

        while (availableSpace > 0 && currentPosition < mPositionOffset + multiData.getCount()) {
            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            bottom = top + measuredHeight;

            layoutDecorated(view, left, top, right, bottom);

            top = bottom;
        }
        mBottom = bottom;
        return Math.min(dy, dy - availableSpace + (anchorBottom - getLayoutManager().getHeight()));
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
