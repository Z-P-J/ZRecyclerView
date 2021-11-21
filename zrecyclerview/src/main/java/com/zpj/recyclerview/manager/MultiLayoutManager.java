package com.zpj.recyclerview.manager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MultiLayoutManager extends RecyclerView.LayoutManager
        implements ItemTouchHelper.ViewDropHandler,
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "MultiLayoutManager";

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    private static final int OVER_SCROLL_DOWN = 1;
    private static final int OVER_SCROLL_UP = 2;

    private final Set<View> recycleViews = new LinkedHashSet<>();
    private final Deque<StickyInfo> stickyInfoStack = new ArrayDeque<>();

    private MultiRecycler mRecycler;
    private List<MultiData<?>> multiDataList;

    private int mScrollDirection = DIRECTION_NONE;

    private float mDownX = -1;
    private float mDownY = -1;

    private int mTopMultiDataIndex;
    private int mTopPosition;
    private int mTopOffset;

    private StickyInfo stickyInfo;
    private int currentStickyOffset = 0;

    private boolean isOverScrolling;
    private int overScrollDirection;
    private int overScrollDistance;

    private ValueAnimator mOverScrollAnimator;

    private static class StickyInfo {
        int position;
        MultiData<?> multiData;

        public StickyInfo(int position, MultiData<?> multiData) {
            this.position = position;
            this.multiData = multiData;
        }

        @Override
        public String toString() {
            return "StickyInfo{" +
                    "position=" + position +
                    ", multiData=" + multiData +
                    '}';
        }
    }

    public void attachRecycler(MultiRecycler recycler) {
        this.mRecycler = recycler;
        this.multiDataList = recycler.getDataSet();
        if (recycler.getRefresher() != null) {
            this.multiDataList.add(0, new RefresherMultiData(recycler.getRefresher()));
        }
        recycler.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private VelocityTracker mTracker;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                int action = event.getAction();
                if (mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                }
                mTracker.addMovement(event);
                if (MotionEvent.ACTION_DOWN == action) {
                    mScrollDirection = DIRECTION_NONE;
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    if (mOverScrollAnimator != null) {
                        mOverScrollAnimator.pause();
                        mOverScrollAnimator = null;
                    }
                } else if (MotionEvent.ACTION_MOVE == action) {
                    if (mScrollDirection == 0) {
                        float deltaX = event.getRawX() - mDownX;
                        float deltaY = event.getRawY() - mDownY;
                        float radio = Math.abs(deltaX / deltaY);
                        if (radio == 1f) {
                            return false;
                        }
                        if (radio > 1f) {
                            mScrollDirection = DIRECTION_HORIZONTAL;
                        } else {
                            mScrollDirection = DIRECTION_VERTICAL;
                        }
                    }
                } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
                    mTracker.computeCurrentVelocity(1000);
                    if (mTracker.getYVelocity() < ViewConfiguration.get(mRecycler.getRecyclerView().getContext()).getScaledMinimumFlingVelocity()) {
                        onStopOverScroll();
                    }
                    mTracker.recycle();
                    mTracker = null;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    onStopOverScroll();
                }
            }
        });

    }

    public boolean isOverScrolling() {
        return isOverScrolling;
    }

    private void onStopOverScroll() {
        if (isOverScrolling) {
            if (mOverScrollAnimator != null) {
                mOverScrollAnimator.pause();
                mOverScrollAnimator = null;
            }

            final View firstChild = getChildAt(0);
            if (firstChild != null) {
                final int firstTop = getDecoratedTop(firstChild);
                if (firstTop > 0) {
                    mOverScrollAnimator = ValueAnimator.ofFloat(1f, 0f);
                    mOverScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        int lastTop = firstTop;
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float percent = (float) animation.getAnimatedValue();
                            int top = (int) (percent * firstTop);
                            offsetChildrenVertical(top - lastTop);
                            lastTop = top;
                        }
                    });
                }

            }

            if (mOverScrollAnimator == null) {
                View lastChild = getChildAt(stickyInfo == null ? getChildCount() - 1 : getChildCount() - 2);
                if (lastChild != null) {
                    final int bottom = getDecoratedBottom(lastChild);
                    if (bottom < getHeight()) {
                        mOverScrollAnimator = ValueAnimator.ofFloat(0f, 1f);
                        mOverScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            int lastBottom = bottom;
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float percent = (float) animation.getAnimatedValue();
                                int b = (int) (percent * (getHeight() - bottom) + bottom);
                                offsetChildrenVertical(b - lastBottom);
                                lastBottom = b;
                            }
                        });
                    }
                }
            }




            if (mOverScrollAnimator != null) {
                mOverScrollAnimator.setInterpolator(new FastOutSlowInInterpolator());
                mOverScrollAnimator.setDuration(500);
                mOverScrollAnimator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isOverScrolling = false;
                    }
                });
                mOverScrollAnimator.start();
                return;
            }

            isOverScrolling = false;
        }
    }

    private void onOverScroll() {

    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof MultiLayoutParams;
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new MultiLayoutParams(lp);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new MultiLayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new MultiLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.d(TAG, "onLayoutChildren mTopMultiDataIndex=" + mTopMultiDataIndex
                + " mTopPosition=" + mTopPosition + " mTopOffset=" + mTopOffset + " isPreLayout=" + state.isPreLayout());

        if (multiDataList == null) {
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        StickyInfo temp = stickyInfo;
        stickyInfo = null;

        int positionOffset = 0;
        int topPosition = mTopPosition;

        Layouter last = null;
        for (int i = 0; i < multiDataList.size(); i++) {
            MultiData<?> multiData = multiDataList.get(i);
            Layouter layouter = multiData.getLayouter();
            layouter.setPositionOffset(positionOffset);
            layouter.setLayoutManager(this);
            if (i >= mTopMultiDataIndex) {
                if (last != null) {
                    Log.d(TAG, "onLayoutChildren bottom=" + last.getBottom() + " i=" + i);
                    layouter.setTop(last.getBottom());
                    layouter.layoutChildren(multiData, recycler, positionOffset);
                } else {
                    layouter.setTop(mTopOffset);
                    layouter.layoutChildren(multiData, recycler, mTopPosition + positionOffset);
                    topPosition = mTopPosition + positionOffset;
                }
                last = layouter;
            }
            positionOffset += multiData.getCount();
        }


        for (int i = 0; i < getChildCount(); i++) {
            Log.d(TAG, "onLayoutChildren i=" + i + " psoition=" + getPosition(getChildAt(i)));
        }

        saveState();

        stickyInfo = temp;

        Log.d(TAG, "stickyInfo111=" + stickyInfo + " topPosition=" + topPosition);
        if (stickyInfo == null || topPosition > stickyInfo.position) {
            initStickyInfoStack(topPosition);
            stickyInfo = stickyInfoStack.poll();
        } else if (topPosition < stickyInfo.position) {
            if (stickyInfoStack.isEmpty()) {
                initStickyInfoStack(topPosition);
            }
            while (stickyInfo != null && topPosition < stickyInfo.position) {
                stickyInfo = stickyInfoStack.poll();
            }
        }

        Log.d(TAG, "stickyInfo111222=" + stickyInfo + " stickyInfoStack.size=" + stickyInfoStack.size());

        if (stickyInfo != null) {
            View child = findViewByPosition(stickyInfo.position);
            if (stickyInfoStack.isEmpty() && child != null && getDecoratedTop(child) == 0) {
                stickyInfo = null;
                return;
            }

            if (child == null) {
                child = recycler.getViewForPosition(stickyInfo.position);
            } else {
                detachAndScrapView(child, recycler);
            }
            MultiLayoutParams params = (MultiLayoutParams) child.getLayoutParams();
            params.setMultiData(stickyInfo.multiData);
            super.addView(child, getChildCount());
            measureChild(child, 0, 0);
            layoutDecorated(child, 0, currentStickyOffset, getWidth(), currentStickyOffset + getDecoratedMeasuredHeight(child));

            Layouter layouter = stickyInfo.multiData.getLayouter();
            stickyInfo.multiData.onItemSticky(new EasyViewHolder(child), stickyInfo.position - layouter.getPositionOffset(), true);
        }

    }

    private void initStickyInfoStack(int position) {
        stickyInfoStack.clear();
        int offset = 0;
        for (int i = 0; i < multiDataList.size(); i++) {
            MultiData<?> multiData = multiDataList.get(i);
            int count = multiData.getCount();
            for (int pos = offset; pos < offset + count; pos++) {
                if (multiData.isStickyPosition(pos - offset)) {
                    Log.d(TAG, "stickyInfo111 pos=" + pos);
                    stickyInfoStack.push(new StickyInfo(pos, multiData));
                }
                if (pos >= position) {
                    return;
                }
            }
            offset += count;
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return mScrollDirection == DIRECTION_HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (multiDataList == null) {
            return 0;
        }

        int consumed = 0;
        View firstChild = getChildAt(0);
        View lastChild = getChildAt(getChildCount() - 1);
        if (firstChild == null || lastChild == null) {
            return 0;
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx);
        MultiData<?> tempMultiData = null;
        MultiData<?> scrollMultiData = null;
        int[] location = new int[2];
        int index = 0;
        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                MultiData<?> multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                if (multiData == null || multiData == tempMultiData) {
                    continue;
                }
                tempMultiData = multiData;
                Layouter layouter = multiData.getLayouter();
                if (layouter.canScrollHorizontally()) {

                    view.getLocationInWindow(location);
                    if (mDownY < location[1] - getTopDecorationHeight(view)
                            || mDownY > location[1] - getTopDecorationHeight(view) + getDecoratedMeasuredHeight(view)) {
                        continue;
                    }

                    scrollMultiData = multiData;
                    index = i;

                    view.setTag(i);
                    consumed += layouter.fillHorizontal(view, dx, recycler, multiData);
                    break;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                MultiData<?> multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                if (multiData == null || multiData == tempMultiData) {
                    continue;
                }
                tempMultiData = multiData;
                Layouter layouter = multiData.getLayouter();
                if (layouter.canScrollHorizontally()) {

                    view.getLocationInWindow(location);
                    if (mDownY < location[1] - getTopDecorationHeight(view)
                            || mDownY > location[1] - getTopDecorationHeight(view) + getDecoratedMeasuredHeight(view)) {
                        continue;
                    }

                    scrollMultiData = multiData;
                    index = i;
                    view.setTag(i);
                    consumed -= layouter.fillHorizontal(view, dx, recycler, multiData);
                    break;
                }
            }
        }

        if (scrollMultiData == null) {
            return 0;
        }

        Layouter layouter = scrollMultiData.getLayouter();
//        layouter.offsetLeftAndRight(-consumed);
        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                MultiData<?> multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                if (multiData != scrollMultiData) {
                    continue;
                }

                if (view.getRight() - consumed + getRightDecorationWidth(view) < 0
                        || view.getLeft() - consumed - getLeftDecorationWidth(view) > getWidth()) {
                    recycleViews.add(view);
                } else {
                    view.offsetLeftAndRight(-consumed);
                    index = i;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = index; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view == null) {
                    continue;
                }
                MultiData<?> multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                if (multiData != scrollMultiData) {
                    break;
                }

                if (view.getRight() - consumed + getRightDecorationWidth(view) < 0
                        || view.getLeft() - consumed - getLeftDecorationWidth(view) > getWidth()) {
                    recycleViews.add(view);
                } else {
                    view.offsetLeftAndRight(-consumed);
                }
            }
        }

        recycleViews(recycler);


        View child = getChildAt(index);
        if (child != null) {
            int firstPosition = getPosition(child);
            int firstOffset = layouter.getDecoratedLeft(child);
            layouter.saveState(firstPosition, firstOffset);
        }

        return consumed;
    }

    @Override
    public void offsetChildrenVertical(int dy) {

        if (isOverScrolling && stickyInfo != null && overScrollDirection == OVER_SCROLL_UP) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null && getPosition(child) != stickyInfo.position) {
                    child.offsetTopAndBottom(dy);
                }
            }
        } else {
            super.offsetChildrenVertical(dy);
        }
    }

    @Override
    public boolean canScrollVertically() {
        return mScrollDirection == DIRECTION_VERTICAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (multiDataList == null) {
            return 0;
        }

        if (isOverScrolling) {
            overScrollDistance += dy;

            Log.d(TAG, "scrollVerticallyBy isOverScrolling overScrollDistance=" + overScrollDistance + " dy=" + dy + " overScrollDirection=" + overScrollDirection);

            if (overScrollDirection == OVER_SCROLL_UP) {
                if (overScrollDistance < 0) {
                    isOverScrolling = false;
                }
            } else if (overScrollDirection == OVER_SCROLL_DOWN) {
                if (overScrollDistance > 0) {
                    isOverScrolling = false;
                }
            }

            if (isOverScrolling) {
                Log.d(TAG, "scrollVerticallyBy isOverScrolling");
                int maxHeight = getHeight() / 2;
                float overScrollRatio = (float) Math.min(Math.abs(overScrollDistance), maxHeight) / maxHeight;
                int overScroll = (int) ((0.68f - overScrollRatio / 2f) * dy);

                offsetChildrenVertical(-overScroll);

                if (overScrollRatio >= 1f || Math.abs(overScroll) < 20) {
                    return 0;
                }

                if (Math.abs(overScrollDistance) > maxHeight) {
                    return 0;
                }

                return dy;
            }

        }

        int consumed = 0;

        Log.d(TAG, "\n============================================scrollVerticallyBy dy=" + dy);

        if (dy < 0) {
            // 从上往下滑动
            int i = 0;
            MultiData<?> multiData = null;
            View view;
            do {
                view = getChildAt(i);
                if (view == null) {
                    return 0;
                }
                if (stickyInfo != null && stickyInfo.position == getPosition(view)) {
                    i++;
                    continue;
                }
                view.setTag(i);
                multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                i++;
            } while (multiData == null);
            Layouter layouter = multiData.getLayouter();
            Log.d(TAG, "scrollVerticallyBy firstChildPosition=" + getPosition(view));
            consumed -= layouter.fillVertical(view, dy - consumed, recycler, multiData);
            i = multiDataList.indexOf(multiData);
            while (consumed > dy && i > 0) {
                int top = layouter.getTop();
                multiData = multiDataList.get(--i);
                layouter = multiData.getLayouter();
                layouter.setBottom(top);
                consumed -= layouter.fillVertical(null, dy - consumed, recycler, multiData);
            }
        } else {
            // 从下往上滑动

            int i = getChildCount() - 1;
            MultiData<?> multiData = null;
            View view;
            do {
                view = getChildAt(i);
                if (view == null) {
                    return 0;
                }
                if (stickyInfo != null && stickyInfo.position == getPosition(view)) {
                    i--;
                    continue;
                }
                view.setTag(i);
                multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                i--;
            } while (multiData == null);
            Layouter layouter = multiData.getLayouter();
            Log.w(TAG, "scrollVerticallyBy layouter=" + layouter);
            consumed += layouter.fillVertical(view, dy - consumed, recycler, multiData);
            i = multiDataList.indexOf(multiData);
            Log.w(TAG, "scrollVerticallyBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(view));
            while (consumed < dy && i < multiDataList.size() - 1) {
                int bottom = layouter.getBottom();
                multiData = multiDataList.get(++i);
                layouter = multiData.getLayouter();
                Log.w(TAG, "scrollVerticallyBy layouter=" + layouter);
                layouter.setTop(bottom);
                consumed += layouter.fillVertical(null, dy - consumed, recycler, multiData);
            }
        }

        Log.d(TAG, "scrollVerticallyBy consumed=" + consumed);

        MultiData<?> last = null;
        handleSticky = true;
        List<Layouter> layouters = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            MultiData<?> data = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
            if (data == null) {
                Log.d(TAG, "scrollVerticallyBy i=" + i + " position=" + getPosition(view));
                continue;
            }
            Layouter layouter = data.getLayouter();
            if (data != last) {
                layouters.add(layouter);
                layouter.offsetTopAndBottom(-consumed);
                last = data;
            }
            if (layouter.canScrollVertically()) {
                if (handleSticky(recycler, data, view, i, consumed)) {
                    continue;
                }
                if (layouter.getDecoratedBottom(view) - consumed < 0
                        || layouter.getDecoratedTop(view) - consumed > getHeight()) {
                    recycleViews.add(view);
                } else {
                    view.offsetTopAndBottom(-consumed);
                }
            }
        }
        recycleViews(recycler);

        for (Layouter layouter : layouters) {
            if (layouter instanceof StaggeredGridLayouter) {
                ((StaggeredGridLayouter) layouter).saveState();
            }
        }

        if (dy != consumed) {
            int overScroll = (int) ((consumed - dy) * 0.1f);
            isOverScrolling = true;
            overScrollDirection = dy > 0 ? OVER_SCROLL_UP : OVER_SCROLL_DOWN;
            overScrollDistance = dy - consumed;
            offsetChildrenVertical(overScroll);
            consumed = dy;
        }

        if (stickyInfo != null) {
            View child = findViewByPosition(stickyInfo.position);
            if (child == null) {
                child = recycler.getViewForPosition(stickyInfo.position);
            } else {
                detachAndScrapView(child, recycler);
            }
            MultiLayoutParams params = (MultiLayoutParams) child.getLayoutParams();
            params.setMultiData(stickyInfo.multiData);
            super.addView(child, getChildCount());
            measureChild(child, 0, 0);
            Log.e(TAG, "scrollVerticallyBy currentStickyOffset=" + currentStickyOffset);
            layoutDecorated(child, 0, currentStickyOffset, getWidth(), currentStickyOffset + getDecoratedMeasuredHeight(child));

            Layouter layouter = stickyInfo.multiData.getLayouter();
            stickyInfo.multiData.onItemSticky(new EasyViewHolder(child), stickyInfo.position - layouter.getPositionOffset(), true);
        }

        Log.d(TAG, "scrollVerticallyBy getChildCount=" + getChildCount() + " \n");
        return consumed;
    }

    private boolean handleSticky;
    private boolean handleSticky(RecyclerView.Recycler recycler, MultiData<?> data, View view, int i, int consumed) {
        Layouter layouter = data.getLayouter();
        int position = getPosition(view);

        boolean isStickyPosition = data.isStickyPosition(position - layouter.getPositionOffset());
        if (!isStickyPosition) {
            return false;
        }

        Log.e(TAG, "scrollVerticallyBy");
        Log.e(TAG, "scrollVerticallyBy ================================start");
        Log.d(TAG, "scrollVerticallyBy i=" + i + " stickyPosition=" + position);
        int decoratedTop = layouter.getDecoratedTop(view);
        int decoratedBottom = layouter.getDecoratedBottom(view);
//                        Log.d(TAG, "scrollVerticallyBy decoratedTop=" + decoratedTop
//                                + " decoratedBottom=" + decoratedBottom + " height=" + getDecoratedMeasuredHeight(view)
//                                + " consumed=" + consumed + " dy=" + dy
//                                + " currentStickyPosition=" + currentStickyPosition + " position=" + position + " iii=" + i + " last=" + (getChildCount() - 1));

        Log.d(TAG, "scrollVerticallyBy isStickyChild i=" + i + " position=" + position + " stickyInfo=" + stickyInfo);

        if (stickyInfo != null && stickyInfo.position == position) {
            // 已是当前吸顶view

//                            Log.d(TAG, "scrollVerticallyBy decoratedTop=" + decoratedTop + " childPos=" + (getChildCount() - 1) + " i=" + i + " consumed=" + consumed);
            if (i != getChildCount() - 1 && decoratedTop - consumed >= 0) { //  && decoratedTop - consumed >= 0
//                            currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight(view) - consumed;
                if (stickyInfo != null) {
                    stickyInfo.multiData.onItemSticky(new EasyViewHolder(view), stickyInfo.position - stickyInfo.multiData.getLayouter().getPositionOffset(), false);
                }
                if (stickyInfoStack.isEmpty()) {
                    stickyInfo = null;
                    currentStickyOffset = 0;
                } else {
                    stickyInfo = stickyInfoStack.pop();
                }
                handleSticky = false;
                if (stickyInfo != null) {
                    View child = findViewByPosition(stickyInfo.position);
                    if (child == null) {
                        child = recycler.getViewForPosition(stickyInfo.position);
                        measureChild(child, 0, 0);
                    }
                    MultiLayoutParams params = (MultiLayoutParams) child.getLayoutParams();
                    params.setMultiData(stickyInfo.multiData);

                    currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight(child) - consumed;
                }
                Log.d(TAG, "scrollVerticallyBy 吸顶==>下一个吸顶 stickyInfo=" + stickyInfo + " currentStickyOffset=" + currentStickyOffset);
            } else {
                return true;
            }
        } else if (handleSticky) {
            handleSticky = false;
            // 不是当前吸顶view
            if (stickyInfo == null) {
                // 无吸顶
                // dy > 0 &&
                if (decoratedTop - consumed < 0) {
                    currentStickyOffset = 0;
                    stickyInfo = new StickyInfo(position, data);
                    layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                    Log.d(TAG, "scrollVerticallyBy 无吸顶===》当前吸顶 currentStickyPosition==position : " + position);
                    return true;
                }
                Log.d(TAG, "scrollVerticallyBy 无吸顶");
            } else {
                // 说明已经有sticky吸顶item
                View child = findViewByPosition(stickyInfo.position);
                if (child == null) {
                    child = recycler.getViewForPosition(stickyInfo.position);
                    measureChild(child, 0, 0);
                }
                MultiLayoutParams params = (MultiLayoutParams) child.getLayoutParams();
                params.setMultiData(stickyInfo.multiData);
                Log.d(TAG, "scrollVerticallyBy 有吸顶 stickyInfo=" + stickyInfo + " position=" + position);

                if (position > stickyInfo.position) {
                    Log.d(TAG, "scrollVerticallyBy 有吸顶 decoratedTop - consumed="
                            + (decoratedTop - consumed) + " childHeight=" +  getDecoratedMeasuredHeight(view)
                            + " pos=" + position + " stickyPos=" + stickyInfo.position);
                    if (decoratedTop - consumed <= 0) {
                        stickyInfoStack.push(stickyInfo);
                        recycleViews.add(child);
                        if (stickyInfo != null) {
                            stickyInfo.multiData.onItemSticky(new EasyViewHolder(child), stickyInfo.position - stickyInfo.multiData.getLayouter().getPositionOffset(), false);
                        }
                        stickyInfo = new StickyInfo(position, data);
                        currentStickyOffset = 0;
                        layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue + position=" + position);
                        return true;
                    } else if (decoratedTop - consumed < getDecoratedMeasuredHeight(child)) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        if (layouter.getDecoratedTop(child) > 0) {
                            child.offsetTopAndBottom(-layouter.getDecoratedTop(child));
                        }
                        currentStickyOffset = layouter.getDecoratedTop(child);
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue currentStickyOffset=" + currentStickyOffset + " position=" + position);
                    } else {
                        currentStickyOffset = 0;
                    }
                } else {
                    if (decoratedBottom - consumed >= 0) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        if (layouter.getDecoratedTop(child) > 0) {
                            child.offsetTopAndBottom(-layouter.getDecoratedTop(child));
                        }
                        currentStickyOffset = layouter.getDecoratedTop(child);

                    } else if (decoratedBottom - consumed > getDecoratedMeasuredHeight(child)) {
//                                    currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight() - consumed;
                        if (stickyInfo != null) {
                            stickyInfo.multiData.onItemSticky(new EasyViewHolder(child), stickyInfo.position - stickyInfo.multiData.getLayouter().getPositionOffset(), false);
                        }
                        if (stickyInfoStack.isEmpty()) {
                            stickyInfo = null;
                            currentStickyOffset = 0;
                        } else {
                            stickyInfo = stickyInfoStack.pop();
                        }

                        if (stickyInfo != null) {
                            child = findViewByPosition(stickyInfo.position);
                            if (child == null) {
                                child = recycler.getViewForPosition(stickyInfo.position);
                                measureChild(child, 0, 0);
                            }
                            params = (MultiLayoutParams) child.getLayoutParams();
                            params.setMultiData(stickyInfo.multiData);

                            currentStickyOffset = Math.min(0, decoratedTop - getDecoratedMeasuredHeight(child) - consumed);
                        }

                        layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addView(View child) {
        Log.d(TAG, "addView psoition=" + getPosition(child) + " stickyInfo=" + stickyInfo + " lastIndex=" + (getChildCount() - 1));
        if (stickyInfo == null) {
            super.addView(child, getChildCount());
        } else {
            super.addView(child, getChildCount() - 1);
        }
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }

    @Override
    public void scrollToPosition(int position) {
        scrollToPositionWithOffset(position, 0);
    }

    public void scrollToPositionWithOffset(int position, int offset) {

        int positionOffset = 0;

        for (int i = 0; i < multiDataList.size(); i++) {
            MultiData<?> multiData = multiDataList.get(i);
            int count = multiData.getCount();
            if (position >= positionOffset && position < positionOffset + count) {
                mTopMultiDataIndex = i;
                mTopPosition = position - positionOffset;
                mTopOffset = offset;
                break;
            }
            positionOffset += count;
        }

        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected void onStart() {
                super.onStart();
                mScrollDirection = DIRECTION_VERTICAL;
            }

            @Override
            protected void onStop() {
                super.onStop();
                mScrollDirection = DIRECTION_NONE;
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        this.startSmoothScroll(linearSmoothScroller);
    }

    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        View firstChild = getChildAt(0);
        if (firstChild == null) {
            return null;
        } else {
            int firstChildPosition = getPosition(firstChild);
            float direction = targetPosition < firstChildPosition ? -1f : 1f;
            return new PointF(0f, direction);
        }
    }

    @Override
    public void prepareForDrop(@NonNull View view, @NonNull View view1, int i, int i1) {

    }

    public int indexOfChild(View child) {
        return mRecycler.getRecyclerView().indexOfChild(child);
    }

    private void recycleViews(RecyclerView.Recycler recycler) {
        for (View view : recycleViews) {
            Log.d(TAG, "recycleViews pos=" + getPosition(view));
            detachAndScrapView(view, recycler);
        }
        recycleViews.clear();

        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            removeAndRecycleView(scrapList.get(i).itemView, recycler);
        }

        saveState();
    }

    private void saveState() {
        View firstView = getChildAt(0);
        if (firstView == null) {
            mTopMultiDataIndex = 0;
            mTopPosition = 0;
            mTopOffset = 0;
        } else {
            MultiData<?> data = ((MultiLayoutParams) firstView.getLayoutParams()).getMultiData();
            if (data == null) {
                mTopMultiDataIndex = 0;
                mTopPosition = 0;
                mTopOffset = 0;
                return;
            }
            Layouter layouter = data.getLayouter();
//            if (layouter instanceof StaggeredGridLayouter) {
//                ((StaggeredGridLayouter) layouter).saveState();
//            }
            mTopMultiDataIndex = multiDataList.indexOf(data);
            mTopPosition = getPosition(firstView) - layouter.getPositionOffset();
            mTopOffset = layouter.getDecoratedTop(firstView);

            Log.d(TAG, "onLayoutChildren mTopMultiDataIndex=" + mTopMultiDataIndex
                    + " mTopPosition=" + mTopPosition + " mTopOffset=" + mTopOffset + " getPosition=" + getPosition(firstView));
        }
    }

    private static class RefresherMultiData extends MultiData<Void> {

        private final IRefresher mRefresher;

        public RefresherMultiData(IRefresher mRefresher) {
            super(new VerticalLayouter());
            hasMore = false;
            this.mRefresher = mRefresher;
        }

        @Override
        public View onCreateView(Context context, ViewGroup container, int viewType) {
            if (mRefresher.getView() == null) {
                return mRefresher.onCreateView(context, container);
            }
            return mRefresher.getView();
        }

        @Override
        public int getViewType(int position) {
            return mRefresher.hashCode();
        }

        @Override
        public boolean hasViewType(int viewType) {
            return viewType == mRefresher.hashCode();
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public int getLayoutId(int viewType) {
            return 0;
        }

        @Override
        public boolean loadData() {
            return false;
        }

        @Override
        public void onBindViewHolder(EasyViewHolder holder, List<Void> list, int position, List<Object> payloads) {

        }
    }

}
