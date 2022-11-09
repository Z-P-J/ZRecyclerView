package com.zpj.recycler.demo.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.Px;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.List;

public class StackLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "StackLayoutManager";

    private final RecyclerView mRecyclerView;

    private final float mMinFlingVelocity;
    private final float mMaxFlingVelocity;


    private final AnimationManager mAnimationManager = new AnimationManager();


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

    public void offsetChildrenHorizontal(@Px int dx) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            child.offsetLeftAndRight(dx);
            if (mChildRect.centerX() > child.getLeft() && mChildRect.centerX() < child.getRight()) {
                mCurrentPosition = getPosition(child);
            }
        }

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
//        idle(targetPosition, 0.2f);

        final View current = findViewByPosition(targetPosition);
        if (current == null) {
            return;
        }
        expand = false;

        mAnimationManager.reset();
        ViewTransformer transformer;
        transformer = new ViewTransformer(current, 0, 0, 0.2f, 0.2f);
        transformer.setTargetCenterX(mChildRect.centerX());
        mAnimationManager.addViewTransformer(transformer);
        mAnimationManager.start();
    }

    private void idle(final int targetPosition, float endScale) {

        final View current = findViewByPosition(targetPosition);
        if (current == null) {
            return;
        }
        expand = false;

        obtainPreChild(current, new ViewDecorator() {
            @Override
            public void decorate(View view) {
                view.setTranslationX(-mChildRect.left);
            }
        });
        obtainNextChild(current, new ViewDecorator() {
            @Override
            public void decorate(View view) {
                view.setTranslationX(mChildRect.left);
            }
        });


        mAnimationManager.reset();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int pos = getPosition(child);
            ViewTransformer transformer;
            if (targetPosition == getPosition(child)) {
                transformer = new ViewTransformer(child, 0, 0, endScale, endScale);
                transformer.setTargetCenterX(mChildRect.centerX());
            } else {
                transformer = new ViewTransformer(child);
                transformer.setTargetCenterX(mChildRect.centerX() + (pos - targetPosition) * (mChildWidth + mGap));
            }
            mAnimationManager.addViewTransformer(transformer);
        }

        mAnimationManager.start(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRecyclerView.setLayoutFrozen(false);
            }
        });
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

        mAnimationManager.reset();
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int pos = getPosition(child);
            ViewTransformer transformer;
            if (pos == targetPosition) {
                transformer = new ViewTransformer(child, 0, 0, mScale, mScale);
            } else if (pos == targetPosition - 1) {
                transformer = new ViewTransformer(child, -mChildRect.left, 0, 1f, 1f);
            } else if (pos == targetPosition + 1) {
                transformer = new ViewTransformer(child, mChildRect.left, 0, 1f, 1f);
            } else {
                transformer = new ViewTransformer(child);
            }
            transformer.setTargetCenterX(mChildRect.centerX() + (pos - targetPosition) * (mChildWidth + mGap));
            mAnimationManager.addViewTransformer(transformer);
        }
        mAnimationManager.start();
    }

    public void moveDrag(float downX, float downY, int x, int y, int dx, int dy) {
        moveDrag(mCurrentPosition, downX, downY, x, y, dx, dy);
    }

    public void moveDrag(int targetPosition, float downX, float downY, int x, int y, int dx, int dy) {

        View current = findViewByPosition(targetPosition);
        if (current == null) {
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


        float px = downX / getWidth();
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
        if (current == null) {
            // TODO getView
            return;
        }

        // TODO
        mScroller.fling(0, 0, 0, (int) -velocityY, Integer.MIN_VALUE,
                Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

        int finalY = mScroller.getFinalY();
        this.mScroller.forceFinished(true);

        float scale = current.getScaleX();
        Log.e(TAG, "endDrag scale=" + scale + " velocityY=" + velocityY
                + " minV=" + mMinFlingVelocity + " maxV=" + mMaxFlingVelocity + " finalY=" + finalY);
        if (velocityY < -mMaxFlingVelocity / 2) {
            hide(targetPosition);
        } else if (scale > 1.3f || velocityY > mMaxFlingVelocity / 2) {
            expand(targetPosition);
        } else {
            idle(targetPosition);
        }
    }

    public void swipeBy(float downX, float downY, int x, int y, int dx, int dy) {
        swipeBy(mCurrentPosition, downX, downY, x, y, dx, dy);
    }

    public void swipeBy(int targetPosition, float downX, float downY, int x, int y, int dx, int dy) {
        View current = findViewByPosition(targetPosition);
        if (current == null) {
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


        float px = downX / getWidth();
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


        View pre = obtainPreChild(current);
        if (pre != null) {
            pre.setScaleX(p);
            pre.setScaleY(p);
            int childRight = left - mGap;
            int childLeft = childRight - width;
            pre.setTranslationX((childLeft + childRight - getDecoratedLeft(pre) - getDecoratedRight(pre)) / 2f);
            pre.setTranslationY(translationY);
        }

        View next = obtainNextChild(current);
        if (next != null) {
            next.setScaleX(p);
            next.setScaleY(p);
            int childLeft = right + mGap;
            int childRight = childLeft + width;
            next.setTranslationX((childLeft + childRight - getDecoratedLeft(next) - getDecoratedRight(next)) / 2f);
            next.setTranslationY(translationY);
        }
    }

    public void endSwipe(float velocityY) {
        endSwipe(mCurrentPosition, velocityY);
    }

    public void endSwipe(int targetPosition, float velocityY) {

        View current = findViewByPosition(targetPosition);
        if (current == null) {
            // TODO getView
            return;
        }

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

    public void removeItem(int targetPosition) {
        int currentPosition = mCurrentPosition;
        View current = findViewByPosition(currentPosition);
        if (current == null) {
            return;
        }
        View target = findViewByPosition(targetPosition);
        if (target == null) {
            return;
        }

        removeAndRecycleView(target, RecyclerViewHelper.getRecycler(mRecyclerView));
        RecyclerViewHelper.offsetPositionRecordsForRemove(mRecyclerView, targetPosition, 1, true);
        if (targetPosition == currentPosition) {
            if (targetPosition > 0) {
                mCurrentPosition = targetPosition - 1;
            } else if (targetPosition < getItemCount() - 1) {
                mCurrentPosition = targetPosition;
            } else {
                mCurrentPosition = 0;
            }
        } else if (targetPosition < currentPosition) {
            mCurrentPosition = getPosition(current);
            obtainPreChild(current, new ViewDecorator() {
                @Override
                public void decorate(View view) {
                    view.setTranslationX(-mChildRect.left);
                }
            });
        } else {
            obtainNextChild(current, new ViewDecorator() {
                @Override
                public void decorate(View view) {
                    view.setTranslationX(mChildRect.left);
                }
            });
        }
        idle();


    }


    private interface ViewDecorator {

        void decorate(View view);

    }

    private View obtainPreChild(View current) {
        return obtainPreChild(current, null);
    }

    private View obtainPreChild(View current, ViewDecorator decorator) {
        int pos = getPosition(current);
        if (pos > 0) {
            View child = findViewByPosition(--pos);
            if (child == null) {
                child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(pos);
                addView(child, 0);
                measureChild(child, 0, 0);
                layoutDecorated(child,
                        getDecoratedLeft(current) - mChildWidth - mGap,
                        (getHeight() - mChildHeight) / 2,
                        getDecoratedLeft(current) - mGap,
                        (getHeight() + mChildHeight) / 2);
                if (decorator != null) {
                    decorator.decorate(child);
                }
            }
            return child;
        }
        return null;
    }

    private View obtainNextChild(View current) {
        return obtainNextChild(current, null);
    }

    private View obtainNextChild(View current, ViewDecorator decorator) {
        int pos = getPosition(current);
        if (pos < getItemCount() - 1) {
            View child = findViewByPosition(++pos);
            if (child == null) {
                child = RecyclerViewHelper.getRecycler(mRecyclerView).getViewForPosition(pos);
                addView(child);
                measureChild(child, 0, 0);
                layoutDecorated(child,
                        getDecoratedRight(current) + mGap,
                        (getHeight() - mChildHeight) / 2,
                        getDecoratedRight(current) + mGap + mChildWidth,
                        (getHeight() + mChildHeight) / 2);
                if (decorator != null) {
                    decorator.decorate(child);
                }
            }
            return child;
        }
        return null;
    }


    private static class AnimationManager implements ValueAnimator.AnimatorUpdateListener {

        private final List<ViewTransformer> mTransformers = new ArrayList<>();

        private ValueAnimator mAnimator;

        private void reset() {
            cancelAnimator();
            mTransformers.clear();
        }

        private void addViewTransformer(ViewTransformer transformer) {
            mTransformers.add(transformer);
        }

        private void cancelAnimator() {
            if (mAnimator != null) {
                mAnimator.removeUpdateListener(this);
                mAnimator.cancel();
                mAnimator = null;
            }
        }

        private void start() {
            start(null);
        }

        private void start(AnimatorListenerAdapter listener) {
            cancelAnimator();
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
            animator.setDuration(500);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.addUpdateListener(this);
            if (listener != null) {
                animator.addListener(listener);
            }
//            animator.addListener(new Animator.AnimatorListener() {
//                @Override
//                public void onAnimationStart(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    TODO 动画结束时直接offsetChildrenHorizontal
//                    int centerX = (left + right) / 2;
//                    int targetCenterX = endRect.centerX();
//                    offsetChildrenHorizontal(targetCenterX - centerX);
//                }
//
//                @Override
//                public void onAnimationCancel(Animator animation) {
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animator animation) {
//
//                }
//            });
            mAnimator = animator;
            animator.start();
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float p = (float) animation.getAnimatedValue();
            for (ViewTransformer transformer : mTransformers) {
                transformer.update(p);
            }
        }
    }

    private static class ViewTransformer {

        private final View mView;

        private float mOldTranslationX;
        private float mOldTranslationY;

        private float mOldScaleX;
        private float mOldScaleY;

        private float mTargetTranslationX;
        private float mTargetTranslationY;

        private float mTargetScaleX;
        private float mTargetScaleY;

        private int mOldCenterX;
        private int mTargetCenterX;
        private int mLastCenterX;

        private static ViewTransformer with(View view) {
            return new ViewTransformer(view);
        }

        private ViewTransformer(View view) {
            this(view, 0, 0, 1f, 1f);
        }

        private ViewTransformer(View view, float targetTranslationX, float targetTranslationY,
                                float targetScaleX, float targetScaleY) {
            this.mView = view;
            if (view == null) {
                return;
            }

            mOldCenterX = (view.getLeft() + view.getRight()) / 2;
            mTargetCenterX = mLastCenterX = mOldCenterX;

            mOldTranslationX = view.getTranslationX();
            mOldTranslationY = view.getTranslationY();
            mOldScaleX = view.getScaleX();
            mOldScaleY = view.getScaleY();

            mTargetTranslationX = targetTranslationX;
            mTargetTranslationY = targetTranslationY;
            mTargetScaleX = targetScaleX;
            mTargetScaleY = targetScaleY;
        }

        public ViewTransformer setTargetCenterX(int targetCenterX) {
            this.mTargetCenterX = targetCenterX;
            return this;
        }

        public ViewTransformer setTargetScaleX(float targetScaleX) {
            this.mTargetScaleX = targetScaleX;
            return this;
        }

        public ViewTransformer setTargetScaleY(float targetScaleY) {
            this.mTargetScaleY = targetScaleY;
            return this;
        }

        public ViewTransformer setTargetScale(float targetScale) {
            this.mTargetScaleX = targetScale;
            this.mTargetScaleY = targetScale;
            return this;
        }

        public ViewTransformer setTargetTranslationX(float targetTranslationX) {
            this.mTargetTranslationX = targetTranslationX;
            return this;
        }

        public ViewTransformer setTargetTranslationY(float targetTranslationY) {
            this.mTargetTranslationY = targetTranslationY;
            return this;
        }

        public ViewTransformer setTargetTranslation(float targetTranslation) {
            this.mTargetTranslationX = targetTranslation;
            this.mTargetTranslationY = targetTranslation;
            return this;
        }

        public void update(float p) {
            if (mView == null) {
                return;
            }
            if (mTargetCenterX != mOldCenterX) {
                int centerX = (int) (mOldCenterX + (mTargetCenterX - mOldCenterX) * p);
                mView.offsetLeftAndRight(centerX - mLastCenterX);
                mLastCenterX = centerX;
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
