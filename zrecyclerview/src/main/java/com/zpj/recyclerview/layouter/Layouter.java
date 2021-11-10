package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public interface Layouter {

    class State {

        private MultiData<?> multiData;

        public void setMultiData(MultiData<?> multiData) {
            this.multiData = multiData;
        }

        public MultiData<?> getMultiData() {
            return multiData;
        }
    }

    void setLayoutManager(RecyclerView.LayoutManager manager);

    RecyclerView.LayoutManager getLayoutManager();

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

    int getChildCount();

    void setPositionOffset(int offset);

    int getPositionOffset();

    void setChildOffset(int offset);

    int getChildOffset();

    int getPosition(@NonNull View child);

    int getDecoratedLeft(@NonNull View child);

    int getDecoratedTop(@NonNull View child);

    int getDecoratedRight(@NonNull View child);

    int getDecoratedBottom(@NonNull View child);

    void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom);

    void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition);

    boolean canScrollHorizontally();

//    int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean canScrollVertically();

//    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state);

    int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData);

    int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData);

    void saveState(int firstPosition, int firstOffset);

}
