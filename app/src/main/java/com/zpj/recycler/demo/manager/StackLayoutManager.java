package com.zpj.recycler.demo.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class StackLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "StackLayoutManager";

    private final RecyclerView mRecyclerView;


    private int mGap;
    private int mChildWidth;
    private int mChildHeight;
    private float mScale = 1f;


    public StackLayoutManager(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

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


        mChildWidth = getWidth() / 3 * 2;
        mChildHeight = getHeight() / 3 * 2;
        mGap = (getWidth() - mChildWidth) / 5;
        mScale = (float) getWidth() / mChildWidth;

        // TODO currentPosition
        if (getChildCount() == 0) {
            detachAndScrapAttachedViews(recycler);

            int totalSpace = getWidth() - getPaddingRight();
            int currentPosition = 0;

            int left = (getWidth() - mChildWidth) / 2;
            int top = 0;
            int right = 0;
            int bottom = 0;

            while (totalSpace > 0 && currentPosition < state.getItemCount()) {
                View view = recycler.getViewForPosition(currentPosition);
                addView(view);
                measureChild(view, 0, 0);
                int measureWidth = getDecoratedMeasuredWidth(view);
                int measureHeight = getDecoratedMeasuredHeight(view);
                currentPosition++;
                totalSpace -= measureWidth;

                top = (getHeight() - measureHeight) / 2;
                right = left + measureWidth;
                bottom = top + measureHeight;

                layoutDecorated(view, left, top, right, bottom);
                left = right + mGap;
            }
        }

    }

    @Override
    public boolean canScrollHorizontally() {
        if (expand) {
            return false;
        }
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (expand) {
            return 0;
        }
        // 填充view
        int consumed = fill(dx, recycler);

//        if (consumed == 0) {
//            return 0;
//        }


        // 回弹
        int offset = consumed;
        if (consumed != dx) {
            if (dx > 0) {
                View anchorView = getChildAt(getChildCount() - 1);
                if (anchorView == null) {
                    offset = consumed;
                } else {
                    int minRight = 0;
                    if (getDecoratedRight(anchorView) - dx < minRight) {
                        offset = getDecoratedRight(anchorView) - minRight;
                    } else {
                        offset = dx;
                    }
                }
            } else {
                View anchorView = getChildAt(0);
                if (anchorView == null) {
                    offset = consumed;
                } else {
                    int maxLeft = getWidth();
                    if (getDecoratedLeft(anchorView) - dx > maxLeft) {
                        offset = getDecoratedLeft(anchorView) - maxLeft;
                    } else {
                        offset = dx;
                    }
                }
            }
        }

        offsetChildrenHorizontal(-offset);
        // 回收view
        recycle(consumed, recycler);
        return offset;
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
            left = anchorRight + mGap;

            int minRight = (getWidth() + mChildWidth) / 2;
            fillPosition = getPosition(anchorView) + 1;
            if (anchorRight - dx > minRight) {

                if (anchorRight - dx <= getWidth() && fillPosition < getItemCount()) {
                    View itemView = recycler.getViewForPosition(fillPosition);
                    addView(itemView);
                    measureChild(itemView, 0, 0);
                    int measuredWidth = getDecoratedMeasuredWidth(itemView);
                    int measureHeight = getDecoratedMeasuredHeight(itemView);
                    right = left + measuredWidth;

                    top = (getHeight() - measureHeight) / 2;
                    bottom = top + measureHeight;
                    layoutDecorated(itemView, left, top, right, bottom);
                }

                return dx;
            }

            if (fillPosition >= getItemCount()) {
                return anchorRight - minRight;
            }
        } else if (dx < 0) {
            View anchorView = getChildAt(0);
            if (anchorView == null) {
                return 0;
            }
            int anchorLeft = getDecoratedLeft(anchorView);
            right = anchorLeft - mGap;
            int maxLeft = (getWidth() - mChildWidth) / 2;
            fillPosition = getPosition(anchorView) - 1;
            if (anchorLeft - dx < maxLeft) {
                if (anchorLeft - dx >= 0 && fillPosition >= 0) {
                    View itemView = recycler.getViewForPosition(fillPosition);
                    addView(itemView, 0);
                    measureChild(itemView, 0, 0);
                    int measuredWidth = getDecoratedMeasuredWidth(itemView);
                    int measureHeight = getDecoratedMeasuredHeight(itemView);
                    left = right - measuredWidth;
                    top = (getHeight() - measureHeight) / 2;
                    bottom = top + measureHeight;
                    layoutDecorated(itemView, left, top, right, bottom);
                }
                return dx;
            }

            if (fillPosition < 0) {
                return anchorLeft - maxLeft;
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
            int measureHeight = getDecoratedMeasuredHeight(itemView);
            if (dx > 0) {
                right = left + measuredWidth;
            } else {
                left = right - measuredWidth;
            }

            top = (getHeight() - measureHeight) / 2;
            bottom = top + measureHeight;
            layoutDecorated(itemView, left, top, right, bottom);
            if (dx > 0) {
                left += measuredWidth;
                left += mGap;
                fillPosition++;
            } else {
                right -= measuredWidth;
                right -= mGap;
                fillPosition--;
            }
            if (fillPosition >= 0 && fillPosition < getItemCount()) {
                availableSpace -= measuredWidth;
                availableSpace -= mGap;
            } else {
                break;
            }
        }
        return dx;
    }

    private void fill() {

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









    private ValueAnimator mAnimator;
    private boolean expand;

    public boolean isExpand() {
        return expand;
    }

    public void idle(final int targetPosition) {

        final View current = findViewByPosition(targetPosition);
        if (current == null) {
            return;
        }
        expand = false;


        final int left = getDecoratedLeft(current);
        final int right = getDecoratedRight(current);
        final int top = getDecoratedTop(current);
        final int bottom = getDecoratedBottom(current);

        final Rect endRect = new Rect((getWidth() - mChildWidth) / 2, (getHeight() - mChildHeight) / 2,
                (getWidth() + mChildWidth) / 2, (getHeight() + mChildHeight) / 2);

        View pre = findViewByPosition(targetPosition - 1);
        View next = findViewByPosition(targetPosition + 1);

        final int offsetLeft;
        final int offsetRight;
        if (pre != null) {
            offsetLeft = endRect.left - mGap - getDecoratedRight(pre);
        } else {
            offsetLeft = 0;
        }
        if (next != null) {
            offsetRight = endRect.right + mGap - getDecoratedLeft(next);
        } else {
            offsetRight = 0;
        }

        if (targetPosition > 0 && targetPosition < getItemCount() - 1) {
            if (pre == null) {
                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition - 1);
                addView(child, 0);
                measureChild(child, 0, 0);
                layoutDecorated(child, left - mChildWidth - mGap, (getHeight() - mChildHeight) / 2, left - mGap, (getHeight() + mChildHeight) / 2);
            }

            if (next == null) {
                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition + 1);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child, right + mGap, (getHeight() - mChildHeight) / 2, right + mGap + mChildWidth, (getHeight() + mChildHeight) / 2);
            }
        }

        final float startScale = current.getScaleX();
        final float endScale = 1f;
        final float deltaScale = endScale - startScale;

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(500);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

