package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public abstract class AbsLayouter implements Layouter {

    private static final String TAG = "AbsLayouter";

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
        fillVerticalBottom(recycler, multiData, currentPosition, getLayoutManager().getHeight() - mTop, getTop());
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int result = fillVerticalBottom(recycler, multiData, mPositionOffset, dy, getTop());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                int anchorBottom = getDecoratedBottom(anchorView);
//                Log.d(TAG, "fillVertical anchorBottom=" + anchorBottom + " height=" + getLayoutManager().getHeight());
//                if (anchorBottom > getLayoutManager().getHeight()) {
//                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
//                        Log.d(TAG, "fillVertical return dy=" + dy);
//                        return dy;
//                    } else {
//                        int anchorPosition = getPosition(anchorView);
//                        if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
//                            return Math.max(0, anchorBottom - getLayoutManager().getHeight());
//                        }
//                        int availableSpace = dy + getLayoutManager().getHeight() - anchorBottom;
//                        int result = fillVerticalBottom(recycler, multiData, anchorPosition + 1, availableSpace, anchorBottom);
//                        return Math.min(dy, availableSpace - result);
//                    }
//                }
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + getLayoutManager().getHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > getLayoutManager().getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = getPosition(anchorView);
                    if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                        return Math.max(0, anchorBottom - getLayoutManager().getHeight());
                    }
                    int availableSpace = dy + getLayoutManager().getHeight() - anchorBottom;
                    int result = fillVerticalBottom(recycler, multiData, anchorPosition + 1, availableSpace, anchorBottom);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int result = fillVerticalTop(recycler, multiData, mPositionOffset + multiData.getCount() - 1,
                        -dy, getBottom());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorTop = getDecoratedTop(anchorView);
                int anchorPosition = getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == mPositionOffset) {
                        return -anchorTop;
                    }
                    int availableSpace = -dy + anchorTop;
                    int result = fillVerticalTop(recycler, multiData, anchorPosition - 1, availableSpace, anchorTop);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
//        return 0;
    }

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

    protected abstract int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop);

    protected abstract int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom);

    public View getViewForPosition(int position, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = null;
        if (multiData.isStickyPosition(position - mPositionOffset)) {
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
