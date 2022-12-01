package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.MultiScene;

public class FlowLayouter extends AbsLayouter {

    private static final String TAG = "FlowLayouter";

    private final SparseArray<ItemState> states = new SparseArray<>();

    private int mMaxRow;

    private int mSpaceLeft;
    private int mSpaceTop;
    private int mSpaceRight;
    private int mSpaceBottom;

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

    public FlowLayouter() {
        this(0);
    }

    public FlowLayouter(int space) {
        this(space, space);
    }

    public FlowLayouter(int spaceHorizontal, int spaceVertical) {
        this(spaceHorizontal, spaceVertical, spaceHorizontal, spaceVertical);
    }

    public FlowLayouter(int spaceLeft, int spaceTop, int spaceRight, int spaceBottom) {
        this.mSpaceLeft = spaceLeft;
        this.mSpaceTop = spaceTop;
        this.mSpaceRight = spaceRight;
        this.mSpaceBottom = spaceBottom;
    }

    public int getSpaceLeft() {
        return mSpaceLeft;
    }

    public int getSpaceTop() {
        return mSpaceTop;
    }

    public int getSpaceRight() {
        return mSpaceRight;
    }

    public int getSpaceBottom() {
        return mSpaceBottom;
    }

    @Override
    protected FlowLayoutHelper createLayoutHelper(MultiScene multiScene) {
        return new FlowLayoutHelper(this, multiScene);
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private void initStates(MultiData<?> multiData) {
        if (states.size() == getCount(multiData)) {
            return;
        }
        int row = 0;
        int offsetX = mSpaceLeft;
        int offsetY = 0;
        for (int i = 0; i < getCount(multiData); i++) {
            View view = getViewForPosition(i + mPositionOffset);
            measureChild(view, mSpaceLeft + mSpaceRight, 0);
            int childWidth = getDecoratedMeasuredWidth(view);
            int childHeight = getDecoratedMeasuredHeight(view);

            if (offsetX + childWidth + mSpaceRight > getWidth()) {
                offsetX = mSpaceLeft;
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
            mMaxRow = Math.max(row, mMaxRow);

            offsetX += (childWidth + mSpaceLeft + mSpaceRight);

            getLayoutHelper().recycleView(view);

            Log.d(TAG, "initStates item=" + item);
        }
    }

    // 从上往下滑动
    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        int left = 0;
        int top = anchorTop - mSpaceBottom;
        int right = 0;
        int bottom = anchorTop - mSpaceBottom;

        initStates(multiData);

        while (availableSpace > 0 && currentPosition >= mPositionOffset) {
            int key = currentPosition - mPositionOffset;
            ItemState itemState = states.get(key);

            Log.d(TAG, "onFillVertical itemState=" + itemState);

            View view = getViewForPosition(currentPosition--, multiData);
            addView(view, 0);
            measureChild(view, mSpaceLeft + mSpaceRight, 0);

            left = itemState.offsetX;
            right = left + itemState.width;

            top = bottom - itemState.height;

            Log.d(TAG, "onFillVertical left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
            layoutDecorated(view, left, top, right, bottom);

            top -= mSpaceTop;

            if (itemState.offsetX == mSpaceLeft) {
                availableSpace -= (itemState.height + mSpaceTop + mSpaceBottom);
                bottom = top - mSpaceBottom;
            }
        }
        setTop(top);
        return availableSpace;
    }

    // 从下往上滑动
    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom + mSpaceBottom;
        int right = 0;
        int bottom = anchorBottom + mSpaceBottom;

        initStates(multiData);

        int key = currentPosition - mPositionOffset;
        ItemState itemState = states.get(key);
        int row = -1;
//        Log.d(TAG, "availableSpace1=" + availableSpace);
        while (itemState != null && availableSpace > 0 && currentPosition < getCount(multiData) + mPositionOffset) {

            if (row < 0) {
                row = itemState.row;
            }

            View view = getViewForPosition(currentPosition++, multiData);
            addView(view);
            int childHeight = itemState.height;
            measureChild(view, mSpaceLeft + mSpaceRight, 0);

            left = itemState.offsetX;
            right = left + itemState.width;

            if (row != itemState.row) {
                row = itemState.row;
                top = bottom + mSpaceTop;
            }
            bottom = top + childHeight;

            Log.d(TAG, "availableSpace left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom + " childHeight=" + childHeight);
            layoutDecorated(view, left, top, right, bottom);

            bottom += mSpaceBottom;

            itemState = states.get(currentPosition - mPositionOffset);
            if (itemState == null || itemState.row != row) {
                availableSpace -= (childHeight + mSpaceTop + mSpaceBottom);
//                Log.d(TAG, "availableSpace=" + availableSpace);
            }

        }
        setBottom(Math.max(bottom, getTop()));
        return availableSpace;

    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return 0;
    }

    public static class FlowLayoutHelper extends LayoutHelper {

        protected final FlowLayouter mLayouter;

        public FlowLayoutHelper(FlowLayouter layouter,
                                MultiScene multiScene) {
            super(multiScene);
            mLayouter = layouter;
        }

        @Override
        public int getDecoratedLeft(@NonNull View child) {
            return super.getDecoratedLeft(child) - mLayouter.getSpaceLeft();
        }

        @Override
        public int getDecoratedTop(@NonNull View child) {
            return super.getDecoratedTop(child) - mLayouter.getSpaceTop();
        }

        @Override
        public int getDecoratedRight(@NonNull View child) {
            return super.getDecoratedRight(child) + mLayouter.getSpaceRight();
        }

        @Override
        public int getDecoratedBottom(@NonNull View child) {
            return super.getDecoratedBottom(child) + mLayouter.getSpaceBottom();
        }

    }

}
