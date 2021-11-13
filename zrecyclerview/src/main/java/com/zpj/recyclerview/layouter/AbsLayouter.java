package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public abstract class AbsLayouter implements Layouter {

    private RecyclerView.LayoutManager mManager;
    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    protected int mChildCount;

    protected int mPositionOffset;
    protected int mChildOffset;

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mBottom = mTop;
            return;
        }
//        fillVertical(null, getLayoutManager().getHeight() - mTop, recycler, multiData);

        fillVerticalBottom(recycler, multiData, currentPosition, 0, getTop()); // getLayoutManager().getHeight() - mTop
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(recycler, multiData, mPositionOffset, dy, getTop());
            } else {
                int anchorBottom = getDecoratedBottom(anchorView);
                if (anchorBottom > getLayoutManager().getHeight()) {
                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
                        return dy;
                    } else {
                        int anchorPosition = getPosition(anchorView);
                        if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                            return Math.max(0, anchorBottom - getLayoutManager().getHeight());
                        }
                        return fillVerticalBottom(recycler, multiData, anchorPosition + 1, dy, anchorBottom);
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(recycler, multiData,
                        mPositionOffset + multiData.getCount() - 1,
                        dy, getBottom());
            } else {
                int anchorTop = getDecoratedTop(anchorView);
                int anchorPosition = getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == mPositionOffset) {
                        return -anchorTop;
                    }
                    return fillVerticalTop(recycler, multiData, anchorPosition - 1, dy, anchorTop);
                }
//                if (anchorTop < 0) {
//                    if (anchorTop - dy < 0) {
//                        return -dy;
//                    } else {
//
//                        if (anchorPosition == mPositionOffset) {
//                            return -anchorTop;
//                        }
//                        return fillVerticalTop(recycler, multiData, anchorPosition - 1, dy, anchorTop);
//                    }
//                } else if (anchorPosition >= mPositionOffset) {
//                    if (anchorPosition == mPositionOffset) {
//                        return -anchorTop;
//                    }
//                    return fillVerticalTop(recycler, multiData, anchorPosition - 1, dy, anchorTop);
//                }
            }
        }
        return 0;
    }

    protected abstract int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop);

    protected abstract int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorBottom);

    @Override
    public int getChildCount() {
        return mChildCount;
    }

    @Override
    public void setPositionOffset(int offset) {
        this.mPositionOffset = offset;
    }

    @Override
    public int getPositionOffset() {
        return mPositionOffset;
    }

    @Override
    public void setChildOffset(int offset) {
        this.mChildOffset = offset;
    }

    @Override
    public int getChildOffset() {
        return this.mChildOffset;
    }

    @Override
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        this.mManager = manager;
    }

    @Override
    public RecyclerView.LayoutManager getLayoutManager() {
        return mManager;
    }

    @Override
    public void setLeft(int left) {
        this.mLeft = left;
    }

    @Override
    public void setTop(int top) {
        this.mTop = top;
    }

    @Override
    public void setRight(int right) {
        this.mRight = right;
    }

    @Override
    public void setBottom(int bottom) {
        this.mBottom = bottom;
    }


    @Override
    public int getLeft() {
        return mLeft;
    }

    @Override
    public int getTop() {
        return mTop;
    }

    @Override
    public int getRight() {
        return mRight;
    }

    @Override
    public int getBottom() {
        return mBottom;
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        this.mLeft += offset;
        this.mRight += offset;
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        this.mTop += offset;
        this.mBottom += offset;
    }

    @Override
    public int getPosition(@NonNull View child) {
        return getLayoutManager().getPosition(child);
    }

    @Override
    public int getDecoratedLeft(@NonNull View child) {
        return getLayoutManager().getDecoratedLeft(child);
    }

    @Override
    public int getDecoratedTop(@NonNull View child) {
        return getLayoutManager().getDecoratedTop(child);
    }

    @Override
    public int getDecoratedRight(@NonNull View child) {
        return getLayoutManager().getDecoratedRight(child);
    }

    @Override
    public int getDecoratedBottom(@NonNull View child) {
        return getLayoutManager().getDecoratedBottom(child);
    }

    @Override
    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        if (mManager != null) {
            mManager.layoutDecorated(child, left, top, right, bottom);
        }
    }

    @Override
    public void saveState(int firstPosition, int firstOffset) {

    }

    public View getViewForPosition(int position, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = null;
        if (multiData.isStickyItem(position - mPositionOffset)) {
            view  = getLayoutManager().findViewByPosition(position);
        }
        if (view == null) {
            view = recycler.getViewForPosition(position);
        } else {
            getLayoutManager().detachAndScrapView(view, recycler);
        }
        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
        params.setMultiData(multiData);
        return view;
    }

    public View addViewAndMeasure(int position, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = getViewForPosition(position, recycler, multiData);
        getLayoutManager().addView(view);
        getLayoutManager().measureChild(view, 0, 0);
        return view;
    }

    public View addViewAndMeasure(int position, int index, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = getViewForPosition(position, recycler, multiData);
        getLayoutManager().addView(view, index);
        getLayoutManager().measureChild(view, 0, 0);
        return view;
    }

}
