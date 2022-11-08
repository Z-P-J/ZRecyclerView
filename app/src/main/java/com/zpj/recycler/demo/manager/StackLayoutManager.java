package com.zpj.recycler.demo.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "StackLayoutManager";

    private final RecyclerView mRecyclerView;

    private final float mMinFlingVelocity;
    private final float mMaxFlingVelocity;;

    private OverScroller mScroller;


    private int mGap;
    private int mWidth;
    private int mHeight;
    private int mChildWidth;
    private int mChildHeight;
    private final Rect mChildRect = new Rect();
    private float mScale = 1f;

    private int mCurrentPosition = 0;

    static final Interpolator sQuinticInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public StackLayoutManager(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        ViewConfiguration configuration = ViewConfiguration.get(recyclerView.getContext());
        mMinFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = configuration.getScaledMaximumFlingVelocity();

        mScroller = new OverScroller(recyclerView.getContext(), sQuinticInterpolator);
    }

    public void setTargetPosition(int pos) {
        this.mCurrentPosition = pos;
    }

    //    private void initTouchHelper(RecyclerView recyclerView) {
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
//            @Override
//            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN
//                        | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.UP | ItemTouchHelper.DOWN);
//            }
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
//                Collections.swap(mData, from, to);
//                notifyItemMove(from, to);
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
//
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(recyclerView);
//    }


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


        if (mWidth != getWidth() || mHeight != getHeight()) {
            mWidth = getWidth();
            mHeight = getHeight();

            mChildWidth = mWidth / 3 * 2;
            mChildHeight = mHeight / 3 * 2;
            mGap = (mWidth - mChildWidth) / 5;
            mScale = 1.5f;

            mChildRect.set((mWidth - mChildWidth) / 2, (mHeight - mChildHeight) / 2,
                    (mWidth + mChildWidth) / 2, (mHeight + mChildHeight) / 2);
        }




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


    public void idle() {
        idle(mCurrentPosition);
    }

    public void idle(int targetPosition) {
        idle(targetPosition, 1f);
    }

    private void hide(int targetPosition) {
        idle(targetPosition, 0.2f);
    }

    private void idle(final int targetPosition, float endScale) {

        final View current = findViewByPosition(targetPosition);
        if (current == null) {
            return;
        }
        expand = false;


        final int left = getDecoratedLeft(current);
        final int right = getDecoratedRight(current);
        final int top = getDecoratedTop(current);
        final int bottom = getDecoratedBottom(current);

//        final Rect endRect = new Rect(mChildRect);

        View pre = obtainPreChild(current);
        if (pre != null && pre.getTranslationX() == 0) {
            pre.setTranslationX(-mChildRect.left);
        }
        View next = obtainNextChild(current);
        if (next != null && next.getTranslationX() == 0) {
            next.setTranslationX(mChildRect.left);
        }



//        if (targetPosition > 0 && targetPosition < getItemCount() - 1) {
//            if (pre == null) {
//                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition - 1);
//                addView(child, 0);
//                measureChild(child, 0, 0);
//                layoutDecorated(child, left - mChildWidth - mGap, (getHeight() - mChildHeight) / 2, left - mGap, (getHeight() + mChildHeight) / 2);
//                child.setTranslationX(-nearX);
//                pre = child;
//            }
//
//            if (next == null) {
//                View child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(targetPosition + 1);
//                addView(child);
//                measureChild(child, 0, 0);
//                layoutDecorated(child, right + mGap, (getHeight() - mChildHeight) / 2, right + mGap + mChildWidth, (getHeight() + mChildHeight) / 2);
//                child.setTranslationX(nearX);
//                next = child;
//            }
//        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(500);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        final List<ViewEvaluator> evaluators = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            if (targetPosition == getPosition(child)) {
                evaluators.add(new ViewEvaluator(child, 0, 0, endScale, endScale));
            } else {
                evaluators.add(new ViewEvaluator(child));
            }
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private final int centerX = (left + right) / 2;
            private final int targetCenterX = getWidth() / 2;

            private int lastCenterX = centerX;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                if (centerX != targetCenterX) {
                    int newCenterX = (int) (centerX + (targetCenterX - centerX) * p);
                    offsetChildrenHorizontal(newCenterX - lastCenterX);
                    lastCenterX = newCenterX;
                }

                for (ViewEvaluator evaluator : evaluators) {
                    evaluator.update(p);
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

    public void expand() {
        expand(mCurrentPosition);
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


        animator.setDuration(500);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        float transX = (getWidth() - mChildWidth) / 2f;

        final List<ViewEvaluator> evaluators = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int pos = getPosition(child);
            if (pos == targetPosition) {
                evaluators.add(new ViewEvaluator(child, 0, 0, mScale, mScale));
            } else if (pos == targetPosition - 1) {
                evaluators.add(new ViewEvaluator(child, -transX, 0, 1f, 1f));
            } else if (pos == targetPosition + 1) {
                evaluators.add(new ViewEvaluator(child, transX, 0, 1f, 1f));
            } else {
                evaluators.add(new ViewEvaluator(child));
            }
        }

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private final int centerX = (left + right) / 2;
            private final int targetCenterX = endRect.centerX();

            private int lastCenterX = centerX;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                // TODO 动画结束时直接offsetChildrenHorizontal
                if (centerX != targetCenterX) {
                    int newCenterX = (int) (centerX + (targetCenterX - centerX) * p);
                    offsetChildrenHorizontal(newCenterX - lastCenterX);
                    lastCenterX = newCenterX;
                }

                for (ViewEvaluator evaluator : evaluators) {
                    evaluator.update(p);
                }
            }
        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                int centerX = (left + right) / 2;
