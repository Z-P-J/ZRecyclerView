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
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.scene.RefresherScene;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MultiSceneLayoutManager extends BaseMultiLayoutManager
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

    private List<Scene> mSceneList;

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
        Scene scene;

        public StickyInfo(int position, Scene scene) {
            this.position = position;
            this.scene = scene;
        }

        @Override
        public String toString() {
            return "StickyInfo{" +
                    "position=" + position +
                    ", scene=" + scene +
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

    public void attachRecycler(final MultiSceneRecycler recycler) {
        super.attachRecycler(recycler);
        mSceneList = recycler.getItems();
        if (recycler.getRefresher() != null) {
            mSceneList.add(0, new RefresherScene(
                    new RefresherMultiData(recycler.getRefresher()), recycler.getRefresher()));
        }
        recycler.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        final int touchSlop = ViewConfiguration.get(mRecycler.getContext()).getScaledTouchSlop();
        final int maxVelocity = ViewConfiguration.get(mRecycler.getContext()).getScaledMaximumFlingVelocity();
        recycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            private static final String TAG = "OnItemTouchListener";

            private VelocityTracker mTracker;

            private Scene mTouchedScene;
            private Scene mRefresherScene;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                int action = event.getAction();
                Log.d(TAG, "onInterceptTouchEvent event=" + MotionEvent.actionToString(action) + " mScrollDirection=" + mScrollDirection);
                if (mTracker == null) {
                    mTracker = VelocityTracker.obtain();
                }
                mTracker.addMovement(event);
                if (MotionEvent.ACTION_DOWN == action) {
                    mTouchedScene = null;
                    mRefresherScene = null;
                    mScrollDirection = DIRECTION_NONE;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    if (mOverScrollAnimator != null) {
                        mOverScrollAnimator.pause();
                        mOverScrollAnimator = null;
                    }

                    for (Scene scene : mSceneList) {
                        if (scene.onTouchDown(mDownX, mDownY, event)) {
                            if (scene.getMultiData() instanceof RefresherMultiData) {
                                if (!isOverScrolling) {
                                    mRefresherScene = scene;
                                }
                            } else {
                                mTouchedScene = scene;
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
                        if (mRefresherScene != null) {
                            mRefresherScene.onTouchMove(event.getX(), event.getY(), mDownX, mDownY, event);
                            mTouchedScene = null;
                        }
                    } else if (mScrollDirection == DIRECTION_HORIZONTAL) {
                        if (mTouchedScene != null) {
                            mTouchedScene.onTouchMove(event.getX(), event.getY(), mDownX, mDownY, event);
                        }
                        mRefresherScene = null;
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
                    if (mTouchedScene != null) {
                        mTouchedScene.onTouchUp(velocityX, velocityY, event);
                        mTouchedScene = null;
                        result = direction == DIRECTION_HORIZONTAL;
                    }
                    if (mRefresherScene != null) {
                        result |= mRefresherScene.onTouchUp(velocityX, velocityY, event);
                        mRefresherScene = null;
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
                        RecyclerViewHelper.stopInterceptRequestLayout(MultiSceneLayoutManager.this);
                    }
                });
                mOverScrollAnimator.start();
                return;
            }


            isOverScrolling = false;
            RecyclerViewHelper.stopInterceptRequestLayout(MultiSceneLayoutManager.this);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.d(TAG, "onLayoutChildren state=" + state + " isPreLayout=" + state.isPreLayout());

        if (mSceneList == null) {
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

//        detachAndScrapAttachedViews(recycler);

        for(int i = getChildCount() - 1; i >= 0; --i) {
            View v = this.getChildAt(i);
            Scene scene = getScene(v);
            Log.d(TAG, "onLayoutChildren scrapOrRecycleView layouter=" + scene);
            scene.getLayoutHelper().scrapOrRecycleView(i, v);
        }

        StickyInfo temp = stickyInfo;
        stickyInfo = null;

        int positionOffset = 0;
        int topPosition = 0;


        Scene lastScene = null;
        for (Scene scene : mSceneList) {
            scene.setPositionOffset(positionOffset);
            if (scene.getLayoutManager() == null || scene.isAttached()) {
                scene.attach(this);

                if (lastScene == null) {
//                    topPosition = scene.mAnchorInfo.position + positionOffset;
                    // TODO fixme
                    topPosition = positionOffset;
                } else {
                    scene.setTop(lastScene.getBottom());
                }
                scene.layoutChildren();
                lastScene = scene;
            }
            positionOffset += scene.getItemCount();
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
                SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                params.setScene(stickyInfo.scene);
                super.addView(child, getChildCount());
                measureChild(child, 0, 0);
                layoutDecorated(child, 0, currentStickyOffset, getWidth(),
                        currentStickyOffset + getDecoratedMeasuredHeight(child));

                stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(child),
                        stickyInfo.position - stickyInfo.scene.getPositionOffset(), true);
            }
        }

        lastScene = null;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            final Scene scene = getScene(view);
            if (scene != lastScene) {
                lastScene = scene;
                if (scene.getMultiData().hasMore()) {
                    mRecycler.post(new Runnable() {
                        @Override
                        public void run() {
                            scene.getMultiData().load(mRecycler.getAdapter());
                        }
                    });
                }
            }
        }


    }

