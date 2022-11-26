package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public class LayouterHelper {
    
    protected final BaseMultiLayoutManager mLayoutManager;

    public LayouterHelper(BaseMultiLayoutManager layoutManager) {
        mLayoutManager = layoutManager;
    }
    
    public int getPosition(@NonNull View child) {
        return mLayoutManager.getPosition(child);
    }
    
    public View findViewByPosition(int position) {
        return mLayoutManager.findViewByPosition(position);
    }
    
    public int getDecoratedLeft(@NonNull View child) {
        return mLayoutManager.getDecoratedLeft(child);
    }
    
    public int getDecoratedTop(@NonNull View child) {
        return mLayoutManager.getDecoratedTop(child);
    }
    
    public int getDecoratedRight(@NonNull View child) {
        return mLayoutManager.getDecoratedRight(child);
    }
    
    public int getDecoratedBottom(@NonNull View child) {
        return mLayoutManager.getDecoratedBottom(child);
    }

    public View getViewForPosition(int position) {
        return mLayoutManager.getViewForPosition(position);
    }

    public View getViewForPosition(int position, int offset, MultiData<?> multiData) {
        View view = null;
        if (multiData.isStickyPosition(position - offset)) {
            view  = mLayoutManager.findViewByPosition(position);
        }
        if (view == null) {
            view = getViewForPosition(position);
        } else {
            mLayoutManager.detachAndScrapView(view);
        }
        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
        params.setMultiData(multiData);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        mLayoutManager.measureChild(child, widthUsed, heightUsed);
    }

    public void addView(View child) {
        mLayoutManager.addView(child);
    }

    public void addView(View child, int index) {
        mLayoutManager.addView(child, index);
    }

    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return mLayoutManager.getDecoratedMeasuredWidth(child);
    }

    public int getDecoratedMeasuredHeight(@NonNull View child) {
        return mLayoutManager.getDecoratedMeasuredHeight(child);
    }

    @Px
    public int getWidth() {
        return mLayoutManager.getWidth();
    }

    @Px
    public int getHeight() {
        return mLayoutManager.getHeight();
    }

    @Px
    public int getPaddingLeft() {
        return mLayoutManager.getPaddingLeft();
    }

    @Px
    public int getPaddingTop() {
        return mLayoutManager.getPaddingTop();
    }

    @Px
    public int getPaddingRight() {
        return mLayoutManager.getPaddingRight();
    }

    @Px
    public int getPaddingBottom() {
        return mLayoutManager.getPaddingBottom();
    }

    @Px
    public int getPaddingStart() {
        return mLayoutManager.getPaddingStart();
    }

    @Px
    public int getPaddingEnd() {
        return mLayoutManager.getPaddingEnd();
    }

    public int getChildCount() {
        return mLayoutManager.getChildCount();
    }

    @Nullable
    public View getChildAt(int index) {
        return mLayoutManager.getChildAt(index);
    }

    public MultiData<?> getMultiData(View child) {
        return mLayoutManager.getMultiData(child);
    }

    public Layouter getLayouter(View child) {
        return mLayoutManager.getLayouter(child);
    }

    public int indexOfChild(View child) {
        return mLayoutManager.indexOfChild(child);
    }

    public MultiRecycler getRecycler() {
        return mLayoutManager.getRecycler();
    }

    public int getCount(MultiData<?> multiData) {
        return mLayoutManager.getCount(multiData);
    }

    public void recycleViews() {
        mLayoutManager.recycleViews();
    }

    public View getFirstChild() {
        return getChildAt(0);
    }

    public View getLastChild() {
        return getChildAt(getChildCount() - 1);
    }
    
}
