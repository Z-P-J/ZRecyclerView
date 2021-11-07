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

    private int mFirstPosition = -1;
    private int mFirstOffset;

    @Override
    public void saveState(int firstPosition, int firstOffset) {
        this.mFirstPosition = firstPosition;
        this.mFirstOffset = firstOffset;
    }

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
    public int onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition, int availableSpace) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mBottom = mTop;
            return 0;
        }

        if (mFirstPosition < 0) {
            mFirstPosition = mPositionOffset;
        }

        currentPosition = mFirstPosition;

        int left = Math.min(0, mFirstOffset);
        int top = mTop;
        int right = 0;
        int bottom = mTop;

        availableSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight() - left;

        while (availableSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);

            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            bottom = top + getLayoutManager().getDecoratedMeasuredHeight(view);

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            left = right;
        }

        mBottom = Math.max(bottom, mTop);

        return 0;
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
                if (mFirstPosition < 0) {
                    mFirstPosition = mPositionOffset;
                }
                Log.d(TAG, "fillVertical mFirstPosition=" + mFirstPosition + " mFirstOffset=" + mFirstOffset);
                return onFillVertical2(recycler, state, mFirstPosition, dy, getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getLayoutManager().getDecoratedTop(anchorView);
                if (anchorBottom > getLayoutManager().getHeight()) {
                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
                        return dy;
                    } else {
                        return anchorBottom - getLayoutManager().getHeight();
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
                if (mFirstPosition < 0) {
                    mFirstPosition = mPositionOffset;
                }
                Log.d(TAG, "fillVertical222 mFirstPosition=" + mFirstPosition + " mFirstOffset=" + mFirstOffset);
                int consumed = onFillVertical(recycler, state, mFirstPosition, dy, getBottom());
                return consumed;
            } else {
                // 如果占用两行则需要以下代码
                int anchorTop = getLayoutManager().getDecoratedTop(anchorView);
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

    private int onFillVertical(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {

        int left = Math.min(0, mFirstOffset);
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int availableSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight() - left;

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
            if (top == bottom) {
                top = bottom - getLayoutManager().getDecoratedMeasuredHeight(view);
            }

            layoutDecorated(view, left, top, right, bottom);
            left = right;
        }
        mTop = top;
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, - mTop);
    }

    private int onFillVertical2(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {

        int left = Math.min(0, mFirstOffset);
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int availableSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight() - left;

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
        return Math.min(dy, - anchorTop);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, State state) {
        if (anchorView == null) {
            return 0;
        }
        int anchorPosition = getLayoutManager().getPosition(anchorView);
        if (anchorPosition < mPositionOffset || anchorPosition >= mPositionOffset + state.getMultiData().getCount()) {
            return 0;
        }
        int index = (int) anchorView.getTag();
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getLayoutManager().getDecoratedRight(anchorView);
            if (anchorRight > getLayoutManager().getWidth()) {
                if (anchorRight - dx > getLayoutManager().getWidth()) {
                    return dx;
                } else {


                    if (anchorPosition == mPositionOffset + state.getMultiData().getCount() - 1) {
                        return anchorRight - getLayoutManager().getWidth();
                    }

                    int availableSpace = dx;
                    int currentPosition = anchorPosition + 1;
                    int left = anchorRight;
                    int top = getLayoutManager().getDecoratedTop(anchorView);
                    int right = 0;
                    int bottom = getLayoutManager().getDecoratedBottom(anchorView);

                    int i = index + 1;
                    while (availableSpace > 0 && currentPosition < mPositionOffset + state.getMultiData().getCount()) {
                        View view = recycler.getViewForPosition(currentPosition++);
                        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
                        params.setMultiData(state.getMultiData());
                        getLayoutManager().addView(view, i);
                        i++;

                        getLayoutManager().measureChild(view, 0, bottom - top);
                        int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
                        availableSpace -= measuredWidth;

                        right = left + measuredWidth;
                        layoutDecorated(view, left, top, right, bottom);
                        left = right;
                    }
                    return Math.min(dx, dx - availableSpace + (anchorRight - getLayoutManager().getWidth()));
                }
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getLayoutManager().getDecoratedLeft(anchorView);
            if (anchorLeft < 0) {
                if (anchorLeft - dx < 0) {
                    return -dx;
                } else {


                    if (anchorPosition == mPositionOffset) {
                        return -anchorLeft;
                    }

                    int availableSpace = -dx;
                    int currentPosition = anchorPosition - 1;
                    int left = 0;
                    int top = getLayoutManager().getDecoratedTop(anchorView);
                    int right = anchorLeft;
                    int bottom = getLayoutManager().getDecoratedBottom(anchorView);

                    while (availableSpace > 0 && currentPosition >= mPositionOffset) {
                        View view = recycler.getViewForPosition(currentPosition--);
                        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
                        params.setMultiData(state.getMultiData());
                        getLayoutManager().addView(view, index);

                        getLayoutManager().measureChild(view, 0, bottom - top);
                        int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
                        availableSpace -= measuredWidth;

                        left = right - measuredWidth;
                        layoutDecorated(view, left, top, right, bottom);
                        right = left;
                    }
                    return Math.min(-dx, -dx - availableSpace - anchorLeft);
                }
            }
        }

        return 0;
    }

//    private int onFillHorizontal(RecyclerView.Recycler recycler, State state, int currentPosition, int dx, int anchorRight) {
//
//    }

}
