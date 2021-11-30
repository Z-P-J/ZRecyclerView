package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class HorizontalLayouter extends AbsLayouter {

    private static final String TAG = "HorizontalLayouter";

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        super.layoutChildren(multiData, recycler, mFirstPosition + mPositionOffset);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        Log.d(TAG, "onFillVertical mFirstOffset=" + mFirstOffset);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(recycler, multiData, mFirstPosition + mPositionOffset, dy, getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getDecoratedTop(anchorView);
                if (anchorBottom > getHeight()) {
                    if (anchorBottom - dy > getHeight()) {
                        return dy;
                    } else {
                        return anchorBottom - getHeight();
//                        int anchorPosition = getLayoutManager().getPosition(anchorView);
//                        if (anchorPosition == mPositionOffset + state.getMultiData().getCount() - 1) {
//                            return anchorBottom - getLayoutManager().getHeight();
//                        }
//                        return onFillVertical2(recycler, state, anchorPosition + 1, dy, anchorBottom);
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(recycler, multiData, mFirstPosition + mPositionOffset, dy, getBottom());
            } else {
                // 如果占用两行则需要以下代码
                int anchorTop = getDecoratedTop(anchorView);
                if (anchorTop < 0) {
                    if (anchorTop - dy < 0) {
                        return -dy;
                    } else {
                        return -anchorTop;
//                        int anchorPosition = getLayoutManager().getPosition(anchorView);
//                        Log.d(TAG, "anchorPosition=" + anchorPosition + " mPositionOffset=" + mPositionOffset);
//                        if (anchorPosition == mPositionOffset) {
//                            return -anchorTop;
//                        }
//                        int consumed = onFillVertical(recycler, state, mPositionOffset, dy, anchorTop);
//                        return consumed;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {
        int left = mFirstOffset;
        Log.d(TAG, "fillVerticalTop currentPosition=" + currentPosition + " left=" + left + " anchorTop=" + anchorTop + " mPositionOffset=" + mPositionOffset + " count=" + multiData.getCount());
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int availableSpace = getWidth() - getPaddingRight() - left;

        int i = 0;
        while (availableSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            View view = addViewAndMeasure(currentPosition++, i++, recycler, multiData);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            if (top == bottom) {
                top = bottom - getDecoratedMeasuredHeight(view);
            }

            layoutDecorated(view, left, top, right, bottom);
            left = right;
        }
        mTop = top;
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, - mTop);
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {

        int left = mFirstOffset;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int availableSpace = getWidth() - getPaddingRight() - left;

        while (availableSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            Log.d(TAG, "onFillVertical2 currentPosition=" + currentPosition);
            View view = addViewAndMeasure(currentPosition++, recycler, multiData);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            bottom = top + getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
            left += measuredWidth;
        }
        mBottom = bottom;
        return Math.min(dy, - anchorBottom);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        if (anchorView == null) {
            return 0;
        }
        int anchorPosition = getPosition(anchorView);
        if (anchorPosition < mPositionOffset || anchorPosition >= mPositionOffset + multiData.getCount()) {
            return 0;
        }
        int index = indexOfChild(anchorView);
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getDecoratedRight(anchorView);
            if (anchorRight - dx > getWidth()) {
                return dx;
            } else {


                if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                    return anchorRight - getWidth();
                }

                int availableSpace = dx;
                int currentPosition = anchorPosition + 1;
                int left = anchorRight;
                int top = getDecoratedTop(anchorView);
                int right = 0;
                int bottom = getDecoratedBottom(anchorView);

                int i = index + 1;
                while (availableSpace > 0 && currentPosition < mPositionOffset + multiData.getCount()) {
                    View view = getViewForPosition(currentPosition++, recycler, multiData);
                    addView(view, i++);

                    measureChild(view, 0, bottom - top);
                    int measuredWidth = getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    right = left + measuredWidth;
                    layoutDecorated(view, left, top, right, bottom);
                    left = right;
                }
                return Math.min(dx, dx - availableSpace + (anchorRight - getWidth()));
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {


                if (anchorPosition == mPositionOffset) {
                    return -anchorLeft;
                }

                int availableSpace = -dx;
                int currentPosition = anchorPosition - 1;
                int left = 0;
                int top = getDecoratedTop(anchorView);
                int right = anchorLeft;
                int bottom = getDecoratedBottom(anchorView);

                while (availableSpace > 0 && currentPosition >= mPositionOffset) {
                    View view = getViewForPosition(currentPosition--, recycler, multiData);
                    addView(view, index);

                    measureChild(view, 0, bottom - top);
                    int measuredWidth = getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    left = right - measuredWidth;
                    layoutDecorated(view, left, top, right, bottom);
                    right = left;
                }
                return Math.min(-dx, -dx - availableSpace - anchorLeft);
            }
        }
    }

}
