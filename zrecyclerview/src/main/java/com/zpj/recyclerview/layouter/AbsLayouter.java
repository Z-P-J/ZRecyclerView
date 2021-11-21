package com.zpj.recyclerview.layouter;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutManager;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public abstract class AbsLayouter implements Layouter {

    private static final String TAG = "AbsLayouter";

    private MultiLayoutManager mManager;
    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    protected int mChildCount;

    protected int mPositionOffset;
    protected int mChildOffset;

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getHeight()) {
            mBottom = mTop;
            return;
        }
        fillVerticalBottom(recycler, multiData, currentPosition, getHeight() - mTop, getTop());
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
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + getHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = getPosition(anchorView);
                    if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                        return Math.max(0, anchorBottom - getHeight());
                    }
                    int availableSpace = dy + getHeight() - anchorBottom;
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
    public void setLayoutManager(MultiLayoutManager manager) {
        this.mManager = manager;
    }

    @Override
    public MultiLayoutManager getLayoutManager() {
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
        addView(view);
        measureChild(view, 0, 0);
        return view;
    }

    public View addViewAndMeasure(int position, int index, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = getViewForPosition(position, recycler, multiData);
        addView(view, index);
        measureChild(view, 0, 0);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        getLayoutManager().measureChild(child, widthUsed, heightUsed);
    }

    public void addView(View child) {
        getLayoutManager().addView(child);
    }

    public void addView(View child, int index) {
        getLayoutManager().addView(child, index);
    }

    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getLayoutManager().getDecoratedMeasuredWidth(child);
    }

    public int getDecoratedMeasuredHeight(@NonNull View child) {
        return getLayoutManager().getDecoratedMeasuredHeight(child);
    }

    @Px
    public int getWidth() {
        return getLayoutManager().getWidth();
    }

    @Px
    public int getHeight() {
        return getLayoutManager().getHeight();
    }

    @Px
    public int getPaddingLeft() {
        return getLayoutManager().getPaddingLeft();
    }

    @Px
    public int getPaddingTop() {
        return getLayoutManager().getPaddingTop();
    }

    @Px
    public int getPaddingRight() {
        return getLayoutManager().getPaddingRight();
    }

    @Px
    public int getPaddingBottom() {
        return getLayoutManager().getPaddingBottom();
    }

    @Px
    public int getPaddingStart() {
        return getLayoutManager().getPaddingStart();
    }

    @Px
    public int getPaddingEnd() {
        return getLayoutManager().getPaddingEnd();
    }

    public int getChildCount() {
        return getLayoutManager().getChildCount();
    }

    @Nullable
    public View getChildAt(int index) {
        return getLayoutManager().getChildAt(index);
    }

}
