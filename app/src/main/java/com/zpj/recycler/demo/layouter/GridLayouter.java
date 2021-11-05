package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zpj.recycler.demo.manager.MultiLayoutParams;
import com.zpj.recyclerview.MultiData;

public class GridLayouter extends AbsLayouter {

    @Override
    public void onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getLayoutManager() == null || multiData.getCount() == 0) {
            mChildCount = 0;
            return;
        }

        int totalSpace = getLayoutManager().getHeight() - getTop();
        int currentPosition = mPositionOffset;

        int left = 0;
        int top = getTop();
        int right = 0;
        int bottom = 0;

        int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / 2;

        while (totalSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            View view1 = recycler.getViewForPosition(currentPosition);
            MultiLayoutParams params1 = (MultiLayoutParams) view1.getLayoutParams();
            params1.setMultiData(multiData);
            getLayoutManager().addView(view1);
            getLayoutManager().measureChild(view1, childWidth, 0);
            currentPosition++;


            View view2 = recycler.getViewForPosition(currentPosition);
            MultiLayoutParams params2 = (MultiLayoutParams) view2.getLayoutParams();
            params2.setMultiData(multiData);
            getLayoutManager().addView(view2);
            getLayoutManager().measureChild(view2, childWidth, 0);
            currentPosition++;

            int measuredHeight = Math.max(getLayoutManager().getDecoratedMeasuredHeight(view1), getLayoutManager().getDecoratedMeasuredHeight(view2));

            totalSpace -= measuredHeight;

            bottom = top + measuredHeight;

            setBottom(Math.max(bottom, getBottom()));

            getLayoutManager().layoutDecorated(view1, left, top, left + childWidth, bottom);
            getLayoutManager().layoutDecorated(view2, left + childWidth, top, left + 2 * childWidth, bottom);
            top = bottom;
        }

        mChildCount = currentPosition - mPositionOffset + 1;

    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 10; i < 16; i++) {
            View view = getLayoutManager().getChildAt(i);
            if (view == null) {
                continue;
            }
            view.offsetTopAndBottom(-dy);
        }
        return dy;
    }

    @Override
    public int fillVertical(int dy, RecyclerView.Recycler recycler) {
        return 0;
    }

    @Override
    public int fillHorizontal(int dx, RecyclerView.Recycler recycler) {
        return 0;
    }
}
