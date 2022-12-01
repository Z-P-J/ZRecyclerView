package com.zpj.recyclerview.core;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.GroupMultiData;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.AbsLayouter;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.RefresherLayouter;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MultiLayoutManager extends BaseMultiLayoutManager
        implements ItemTouchHelper.ViewDropHandler,
        RecyclerView.SmoothScroller.ScrollVectorProvider {

    private static final String TAG = "MultiLayoutManager";

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    private static final int OVER_SCROLL_DOWN = 1;
    private static final int OVER_SCROLL_UP = 2;
    private static final int OVER_SCROLL_LEFT = 3;
    private static final int OVER_SCROLL_RIGHT = 4;

    private final Deque<StickyInfo> stickyInfoStack = new ArrayDeque<>();

    private List<MultiData<?>> multiDataList;

    private int mScrollDirection = DIRECTION_NONE;

    private float mDownX = -1;
    private float mDownY = -1;

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

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    public void attachRecycler(final MultiRecycler recycler) {
        super.attachRecycler(recycler);
        this.multiDataList = recycler.getItems();
        if (recycler.getRefresher() != null) {
            this.multiDataList.add(0, new RefresherMultiData(recycler.getRefresher()));
        }
        recycler.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        final int touchSlop = ViewConfiguration.get(mRecycler.getContext()).getScaledTouchSlop();
        final int maxVelocity = ViewConfiguration.get(mRecycler.getContext()).getScaledMaximumFlingVelocity();
        recycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private static final String TAG = "OnItemTouchListener";

            private VelocityTracker mTracker;
            private MultiData<?> mMultiData = null;
            private RefresherMultiData mRefresherData;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                int action = event.getAction();
                Log.d(TAG, "onInterceptTouchEvent event=" + MotionEvent.actionToString(action) + " mScrollDirection=" + mScrollDirection);
                if (mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                }
                mTracker.addMovement(event);
                if (MotionEvent.ACTION_DOWN == action) {
                    mMultiData = null;
                    mRefresherData = null;
                    mScrollDirection = DIRECTION_NONE;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    if (mOverScrollAnimator != null) {
                        mOverScrollAnimator.pause();
                        mOverScrollAnimator = null;
                    }

                    List<MultiData<?>> dataList = getAllMultiData(multiDataList);
                    for (int i = 0; i < dataList.size(); i++) {
                        MultiData<?> multiData = dataList.get(i);
                        Layouter layouter = multiData.getLayouter();
                        Log.d(TAG, "onInterceptTouchEvent mDownX=" + mDownX + " mDownY=" + mDownY + " layouter=" + layouter);
                        if (layouter.onTouchDown(multiData, mDownX, mDownY, event)) {
                            if (multiData instanceof RefresherMultiData && !isOverScrolling) {
                                mRefresherData = (RefresherMultiData) multiData;
                            } else {
                                mMultiData = multiData;
                                break;
                            }
                        }
                    }
                } else if (MotionEvent.ACTION_MOVE == action) {
                    if (mScrollDirection == DIRECTION_NONE) {
                        float deltaX = Math.abs(event.getX() - mDownX);
                        float deltaY = Math.abs(event.getY() - mDownY);

                        Log.d(TAG, "onInterceptTouchEvent deltaX=" + deltaX + " deltaY=" + deltaY + " touchSlop=" + touchSlop);
                        if (deltaX < touchSlop && deltaY < touchSlop) {
                            return false;
                        }

                        float radio = Math.abs(deltaX / deltaY);
                        Log.d(TAG, "onInterceptTouchEvent deltaX=" + deltaX + " deltaY=" + deltaY + " radio=" + radio);
                        if (radio == 1f) {
                            return false;
                        }
                        if (radio > 1f) {
                            mScrollDirection = DIRECTION_HORIZONTAL;
                        } else {
                            mScrollDirection = DIRECTION_VERTICAL;
                        }
                    }

                    if (mScrollDirection == DIRECTION_VERTICAL) {
                        if (mRefresherData != null) {
                            mRefresherData.getLayouter().onTouchMove(mRefresherData, event.getX(), event.getY(), mDownX, mDownY, event);
                            mMultiData = null;
                        }
                    } else if (mScrollDirection == DIRECTION_HORIZONTAL) {
                        if (mMultiData != null) {
                            mMultiData.getLayouter().onTouchMove(mMultiData, event.getX(), event.getY(), mDownX, mDownY, event);
                        }
                        mRefresherData = null;
                    }
                } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {

                    mTracker.computeCurrentVelocity(1000, maxVelocity);
                    float velocityX = mTracker.getXVelocity();
                    float velocityY = mTracker.getYVelocity();
                    mTracker.recycle();
                    mTracker = null;

                    if (isOverScrolling) {
                        onStopOverScroll();
                    }

                    int direction = mScrollDirection;
                    if (direction == DIRECTION_HORIZONTAL) {
                        mScrollDirection = DIRECTION_VERTICAL;
                        velocityY = 0f;
                    } else if (direction == DIRECTION_VERTICAL) {
                        velocityX = 0f;
                    }

                    boolean result = false;
                    if (mMultiData != null) {
                        mMultiData.getLayouter().onTouchUp(mMultiData, velocityX, velocityY, event);
                        mMultiData = null;
                        result = direction == DIRECTION_HORIZONTAL;
                    }
                    if (mRefresherData != null) {
                        result |= mRefresherData.getLayouter().onTouchUp(mRefresherData, velocityX, velocityY, event);
                        mRefresherData = null;
                    }
                    return result;
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                int action = event.getAction();
                Log.d(TAG, "onTouchEvent event=" + MotionEvent.actionToString(action) + " mScrollDirection=" + mScrollDirection);
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

    @Override
    public boolean isOverScrolling() {
        return isOverScrolling;
    }

    private void onStopOverScroll() {
        if (isOverScrolling) {
            if (mOverScrollAnimator != null) {
                mOverScrollAnimator.pause();
                mOverScrollAnimator = null;
            }

            if (overScrollDirection <= OVER_SCROLL_UP) {
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

                    if (mOverScrollAnimator == null) {
                        View lastChild = getChildAt(stickyInfo == null ? getChildCount() - 1 : getChildCount() - 2);
                        if (lastChild != null) {
                            final int bottom = getDecoratedBottom(lastChild);
                            final int contentHeight = Math.min(getHeight(), bottom - firstTop);
                            if (bottom < getHeight()) {
                                mOverScrollAnimator = ValueAnimator.ofFloat(0f, 1f);
                                mOverScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    int lastBottom = bottom;

                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        float percent = (float) animation.getAnimatedValue();
                                        int b = (int) (percent * (contentHeight - bottom) + bottom);
                                        offsetChildrenVertical(b - lastBottom);
                                        lastBottom = b;
                                    }
                                });
                            }
                        }
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
                        RecyclerViewHelper.stopInterceptRequestLayout(MultiLayoutManager.this);
                    }
                });
                mOverScrollAnimator.start();
                return;
            }


            isOverScrolling = false;
            RecyclerViewHelper.stopInterceptRequestLayout(MultiLayoutManager.this);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.d(TAG, "onLayoutChildren state=" + state + " isPreLayout=" + state.isPreLayout());

        if (multiDataList == null) {
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

//        detachAndScrapAttachedViews(recycler);

        for(int i = getChildCount() - 1; i >= 0; --i) {
            View v = this.getChildAt(i);
            Layouter layouter = getLayouter(v);
            Log.d(TAG, "onLayoutChildren scrapOrRecycleView layouter=" + layouter);
            layouter.getLayoutHelper().scrapOrRecycleView(i, v);
        }

        StickyInfo temp = stickyInfo;
        stickyInfo = null;

        int positionOffset = 0;
        int topPosition = 0;


        Layouter last = null;
        List<MultiData<?>> dataList = getAllMultiData(multiDataList);
        for (int i = 0; i < dataList.size(); i++) {
            MultiData<?> multiData = dataList.get(i);
            AbsLayouter layouter = (AbsLayouter) multiData.getLayouter();
            layouter.setPositionOffset(positionOffset);
            if (layouter.getLayoutHelper() == null || layouter.isAttached()) {
                layouter.attach(new MultiScene(this, multiData));
                if (last != null) {
                    Log.d(TAG, "onLayoutChildren i=" + i + " bottom=" + last.getBottom());
                    layouter.setTop(last.getBottom());
                    layouter.layoutChildren(multiData);
                } else {
                    topPosition = layouter.mAnchorInfo.position + positionOffset;
                    layouter.layoutChildren(multiData);
                }
                last = layouter;
            }
            positionOffset += getCount(multiData);
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
            } else {
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

        MultiData<?> lastData = null;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            final MultiData<?> data = getMultiData(view);
            if (data != lastData) {
                lastData = data;
                if (data.hasMore()) {
                    mRecycler.post(new Runnable() {
                        @Override
                        public void run() {
                            data.load(mRecycler.getAdapter());
                        }
                    });
                }
            }
        }


    }

    private List<MultiData<?>> getAllMultiData(List<MultiData<?>> dataList) {
        List<MultiData<?>> list = new ArrayList<>();
        for (MultiData<?> data : dataList) {
            if (data instanceof GroupMultiData) {
                if (data.getCount() > 0) {
                    list.addAll(getAllMultiData(((GroupMultiData) data).getData()));
                }
            } else {
                list.add(data);
            }
        }
        return list;
    }

    private void initStickyInfoStack(int position) {
        stickyInfoStack.clear();
        int offset = 0;
        List<MultiData<?>> dataList = getAllMultiData(multiDataList);
        for (int i = 0; i < dataList.size(); i++) {
            MultiData<?> multiData = dataList.get(i);
            int count = getCount(multiData);
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
        return false;
//        return mScrollDirection == DIRECTION_HORIZONTAL;
    }

    public boolean isScrollHorizontally() {
        return mScrollDirection == DIRECTION_HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollHorizontallyBy(dx, getTouchMultiData(mDownX, mDownY));
    }

    public int scrollHorizontallyBy(int dx) {
        return scrollHorizontallyBy(dx, getTouchMultiData(mDownX, mDownY));
    }

    public int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData) {
        return 0;
//        if (multiDataList == null || scrollMultiData == null) {
//            return 0;
//        }
//        return scrollMultiData.getLayouter().scrollHorizontallyBy(dx, scrollMultiData);
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
        List<MultiData<?>> dataList = getAllMultiData(multiDataList);

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
            RecyclerViewHelper.stopInterceptRequestLayout(MultiLayoutManager.this);

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
//                view.setTag(i);
                multiData = getMultiData(view);
                i++;
            } while (multiData == null);
            Layouter layouter = multiData.getLayouter();
            Log.d(TAG, "scrollVerticallyBy firstChildPosition=" + getPosition(view));
            consumed -= layouter.fillVertical(view, dy - consumed, multiData);
            i = dataList.indexOf(multiData);
            while (consumed > dy && i > 0) {
                int top = layouter.getTop();
                multiData = dataList.get(--i);
                layouter = multiData.getLayouter();
                layouter.setBottom(top);
                Log.d(TAG, "scrollVerticallyBy layouter=" + layouter);
                consumed -= layouter.fillVertical(null, dy - consumed, multiData);
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
//                view.setTag(i);
                multiData = getMultiData(view);
                i--;
            } while (multiData == null);
            Layouter layouter = multiData.getLayouter();
            Log.w(TAG, "scrollVerticallyBy layouter=" + layouter);
            consumed += layouter.fillVertical(view, dy - consumed, multiData);
            i = dataList.indexOf(multiData);
            Log.w(TAG, "scrollVerticallyBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(view));
            while (consumed < dy && i < dataList.size() - 1) {
                int bottom = layouter.getBottom();
                multiData = dataList.get(++i);
                layouter = multiData.getLayouter();
                Log.w(TAG, "scrollVerticallyBy layouter=" + layouter + " consumed=" + consumed);
                layouter.setTop(bottom);
                consumed += layouter.fillVertical(null, dy - consumed, multiData);
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
            MultiData<?> data = getMultiData(view);
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
                if (handleSticky(data, view, i, consumed)) {
                    continue;
                }
                if (layouter.getLayoutHelper().shouldRecycleChildViewVertically(view, consumed)) {
                    layouter.getLayoutHelper().addViewToRecycler(view);
                } else {
                    view.offsetTopAndBottom(-consumed);
                }
            }
        }
        recycleViews();

