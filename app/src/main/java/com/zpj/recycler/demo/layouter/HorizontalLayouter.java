package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recycler.demo.manager.MultiLayoutParams;
import com.zpj.recyclerview.MultiData;

import java.util.ArrayList;
import java.util.List;

public class HorizontalLayouter extends AbsLayouter {

    private static final String TAG = "HorizontalLayouter";

    @Override
    public int onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mChildCount = 0;
            mBottom = mTop;
            return mChildCount;
        }

        int totalSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight();
        int currentPosition = mPositionOffset;

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
            int measureWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
            currentPosition++;
            totalSpace -= measureWidth;

            right = left + measureWidth;
            bottom = top + getLayoutManager().getDecoratedMeasuredHeight(view);

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            left = right;
        }

        mBottom = Math.max(bottom, mTop);

        mChildCount = currentPosition - mPositionOffset + 1;
        return mChildCount;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < 5; i++) {
            View view = getLayoutManager().getChildAt(i);
            if (view == null) {
                continue;
            }
            view.offsetLeftAndRight(-dx);
        }
        return dx;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < 5; i++) {
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
                int anchorBottom = getLayoutManager().getDecoratedTop(anchorView);
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
                int consumed = onFillVertical(recycler, state, mPositionOffset, dy, getBottom());
                return consumed;
            } else {
                int anchorTop = getLayoutManager().getDecoratedTop(anchorView);
                if (anchorTop < 0) {
                    if (anchorTop - dy < 0) {
                        return -dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        Log.d(TAG, "anchorPosition=" + anchorPosition + " mPositionOffset=" + mPositionOffset);
                        if (anchorPosition == mPositionOffset) {
                            return -anchorTop;
                        }
                        int consumed = onFillVertical(recycler, state, mPositionOffset, dy, anchorTop);
                        return consumed;
                    }
                }
            }
        }
        return 0;
    }

    private int onFillVertical(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
        int availableSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight();

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int i = 0;
        while (availableSpace > 0 && currentPosition < state.getMultiData().getCount() + mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
            getLayoutManager().addView(view, i);
            i++;

            getLayoutManager().measureChild(view, 0, 0);
            int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            top = bottom - getLayoutManager().getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
            left += measuredWidth;
        }
        mTop = top;
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, mBottom - mTop);
    }

    private int onFillVertical2(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
        int availableSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight();

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition < state.getMultiData().getCount() + mPositionOffset) {
            Log.d(TAG, "onFillVertical2 currentPosition=" + currentPosition);
            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
            getLayoutManager().addView(view);

            getLayoutManager().measureChild(view, 0, 0);
            int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            bottom = top + getLayoutManager().getDecoratedMeasuredHeight(view);
            Log.d(TAG, "onFillVertical2 height=" + getLayoutManager().getDecoratedMeasuredHeight(view) + " top=" + top + " bottom=" + bottom);

            layoutDecorated(view, left, top, right, bottom);
            left += measuredWidth;
        }
        mBottom = bottom;
        Log.d(TAG, "onFillVertical2 dy=" + dy + " height=" + (mBottom - anchorTop) + " mTop=" + mTop + " mBottom=" + mBottom);
        return Math.min(dy, mBottom - anchorTop);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, State state) {
        return 0;
    }

}
