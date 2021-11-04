package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;

public interface Layouter {

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

    void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean canScrollHorizontally();

    int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state);

    boolean canScrollVertically();

    int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state);

}