//        for (Layouter layouter : layouters) {
//            if (layouter instanceof StaggeredGridLayouter) {
//                ((StaggeredGridLayouter) layouter).saveState();
//            }
//        }

        if (dy != consumed) {
            int overScroll = (int) ((consumed - dy) * 0.1f);
            isOverScrolling = true;
            RecyclerViewHelper.startInterceptRequestLayout(MultiLayoutManager.this);
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

    private boolean handleSticky(MultiData<?> data, View view, int i, int consumed) {
        Layouter layouter = data.getLayouter();
        int position = getPosition(view);

        boolean isStickyPosition = data.isStickyPosition(position - layouter.getPositionOffset());
        if (!isStickyPosition) {
            return false;
        }

        Log.e(TAG, "scrollVerticallyBy");
        Log.e(TAG, "scrollVerticallyBy ================================start");
        Log.d(TAG, "scrollVerticallyBy i=" + i + " stickyPosition=" + position);
        int decoratedTop = layouter.getLayoutHelper().getDecoratedTop(view);
        int decoratedBottom = layouter.getLayoutHelper().getDecoratedBottom(view);
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
                        child = getViewForPosition(stickyInfo.position);
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
                    child = getViewForPosition(stickyInfo.position);
                    measureChild(child, 0, 0);
                }
                MultiLayoutParams params = (MultiLayoutParams) child.getLayoutParams();
                params.setMultiData(stickyInfo.multiData);
                Log.d(TAG, "scrollVerticallyBy 有吸顶 stickyInfo=" + stickyInfo + " position=" + position);

                if (position > stickyInfo.position) {
                    Log.d(TAG, "scrollVerticallyBy 有吸顶 decoratedTop - consumed="
                            + (decoratedTop - consumed) + " childHeight=" + getDecoratedMeasuredHeight(view)
                            + " pos=" + position + " stickyPos=" + stickyInfo.position);
                    if (decoratedTop - consumed <= 0) {
                        stickyInfoStack.push(stickyInfo);
                        layouter.getLayoutHelper().addViewToRecycler(child);
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
                        int top = layouter.getLayoutHelper().getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = layouter.getLayoutHelper().getDecoratedTop(child);
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue currentStickyOffset=" + currentStickyOffset + " position=" + position);
                    } else {
                        currentStickyOffset = 0;
                    }
                } else {
                    if (decoratedBottom - consumed >= 0) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        int top = layouter.getLayoutHelper().getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = layouter.getLayoutHelper().getDecoratedTop(child);

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
                                child = getViewForPosition(stickyInfo.position);
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
        List<MultiData<?>> dataList = getAllMultiData(multiDataList);
        for (int i = 0; i < dataList.size(); i++) {
            MultiData<?> multiData = dataList.get(i);
            Layouter layouter = multiData.getLayouter();
            if (position < layouter.getPositionOffset()) {
                layouter.detach();
            } else if (!layouter.scrollToPositionWithOffset(multiData, position, offset)) {
                layouter.setBottom(-1);
            }
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

    private MultiData<?> getTouchMultiData(float downX, float downY) {
        MultiData<?> tempMultiData = null;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            MultiData<?> multiData = getMultiData(view);
            if (multiData == null || multiData == tempMultiData) {
                continue;
            }
            tempMultiData = multiData;
            Layouter layouter = multiData.getLayouter();
            if (layouter.canScrollHorizontally() && downY >= layouter.getTop() && downY <= layouter.getBottom()) {
                return multiData;
            }
        }
        return null;
    }

    @Override
    public void recycleViews() {
        super.recycleViews();
        saveState();
    }

    private void saveState() {
        MultiData<?> last = null;
        for (int i =0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            MultiData<?> data = getMultiData(child);
            if (last != data) {
                last = data;
                data.getLayouter().saveState(child);

                Log.e(TAG, "saveState i=" + i + " mAnchorInfo=" + ((AbsLayouter) data.getLayouter()).mAnchorInfo);

            }
        }
    }

    private static class RefresherMultiData extends MultiData<Void> {

        private final IRefresher mRefresher;

        public RefresherMultiData(IRefresher mRefresher) {
            super(new RefresherLayouter(mRefresher));
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