//    private List<MultiData<?>> getAllMultiData(List<MultiData<?>> dataList) {
//        List<MultiData<?>> list = new ArrayList<>();
//        for (MultiData<?> data : dataList) {
//            if (data instanceof GroupMultiData) {
//                if (data.getCount() > 0) {
//                    list.addAll(getAllMultiData(((GroupMultiData) data).getData()));
//                }
//            } else {
//                list.add(data);
//            }
//        }
//        return list;
//    }

    private void initStickyInfoStack(int position) {
        stickyInfoStack.clear();
        int offset = 0;

        for (Scene scene : mSceneList) {
            int count = scene.getItemCount();
            for (int pos = offset; pos < offset + count; pos++) {
                if (scene.getMultiData().isStickyPosition(pos - offset)) {
                    Log.d(TAG, "stickyInfo111 pos=" + pos);
                    stickyInfoStack.push(new StickyInfo(pos, scene));
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
        return 0;
//        return scrollHorizontallyBy(dx, getTouchMultiData(mDownX, mDownY));
    }

    public int scrollHorizontallyBy(int dx) {
        return 0;
//        return scrollHorizontallyBy(dx, getTouchMultiData(mDownX, mDownY));
    }

//    public int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData) {
//        return 0;
////        if (multiDataList == null || scrollMultiData == null) {
////            return 0;
////        }
////        return scrollMultiData.getLayouter().scrollHorizontallyBy(dx, scrollMultiData);
//    }

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
        if (mSceneList == null) {
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
            RecyclerViewHelper.stopInterceptRequestLayout(MultiSceneLayoutManager.this);

        }

        int consumed = 0;

        Log.d(TAG, "\n============================================scrollVerticallyBy dy=" + dy);


        if (dy < 0) {
            // 从上往下滑动
            int i = 0;
            Scene scene = null;
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
                scene = getScene(view);
                i++;
            } while (scene == null);
            Log.d(TAG, "scrollVerticallyBy firstChildPosition=" + getPosition(view));
            consumed -= scene.fillVertical(view, dy - consumed);
            i = mSceneList.indexOf(scene);
            while (consumed > dy && i > 0) {
                int top = scene.getTop();
                scene = mSceneList.get(--i);
                scene.setBottom(top);
                consumed -= scene.fillVertical(null, dy - consumed);
            }
        } else {
            // 从下往上滑动

            int i = getChildCount() - 1;
            Scene scene = null;
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
                scene = getScene(view);
                i--;
            } while (scene == null);
            consumed += scene.fillVertical(view, dy - consumed);
            i = mSceneList.indexOf(scene);
            Log.w(TAG, "scrollVerticallyBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(view));
            while (consumed < dy && i < mSceneList.size() - 1) {
                int bottom = scene.getBottom();
                scene = mSceneList.get(++i);
                scene.setTop(bottom);
                consumed += scene.fillVertical(null, dy - consumed);
            }
        }

        Log.d(TAG, "scrollVerticallyBy consumed=" + consumed);

        Scene last = null;
        handleSticky = true;
//        List<Layouter> layouters = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            Scene scene = getScene(view);
            if (scene == null) {
                Log.d(TAG, "scrollVerticallyBy i=" + i + " position=" + getPosition(view));
                continue;
            }
//            Layouter layouter = scene.getLayouter();
            if (scene != last) {
//                layouters.add(layouter);
                scene.offsetTopAndBottom(-consumed);
                last = scene;
            }
            if (scene.canScrollVertically()) {
                if (handleSticky(scene, view, i, consumed)) {
                    continue;
                }
                if (scene.getLayoutHelper().shouldRecycleChildViewVertically(view, consumed)) {
                    scene.getLayoutHelper().addViewToRecycler(view);
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
            RecyclerViewHelper.startInterceptRequestLayout(MultiSceneLayoutManager.this);
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
            SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
            params.setScene(stickyInfo.scene);
            super.addView(child, getChildCount());
            measureChild(child, 0, 0);
            Log.e(TAG, "scrollVerticallyBy currentStickyOffset=" + currentStickyOffset);
            layoutDecorated(child, 0, currentStickyOffset, getWidth(), currentStickyOffset + getDecoratedMeasuredHeight(child));

            stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(child),
                    stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                    true);
        }

        Log.d(TAG, "scrollVerticallyBy getChildCount=" + getChildCount() + " \n");
        return consumed;
    }

    private boolean handleSticky;

    private boolean handleSticky(Scene scene, View view, int i, int consumed) {
        int position = getPosition(view);

        boolean isStickyPosition = scene.getMultiData().isStickyPosition(position - scene.getPositionOffset());
        if (!isStickyPosition) {
            return false;
        }

        Log.e(TAG, "scrollVerticallyBy");
        Log.e(TAG, "scrollVerticallyBy ================================start");
        Log.d(TAG, "scrollVerticallyBy i=" + i + " stickyPosition=" + position);
        int decoratedTop = scene.getDecoratedTop(view);
        int decoratedBottom = scene.getDecoratedBottom(view);
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
                    stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(view),
                            stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                            false);
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
                    SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                    params.setScene(stickyInfo.scene);

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
                    stickyInfo = new StickyInfo(position, scene);
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
                SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                params.setScene(stickyInfo.scene);
                Log.d(TAG, "scrollVerticallyBy 有吸顶 stickyInfo=" + stickyInfo + " position=" + position);

                if (position > stickyInfo.position) {
                    Log.d(TAG, "scrollVerticallyBy 有吸顶 decoratedTop - consumed="
                            + (decoratedTop - consumed) + " childHeight=" + getDecoratedMeasuredHeight(view)
                            + " pos=" + position + " stickyPos=" + stickyInfo.position);
                    if (decoratedTop - consumed <= 0) {
                        stickyInfoStack.push(stickyInfo);
                        scene.getLayoutHelper().addViewToRecycler(child);
                        if (stickyInfo != null) {
                            stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(child), stickyInfo.position - stickyInfo.scene.getPositionOffset(), false);
                        }
                        stickyInfo = new StickyInfo(position, scene);
                        currentStickyOffset = 0;
                        layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue + position=" + position);
                        return true;
                    } else if (decoratedTop - consumed < getDecoratedMeasuredHeight(child)) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        int top = scene.getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = scene.getDecoratedTop(child);
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue currentStickyOffset=" + currentStickyOffset + " position=" + position);
                    } else {
                        currentStickyOffset = 0;
                    }
                } else {
                    if (decoratedBottom - consumed >= 0) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        int top = scene.getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = scene.getDecoratedTop(child);

                    } else if (decoratedBottom - consumed > getDecoratedMeasuredHeight(child)) {
//                                    currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight() - consumed;
                        if (stickyInfo != null) {
                            stickyInfo.scene.getMultiData()
                                    .onItemSticky(new EasyViewHolder(child),
                                            stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                                            false);
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
                            params = (SceneLayoutParams) child.getLayoutParams();
                            params.setScene(stickyInfo.scene);

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

        for (Scene scene : mSceneList) {
            if (position < scene.getPositionOffset()) {
                scene.detach();
            } else if (!scene.scrollToPositionWithOffset(position, offset)) {
                scene.setBottom(-1);
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

//    private MultiData<?> getTouchMultiData(float downX, float downY) {
//        MultiData<?> tempMultiData = null;
//        for (int i = getChildCount() - 1; i >= 0; i--) {
//            View view = getChildAt(i);
//            if (view == null) {
//                continue;
//            }
//            MultiData<?> multiData = getMultiData(view);
//            if (multiData == null || multiData == tempMultiData) {
//                continue;
//            }
//            tempMultiData = multiData;
//            Layouter layouter = multiData.getLayouter();
//            if (layouter.canScrollHorizontally() && downY >= layouter.getTop() && downY <= layouter.getBottom()) {
//                return multiData;
//            }
//        }
//        return null;
//    }

    @Override
    public void recycleViews() {
        super.recycleViews();
        saveState();
    }

    private void saveState() {
        Scene last = null;
        for (int i =0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == null) {
                continue;
            }
            Scene scene = getScene(child);
            if (last != scene) {
                last = scene;
                scene.saveState(child);
            }
        }
    }

    private static class RefresherMultiData extends MultiData<Void> {

        private final IRefresher mRefresher;

        public RefresherMultiData(IRefresher mRefresher) {
            super();
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
