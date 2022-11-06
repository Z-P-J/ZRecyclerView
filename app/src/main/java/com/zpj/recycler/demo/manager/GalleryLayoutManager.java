package com.zpj.recycler.demo.manager;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * 为了点击item使相应的item居中显示，且实现RecyclerView的平缓滑动，重写LinearLayoutManager<P/>
 *
 * @author mailanglideguozhe 20210520
 */
public class GalleryLayoutManager extends LinearLayoutManager {

    private final RecyclerView mRecyclerView;



    public GalleryLayoutManager(RecyclerView recyclerView) {
        super(recyclerView.getContext(), HORIZONTAL, false);
        mRecyclerView = recyclerView;
        mRecyclerView.setLayoutManager(this);
    }

    private ValueAnimator mAnimator;
    private boolean expand;

    public boolean isExpand() {
        return expand;
    }

    public void idle(int position) {

        final View current = findViewByPosition(position);
        if (current == null) {
            return;
        }
        expand = false;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            int pos = getPosition(child);
            if (pos == position - 1) {
                child.setTranslationX(-getWidth());
            } else if (pos == position + 1) {
                child.setTranslationX(getWidth());
            } else {
                child.setTranslationX(0);
            }
        }


        final View pre = findViewByPosition(position - 1);
        final View next = findViewByPosition(position + 1);

        final int left = getDecoratedLeft(current);
        final int right = getDecoratedRight(current);
        final int top = getDecoratedTop(current);
        final int bottom = getDecoratedBottom(current);


        int childWidth = getWidth() / 3 * 2;
        int childHeight = getHeight() / 3 * 2;


        final Rect endRect = new Rect((getWidth() - childWidth) / 2, 0, getWidth() - (getWidth() - childWidth) / 2, childHeight);

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                int newL = (int) (left + (endRect.left - left) * p);
                int newTop = (int) (top + (endRect.top - top) * p);
                int newRight = (int) (right + (endRect.right - right) * p);
                int newBottom = (int) (bottom + (endRect.bottom - bottom) * p);


                if (pre != null) {
                    pre.setTranslationX((p - 1f) * getWidth());
                }

                if (next != null) {
                    next.setTranslationX((1f - p) * getWidth());
                }

                layoutDecorated(current, newL, newTop, newRight, newBottom);

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

    public void expand(int pos) {


        final View current = findViewByPosition(pos);
        if (current == null) {
            return;
        }
        mRecyclerView.setLayoutFrozen(true);
        expand = true;
        final View pre = findViewByPosition(pos - 1);
        final View next = findViewByPosition(pos + 1);

        final int left = getDecoratedLeft(current);
        final int right = getDecoratedRight(current);
        final int top = getDecoratedTop(current);
        final int bottom = getDecoratedBottom(current);

        final Rect endRect = new Rect(0, 0, getWidth(), getHeight());

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);

        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float p = (float) animation.getAnimatedValue();

                int newL = (int) (left + (endRect.left - left) * p);
                int newTop = (int) (top + (endRect.top - top) * p);
                int newRight = (int) (right + (endRect.right - right) * p);
                int newBottom = (int) (bottom + (endRect.bottom - bottom) * p);


                if (pre != null) {
                    pre.setTranslationX(-p * getWidth());
                }

                if (next != null) {
                    next.setTranslationX(p * getWidth());
                }

                layoutDecorated(current, newL, newTop, newRight, newBottom);

            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

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

    @Override
    public boolean canScrollHorizontally() {
        if (expand) {
            return false;
        }
        return super.canScrollHorizontally();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (expand) {
            return 0;
        }
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    /**
     * 重写smoothScrollToPosition方法，实现平滑滑动及停留在中间位置
     *
     * @param recyclerView
     * @param state
     * @param position     目标位置
     */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new RecyclerviewSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    /**
     * 重写LinearSmoothScroller自定义 中间对齐需要的校正位置
     */
    private static class RecyclerviewSmoothScroller extends LinearSmoothScroller {
        public RecyclerviewSmoothScroller(Context context) {
            super(context);
        }

        /**
         * 为了确保子视图在显示的位置中间位置，需要设置应当校正子视图的位置。<P/>
         * 子视图校正需要移动的距离为Recycler布局中间位置与子视图中间位置的距离<P/>
         *
         * @param viewStart 子视图的左侧位置
         * @param viewEnd   子视图的右侧位置
         * @param boxStart  RecyclerView视图的左侧位置
         * @param boxEnd    RecyclerView视图的左侧位置
         * @return 返回子视图校正需要移动的距离
         */
        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        /**
         * 计算滑动的速度，返回1px滑动所需的时间，举例 如返回0.8f，即滑动1000个像素点距离需要0.8s<P/>
         *
         * @param displayMetrics
         * @return 返回1px滑动所需的时间，单位ms
         */
        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return super.calculateSpeedPerPixel(displayMetrics);
        }
    }
}

