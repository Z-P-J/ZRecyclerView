package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        if (mManager != null) {
            mManager.layoutDecorated(child, left, top, right, bottom);
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return 0;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return 0;
    }

    @Override
    public void saveState(int firstPosition, int firstOffset) {

    }
}
