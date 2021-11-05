package com.zpj.recycler.demo.layouter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recycler.demo.manager.MultiLayoutParams;
import com.zpj.recyclerview.MultiData;

import java.util.ArrayList;
import java.util.List;

public class HorizontalLayouter extends AbsLayouter {

    @Override
    public void onLayoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getLayoutManager() == null || multiData.getCount() == 0) {
            mChildCount = 0;
            return;
        }

        int totalSpace = getLayoutManager().getWidth() - getLayoutManager().getPaddingRight();
        int currentPosition = mPositionOffset;

        int left = 0;
        int top = getTop();
        int right = 0;
        int bottom = 0;

        while (totalSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            View view = recycler.getViewForPosition(currentPosition);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int measureWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
            currentPosition++;
            totalSpace -= measureWidth;

            right = left + measureWidth;
            bottom = top + getLayoutManager().getDecoratedMeasuredHeight(view);

            setBottom(Math.max(bottom, getBottom()));

            getLayoutManager().layoutDecorated(view, left, top, right, bottom);
            left = right;
        }

        mChildCount = currentPosition - mPositionOffset + 1;

    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < 5; i++) {
            View view = getLayoutManager().getChildAt(i);
            if (view == null) {
                continue;
            }
            view.offsetLeftAndRight(-dx);
        }
        return dx;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (int i = 0; i < 5; i++) {
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
