package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public class GridLayouter extends AbsLayouter {

    private static final String TAG = "GridLayouter";

    private int mSpanCount;

    public GridLayouter(int mSpanCount) {
        this.mSpanCount = mSpanCount;
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mBottom = mTop;
            return;
        }

        int left = 0;
        int top = mTop;
        int right = 0;
        int bottom = mTop;

        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / mSpanCount;
        int childHeight = 0;
        while (bottom <= getLayoutManager().getHeight() && currentPosition < multiData.getCount() + mPositionOffset) {

            int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
            left = posInLine * childWidth;
            right = left + childWidth;

            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, childWidth, 0);

            if (childHeight <= 0) {
                childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            }
            bottom = top + childHeight;

            Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
            layoutDecorated(view, left, top, right, bottom);

            if (posInLine == mSpanCount - 1 || currentPosition == multiData.getCount() + mPositionOffset) {
                top = bottom;
                childHeight = 0;
            }
        }

        mBottom = Math.max(bottom, mTop);

        return;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        for (int i = 10; i < 16; i++) {
//            View view = getLayoutManager().getChildAt(i);
//            if (view == null) {
//                continue;
//            }
//            view.offsetTopAndBottom(-dy);
//        }
        return 0;
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
                            return Math.max(0, anchorBottom - getLayoutManager().getHeight());
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
                        Log.d(TAG, "scrollVerticallyBy anchorPosition=" + anchorPosition + " mPositionOffset=" + mPositionOffset);
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
//        int availableSpace = -dy;
//
//        int left = 0;
//        int top = anchorTop;
//        int right = getLayoutManager().getWidth();
//        int bottom = anchorTop;
//
//        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / 2;
//
//        while (availableSpace > 0 && currentPosition > mPositionOffset) {
//            View view1 = recycler.getViewForPosition(currentPosition - 2);
//            MultiLayoutParams params1 = (MultiLayoutParams) view1.getLayoutParams();
//            params1.setMultiData(state.getMultiData());
//            getLayoutManager().addView(view1, 0);
//            getLayoutManager().measureChild(view1, childWidth, 0);
//            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view1);
//
//            View view2 = recycler.getViewForPosition(currentPosition - 1);
//            MultiLayoutParams params2 = (MultiLayoutParams) view2.getLayoutParams();
//            params2.setMultiData(state.getMultiData());
//            getLayoutManager().addView(view2, 1);
//            getLayoutManager().measureChild(view2, childWidth, 0);
//
//            top = bottom - measuredHeight;
//
//            layoutDecorated(view1, left, top, left + childWidth, bottom);
//            layoutDecorated(view2, left + childWidth, top, right, bottom);
//
//            availableSpace -= measuredHeight;
//
//            bottom = top;
//
//            currentPosition -= 2;
//        }
//        mTop = top;
//        return Math.min(-dy, -dy - availableSpace);


        int availableSpace = -dy;

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / mSpanCount;
        int childHeight = 0;

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
            right = (posInLine + 1) * childWidth;
            left = right - childWidth;

            View view = recycler.getViewForPosition(currentPosition--);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
            getLayoutManager().addView(view, 0);
            getLayoutManager().measureChild(view, childWidth, 0);

            if (childHeight <= 0) {
                childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            }
            top = bottom - childHeight;

            Log.d(TAG, "Grid onFillVertical currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
            layoutDecorated(view, left, top, right, bottom);

            if (posInLine == 0) {
                bottom = top;
                availableSpace -= childHeight;
                childHeight = 0;
            }
        }
        mTop = top;
        Log.d(TAG, "Grid onFillVertical dy=" + dy + " av=" + (-dy - availableSpace) + " av2=" + (-mTop - anchorTop));
        return Math.min(-dy, -dy - availableSpace - anchorTop);
    }

    private int onFillVertical2(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
//        int availableSpace = dy;
//
//        int left = 0;
//        int top = anchorTop;
//        int right = getLayoutManager().getWidth();
//        int bottom = anchorTop;
//
//        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / 2;
//
//        int total = mPositionOffset + state.getMultiData().getCount() - 1;
//        while (availableSpace > 0 && currentPosition < total) {
//            View view1 = recycler.getViewForPosition(currentPosition++);
//            Log.d(TAG, "onFillVertical currentPosition1=" + currentPosition + " total=" + total);
//            MultiLayoutParams params1 = (MultiLayoutParams) view1.getLayoutParams();
//            params1.setMultiData(state.getMultiData());
//            getLayoutManager().addView(view1);
//            getLayoutManager().measureChild(view1, childWidth, 0);
//            int measuredHeight= getLayoutManager().getDecoratedMeasuredHeight(view1);
//
//            View view2 = recycler.getViewForPosition(currentPosition++);
//            Log.d(TAG, "onFillVertical currentPosition2=" + currentPosition + " total=" + total);
//            MultiLayoutParams params2 = (MultiLayoutParams) view2.getLayoutParams();
//            params2.setMultiData(state.getMultiData());
//            getLayoutManager().addView(view2);
//            getLayoutManager().measureChild(view2, childWidth, 0);
//
//            bottom = top + measuredHeight;
//
//            layoutDecorated(view1, left, top, left + childWidth, bottom);
//            layoutDecorated(view2, left + childWidth, top, right, bottom);
//
//            availableSpace -= measuredHeight;
//
//            top = bottom;
//        }
//        mBottom = bottom;
//        return Math.min(dy, dy - availableSpace);


        int availableSpace = dy;

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / mSpanCount;
        int childHeight = 0;
        while (availableSpace > 0 && currentPosition < state.getMultiData().getCount() + mPositionOffset) {

            int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
            left = posInLine * childWidth;
            right = left + childWidth;

            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params1 = (MultiLayoutParams) view.getLayoutParams();
            params1.setMultiData(state.getMultiData());
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, childWidth, 0);

            if (childHeight <= 0) {
                childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            }
            bottom = top + childHeight;

            Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
            layoutDecorated(view, left, top, right, bottom);

            if (posInLine == mSpanCount - 1 || currentPosition == state.getMultiData().getCount() + mPositionOffset) {
                top = bottom;
                availableSpace -= childHeight;
                childHeight = 0;
            }
        }
        mBottom = bottom;
        return Math.min(dy, dy - availableSpace + (anchorTop - getLayoutManager().getHeight()));
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, State state) {
        return 0;
    }

}
