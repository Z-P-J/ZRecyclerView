package com.zpj.recycler.demo.manager;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecentLayoutManager2 extends RecyclerView.LayoutManager {


    private static final String TAG = "RecentLayoutManager2";

    private int mAnchorLeft;
    private int mAnchorPosition;

    @Override
    public boolean isAutoMeasureEnabled() {
        return false;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {

        detachAndScrapAttachedViews(recycler);

        layoutChildren(recycler);
    }

    private void layoutChildren(RecyclerView.Recycler recycler) {


        int left = mAnchorLeft;
        int right = mAnchorLeft + getWidth();
        int pos = mAnchorPosition;
        do {
            View child = recycler.getViewForPosition(pos++);
            addView(child);
            measureChild(child, 0, 0);
            layoutDecorated(child, left, 0, right, getHeight());
            left = right;
            right = left + getWidth();
        } while (left < 2 * getWidth());

//        View pre = getPreChild(recycler, mCurrentPosition);
//        if (pre != null) {
//            addView(pre, 0);
//            measureChild(pre, 0, 0);
//            layoutDecorated(pre, left - getDecoratedMeasuredWidth(pre), 0, left, getHeight());
//            pre.setScaleX(0.8f);
//            pre.setScaleY(0.8f);
//        }
//
//
//        int right = 0;
//        View current = recycler.getViewForPosition(mCurrentPosition);
//        addView(current, 1);
//        measureChild(current, 0, 0);
//        right = left + getDecoratedMeasuredWidth(current);
//        layoutDecorated(current, left, 0, right, getHeight());
//        current.setScaleX(0.8f);
//        current.setScaleY(0.8f);
//
//
//
//
//        View next = getNextChild(recycler, mCurrentPosition);
//        if (next != null) {
//            addView(next, 2);
//            measureChild(next, 0, 0);
//            layoutDecorated(next, right, 0, right + getDecoratedMeasuredWidth(next), getHeight());
//            next.setScaleX(0.8f);
//            next.setScaleY(0.8f);
//        }
//
//        for (int i = 3; i < getChildCount(); i++) {
//            removeAndRecycleViewAt(i, recycler);
//        }

    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {


        offsetChildrenHorizontal(-dx);

        Log.e(TAG, "scrollHorizontallyBy dx=" + dx);

        if (dx > 0) {

            // 向左滑动

            View last = getChildAt(getChildCount() - 1);
            if (last == null) {
                return 0;
            }



            int lastPosition = getPosition(last);
            int lastRight = getDecoratedRight(last);
            Log.e(TAG, "scrollHorizontallyBy width=" + getWidth() + " lastPosition=" + lastPosition + " lastRight=" + lastRight);
            int tempLastPosition = lastPosition;
            int tempLastRight = lastRight;
            while (lastRight < getWidth()) {
                int pos = lastPosition + 1;
                if (pos >= getItemCount()) {
                    break;
                }
                int left = lastRight;
                int right = left + getWidth();
                View child = recycler.getViewForPosition(pos);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child, left, 0, right, getHeight());
                lastPosition = pos;
                lastRight = right;
            }


            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                int right = getDecoratedRight(child);
                if (right > -getWidth()) {
                    mAnchorLeft = getDecoratedLeft(child);
                    mAnchorPosition = getPosition(child);
                    Log.e(TAG, "scrollHorizontallyBy mAnchorLeft=" + mAnchorLeft + " mAnchorPosition=" + mAnchorPosition);
                    for (int j = i - 1; j >= 0; j--) {
                        // 回收
//                        Log.e(TAG, "scrollHorizontallyBy mAnchorLeft=" + mAnchorLeft + " mAnchorPosition=" + mAnchorPosition);
                        removeAndRecycleViewAt(j, recycler);
//                        detachAndScrapViewAt(j, recycler);
                    }
                    break;
                }
            }

            if (lastPosition == getItemCount() - 1 && lastRight < getWidth()) {
                lastRight = getWidth();
                int consumed = lastRight - (lastPosition - tempLastPosition) * getWidth() - tempLastRight;
                Log.e(TAG, "111111 dx=" + dx + " consumed=" + consumed);
                return consumed;
            }


        } else {
            // 向右滑动

            View first = getChildAt(0);
            if (first == null) {
                return 0;
            }

            int firstPosition = getPosition(first);
            int firstLeft = getDecoratedRight(first);
            int tempLastPosition = firstPosition;
            int tempLastLeft = firstLeft;

            Log.e(TAG, "scrollHorizontallyBy width=" + getWidth() + " firstPosition=" + firstPosition + " firstLeft=" + firstLeft);
            while (firstLeft > -getWidth()) {
                int pos = firstPosition - 1;
                if (pos < 0) {
                    break;
                }
                int right = firstLeft;
                int left = firstLeft - getWidth();
                View child = recycler.getViewForPosition(pos);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child, left, 0, right, getHeight());
                firstPosition = pos;
                firstLeft = left;

                mAnchorLeft = firstLeft;
                mAnchorPosition = pos;

                Log.e(TAG, "scrollHorizontallyBy mAnchorLeft=" + mAnchorLeft + " mAnchorPosition=" + mAnchorPosition);
            }


            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                int left = getDecoratedLeft(child);
                if (left >= 2 * getWidth()) {
                    removeAndRecycleViewAt(i, recycler);
//                    detachAndScrapViewAt(i, recycler);
                } else {
                    break;
                }
            }

            if (firstPosition == 0 && firstLeft > 0) {
                firstLeft = 0;
                int consumed = firstLeft + (tempLastPosition - firstPosition) * getWidth() - tempLastLeft;
                Log.e(TAG, "22222 dx=" + dx + " consumed=" + consumed);
                return consumed;
            }
        }




        return dx;
    }

//    @Nullable
//    @Override
//    public PointF computeScrollVectorForPosition(int targetPosition) {
//
//        View child = getChildAt(0);
//        if (child == null) {
//            return null;
//        } else {
//
//
//
//
//            int firstChildPos = this.getPosition(child);
////            if (targetPosition == firstChildPos) {
////
////                int direction = getDecoratedLeft(child) > 0 ? -1 : 1;
////
////                return canScrollHorizontally() ? new PointF((float)direction, 0.0F)
////                        : new PointF(0.0F, (float)direction);
////            }
//            int direction = targetPosition < firstChildPos ? -1 : 1;
//            Log.e(TAG, "computeScrollVectorForPosition direction=" + direction + " targetPosition=" + targetPosition + " firstChildPos=" + firstChildPos);
//            return canScrollHorizontally() ? new PointF((float)direction, 0.0F)
//                    : new PointF(0.0F, (float)direction);
//        }
//    }


    private View getPreChild(RecyclerView.Recycler recycler, int pos) {
        if (pos < 1) {
            return null;
        }
        return recycler.getViewForPosition(pos - 1);
    }

    private View getNextChild(RecyclerView.Recycler recycler, int pos) {
        if (pos >= getItemCount() - 1) {
            return null;
        }
        return recycler.getViewForPosition(pos + 1);
    }

    private View findPreChild(int pos) {
        if (pos < 1) {
            return null;
        }
        return findViewByPosition(pos - 1);
    }

    private View findNextChild(int pos) {
        if (pos >= getItemCount() - 1) {
            return null;
        }
        return findViewByPosition(pos + 1);
    }
}
