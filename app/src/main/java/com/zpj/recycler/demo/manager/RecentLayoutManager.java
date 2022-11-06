package com.zpj.recycler.demo.manager;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class RecentLayoutManager extends RecyclerView.LayoutManager {


    private static final String TAG = "RecentLayoutManager";

    private int mCurrentPosition = 5;

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

        layoutChildren(recycler, 0);

//        int left = 0;
//        int right = 0;
////        for (int i = 0; i < getItemCount(); i++) {
////            View child = recycler.getViewForPosition(i);
////            addView(child);
////            measureChild(child, 0, 0);
////            right = left + getDecoratedMeasuredWidth(child);
////            layoutDecorated(child, left, 0, right, getHeight());
////            child.setScaleX(0.8f);
////            child.setScaleY(0.8f);
////            left = right;
////        }
//
//
//        View pre = getPreChild(recycler, mCurrentPosition);
//        if (pre != null) {
//            addView(pre);
//            measureChild(pre, 0, 0);
//            layoutDecorated(pre, left - getDecoratedMeasuredWidth(pre), 0, left, getHeight());
//            pre.setScaleX(0.8f);
//            pre.setScaleY(0.8f);
//        }
//
//        View current = recycler.getViewForPosition(mCurrentPosition);
//        addView(current);
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
//            addView(next);
//            measureChild(next, 0, 0);
//            layoutDecorated(next, right, 0, right + getDecoratedMeasuredWidth(next), getHeight());
//            next.setScaleX(0.8f);
//            next.setScaleY(0.8f);
//        }

    }

    private void layoutChildren(RecyclerView.Recycler recycler, final int left) {


        View pre = getPreChild(recycler, mCurrentPosition);
        if (pre != null) {
            addView(pre, 0);
            measureChild(pre, 0, 0);
            layoutDecorated(pre, left - getDecoratedMeasuredWidth(pre), 0, left, getHeight());

//            pre.setPivotX(getWidth());
//            pre.setPivotY(getHeight() / 2f);
            pre.setScaleX(0.8f);
            pre.setScaleY(0.8f);
        }


        int right = 0;
        View current = recycler.getViewForPosition(mCurrentPosition);
        addView(current, 1);
        measureChild(current, 0, 0);
        right = left + getDecoratedMeasuredWidth(current);
        layoutDecorated(current, left, 0, right, getHeight());

//        current.setPivotX(getWidth() / 2f);
//        current.setPivotY(getHeight() / 2f);
        current.setScaleX(0.8f);
        current.setScaleY(0.8f);



        View next = getNextChild(recycler, mCurrentPosition);
        if (next != null) {
            addView(next, 2);
            measureChild(next, 0, 0);
            layoutDecorated(next, right, 0, right + getDecoratedMeasuredWidth(next), getHeight());
//            next.setPivotX(0);
//            next.setPivotY(getHeight() / 2f);
            next.setScaleX(0.8f);
            next.setScaleY(0.8f);
        }

        for (int i = 3; i < getChildCount(); i++) {
            removeAndRecycleViewAt(i, recycler);
        }

    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {

        View current = findViewByPosition(mCurrentPosition);
        if (current == null) {
            return 0;
        }

        int childWidth = getDecoratedMeasuredWidth(current);

        int currentLeft = getDecoratedLeft(current);
        int currentRight = getDecoratedRight(current);
        int movedLeft = currentLeft - dx;
        int movedRight = currentRight - dx;
        int center = getWidth() / 2;
        if (movedLeft > center) {

            int tempPos = mCurrentPosition;
            while (mCurrentPosition > 0 && movedLeft > center) {
                movedLeft -= childWidth;
                mCurrentPosition--;
            }

            Log.e(TAG, "mCurrentPosition=" + mCurrentPosition + " dx=" + dx);


            if (mCurrentPosition == 0) {
                if (movedLeft >= 0) {

                    int consumed = (tempPos - mCurrentPosition) * childWidth - currentLeft;




//                    int consumed = dx + movedLeft - center;
                    movedLeft = 0;



                    detachAndScrapAttachedViews(recycler);

                    layoutChildren(recycler, 0);

                    Log.e(TAG, "1111111 consumed=" + consumed + " dx=" + dx);
                    return consumed;
                } else {
                    detachAndScrapAttachedViews(recycler);

                    layoutChildren(recycler, movedLeft);
                    return dx;
                }
            } else {
                detachAndScrapAttachedViews(recycler);
                layoutChildren(recycler, movedLeft);
            }


//            View pre = findPreChild(mCurrentPosition);
//            if (pre == null) {
//                pre = getPreChild()
//            }
        } else if (movedRight < center) {
            int max = getItemCount() - 1;
            int tempPos = mCurrentPosition;
            while (mCurrentPosition < max && movedRight < center) {
                movedRight += childWidth;
                mCurrentPosition++;
            }

            Log.e(TAG, "mCurrentPosition=" + mCurrentPosition + " dx=" + dx);

            if (mCurrentPosition == max) {
                if (movedRight <= getWidth()) {

                    int consumed = getWidth() - (mCurrentPosition - tempPos) * childWidth - currentRight;


//                    int consumed = dx + movedRight + center - getWidth();

                    detachAndScrapAttachedViews(recycler);

                    layoutChildren(recycler, 0);


                    Log.e(TAG, "222222 consumed=" + consumed + " dx=" + dx);
                    return consumed;
                }
            }

            detachAndScrapAttachedViews(recycler);
            layoutChildren(recycler, movedRight - childWidth);

        } else {
            offsetChildrenHorizontal(-dx);
        }

        return dx;
    }

//    @Nullable
//    @Override
//    public PointF computeScrollVectorForPosition(int targetPosition) {
//
//        View child = getChildAt(1);
//        if (child == null) {
//            return null;
//        } else {
//
//
//
//
//            int firstChildPos = this.getPosition(child);
//            if (targetPosition == firstChildPos) {
//
//                int direction = getDecoratedLeft(child) > 0 ? -1 : 1;
//
//                return canScrollHorizontally() ? new PointF((float)direction, 0.0F)
//                        : new PointF(0.0F, (float)direction);
//            }
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