//                int targetCenterX = endRect.centerX();
//                offsetChildrenHorizontal(targetCenterX - centerX);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });

        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator = null;
        }
        mAnimator = animator;
        animator.start();
    }

    public void moveDrag(float downX, float downY, int x, int y, int dx, int dy) {
        moveDrag(mCurrentPosition, downX, downY, x, y, dx, dy);
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

    public void endDrag(float velocityY) {
        endDrag(mCurrentPosition, velocityY);
    }

    public void endDrag(int targetPosition, float velocityY) {
        View current = findViewByPosition(targetPosition);
        if(current == null) {
            // TODO getView
            return;
        }




        mScroller.fling(0, 0, 0, (int) -velocityY, Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        int finalY = mScroller.getFinalY();
        this.mScroller.forceFinished(true);

        float scale = current.getScaleX();
        Log.e(TAG, "endDrag scale=" + scale + " velocityY=" + velocityY
                + " minV=" + mMinFlingVelocity + " maxV=" + mMaxFlingVelocity + " finalY=" + finalY);
        if (scale > 1.3f || velocityY > 5000) {
            expand(targetPosition);
        } else if (velocityY < -5000) {
            hide(targetPosition);
        } else {
            idle(targetPosition);
        }
    }

    public void swipeBy(float downX, float downY, int x, int y, int dx, int dy) {
        swipeBy(mCurrentPosition, downX, downY, x, y, dx, dy);
    }

    public void swipeBy(int targetPosition, float downX, float downY, int x, int y, int dx, int dy) {
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
        float translationX = (left + right - getWidth()) / 2f;
        float translationY = (top + bottom - getHeight()) / 2f;
        current.setTranslationX(translationX);
        current.setTranslationY(translationY);

        Log.e(TAG, "moveDrag translationX=" + translationX + " targetPosition=" + targetPosition);

        if (translationX > 0) {
            View pre = obtainPreChild(current);
            if (pre != null) {
                pre.setScaleX(p);
                pre.setScaleY(p);

//                left = right + mGap;
                right = left - mGap;
                left = right - width;

                pre.setTranslationX((left + right - getDecoratedLeft(pre) - getDecoratedRight(pre)) / 2f);
                pre.setTranslationY(translationY);
            }
        } else if (translationX < 0) {
            View next = obtainNextChild(current);
            if (next != null) {
                next.setScaleX(p);
                next.setScaleY(p);

                left = right + mGap;
                right = left + width;


                next.setTranslationX((left + right - getDecoratedLeft(next) - getDecoratedRight(next)) / 2f);
                next.setTranslationY(translationY);
            }
        }
    }

    public void endSwipe(float velocityY) {
        endSwipe(mCurrentPosition, velocityY);
    }

    public void endSwipe(int targetPosition, float velocityY) {

        View current = findViewByPosition(targetPosition);
        if(current == null) {
            // TODO getView
            return;
        }


//        current.setScaleX(p);
//        current.setScaleY(p);
//        current.setTranslationX((left + right - getWidth()) / 2f);
//        current.setTranslationY((top + bottom - getHeight()) / 2f);

        float translationX = current.getTranslationX();
        float scale = current.getScaleX();

        if (translationX < 0) {

            if (scale * mChildWidth / 2 + translationX < 0 && findViewByPosition(targetPosition + 1) != null) {
                mCurrentPosition = targetPosition + 1;
                expand(mCurrentPosition);
            } else {
                expand(targetPosition);
            }
        } else {
            if (translationX - scale * mChildWidth / 2 > 0 && findViewByPosition(targetPosition - 1) != null) {
                mCurrentPosition = targetPosition - 1;
                expand(mCurrentPosition);
            } else {
                expand(targetPosition);
            }
        }
    }




    private View obtainPreChild(View current) {
        int pos = getPosition(current);
        if (pos > 0) {
            View child = findViewByPosition(--pos);
            if (child == null) {
                child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(pos);
                addView(child, 0);
                measureChild(child, 0, 0);
                layoutDecorated(child, getDecoratedLeft(current) - mChildWidth - mGap, (getHeight() - mChildHeight) / 2, getDecoratedLeft(current) - mGap, (getHeight() + mChildHeight) / 2);
            }
            return child;
        }
        return null;
    }

    private View obtainNextChild(View current) {
        int pos = getPosition(current);
        if (pos < getItemCount() - 1) {
            View child = findViewByPosition(++pos);
            if (child == null) {
                child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(pos);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child, getDecoratedRight(current) + mGap, (getHeight() - mChildHeight) / 2, getDecoratedRight(current) + mGap + mChildWidth, (getHeight() + mChildHeight) / 2);
            }
            return child;
        }
        return null;
    }


    private static class ViewEvaluator {

        private final View mView;

        private float mOldTranslationX;
        private float mOldTranslationY;

        private float mOldScaleX;
        private float mOldScaleY;

        private float mTargetTranslationX;
        private float mTargetTranslationY;

        private float mTargetScaleX;
        private float mTargetScaleY;

        private ViewEvaluator(View view) {
            this(view, 0, 0, 1f, 1f);
        }

        private ViewEvaluator(View view, float targetTranslationX, float targetTranslationY,
                              float targetScaleX, float targetScaleY) {
            this.mView = view;
            if (view == null) {
                return;
            }

            mOldTranslationX = view.getTranslationX();
            mOldTranslationY = view.getTranslationY();
            mOldScaleX = view.getScaleX();
            mOldScaleY = view.getScaleY();

            mTargetTranslationX = targetTranslationX;
            mTargetTranslationY = targetTranslationY;
            mTargetScaleX = targetScaleX;
            mTargetScaleY = targetScaleY;
        }

//        public void setTargetScaleX(float targetScaleX) {
//            this.mTargetScaleX = targetScaleX;
//        }
//
//        public void setTargetScaleY(float targetScaleY) {
//            this.mTargetScaleY = targetScaleY;
//        }
//
//        public void setTargetTranslationX(float targetTranslationX) {
//            this.mTargetTranslationX = targetTranslationX;
//        }
//
//        public void setTargetTranslationY(float targetTranslationY) {
//            this.mTargetTranslationY = targetTranslationY;
//        }

        public void update(float p) {
            if (mView == null) {
                return;
            }
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

    }


}
