package com.zpj.recyclerview.core;

import android.support.v7.widget.BaseMultiLayoutManager;

public class MultiScene {
    
    private final BaseMultiLayoutManager mLayoutManager;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    private boolean mAttached = false;

    public MultiScene(BaseMultiLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
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
        
    }
    
    protected void onDetached() {
        
    }
    
    public boolean isAttached() {
        return mAttached;
    }
    
}
