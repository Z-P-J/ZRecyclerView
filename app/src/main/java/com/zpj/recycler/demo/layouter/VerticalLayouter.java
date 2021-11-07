package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.recycler.demo.manager.MultiLayoutParams;
import com.zpj.recyclerview.MultiData;

public class VerticalLayouter extends AbsLayouter {

    private static final String TAG = "VerticalLayouter";

    public int onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mChildCount = 0;
            mBottom = mTop;
            return mChildCount;
        }

        int totalSpace = getLayoutManager().getHeight() - getTop();

        int currentPosition = mPositionOffset;
        int childWidth = getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight();

        int left = 0;
        int top = getTop();
        int right = 0;
        int bottom = getTop();

        while (totalSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            currentPosition++;
            totalSpace -= measuredHeight;

            right = left + childWidth;
            bottom = top + measuredHeight;

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            top = bottom;
        }
        mBottom = Math.max(bottom, mTop);
        mChildCount = currentPosition - mPositionOffset + 1;
        return mChildCount;
    }

    @Override
    public int onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition, int availableSpace) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mBottom = mTop;
            return 0;
        }

        int childWidth = getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight();

        int left = 0;
        int top = mTop;
        int right = 0;
        int bottom = mTop;

        while (bottom <= getLayoutManager().getHeight() && currentPosition < multiData.getCount() + mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            currentPosition++;
            availableSpace -= measuredHeight;

            right = left + childWidth;
            bottom = top + measuredHeight;

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            top = bottom;
        }
        mBottom = Math.max(bottom, mTop);

        return 0;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 5; i < 10; i++) {
            View view = getLayoutManager().getChildAt(i);
            if (view == null) {
                continue;
            }
            view.offsetTopAndBottom(-dy);
        }
        return dy;
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, State state) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return onFillVertical2(recycler, state, mPositionOffset, dy, getTop());
            } else {
                int anchorBottom = getLayoutManager().getDecoratedBottom(anchorView);
                if (anchorBottom > getLayoutManager().getHeight()) {
                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
                        return dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        if (anchorPosition == mPositionOffset + state.getMultiData().getCount() - 1) {
                            return anchorBottom - getLayoutManager().getHeight();
                        }
                        return onFillVertical2(recycler, state, anchorPosition + 1, dy, anchorBottom);
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int consumed = onFillVertical(recycler, state,
                        mPositionOffset + state.getMultiData().getCount() - 1,
                        dy, getBottom());
                return consumed;
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
                        int consumed = onFillVertical(recycler, state, anchorPosition - 1, dy, anchorTop);
                        return consumed;
                    }
                }
            }
        }
        return 0;
    }

    private int onFillVertical(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
        int availableSpace = -dy;

        int left = 0;
        int top = anchorTop;
        int right = getLayoutManager().getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition--);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
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

    private int onFillVertical2(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
        int availableSpace = dy;

        int left = 0;
        int top = anchorTop;
        int right = getLayoutManager().getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition < mPositionOffset + state.getMultiData().getCount()) {
            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
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
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, State state) {
        return 0;
    }

}