//            private int lastLeft = left;
//            private int lastRight = right;

            private int lastLeft = 0;
            private int lastRight = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                int newLeft = (int) (left + (endRect.left - left) * p);
                int newTop = (int) (top + (endRect.top - top) * p);
                int newRight = (int) (right + (endRect.right - right) * p);
                int newBottom = (int) (bottom + (endRect.bottom - bottom) * p);

                layoutDecorated(current, newLeft, newTop, newRight, newBottom);

//                float scale = startScale + deltaScale * p;
//                current.setScaleX(scale);
//                current.setScaleY(scale);

//                int offsetPre = newLeft - lastLeft;
//                int offsetNext = newRight - lastRight;
//
//                lastLeft = newLeft;
//                lastRight = newRight;

                newLeft = (int) (p * offsetLeft);
                newRight = (int) (p * offsetRight);
                int offsetPre = newLeft - lastLeft;
                int offsetNext = newRight - lastRight;

                lastLeft = newLeft;
                lastRight = newRight;


                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    int pos = getPosition(child);
                    if (pos == targetPosition) {
                        continue;
                    }
                    if (pos < targetPosition) {
                        child.offsetLeftAndRight(offsetPre);
                    } else {
                        child.offsetLeftAndRight(offsetNext);
                    }
//                    if (pos < targetPosition) {
//                        int left = newL - (targetPosition - pos) * (mChildWidth + mGap);
//                        Log.e(TAG, "i=" + i + " getDecoratedLeft=" + getDecoratedLeft(child) + " off=" + (getDecoratedLeft(child) - left));
//                        child.offsetLeftAndRight(left - getDecoratedLeft(child));
//                        Log.e(TAG, "right=" + child.getRight());
//                    } else {
//                        int right = newRight + (pos - targetPosition) * (mChildWidth + mGap);
//                        Log.e(TAG, "i=" + i + " getDecoratedRight=" + getDecoratedRight(child) + " off=" + (getDecoratedRight(child) - right));
//                        child.offsetLeftAndRight(right - getDecoratedRight(child));
//                        Log.e(TAG, "left=" + child.getLeft());
//                    }
                }

            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mRecyclerView.setLayoutFrozen(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = animator;
        animator.start();
    }

    public void expand(final int targetPosition) {


        final View current = findViewByPosition(targetPosition);
        if (current == null) {
            return;
        }
        mRecyclerView.setLayoutFrozen(true);
        expand = true;

        final int left = getDecoratedLeft(current);
        final int right = getDecoratedRight(current);
        final int top = getDecoratedTop(current);
        final int bottom = getDecoratedBottom(current);

        final Rect endRect = new Rect(0, 0, getWidth(), getHeight());

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);

        final float startScale = current.getScaleX();
        final float endScale = mScale;
        final float deltaScale = endScale - startScale;


        animator.setDuration(500);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private int lastLeft = left;
            private int lastRight = right;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                int newLeft = (int) (left + (endRect.left - left) * p);
                int newTop = (int) (top + (endRect.top - top) * p);
                int newRight = (int) (right + (endRect.right - right) * p);
                int newBottom = (int) (bottom + (endRect.bottom - bottom) * p);

                layoutDecorated(current, newLeft, newTop, newRight, newBottom);

