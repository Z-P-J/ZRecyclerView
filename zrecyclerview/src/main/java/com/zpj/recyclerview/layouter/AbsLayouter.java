package com.zpj.recyclerview.layouter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.manager.MultiLayoutManager;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public abstract class AbsLayouter implements Layouter {

    private static final String TAG = "AbsLayouter";

    private MultiLayoutManager mManager;
    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    protected int mChildCount;

    protected int mPositionOffset;
    protected int mChildOffset;

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getHeight()) {
            mBottom = mTop;
            return;
        }
        fillVerticalBottom(recycler, multiData, currentPosition, getHeight() - mTop, getTop());
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int result = fillVerticalBottom(recycler, multiData, mPositionOffset, dy, getTop());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                int anchorBottom = getDecoratedBottom(anchorView);
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + getHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = getPosition(anchorView);
                    if (anchorPosition == mPositionOffset + multiData.getCount() - 1) {
                        return Math.max(0, anchorBottom - getHeight());
                    }
                    int availableSpace = dy + getHeight() - anchorBottom;
                    int result = fillVerticalBottom(recycler, multiData, anchorPosition + 1, availableSpace, anchorBottom);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int result = fillVerticalTop(recycler, multiData, mPositionOffset + multiData.getCount() - 1,
                        -dy, getBottom());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorTop = getDecoratedTop(anchorView);
                int anchorPosition = getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == mPositionOffset) {
                        return -anchorTop;
                    }
                    int availableSpace = -dy + anchorTop;
                    int result = fillVerticalTop(recycler, multiData, anchorPosition - 1, availableSpace, anchorTop);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
