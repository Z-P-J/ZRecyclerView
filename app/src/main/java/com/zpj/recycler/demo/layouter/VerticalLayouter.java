package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalLayouter extends AbsLayouter {

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getLayoutManager() == null) {
            return;
        }

        int totalSpace = getLayoutManager().getHeight() - getTop();
        int currentPosition = 5;

        int left = 0;
        int top = getTop();
        int right = 0;
        int bottom = 0;

        while (totalSpace > 0 && currentPosition < 10) {
            View view = recycler.getViewForPosition(currentPosition);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measuredHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            currentPosition++;
            totalSpace -= measuredHeight;

            right = left + getLayoutManager().getDecoratedMeasuredWidth(view);
            bottom = top + measuredHeight;

            setBottom(Math.max(bottom, getBottom()));

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            top = bottom;
        }

    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 5; i < 10; i++) {
            View view = getLayoutManager().getChildAt(i);
            if (view == null) {
                continue;
            }
            view.offsetTopAndBottom(-dy);
        }
        return dy;
    }
}
