package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public class FlowLayouter extends AbsLayouter {

    private static final String TAG = "FlowLayouter";

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getLayoutManager().getHeight()) {
            mBottom = mTop;
            return;
        }

        initStates(multiData, recycler);

        onFillVertical2(recycler, multiData, currentPosition, getLayoutManager().getHeight() - mTop, mTop);

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
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, State state) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return onFillVertical2(recycler, state.getMultiData(), mPositionOffset, dy, mTop);
            } else {
                int anchorBottom = getLayoutManager().getDecoratedBottom(anchorView);
                if (anchorBottom > getLayoutManager().getHeight()) {
                    if (anchorBottom - dy > getLayoutManager().getHeight()) {
                        return dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        if (anchorPosition == mPositionOffset + state.getMultiData().getCount() - 1) {
                            return anchorBottom - getLayoutManager().getHeight();
                        }
                        return onFillVertical2(recycler, state.getMultiData(), anchorPosition + 1, dy, anchorBottom);
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return onFillVertical(recycler, state,
                        mPositionOffset + state.getMultiData().getCount() - 1,
                        dy, getBottom());
            } else {
                int anchorTop = getLayoutManager().getDecoratedTop(anchorView);
                if (anchorTop < 0) {
                    if (anchorTop - dy < 0) {
                        return -dy;
                    } else {
                        int anchorPosition = getLayoutManager().getPosition(anchorView);
                        if (anchorPosition == mPositionOffset) {
                            return -anchorTop;
                        }
                        return onFillVertical(recycler, state, anchorPosition - 1, dy, anchorTop);
                    }
                }
            }
        }
        return 0;
    }

    SparseArray<ItemState> states = new SparseArray<>();

    private static class ItemState {
        int row;
        int offsetX;
        int offsetY;
        int width;
        int height;

        @Override
        public String toString() {
            return "ItemState{" +
                    "row=" + row +
                    ", offsetX=" + offsetX +
                    ", offsetY=" + offsetY +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }
    }

    private void initStates(MultiData<?> multiData, RecyclerView.Recycler recycler) {
        if (states.size() == multiData.getCount()) {
            return;
        }
        int row = 0;
        int offsetX = 0;
        int offsetY = 0;
        for (int i = 0; i < multiData.getCount(); i++) {
            View view = recycler.getViewForPosition(i + mPositionOffset);
            getLayoutManager().measureChild(view, 0, 0);
            int childWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
            int childHeight = getLayoutManager().getDecoratedMeasuredHeight(view);

            if (offsetX + childWidth > getLayoutManager().getWidth()) {
                offsetX = 0;
                offsetY += childHeight;
                row++;
            }

            ItemState item = states.get(i);
            if (item == null) {
                item = new ItemState();
                states.put(i, item);
            }
            item.row = row;
            item.width = childWidth;
            item.height = childHeight;
            item.offsetX = offsetX;
            item.offsetY = offsetY;

            offsetX += childWidth;

            recycler.recycleView(view);

            Log.d(TAG, "initStates item=" + item);
        }
    }

    private int onFillVertical(RecyclerView.Recycler recycler, State state, int currentPosition, int dy, int anchorTop) {
        int availableSpace = -dy;

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        initStates(state.getMultiData(), recycler);

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            int key = currentPosition - mPositionOffset;
            ItemState itemState = states.get(key);

            Log.d(TAG, "onFillVertical itemState=" + itemState);

            View view = recycler.getViewForPosition(currentPosition--);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(state.getMultiData());
            getLayoutManager().addView(view, 0);
            getLayoutManager().measureChild(view, itemState.width, itemState.height);

            left = itemState.offsetX;
            right = left + itemState.width;

            top = bottom - itemState.height;

            Log.d(TAG, "onFillVertical left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
            layoutDecorated(view, left, top, right, bottom);

            if (itemState.offsetX == 0) {
                availableSpace -= itemState.height;
                bottom = top;
            }
        }
        mTop = top;
        return Math.min(-dy, -dy - availableSpace - anchorTop);
    }

    private int onFillVertical2(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {

        int availableSpace = dy;

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        initStates(multiData, recycler);

//        while (availableSpace > 0 && currentPosition < state.getMultiData().getCount() + mPositionOffset) {
//            View view = recycler.getViewForPosition(currentPosition++);
//            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
//            params.setMultiData(state.getMultiData());
//            getLayoutManager().addView(view);
//            getLayoutManager().measureChild(view, 0, 0);
//            int measuredWidth = getLayoutManager().getDecoratedMeasuredWidth(view);
//            int measuredHeight = getLayoutManager().getDecoratedMeasuredHeight(view);
//
//            right = left + measuredWidth;
//            if (right > getLayoutManager().getWidth()) {
//                left = 0;
//                right = measuredWidth;
//                top = bottom;
//            }
//            if (left == 0) {
//                availableSpace -= measuredHeight;
//            }
//            bottom = top + measuredHeight;
//
//            layoutDecorated(view, left, top, right, bottom);
//            left = right;
//        }
//        mBottom = Math.max(bottom, mTop);
//        return Math.min(dy, dy - availableSpace + (anchorTop - getLayoutManager().getHeight()));

        int row = -1;
        while (availableSpace > 0 && currentPosition < multiData.getCount() + mPositionOffset) {
            int key = currentPosition - mPositionOffset;
            ItemState itemState = states.get(key);
            if (row < 0) {
                row = itemState.row;
            }

            View view = recycler.getViewForPosition(currentPosition++);
            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            params.setMultiData(multiData);
            getLayoutManager().addView(view);
            getLayoutManager().measureChild(view, itemState.width, itemState.height);

            left = itemState.offsetX;
            right = left + itemState.width;

            if (row != itemState.row) {
                row = itemState.row;
                top = bottom;
                availableSpace -= itemState.height;
            }
            bottom = top + itemState.height;

            layoutDecorated(view, left, top, right, bottom);

        }
        mBottom = Math.max(bottom, mTop);
        return Math.min(dy, dy - availableSpace + (anchorTop - getLayoutManager().getHeight()));

    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, State state) {
        return 0;
    }

}
