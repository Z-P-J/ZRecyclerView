package com.zpj.recyclerview.core;

import android.support.v7.widget.BaseMultiLayoutManager;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.AbsLayouter;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;

public class MultiScene {

    protected BaseMultiLayoutManager mLayoutManager;
    protected final MultiData<?> mMultiData;
    protected final Layouter mLayouter;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    private boolean mAttached = false;

    protected int mPositionOffset;

    public MultiScene(MultiData<?> multiData) {
        this(multiData, new VerticalLayouter());
    }

    public MultiScene(MultiData<?> multiData, Layouter layouter) {
        mMultiData = multiData;
        mLayouter = layouter;
    }

    public void attach(BaseMultiLayoutManager manager) {
        mLayoutManager = manager;
        mLayouter.attach(this);
    }

    public void detach() {
        mLayouter.detach();
        mTop = 0;
        mLeft = 0;
        mRight = 0;
        mBottom = 0;
        mAttached = false;
    }

    public Layouter getLayouter() {
        return mLayouter;
    }

    public MultiRecycler getRecycler() {
        return mLayoutManager.getRecycler();
    }

    public BaseMultiLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public MultiData<?> getMultiData() {
        return mMultiData;
    }

    public void setPositionOffset(int offset) {
        mPositionOffset = offset;
        mLayouter.setPositionOffset(offset);
    }


    public int getPositionOffset() {
        return mPositionOffset;
    }

    public int getItemCount() {
        return mMultiData.getCount();
    }

    public void setLeft(int left) {
        mLeft = left;
    }

    public void setTop(int top) {
        mTop = top;
        checkAttach();
    }

    public void setRight(int right) {
        mRight = right;
    }

    public void setBottom(int bottom) {
        mBottom = bottom;
        checkAttach();
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getRight() {
        return mRight;
    }

    public int getBottom() {
        return mBottom;
    }

    public void offsetLeftAndRight(int offset) {
        mLeft += offset;
        mRight += offset;
    }

    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        mBottom += offset;

        checkAttach();
    }

    private void checkAttach() {
        if (getBottom() < 0 || getTop() > mLayoutManager.getHeight()) {
            if (mAttached) {
                mAttached = false;
                onDetached();
            }
        } else {
            if (!mAttached) {
                mAttached = true;
                onAttached();
            }
        }
    }

    protected void onAttached() {
        // TODO
        ((AbsLayouter) mLayouter).onAttached();
    }

    protected void onDetached() {
        // TODO
        ((AbsLayouter) mLayouter).onDetached();
    }

    public boolean isAttached() {
        return mAttached;
    }

    boolean onTouchDown(float downX, float downY, MotionEvent event) {
        return mLayouter.onTouchDown(mMultiData, downX, downY, event);
    }

    boolean onTouchMove(float x, float y, float downX, float downY, MotionEvent event) {
        return mLayouter.onTouchMove(mMultiData, x, y, downX, downY, event);
    }

    boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        return mLayouter.onTouchUp(mMultiData, velocityX, velocityY, event);
    }

    public void layoutChildren() {
        mLayouter.layoutChildren(mMultiData);
    }

    public boolean scrollToPositionWithOffset(int position, int offset) {
        return mLayouter.scrollToPositionWithOffset(mMultiData, position, offset);
    }

    void saveState(View firstChild) {
        mLayouter.saveState(firstChild);
    }
    
}
