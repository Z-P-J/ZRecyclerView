package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;

import com.zpj.recyclerview.MultiData;

public interface Layouter {

    void setLayoutManager(RecyclerView.LayoutManager manager);

    RecyclerView.LayoutManager getLayoutManager();

    int getChildCount();

    int getPositionOffset();

    void setPositionOffset(int offset);

    int getChildOffset();

    void setChildOffset(int offset);

    void setLeft(int left);

    void setTop(int top);

    void setRight(int right);

    void setBottom(int bottom);

    int getLeft();

    int getTop();

    int getRight();

    int getBottom();

    void onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean canScrollHorizontally();

    int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean canScrollVertically();

    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state);

    int fillVertical(int dy, RecyclerView.Recycler recycler);

    int fillHorizontal(int dx, RecyclerView.Recycler recycler);

}
