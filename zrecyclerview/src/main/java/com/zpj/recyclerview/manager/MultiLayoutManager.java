package com.zpj.recyclerview.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.Layouter;

import java.util.ArrayList;
import java.util.List;

public class MultiLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "MultiLayoutManager";

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    private List<MultiData<?>> multiDataList;

    private int mScrollDirection = DIRECTION_NONE;

    private float mDownX = -1;
    private float mDownY = -1;

    private int mTopMultiDataIndex;
    private int mTopPosition;
    private int mTopOffset;
    private int mTopPositionOffset;
    private int mTopChildOffset;

    public void attachRecycler(RecyclerView recyclerView, List<MultiData<?>> multiDataList) {
        this.multiDataList = multiDataList;
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {
                int action = event.getAction();
                if (MotionEvent.ACTION_DOWN == action) {
                    mScrollDirection = DIRECTION_NONE;
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
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
        Log.d(TAG, "scrollHorizontallyBy mTopMultiDataIndex=" + mTopMultiDataIndex
                + " mTopPosition=" + mTopPosition + " mTopOffset=" + mTopOffset + " isPreLayout=" + state.isPreLayout());

        if (multiDataList == null) {
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        int positionOffset = mTopPositionOffset;
        Layouter last = null;

        for (int i = mTopMultiDataIndex; i < multiDataList.size(); i++) {
            MultiData<?> multiData = multiDataList.get(i);
            Layouter layouter = multiData.getLayouter();
            layouter.setPositionOffset(positionOffset);
            layouter.setLayoutManager(this);
            if (last != null) {
                layouter.setTop(last.getBottom());
                layouter.layoutChildren(multiData, recycler, positionOffset);
            } else {
                layouter.setTop(mTopOffset);
                layouter.layoutChildren(multiData, recycler, mTopPosition);
            }
            last = layouter;
            positionOffset += multiData.getCount();
        }


//        int positionOffset = 0;
//        int childOffset = 0;
//        Layouter last = null;
//        for (MultiData<?> multiData : multiDataList) {
//            if (multiData instanceof LayouterMultiData) {
//                Layouter layouter = ((LayouterMultiData) multiData).getLayouter();
//                layouter.setPositionOffset(positionOffset);
//                layouter.setChildOffset(childOffset);
//                layouter.setLayoutManager(this);
//                if (last != null) {
//                    Log.d(TAG, "onLayoutChildren bottom=" + last.getBottom());
//                    layouter.setTop(last.getBottom());
//                } else {
//                    Log.d(TAG, "onLayoutChildren bottom=00");
//                    layouter.setTop(0);
//                }
//                layouter.setPositionOffset(positionOffset);
//                int count = layouter.onLayoutChildren(multiData, recycler, state);
//                last = layouter;
//                positionOffset += multiData.getCount();
//                childOffset += count;
//            }
//        }
//
//        for (int i = 0; i < getChildCount(); i++) {
//            View view = getChildAt(i);
//            if (view == null) {
//                continue;
//            }
//            if (getDecoratedBottom(view) < 0 || getDecoratedTop(view) > getHeight()) {
//                Log.d(TAG, "onLayoutChildren position=" + getPosition(view));
//                recycleViews.add(view);
//            }
//        }
//
//        recycleViews(recycler);

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
        Layouter.State s = new Layouter.State();

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
                if (multiData == tempMultiData) {
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

                    s.setMultiData(multiData);
                    view.setTag(i);
                    consumed += layouter.fillHorizontal(view, dx, recycler, s);
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
                if (multiData == tempMultiData) {
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

                    s.setMultiData(multiData);
                    consumed -= layouter.fillHorizontal(view, dx, recycler, s);
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
            int firstOffset = getDecoratedLeft(child);
            layouter.saveState(firstPosition, firstOffset);
        }

        return consumed;
    }

    @Override
    public boolean canScrollVertically() {
        return mScrollDirection == DIRECTION_VERTICAL;
    }

    private final List<View> recycleViews = new ArrayList<>();

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (multiDataList == null) {
            return 0;
        }

        int consumed = 0;

        Layouter.State s = new Layouter.State();
        Log.d(TAG, "scrollVerticalBy dy=" + dy);

        if (dy < 0) {
            // 从上往下滑动
            View firstChild = getChildAt(0);
            if (firstChild == null) {
                return 0;
            }
            MultiData<?> multiData = ((MultiLayoutParams) firstChild.getLayoutParams()).getMultiData();
            Layouter layouter = multiData.getLayouter();
            s.setMultiData(multiData);
            Log.d(TAG, "scrollVerticallyBy firstChildPosition=" + getPosition(firstChild));
            consumed -= layouter.fillVertical(firstChild, dy - consumed, recycler, s);
            int i = multiDataList.indexOf(multiData);
            while (consumed > dy && i > 0) {
                int top = layouter.getTop();
                multiData = multiDataList.get(--i);
                layouter = multiData.getLayouter();
                layouter.setBottom(top);
                s.setMultiData(multiData);
                consumed -= layouter.fillVertical(null, dy - consumed, recycler, s);
            }
        } else {
            // 从下往上滑动
            View lastChild = getChildAt(getChildCount() - 1);
            if (lastChild == null) {
                return 0;
            }
            MultiData<?> multiData = ((MultiLayoutParams) lastChild.getLayoutParams()).getMultiData();
            Layouter layouter = multiData.getLayouter();
            s.setMultiData(multiData);
            consumed += layouter.fillVertical(lastChild, dy - consumed, recycler, s);
            int i = multiDataList.indexOf(multiData);
            Log.d(TAG, "scrollVerticalBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(lastChild));
            while (consumed < dy && i < multiDataList.size() - 1) {
                int bottom = layouter.getBottom();
                multiData = multiDataList.get(++i);
                layouter = multiData.getLayouter();
                Log.d(TAG, "scrollVerticalBy layouter=" + layouter);
                layouter.setTop(bottom);
                s.setMultiData(multiData);
                consumed += layouter.fillVertical(null, dy - consumed, recycler, s);
            }
        }

        Log.d(TAG, "scrollVerticalBy consumed=" + consumed);

        MultiData<?> last = null;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            MultiData<?> data = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
            Layouter layouter = data.getLayouter();
            if (data != last) {
                layouter.offsetTopAndBottom(-consumed);
                last = data;
            }
            if (layouter.canScrollVertically()) {
                if (view.getBottom() - consumed + getBottomDecorationHeight(view) < 0
                        || view.getTop() - consumed - getTopDecorationHeight(view) > getHeight()) {
                    recycleViews.add(view);
                } else {
                    view.offsetTopAndBottom(-consumed);
                }
            }
        }
        recycleViews(recycler);
        Log.d(TAG, "scrollVerticallyBy getChildCount=" + getChildCount());
        return consumed;
    }



    private void recycleViews(RecyclerView.Recycler recycler) {
        for (View view : recycleViews) {
            detachAndScrapView(view, recycler);
        }
        recycleViews.clear();

        saveState();
    }

    private void saveState() {
        View firstView = getChildAt(0);
        if (firstView == null) {
            mTopPositionOffset = 0;
            mTopChildOffset = 0;
            mTopMultiDataIndex = 0;
            mTopPosition = 0;
            mTopOffset = 0;
        } else {
            MultiData<?> data = ((MultiLayoutParams) firstView.getLayoutParams()).getMultiData();
            Layouter layouter = data.getLayouter();
            mTopPositionOffset = layouter.getPositionOffset();
            mTopChildOffset = layouter.getChildOffset();
            mTopMultiDataIndex = multiDataList.indexOf(data);
            mTopPosition = getPosition(firstView);
            mTopOffset = getDecoratedTop(firstView);
        }
    }



}
