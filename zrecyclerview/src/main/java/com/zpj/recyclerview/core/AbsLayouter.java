package com.zpj.recyclerview.core;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.Layouter;

public abstract class AbsLayouter<T extends MultiScene> implements Layouter {

    private static final String TAG = "AbsLayouter";

    protected T mScene;

    @Override
    public void layoutChildren(MultiData<?> multiData) {
        int availableSpace = getRecyclerHeight() - mScene.getTop();
        if (mScene.getItemCount() == 0 || availableSpace < 0) {
            mScene.setBottom(mScene.getTop());
            return;
        }
        fillVerticalBottom(multiData, mScene.mAnchorInfo.position + mScene.getPositionOffset(), availableSpace, mScene.getTop());
    }

    @Override
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int result = fillVerticalBottom(multiData, mScene.getPositionOffset(), dy, mScene.getTop());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                int anchorBottom = getDecoratedBottom(anchorView);
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + getRecyclerHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > getRecyclerHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = getPosition(anchorView);
                    if (anchorPosition == mScene.getPositionOffset() + mScene.getItemCount() - 1) {
                        return Math.max(0, anchorBottom - getRecyclerHeight());
                    }
                    int availableSpace = dy + getRecyclerHeight() - anchorBottom;
                    int result = fillVerticalBottom(multiData, anchorPosition + 1, availableSpace, anchorBottom);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int result = fillVerticalTop(multiData, mScene.getPositionOffset() + mScene.getItemCount() - 1,
                        -dy, mScene.getBottom());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorTop = getDecoratedTop(anchorView);
                int anchorPosition = getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == mScene.getPositionOffset()) {
                        return -anchorTop;
                    }
                    int availableSpace = -dy + anchorTop;
                    int result = fillVerticalTop(multiData, anchorPosition - 1, availableSpace, anchorTop);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
    }

    @Override
    public void attach(MultiScene multiScene) {
        mScene = (T) multiScene;
    }

    @Override
    public void detach() {
        mScene = null;
    }

    protected abstract int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop);

    protected abstract int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom);

    protected int getPosition(@NonNull View child) {
        return mScene.getPosition(child);
    }

    protected int getDecoratedLeft(@NonNull View child) {
        return mScene.getDecoratedLeft(child);
    }

    protected int getDecoratedTop(@NonNull View child) {
        return mScene.getDecoratedTop(child);
    }

    protected int getDecoratedRight(@NonNull View child) {
        return mScene.getDecoratedRight(child);
    }

    protected int getDecoratedBottom(@NonNull View child) {
        return mScene.getDecoratedBottom(child);
    }

    protected void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        mScene.layoutDecorated(child, left, top, right, bottom);
    }

    protected void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        mScene.measureChild(child, widthUsed, heightUsed);
    }

    protected int getDecoratedMeasuredWidth(@NonNull View child) {
        return mScene.getDecoratedMeasuredWidth(child);
    }

    protected int getDecoratedMeasuredHeight(@NonNull View child) {
        return mScene.getDecoratedMeasuredHeight(child);
    }

    @Px
    protected int getRecyclerWidth() {
        return mScene.getWidth();
    }

    @Px
    protected int getRecyclerHeight() {
        return mScene.getHeight();
    }


}
