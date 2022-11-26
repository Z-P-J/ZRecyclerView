package com.zpj.recyclerview.scene;

import android.support.annotation.NonNull;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public interface Scene {

    BaseMultiLayoutManager getLayoutManager();

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

    void setPositionOffset(int offset);

    int getPositionOffset();

    void setChildOffset(int offset);

    int getChildOffset();

    int getPosition(@NonNull View child);

    View findViewByPosition(int position);

    int getDecoratedLeft(@NonNull View child);

    int getDecoratedTop(@NonNull View child);

    int getDecoratedRight(@NonNull View child);

    int getDecoratedBottom(@NonNull View child);

    void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom);

    boolean canScrollHorizontally();

//    int scrollHorizontallyBy(int dx, RecyclerView.State state);

    boolean canScrollVertically();

    void layoutChildren(MultiData<?> multiData, int currentPosition);

//    int scrollVerticallyBy(int dy, RecyclerView.State state);

    int fillVertical(View anchorView, int dy, MultiData<?> multiData);

    int fillHorizontal(View anchorView, int dx, MultiData<?> multiData);

    void saveState(int firstPosition, int firstOffset);

    boolean onTouchDown(MultiData<?> multiData, float downX, float downY);

    boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY);

    boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY);

    void scrapOrRecycleView(BaseMultiLayoutManager manager, int index, View view);

    boolean shouldRecycleChildViewVertically(View view, int consumed);

    boolean shouldRecycleChildViewHorizontally(View view, int consumed);

    void addViewToRecycler(View view);

}
