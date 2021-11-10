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
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return onFillVertical2(recycler, multiData, mPositionOffset, dy, getTop());
            } else {
                int anchorBottom = getLayoutManager().getDecoratedBottom(anchorView);
                if (anchorBottom > getLayoutManager().getHeight()) {
                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
                        return dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                            return anchorBottom - getLayoutManager().getHeight();
                        }
                        return onFillVertical2(recycler, multiData, anchorPosition + 1, dy, anchorBottom);
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return onFillVertical(recycler, multiData,
                        mPositionOffset + multiData.getCount() - 1,
                        dy, getBottom());
            } else {
                int anchorTop = getLayoutManager().getDecoratedTop(anchorView);
                if (anchorTop < 0) {
                    if (anchorTop - dy < 0) {
                        return -dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        if (anchorPosition == mPositionOffset) {
                            return -anchorTop;
                        }
                        return onFillVertical(recycler, multiData, anchorPosition - 1, dy, anchorTop);
                    }
                }
            }
        }
        return 0;
    }

    private int onFillVertical(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {
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

    private int onFillVertical2(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {
        int availableSpace = dy;

        int left = 0;
        int top = anchorTop;
        int right = getLayoutManager().getWidth();
        int bottom = anchorTop;

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
        return Math.min(dy, dy - availableSpace + (anchorTop - getLayoutManager().getHeight()));
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
