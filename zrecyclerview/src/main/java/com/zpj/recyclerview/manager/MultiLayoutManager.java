package com.zpj.recyclerview.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.ArrayList;
import java.util.List;

public class MultiLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "MultiLayoutManager";

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    private final List<View> recycleViews = new ArrayList<>();

    private MultiRecycler mRecycler;
    private List<MultiData<?>> multiDataList;

    private int mScrollDirection = DIRECTION_NONE;

    private float mDownX = -1;
    private float mDownY = -1;

    private int mTopMultiDataIndex;
    private int mTopPosition;
    private int mTopOffset;

    private int preStickyPosition = RecyclerView.NO_POSITION;
    private int currentStickyPosition = RecyclerView.NO_POSITION;

    public void attachRecycler(MultiRecycler recycler) {
        this.mRecycler = recycler;
        this.multiDataList = recycler.getDataSet();
        if (recycler.getRefresher() != null) {
            this.multiDataList.add(0, new RefresherMultiData(recycler.getRefresher()));
        }
        recycler.getRecyclerView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        recycler.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
        Log.d(TAG, "onLayoutChildren mTopMultiDataIndex=" + mTopMultiDataIndex
                + " mTopPosition=" + mTopPosition + " mTopOffset=" + mTopOffset + " isPreLayout=" + state.isPreLayout());

        if (multiDataList == null) {
            return;
        }

        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        int positionOffset = 0;

        Layouter last = null;
        for (int i = 0; i < multiDataList.size(); i++) {
            MultiData<?> multiData = multiDataList.get(i);
            Layouter layouter = multiData.getLayouter();
            layouter.setPositionOffset(positionOffset);
            layouter.setLayoutManager(this);
            if (i >= mTopMultiDataIndex) {
                if (last != null) {
                    layouter.setTop(last.getBottom());
                    layouter.layoutChildren(multiData, recycler, positionOffset);
                } else {
                    layouter.setTop(mTopOffset);
                    layouter.layoutChildren(multiData, recycler, mTopPosition + positionOffset);
                }
                last = layouter;
            }
            positionOffset += multiData.getCount();
        }

        saveState();

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
    public boolean canScrollVertically() {
        return mScrollDirection == DIRECTION_VERTICAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (multiDataList == null) {
            return 0;
        }

        int consumed = 0;

        Log.d(TAG, "scrollVerticalBy dy=" + dy);

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
                if (currentStickyPosition == getPosition(view)) {
                    i++;
                    continue;
                }
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
                if (currentStickyPosition == getPosition(view)) {
                    i--;
                    continue;
                }
                multiData = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
                i--;
            } while (multiData == null);
            Layouter layouter = multiData.getLayouter();
            consumed += layouter.fillVertical(view, dy - consumed, recycler, multiData);
            i = multiDataList.indexOf(multiData);
            Log.d(TAG, "scrollVerticalBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(view));
            while (consumed < dy && i < multiDataList.size() - 1) {
                int bottom = layouter.getBottom();
                multiData = multiDataList.get(++i);
                layouter = multiData.getLayouter();
                Log.d(TAG, "scrollVerticalBy layouter=" + layouter);
                layouter.setTop(bottom);
                consumed += layouter.fillVertical(null, dy - consumed, recycler, multiData);
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
            if (data != null) {
                Layouter layouter = data.getLayouter();
                if (data != last) {
                    layouter.offsetTopAndBottom(-consumed);
                    last = data;
                }
                if (layouter.canScrollVertically()) {
                    int position = getPosition(view);
                    boolean isStickyChild = data.isStickyItem(position - layouter.getPositionOffset());
                    if (isStickyChild) {
                        int decoratedTop = layouter.getDecoratedTop(view);
                        int decoratedBottom = layouter.getDecoratedBottom(view);
                        Log.d(TAG, "scrollVerticalBy decoratedTop=" + decoratedTop
                                + " decoratedBottom=" + decoratedBottom + " height=" + getDecoratedMeasuredHeight(view)
                                + " consumed=" + consumed + " dy=" + dy
                                + " currentStickyPosition=" + currentStickyPosition + " position=" + position + " iii=" + i + " last=" + (getChildCount() - 1));


//                        if (currentStickyPosition != position && )

                        if (dy > 0 && decoratedTop - consumed < 0) {
                            Log.d(TAG, "scrollVerticalBy decoratedTop 1");
                            if (currentStickyPosition != RecyclerView.NO_POSITION && currentStickyPosition != position) {
                                View v = getChildAt(getChildCount() - 1);
                                if (v != null && getPosition(v) == currentStickyPosition) {
                                    preStickyPosition = currentStickyPosition;
                                    recycleViews.add(v);
                                }
                            }
                            currentStickyPosition = position;
                            layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                            continue;
                        }
                        else if (dy < 0 && decoratedBottom - consumed > getDecoratedMeasuredHeight(view)) {
                            Log.d(TAG, "scrollVerticalBy decoratedTop 2");
                            if (i == getChildCount() - 1) {
                                if (currentStickyPosition != RecyclerView.NO_POSITION && currentStickyPosition != position) {
                                    View v = getChildAt(getChildCount() - 1);
                                    if (v != null && getPosition(v) == currentStickyPosition) {
                                        preStickyPosition = currentStickyPosition;
                                        recycleViews.add(v);
                                    }
                                }
                                currentStickyPosition = position;
                                layoutDecorated(view, 0, 0, getWidth(), getDecoratedMeasuredHeight(view));
                                continue;
                            } else {
                                if (currentStickyPosition == position) {
                                    currentStickyPosition = RecyclerView.NO_POSITION;
                                }

                            }
                        }
                        else if (currentStickyPosition == position) {
//                            currentStickyPosition = RecyclerView.NO_POSITION;
                            if (i != getChildCount() - 1 && decoratedTop - consumed >= 0) {
                                currentStickyPosition = preStickyPosition;
//                                currentStickyPosition = RecyclerView.NO_POSITION;
                            }
                            Log.d(TAG, "scrollVerticalBy decoratedTop 3 currentStickyPosition=" + currentStickyPosition);
                        }
                    }
                    if (layouter.getDecoratedBottom(view) - consumed < 0
                            || layouter.getDecoratedTop(view) - consumed > getHeight()) {
                        recycleViews.add(view);
                    } else {
                        view.offsetTopAndBottom(-consumed);
                    }
                }
            } else {
                if (getDecoratedBottom(view) - consumed < 0
                        || getDecoratedTop(view) - consumed > getHeight()) {
                    recycleViews.add(view);
                } else {
                    view.offsetTopAndBottom(-consumed);
                }
            }
        }
        recycleViews(recycler);

//        View child = getChildAt(0);
//        if (child != null) {
//            int pos = getPosition(child);
//            Log.d(TAG, "currentStickyPosition=" + currentStickyPosition + " pos=" + pos);
//            if (pos == currentStickyPosition) {
//                detachAndScrapView(child, recycler);
//                child = recycler.getViewForPosition(pos);
//                super.addView(child, getChildCount());
//                measureChild(child, 0, 0);
//                layoutDecorated(child, 0, 0, getWidth(), getDecoratedMeasuredHeight(child));
//            }
//        }

        if (currentStickyPosition != RecyclerView.NO_POSITION) {
            View child = findViewByPosition(currentStickyPosition);
            if (child == null) {
                child = recycler.getViewForPosition(currentStickyPosition);
            } else {
                detachAndScrapView(child, recycler);
            }
            super.addView(child, getChildCount());
            measureChild(child, 0, 0);
            layoutDecorated(child, 0, 0, getWidth(), getDecoratedMeasuredHeight(child));
        }

        Log.d(TAG, "scrollVerticallyBy getChildCount=" + getChildCount());
        return consumed;
    }

    @Override
    public void addView(View child) {
        if (currentStickyPosition == RecyclerView.NO_POSITION) {
            super.addView(child);
        } else {
            super.addView(child, getChildCount() - 1);
        }
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }

    private void recycleViews(RecyclerView.Recycler recycler) {
        for (View view : recycleViews) {
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
            mTopMultiDataIndex = multiDataList.indexOf(data);
            mTopPosition = getPosition(firstView) - layouter.getPositionOffset();
            mTopOffset = layouter.getDecoratedTop(firstView);
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
