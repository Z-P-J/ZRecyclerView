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
        mScale = 1.5f;

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

        float nearX = (getWidth() - mChildWidth) / 2f;

        if (targetPosition > 0 && targetPosition < getItemCount() - 1) {
            if (pre == null) {
                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition - 1);
                addView(child, 0);
                measureChild(child, 0, 0);
                layoutDecorated(child, left - mChildWidth - mGap, (getHeight() - mChildHeight) / 2, left - mGap, (getHeight() + mChildHeight) / 2);
                child.setTranslationX(-nearX);
                pre = child;
            }

            if (next == null) {
                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition + 1);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child, right + mGap, (getHeight() - mChildHeight) / 2, right + mGap + mChildWidth, (getHeight() + mChildHeight) / 2);
                child.setTranslationX(nearX);
                next = child;
            }
        }

        final float startScale = current.getScaleX();
        final float endScale = 1f;
        final float deltaScale = endScale - startScale;

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(500);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        final View finalPre = pre;
        final View finalNext = next;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private final float preX = finalPre == null ? 0 : finalPre.getTranslationX();
            private final float nextX = finalNext == null ? 0 : finalNext.getTranslationX();

            private float tx = current.getTranslationX();
            private float ty = current.getTranslationY();

            private final int centerX = (left + right) / 2;
            private final int targetCenterX = endRect.centerX();

            private int lastCenterX = centerX;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                float scale = startScale + deltaScale * p;
                current.setScaleX(scale);
                current.setScaleY(scale);

                if (centerX != targetCenterX) {
                    int newCenterX = (int) (centerX + (targetCenterX - centerX) * p);
                    offsetChildrenHorizontal(newCenterX - lastCenterX);
                    lastCenterX = newCenterX;
                }

                if (tx != 0) {
                    current.setTranslationX(tx * (1f - p));
                }
                if (ty != 0) {
                    current.setTranslationY(ty * (1f - p));
                }

                if (finalPre != null) {
                    finalPre.setTranslationX(preX * (1f - p));
                }

                if (finalNext != null) {
                    finalNext.setTranslationX(nextX * (1f - p));
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

        final View pre = findViewByPosition(targetPosition - 1);
        final View next = findViewByPosition(targetPosition + 1);
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

         float transX = (getWidth() - mChildWidth) / 2f;
//        ViewAnimator currentViewAnimator = new ViewAnimator(current, 0, 0, mScale, mScale);
//        ViewAnimator preViewAnimator = new ViewAnimator(pre, -transX, 0, mScale, mScale);
//        ViewAnimator nextViewAnimator = new ViewAnimator(next, transX, 0, mScale, mScale);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private int centerX = (left + right) / 2;
            private int targetCenterX = endRect.centerX();

            private int lastCenterX = centerX;

            private float tx = current.getTranslationX();
            private float ty = current.getTranslationY();

            private float preX = pre == null ? 0 : pre.getTranslationX();
            private float nextX = next == null ? 0 : next.getTranslationX();

            private float nearX = (getWidth() - mChildWidth) / 2f;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                float scale = startScale + deltaScale * p;
                current.setScaleX(scale);
                current.setScaleY(scale);


                if (centerX != targetCenterX) {
                    int newCenterX = (int) (centerX + (targetCenterX - centerX) * p);
                    offsetChildrenHorizontal(newCenterX - lastCenterX);
                    lastCenterX = newCenterX;
                }

                if (tx != 0) {
                    current.setTranslationX(tx * p);
                }
                if (ty != 0) {
                    current.setTranslationY(ty * p);
                }

                if (pre != null) {
                    pre.setTranslationX(preX - (nearX + preX) * p);
                }

                if (next != null) {
                    next.setTranslationX(nextX + (nearX - nextX) * p);
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


    public void moveDrag(int targetPosition, float downX, float downY, int x, int y, int dx, int dy) {

        View current = findViewByPosition(targetPosition);
        if(current == null) {
            // TODO getView
            return;
        }


        float p = Math.max(0.4f, mScale - (downY - y) / downY * 2f);
        p = Math.min(mScale, p);


        Log.e(TAG, "moveDrag height=" + getHeight() + " width=" + getWidth()
                + " x=" + x + " y=" + y
                + " dx=" + dx + " dy=" + dy + " p=" + p);


        int height = (int) (mChildHeight * p);
        int width = (int) (mChildWidth * p);


        float px = downX / getWidth() ;
        float py = downY / getHeight();
        int left = (int) (x - px * width);
        int right = left + width;

        int top = (int) (y - py * height);
        int bottom = top + height;


        current.setScaleX(p);
        current.setScaleY(p);
        current.setTranslationX((left + right - getWidth()) / 2f);
        current.setTranslationY((top + bottom - getHeight()) / 2f);
    }

    public void endDrag(int targetPosition, float velocityY) {
        idle(targetPosition);
    }



    private static class ViewAnimator {

        private final View mView;

        private final float mOldTranslationX;
        private final float mOldTranslationY;

        private final float mOldScaleX;
        private final float mOldScaleY;

        private float mTargetTranslationX;
        private float mTargetTranslationY;

        private float mTargetScaleX;
        private float mTargetScaleY;

        private ViewAnimator(View view) {
            this.mView = view;

            mOldTranslationX = view.getTranslationX();
            mOldTranslationY = view.getTranslationY();
            mOldScaleX = view.getScaleX();
            mOldScaleY = view.getScaleY();

            mTargetTranslationX = mOldTranslationX;
            mTargetTranslationY = mOldTranslationY;
            mTargetScaleX = mOldScaleX;
            mTargetScaleY = mOldScaleY;
        }

        private ViewAnimator(View view, float targetTranslationX, float targetTranslationY,
                             float targetScaleX, float targetScaleY) {
            this.mView = view;

            mOldTranslationX = view.getTranslationX();
            mOldTranslationY = view.getTranslationY();
            mOldScaleX = view.getScaleX();
            mOldScaleY = view.getScaleY();

            mTargetTranslationX = targetTranslationX;
            mTargetTranslationY = targetTranslationY;
            mTargetScaleX = targetScaleX;
            mTargetScaleY = targetScaleY;
        }

        public void setTargetScaleX(float targetScaleX) {
            this.mTargetScaleX = targetScaleX;
        }

        public void setTargetScaleY(float targetScaleY) {
            this.mTargetScaleY = targetScaleY;
        }

        public void setTargetTranslationX(float targetTranslationX) {
            this.mTargetTranslationX = targetTranslationX;
        }

        public void setTargetTranslationY(float targetTranslationY) {
            this.mTargetTranslationY = targetTranslationY;
        }

        public void update(float p) {
            if (mTargetTranslationX != mOldTranslationX) {
                mView.setTranslationX(mOldTranslationX + (mTargetTranslationX - mOldTranslationX) * p);
            }
            if (mTargetTranslationY != mOldTranslationY) {
                mView.setTranslationY(mOldTranslationY + (mTargetTranslationY - mOldTranslationY) * p);
            }
            if (mTargetScaleX != mOldScaleX) {
                mView.setScaleX(mOldScaleX + (mTargetScaleX - mOldScaleX) * p);
            }
            if (mTargetScaleY != mOldScaleY) {
                mView.setScaleY(mOldScaleY + (mTargetScaleY - mOldScaleY) * p);
            }
        }

//        public void start() {
//
//        }

    }


}