//        return 0;
    }

    @Override
    public void setPositionOffset(int offset) {
        this.mPositionOffset = offset;
    }

    @Override
    public int getPositionOffset() {
        return mPositionOffset;
    }

    @Override
    public void setChildOffset(int offset) {
        this.mChildOffset = offset;
    }

    @Override
    public int getChildOffset() {
        return this.mChildOffset;
    }

    @Override
    public void setLayoutManager(MultiLayoutManager manager) {
        this.mManager = manager;
    }

    @Override
    public MultiLayoutManager getLayoutManager() {
        return mManager;
    }

    @Override
    public void setLeft(int left) {
        this.mLeft = left;
    }

    @Override
    public void setTop(int top) {
        this.mTop = top;
    }

    @Override
    public void setRight(int right) {
        this.mRight = right;
    }

    @Override
    public void setBottom(int bottom) {
        this.mBottom = bottom;
    }


    @Override
    public int getLeft() {
        return mLeft;
    }

    @Override
    public int getTop() {
        return mTop;
    }

    @Override
    public int getRight() {
        return mRight;
    }

    @Override
    public int getBottom() {
        return mBottom;
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        this.mLeft += offset;
        this.mRight += offset;
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        this.mTop += offset;
        this.mBottom += offset;
    }

    @Override
    public int getPosition(@NonNull View child) {
        return getLayoutManager().getPosition(child);
    }

    @Override
    public int getDecoratedLeft(@NonNull View child) {
        return getLayoutManager().getDecoratedLeft(child);
    }

    @Override
    public int getDecoratedTop(@NonNull View child) {
        return getLayoutManager().getDecoratedTop(child);
    }

    @Override
    public int getDecoratedRight(@NonNull View child) {
        return getLayoutManager().getDecoratedRight(child);
    }

    @Override
    public int getDecoratedBottom(@NonNull View child) {
        return getLayoutManager().getDecoratedBottom(child);
    }

    @Override
    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        if (mManager != null) {
            mManager.layoutDecorated(child, left, top, right, bottom);
        }
    }

    @Override
    public void saveState(int firstPosition, int firstOffset) {

    }

    protected abstract int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop);

    protected abstract int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom);

    public View getViewForPosition(int position, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = null;
        if (multiData.isStickyPosition(position - mPositionOffset)) {
            view  = getLayoutManager().findViewByPosition(position);
        }
        if (view == null) {
            view = recycler.getViewForPosition(position);
        } else {
            getLayoutManager().detachAndScrapView(view, recycler);
        }
        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
        params.setMultiData(multiData);
        return view;
    }

    public View addViewAndMeasure(int position, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = getViewForPosition(position, recycler, multiData);
        addView(view);
        measureChild(view, 0, 0);
        return view;
    }

    public View addViewAndMeasure(int position, int index, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        View view = getViewForPosition(position, recycler, multiData);
        addView(view, index);
        measureChild(view, 0, 0);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        getLayoutManager().measureChild(child, widthUsed, heightUsed);
    }

    public void addView(View child) {
        getLayoutManager().addView(child);
    }

    public void addView(View child, int index) {
        getLayoutManager().addView(child, index);
    }

    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getLayoutManager().getDecoratedMeasuredWidth(child);
    }

    public int getDecoratedMeasuredHeight(@NonNull View child) {
        return getLayoutManager().getDecoratedMeasuredHeight(child);
    }

    @Px
    public int getWidth() {
        return getLayoutManager().getWidth();
    }

    @Px
    public int getHeight() {
        return getLayoutManager().getHeight();
    }

    @Px
    public int getPaddingLeft() {
        return getLayoutManager().getPaddingLeft();
    }

    @Px
    public int getPaddingTop() {
        return getLayoutManager().getPaddingTop();
    }

    @Px
    public int getPaddingRight() {
        return getLayoutManager().getPaddingRight();
    }

    @Px
    public int getPaddingBottom() {
        return getLayoutManager().getPaddingBottom();
    }

    @Px
    public int getPaddingStart() {
        return getLayoutManager().getPaddingStart();
    }

    @Px
    public int getPaddingEnd() {
        return getLayoutManager().getPaddingEnd();
    }

    public int getChildCount() {
        return getLayoutManager().getChildCount();
    }

    @Nullable
    public View getChildAt(int index) {
        return getLayoutManager().getChildAt(index);
    }

    public MultiData<?> getMultiData(View child) {
        return getLayoutManager().getMultiData(child);
    }

    public Layouter getLayouter(View child) {
        return getLayoutManager().getLayouter(child);
    }

    public int indexOfChild(View child) {
        return getLayoutManager().indexOfChild(child);
    }

    public MultiRecycler getRecycler() {
        return getLayoutManager().getRecycler();
    }


    protected HorizontalFlinger mFlinger;

    @Override
    public void onTouchDown(MultiData<?> multiData, float downX, float downY) {
        if (canScrollHorizontally()) {
//            getRecycler().getRecyclerView().stopScroll();
            if (mFlinger != null) {
                mFlinger.stop();
            } else {
                mFlinger = new HorizontalFlinger(getRecycler().getContext(), multiData);
            }
        }
    }

    @Override
    public void onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        //  && velocityY < ViewConfiguration.get(getRecycler().getContext()).getScaledMinimumFlingVelocity()
        if (canScrollHorizontally()) {
//            getRecycler().getRecyclerView().stopScroll();
            if (isOverScrolling) {
                onStopOverScroll(multiData);
            } else if (mFlinger != null) {
                mFlinger.fling(velocityX, velocityY);
            }
        }
    }





    private static final int OVER_SCROLL_DOWN = 1;
    private static final int OVER_SCROLL_UP = 2;
    private static final int OVER_SCROLL_LEFT = 3;
    private static final int OVER_SCROLL_RIGHT = 4;

    private boolean isOverScrolling;
    private int overScrollDirection;
    private int overScrollDistance;

    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, MultiData<?> scrollMultiData) {
        if (scrollMultiData == null) {
            return 0;
        }

        if (isOverScrolling) {
            overScrollDistance += dx;
            if (overScrollDirection == OVER_SCROLL_LEFT) {
                if (overScrollDistance > 0) {
                    isOverScrolling = false;
                }
            } else if (overScrollDirection == OVER_SCROLL_RIGHT) {
                if (overScrollDistance < 0) {
                    isOverScrolling = false;
                }
            }

            if (isOverScrolling) {

                float maxWidth = getWidth() / 1.5f;
                float overScrollRadio = Math.min(Math.abs(overScrollDistance), maxWidth) / maxWidth;
                int overScroll = (int) ((0.68f - overScrollRadio / 2f) * dx);

                Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScrollRadio=" + overScrollRadio + " overScroll=" + overScroll);


                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    final MultiData<?> multiData = getMultiData(view);
                    if (multiData == scrollMultiData) {
                        for (int j = i; j < getChildCount(); j++) {
                            View child = getChildAt(j);
                            if (getMultiData(child) != scrollMultiData) {
                                break;
                            }
                            Log.d(TAG, "scrollHorizontallyBy i=" + i);
                            child.offsetLeftAndRight(-overScroll);
                        }
                        break;
                    }
                }

                if (overScrollRadio >= 1f) {
                    Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScrollRadio=" + overScrollRadio + " overScroll=" + overScroll);
                    return 0;
                }

                if (Math.abs(overScrollDistance) > maxWidth) {
                    return 0;
                }

                return dx;

            }
        }

        int consumed = 0;
        View firstChild = getChildAt(0);
        View lastChild = getChildAt(getChildCount() - 1);
        if (firstChild == null || lastChild == null) {
            return 0;
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx);
        int index = 0;
        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (getMultiData(view) == scrollMultiData) {
                    index = i;
                    consumed += fillHorizontal(view, dx, recycler, scrollMultiData);
                    break;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getMultiData(view) == scrollMultiData) {
                    index = i;
                    consumed -= fillHorizontal(view, dx, recycler, scrollMultiData);
                    break;
                }
            }
        }

        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                MultiData<?> multiData = getMultiData(view);
                if (multiData != scrollMultiData) {
                    continue;
                }

                if (view.getRight() - consumed + getLayoutManager().getRightDecorationWidth(view) < 0
                        || view.getLeft() - consumed - getLayoutManager().getLeftDecorationWidth(view) > getWidth()) {
                    getLayoutManager().recycleViews.add(view);
                } else {
                    view.offsetLeftAndRight(-consumed);
                    index = i;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = index; i < getChildCount(); i++) {
                View view = getChildAt(i);
                MultiData<?> multiData = getMultiData(view);
                if (multiData != scrollMultiData) {
                    break;
                }

                if (view.getRight() - consumed + getLayoutManager().getRightDecorationWidth(view) < 0
                        || view.getLeft() - consumed - getLayoutManager().getLeftDecorationWidth(view) > getWidth()) {
                    getLayoutManager().recycleViews.add(view);
                } else {
                    view.offsetLeftAndRight(-consumed);
                }
            }
        }

        getLayoutManager().recycleViews(recycler);


        if (dx != consumed) {
            isOverScrolling = true;
            overScrollDirection = dx < 0 ? OVER_SCROLL_LEFT : OVER_SCROLL_RIGHT;
            overScrollDistance = dx - consumed;

            int overScroll = (int) (overScrollDistance * 0.1f);

            Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScroll=" + overScroll);

            for (int i = index; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getMultiData(view) != scrollMultiData) {
                    break;
                }
                view.offsetLeftAndRight(-overScroll);
            }
            consumed = dx;
        }

        View child = getChildAt(index);
        if (child != null) {
            int firstPosition = getPosition(child);
            int firstOffset = getDecoratedLeft(child);
            saveState(firstPosition, firstOffset);
            Log.e(TAG, "scrollHorizontallyBy firstPosition=" + firstPosition + " firstOffset=" + firstOffset);
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " consumed=" + consumed + " isOverScrolling=" + isOverScrolling);

        return consumed;
    }






    protected void onStopOverScroll(final MultiData<?> scrollMultiData) {
        if (scrollMultiData == null) {
            return;
        }
        if (isOverScrolling) {
            isOverScrolling = false;
            mFlinger.stop();
            if (overScrollDirection <= OVER_SCROLL_UP) {
                if (overScrollDirection == OVER_SCROLL_DOWN) {
                    final View firstChild = getLayoutManager().getFirstChild();
                    if (firstChild != null) {
                        final int firstTop = getDecoratedTop(firstChild);
                        if (firstTop > 0) {
                            // TODO
                        }

                    }
                } else if (overScrollDirection == OVER_SCROLL_UP) {
                    View lastChild = getLayoutManager().getLastChild();
                    if (lastChild != null) {
                        final int bottom = getDecoratedBottom(lastChild);
                        if (bottom < getHeight()) {
                            // TODO
                        }
                    }
                }
            } else {
                if (overScrollDirection == OVER_SCROLL_LEFT) {
                    for (int i = 0; i < getChildCount(); i++) {
                        View view = getChildAt(i);
                        if (getMultiData(view) == scrollMultiData) {
                            final int firstLeft = getDecoratedLeft(view);
                            Log.d(TAG, "onStopOverScroll firstLeft=" + firstLeft);
                            if (firstLeft > 0 && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset()) {
                                mFlinger.startScroll(-firstLeft, 0, 500);
                            }
                            break;
                        }
                    }
                } else if (overScrollDirection == OVER_SCROLL_RIGHT) {
                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        View view = getChildAt(i);
                        if (getMultiData(view) == scrollMultiData) {
                            final int right = getDecoratedRight(view);
                            Log.d(TAG, "onStopOverScroll right=" + right);
                            if (right < getWidth() && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset() + scrollMultiData.getCount() - 1) {
                                mFlinger.startScroll(getWidth() - right, 0, 500);
                            }
                            break;
                        }
                    }
                }
            }


        }
    }




    private static final Interpolator sScrollInterpolator = new FastOutSlowInInterpolator();

    protected class HorizontalFlinger implements Runnable, Interpolator {



        protected int mLastFlingX;
        protected final OverScroller mScroller;
        protected final MultiData<?> scrollMultiData;

        protected Interpolator mInterpolator;

        public HorizontalFlinger(Context context, MultiData<?> scrollMultiData) {
            this.scrollMultiData = scrollMultiData;
            this.mScroller = new OverScroller(context, this);
        }

        @Override
        public void run() {
            if (scrollMultiData == null) {
                stop();
                return;
            }
            if (mScroller.computeScrollOffset()) {
                int x = mScroller.getCurrX();
                int dx = mLastFlingX - x;
                if (dx == 0 && !mScroller.isFinished()) {
                    postOnAnimation();
                    return;
                }

                int consumed = scrollHorizontallyBy(dx, RecyclerViewHelper.getRecycler(getLayoutManager().getRecycler()), scrollMultiData);

                Log.d(TAG, "HorizontalFlinger run dx=" + dx + " consumed=" + consumed);

                if (consumed != dx) {
                    stop();
                    onStopOverScroll(scrollMultiData);
                    return;
                }
                mLastFlingX = x;
                postOnAnimation();

            }
        }

        protected void postOnAnimation() {
            getLayoutManager().getRecycler().getRecyclerView().removeCallbacks(this);
            ViewCompat.postOnAnimation(getLayoutManager().getRecycler().getRecyclerView(), this);
        }



        public void fling(float velocityX, float velocityY) {
            if (scrollMultiData == null) {
                return;
            }
            Log.d(TAG, "=======================================================HorizontalFlinger fling velocityX=" + velocityX + " velocityY=" + velocityY);
            stop();
            mInterpolator = null;
            this.mScroller.fling(0, 0, (int) velocityX, (int) velocityY,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            this.postOnAnimation();
        }

        public void startScroll(int dx, int dy, int duration) {
            if (scrollMultiData == null) {
                return;
            }
            Log.d(TAG, "-------------------------------------------------------HorizontalFlinger startScroll dx=" + dx + " dy=" + dy);
            stop();
            mInterpolator = null;
            mInterpolator = sScrollInterpolator;
            this.mScroller.startScroll(0, 0, dx, dy, duration);
            this.postOnAnimation();
        }

        public boolean isStop() {
            return this.mScroller.isFinished();
        }

        public void stop() {
            getLayoutManager().getRecycler().getRecyclerView().removeCallbacks(this);
            this.mScroller.forceFinished(true);
            this.mLastFlingX = 0;
        }

        public void setInterpolator(Interpolator mInterpolator) {
            this.mInterpolator = mInterpolator;
        }

        @Override
        public float getInterpolation(float input) {
            if (mInterpolator == null) {
                return RecyclerViewHelper.getInterpolator().getInterpolation(input);
            }
            return mInterpolator.getInterpolation(input);
        }
    }

}
