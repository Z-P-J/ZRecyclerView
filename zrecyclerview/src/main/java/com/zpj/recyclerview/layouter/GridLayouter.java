package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.StateMultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;
import com.zpj.statemanager.State;

public class GridLayouter extends AbsLayouter {

    private static final String TAG = "GridLayouter";

    private int mSpanCount;

    public GridLayouter(int mSpanCount) {
        this.mSpanCount = mSpanCount;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    protected int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {
//        int availableSpace = -dy;
        int availableSpace = -dy + anchorTop;

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = recycler.getViewForPosition(mPositionOffset);
            MultiLayoutParams params1 = (MultiLayoutParams) view.getLayoutParams();
            params1.setMultiData(multiData);
            getLayoutManager().addView(view, 0);
            getLayoutManager().measureChild(view, 0, 0);
            int childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            top = bottom - childHeight;
            right = getLayoutManager().getWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / mSpanCount;
            int childHeight = 0;

            while (availableSpace > 0 && currentPosition >= mPositionOffset) {
                int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
                right = (posInLine + 1) * childWidth;
                left = right - childWidth;

                View view = recycler.getViewForPosition(currentPosition--);
                MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
                params.setMultiData(multiData);
                getLayoutManager().addView(view, 0);
                getLayoutManager().measureChild(view, childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
                }
                top = bottom - childHeight;

                layoutDecorated(view, left, top, right, bottom);

                if (posInLine == 0) {
                    bottom = top;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        mTop = top;
//        return Math.min(-dy, -dy - availableSpace - anchorTop);
        return Math.min(-dy, -dy - availableSpace);
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {
//        int availableSpace = dy;
        int availableSpace = dy + getLayoutManager().getHeight() - anchorBottom;

        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        if (multiData instanceof StateMultiData && ((StateMultiData<?>) multiData).getState() != com.zpj.statemanager.State.STATE_CONTENT) {
            View view = recycler.getViewForPosition(mPositionOffset);
            MultiLayoutParams params1 = (MultiLayoutParams) view.getLayoutParams();
            params1.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, 0, 0);
            int childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
            bottom = top + childHeight;
            right = getLayoutManager().getWidth();
            layoutDecorated(view, left, top, right, bottom);
            availableSpace -= childHeight;
        } else {
            int childWidth = (getLayoutManager().getWidth() - getLayoutManager().getPaddingLeft() - getLayoutManager().getPaddingRight()) / mSpanCount;
            int childHeight = 0;
            while (availableSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {

                int posInLine = (currentPosition - mPositionOffset) % mSpanCount;
                left = posInLine * childWidth;
                right = left + childWidth;

                View view = recycler.getViewForPosition(currentPosition++);
                MultiLayoutParams params1 = (MultiLayoutParams) view.getLayoutParams();
                params1.setMultiData(multiData);
                getLayoutManager().addView(view);
                getLayoutManager().measureChild(view, childWidth, 0);

                if (childHeight <= 0) {
                    childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
                }
                bottom = top + childHeight;

                Log.d(TAG, "Grid onFillVertical2 currentPosition=" + currentPosition + " left=" + left + " right=" + right + " top=" + top + " bottom=" + bottom + " posInLine=" + posInLine);
                layoutDecorated(view, left, top, right, bottom);

                if (posInLine == mSpanCount - 1 || currentPosition == multiData.getCount() + mPositionOffset) {
                    top = bottom;
                    availableSpace -= childHeight;
                    childHeight = 0;
                }
            }
        }
        mBottom = bottom;
//        return Math.min(dy, dy - availableSpace + (anchorBottom - getLayoutManager().getHeight()));
        return Math.min(dy, dy - availableSpace);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

}
