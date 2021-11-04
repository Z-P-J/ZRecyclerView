package com.zpj.recycler.demo.manager;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class StackLayoutManager extends RecyclerView.LayoutManager {
    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        int totalSpace = getWidth() - getPaddingRight();
        int currentPosition = 0;

        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;

        while (totalSpace > 0 && currentPosition < state.getItemCount()) {
            View view = recycler.getViewForPosition(currentPosition);
            addView(view);
            measureChild(view, 0, 0);
            int measureWidth = getDecoratedMeasuredWidth(view);
            currentPosition++;
            totalSpace -= measureWidth;

            right = left + measureWidth;
            bottom = top + getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
            left = right;
        }

    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 填充view
        int consumed = fill(dx, recycler);
        offsetChildrenHorizontal(-consumed);
        // 回收view
        recycle(consumed, recycler);
        return consumed;
    }

    private int fill(int dx, RecyclerView.Recycler recycler) {
        // dx > 0 : 手指从右往左滑动
        // dx < 0 : 手指从左往右滑动

        int fillPosition = 0;
        int left = 0;
        int top = 0;
        int right = 0;
        int bottom = 0;
        if (dx > 0) {
            View anchorView = getChildAt(getChildCount() - 1);
            if (anchorView == null) {
                return 0;
            }
            int anchorRight = getDecoratedRight(anchorView);
            left = anchorRight;
            if (anchorRight - dx > getWidth()) {
                return dx;
            }
            fillPosition = getPosition(anchorView) + 1;
            if (fillPosition >= getItemCount()) {
                return anchorRight - getWidth();
            }
        } else if (dx < 0) {
            View anchorView = getChildAt(0);
            if (anchorView == null) {
                return 0;
            }
            int anchorLeft = getDecoratedLeft(anchorView);
            right = anchorLeft;
            if (anchorLeft - dx < 0) {
                return dx;
            }
            fillPosition = getPosition(anchorView) - 1;
            if (fillPosition < 0) {
                return anchorLeft;
            }
        }

        int availableSpace = Math.abs(dx);
        while (availableSpace > 0) {
            View itemView = recycler.getViewForPosition(fillPosition);
            if (dx > 0) {
                addView(itemView);
            } else {
                addView(itemView, 0);
            }
            measureChild(itemView, 0, 0);
            int measuredWidth = getDecoratedMeasuredWidth(itemView);
            if (dx > 0) {
                right = left + measuredWidth;
            } else {
                left = right - measuredWidth;
            }
            bottom = top + getDecoratedMeasuredHeight(itemView);
            layoutDecorated(itemView, left, top, right, bottom);
            if (dx > 0) {
                left += measuredWidth;
                fillPosition++;
            } else {
                right -= measuredWidth;
                fillPosition--;
            }
            if (fillPosition >= 0 && fillPosition < getItemCount()) {
                availableSpace -= measuredWidth;
            } else {
                break;
            }
        }
        return dx;
    }

    private final List<View> list = new ArrayList<>(0);
    private void recycle(int dx, RecyclerView.Recycler recycler) {
        // dx > 0 : 手指从右往左滑动
        // dx < 0 : 手指从左往右滑动


        if (dx > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                if (getDecoratedRight(view) < 0) {
                    list.add(view);
                } else {
                    break;
                }
            }
        } else if (dx < 0) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                if (getDecoratedLeft(view) > getWidth()) {
                    list.add(view);
                } else {
                    break;
                }
            }
        }
        for (View view : list) {
            removeAndRecycleView(view, recycler);
        }
        list.clear();
    }

}
