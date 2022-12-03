package com.zpj.recyclerview.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.RecyclerViewHelper;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.Layouter;

public class LayoutHelper {

    protected final MultiScene mScene;
    protected final BaseMultiLayoutManager mLayoutManager;

    public LayoutHelper(MultiScene multiScene) {
        mScene = multiScene;
        mLayoutManager = multiScene.getLayoutManager();
    }

    public BaseMultiLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public int getPosition(@NonNull View child) {
        return mLayoutManager.getPosition(child);
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

    public View findViewByPosition(int position) {
        return mLayoutManager.findViewByPosition(position);
    }

    public View getViewForPosition(int position) {
        return mLayoutManager.getViewForPosition(position);
    }

    public View obtainViewForPosition(int position) {
        View view = null;
        if (mScene.getMultiData().isStickyPosition(position - mScene.getPositionOffset())) {
            view  = findViewByPosition(position);
        }
        if (view == null) {
            view = getViewForPosition(position);
        } else {
            detachAndScrapView(view);
        }
        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
        params.setScene(mScene);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        mLayoutManager.measureChild(child, widthUsed, heightUsed);
    }

//    public View addView(int position, MultiData<?> multiData) {
//        View view = getViewForPosition(position, multiData);
//        addView(view);
//        return view;
//    }
//
//    public View addView(int position, int index, MultiData<?> multiData) {
//        View view = getViewForPosition(position, multiData);
//        addView(view, index);
//        return view;
//    }
//
//    public View addViewAndMeasure(int position, MultiData<?> multiData) {
//        View view = getViewForPosition(position, multiData);
//        addView(view);
//        measureChild(view, 0, 0);
//        return view;
//    }
//
//    public View addViewAndMeasure(int position, int index, MultiData<?> multiData) {
//        View view = getViewForPosition(position, multiData);
//        addView(view, index);
//        measureChild(view, 0, 0);
//        return view;
//    }

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

    public MultiScene getMultiScene(View child) {
        return mLayoutManager.getScene(child);
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

    public void recycleViews() {
        mLayoutManager.recycleViews();
    }

    public View getFirstChild() {
        return getChildAt(0);
    }

    public View getLastChild() {
        return getChildAt(getChildCount() - 1);
    }

    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        mLayoutManager.layoutDecorated(child, left, top, right, bottom);
    }

    public void offsetChildLeftAndRight(@NonNull View child, int offset) {
        if (offset != 0) {
            child.offsetLeftAndRight(offset);
        }
    }

    public void scrapOrRecycleView(int index, View view) {
        RecyclerViewHelper.scrapOrRecycleView(mLayoutManager, index, view);
    }

    public void detachAndScrapView(@NonNull View child) {
        mLayoutManager.detachAndScrapView(child);
    }

    public boolean shouldRecycleChildViewHorizontally(View view, int consumed) {
        return getDecoratedRight(view) - consumed < 0 || getDecoratedLeft(view) - consumed > getWidth();
    }

    public boolean shouldRecycleChildViewVertically(View view, int consumed) {
        return getDecoratedBottom(view) - consumed < 0 || getDecoratedTop(view) - consumed > getHeight();
    }

    public void addViewToRecycler(View view) {
        offsetChildLeftAndRight(view, Integer.MAX_VALUE);
        mLayoutManager.recycleViews.add(view);
    }

    public void requestLayout() {
        mLayoutManager.requestLayout();
    }

    public void startInterceptRequestLayout() {
        RecyclerViewHelper.startInterceptRequestLayout(mLayoutManager);
    }

    public void stopInterceptRequestLayout() {
        RecyclerViewHelper.stopInterceptRequestLayout(mLayoutManager);
    }

    public void recycleView(View view) {
        mLayoutManager.recycleView(view);
    }
    
}
