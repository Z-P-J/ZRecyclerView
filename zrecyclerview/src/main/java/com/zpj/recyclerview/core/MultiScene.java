package com.zpj.recyclerview.core;

import android.support.v7.widget.BaseMultiLayoutManager;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;

public class MultiScene {

    protected final BaseMultiLayoutManager mLayoutManager;
    protected final MultiData<?> mMultiData;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    private boolean mAttached = false;

    public MultiScene(BaseMultiLayoutManager layoutManager, MultiData<?> multiData) {
        mLayoutManager = layoutManager;
        mMultiData = multiData;
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
