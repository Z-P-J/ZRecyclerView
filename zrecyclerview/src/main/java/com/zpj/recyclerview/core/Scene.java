package com.zpj.recyclerview.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.HorizontalFlinger;
import com.zpj.recyclerview.layouter.Layouter;

public interface Scene {

    boolean canScrollHorizontally();

    boolean canScrollVertically();

    Context getContext();

    void attach(BaseMultiLayoutManager manager);

    void detach();

    Layouter getLayouter();

    MultiSceneRecycler getRecycler();

    BaseMultiLayoutManager getLayoutManager();

    MultiData<?> getMultiData();

    LayoutHelper getLayoutHelper();

    void setPositionOffset(int offset);


    int getPositionOffset();

    int getItemCount();

    void setLeft(int left);

    void setTop(int top);

    void setRight(int right);

    void setBottom(int bottom);

    int getLeft();

    int getTop();

    int getRight();

    int getBottom();

    void offsetLeftAndRight(int offset);

    void offsetTopAndBottom(int offset);


    boolean isAttached();

    boolean onTouchDown(MotionEvent event);

    boolean onTouchMove(MotionEvent event);

    boolean onTouchUp(MotionEvent event, float velocityX, float velocityY);

    void onFlingFinished();

    void onFlingStopped();

    void layoutChildren();

    boolean scrollToPositionWithOffset(int position, int offset);

    void saveState(View firstChild);

    int scrollHorizontallyBy(int dx);

    void onStopOverScroll();












    int getPosition(@NonNull View child);

    int getDecoratedLeft(@NonNull View child);

    int getDecoratedTop(@NonNull View child);

    int getDecoratedRight(@NonNull View child);

    int getDecoratedBottom(@NonNull View child);

    void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom);

    View findViewByPosition(int position);

    View getViewForPosition(int position);

    View obtainViewForPosition(int position);

    View addView(int position);

    View addView(int position, int index);

    void addView(View child);

    void addView(View child, int index);

    View addViewAndMeasure(int position);

    View addViewAndMeasure(int position, int index);

    void measureChild(@NonNull View child, int widthUsed, int heightUsed);



    int getDecoratedMeasuredWidth(@NonNull View child);

    int getDecoratedMeasuredHeight(@NonNull View child);

    @Px
    int getWidth();

    @Px
    int getHeight();

    @Px
    int getPaddingLeft();

    @Px
    int getPaddingTop();

    @Px
    int getPaddingRight();

    @Px
    int getPaddingBottom();

    @Px
    int getPaddingStart();

    @Px
    int getPaddingEnd();

    int getChildCount();

    @Nullable
    View getChildAt(int index);

    int indexOfChild(View child);


    int fillVertical(View anchorView, int dy);

    int fillHorizontal(View anchorView, int dx);

}