//                float scale = startScale + deltaScale * p;
//                current.setScaleX(scale);
//                current.setScaleY(scale);

                int offsetPre = newLeft - lastLeft;
                int offsetNext = newRight - lastRight;

                lastLeft = newLeft;
                lastRight = newRight;

                for (int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    if (child == null) {
                        continue;
                    }
                    int pos = getPosition(child);
                    if (pos == targetPosition) {
                        continue;
                    }
                    if (pos < targetPosition) {
                        child.offsetLeftAndRight(offsetPre);
                    } else {
                        child.offsetLeftAndRight(offsetNext);
                    }
//                    if (pos < targetPosition) {
//                        int left = newLeft - (targetPosition - pos) * (mChildWidth + mGap);
//                        child.offsetLeftAndRight(left - getDecoratedLeft(child));
//
//                        layoutDecorated(child, left, getDecoratedTop(child),
//                                left + mChildWidth, getDecoratedBottom(child));
//                    } else {
//                        int right = newRight + (pos - targetPosition) * (mChildWidth + mGap);
//                        child.offsetLeftAndRight(right - getDecoratedRight(child));
//
//                        layoutDecorated(child, right - mChildWidth, getDecoratedTop(child),
//                                right, getDecoratedBottom(child));
//                    }
                }
            }
        });

        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = animator;
        animator.start();
    }


    public void moveDrag(int targetPosition, int x, int y, int dx, int dy) {

        View current = findViewByPosition(targetPosition);
        if(current == null) {
            // TODO getView
            return;
        }


        int min = getHeight() / 4;

        float p = (float) (Math.max(min, y)) / (getHeight() + min);

        Log.e(TAG, "moveDrag height=" + getHeight() + " width=" + getWidth()
                + " x=" + x + " y=" + y
                + " dx=" + dx + " dy=" + dy + " p=" + p);


        int height = (int) (getHeight() * p);
        int width = (int) (getWidth() * p);
        int left = dx;
        int right = left + width;
        int bottom = y;
        int top = bottom - height;

        layoutDecorated(current, left, top, right, bottom);


    }

    public void endDrag(int targetPosition, float velocityY) {
        idle(targetPosition);
    }


}
