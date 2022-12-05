package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.AnchorInfo;
import com.zpj.recyclerview.core.Scene;

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
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private void initStates(Scene scene) {
        int itemCount = scene.getItemCount();
        if (states.size() == itemCount) {
            return;
        }
        int row = 0;
        int offsetX = mSpaceLeft;
        int offsetY = 0;
        int positionOffset = scene.getPositionOffset();
        for (int i = 0; i < itemCount; i++) {
            View view = scene.getViewForPosition(i + positionOffset);
            scene.measureChild(view, mSpaceLeft + mSpaceRight, 0);
            int childWidth = scene.getDecoratedMeasuredWidth(view);
            int childHeight = scene.getDecoratedMeasuredHeight(view);

            if (offsetX + childWidth + mSpaceRight > scene.getWidth()) {
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

            scene.getLayoutHelper().recycleView(view);

            Log.d(TAG, "initStates item=" + item);
        }
    }

    // 从上往下滑动
    @Override
    protected int fillVerticalTop(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y - mSpaceBottom;
        int right = 0;
        int bottom = top;

        initStates(scene);

        while (availableSpace > 0 && currentPosition >= positionOffset) {
            int key = currentPosition - positionOffset;
            ItemState itemState = states.get(key);

            Log.d(TAG, "onFillVertical itemState=" + itemState);

            View view = scene.obtainViewForPosition(currentPosition--);
            scene.addView(view, 0);
            scene.measureChild(view, mSpaceLeft + mSpaceRight, 0);

            left = itemState.offsetX;
            right = left + itemState.width;

            top = bottom - itemState.height;

            Log.d(TAG, "onFillVertical left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
            scene.layoutDecorated(view, left, top, right, bottom);

            top -= mSpaceTop;

            if (itemState.offsetX == mSpaceLeft) {
                availableSpace -= (itemState.height + mSpaceTop + mSpaceBottom);
                bottom = top - mSpaceBottom;
            }
        }
        scene.setTop(top);
        return availableSpace;
    }

    // 从下往上滑动
    @Override
    protected int fillVerticalBottom(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y + mSpaceBottom;
        int right = 0;
        int bottom = top;

        initStates(scene);

        int itemCount = scene.getItemCount();
        int key = currentPosition - positionOffset;
        ItemState itemState = states.get(key);
        int row = -1;
//        Log.d(TAG, "availableSpace1=" + availableSpace);
        while (itemState != null && availableSpace > 0 && currentPosition < itemCount + positionOffset) {

            if (row < 0) {
                row = itemState.row;
            }

            View view = scene.obtainViewForPosition(currentPosition++);
            scene.addView(view);
            int childHeight = itemState.height;
            scene.measureChild(view, mSpaceLeft + mSpaceRight, 0);

            left = itemState.offsetX;
            right = left + itemState.width;

            if (row != itemState.row) {
                row = itemState.row;
                top = bottom + mSpaceTop;
            }
            bottom = top + childHeight;

            Log.d(TAG, "availableSpace left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom + " childHeight=" + childHeight);
            scene.layoutDecorated(view, left, top, right, bottom);

            bottom += mSpaceBottom;

            itemState = states.get(currentPosition - positionOffset);
            if (itemState == null || itemState.row != row) {
                availableSpace -= (childHeight + mSpaceTop + mSpaceBottom);
//                Log.d(TAG, "availableSpace=" + availableSpace);
            }

        }
        scene.setBottom(Math.max(bottom, scene.getTop()));
        return availableSpace;

    }

}
