package com.zpj.recycler.demo.manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recycler.demo.layouter.Layouter;
import com.zpj.recyclerview.IMultiLayoutManager;
import com.zpj.recyclerview.MultiData;

import java.util.ArrayList;
import java.util.List;

public class MultiLayoutManager extends RecyclerView.LayoutManager implements IMultiLayoutManager {

    private static final String TAG = "MultiLayoutManager";

    private final List<MultiData<?>> multiDataList;

    public MultiLayoutManager(List<MultiData<?>> multiDataList) {
        this.multiDataList = multiDataList;
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
        detachAndScrapAttachedViews(recycler);

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
//                    layouter.setTop(last.getBottom());
//                }
//                layouter.setPositionOffset(positionOffset);
//                int count = layouter.onLayoutChildren(multiData, recycler, state);
//                last = layouter;
//                positionOffset += multiData.getCount();
//                childOffset += count;
//            }
//        }


        int positionOffset = 0;
        int childOffset = 0;
        Layouter last = null;
        for (MultiData<?> multiData : multiDataList) {
            if (multiData instanceof LayouterMultiData) {
                Layouter layouter = ((LayouterMultiData) multiData).getLayouter();
                layouter.setPositionOffset(positionOffset);
                layouter.setChildOffset(childOffset);
                layouter.setLayoutManager(this);
                if (last != null) {
                    Log.d(TAG, "onLayoutChildren bottom=" + last.getBottom());
                    layouter.setTop(last.getBottom());
                } else {
                    Log.d(TAG, "onLayoutChildren bottom=00");
                    layouter.setTop(0);
                }
                layouter.setPositionOffset(positionOffset);
                int count = layouter.onLayoutChildren(multiData, recycler, state);
                last = layouter;
                positionOffset += multiData.getCount();
                childOffset += count;
            }
        }

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            if (getDecoratedBottom(view) < 0 || getDecoratedTop(view) > getHeight() ) {
                Log.d(TAG, "onLayoutChildren position=" + getPosition(view));
                recycleViews.add(view);
            }
        }
        for (View view : recycleViews) {
            detachAndScrapView(view, recycler);
        }
        recycleViews.clear();

    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollDirection == DIRECTION_HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }
            MultiData<?> data = ((MultiLayoutParams) view.getLayoutParams()).getMultiData();
            if (data instanceof LayouterMultiData) {
                Layouter layouter = ((LayouterMultiData) data).getLayouter();
                if (layouter.canScrollHorizontally()) {
                    view.offsetLeftAndRight(-dx);
                }
            }
        }
        return dx;
    }

    @Override
    public boolean canScrollVertically() {
        return scrollDirection == DIRECTION_VERTICAL;
    }

    private final List<View> recycleViews = new ArrayList<>();

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
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
            Layouter layouter = ((LayouterMultiData) multiData).getLayouter();
            s.setMultiData(multiData);
            Log.d(TAG, "scrollVerticallyBy firstChildPosition=" + getPosition(firstChild));
            consumed -= layouter.fillVertical(firstChild, dy - consumed, recycler, s);
            int i = multiDataList.indexOf(multiData);
            while (consumed > dy && i > 0) {
                int top = layouter.getTop();
                multiData = multiDataList.get(--i);
                layouter = ((LayouterMultiData) multiData).getLayouter();
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
            Layouter layouter = ((LayouterMultiData) multiData).getLayouter();
            s.setMultiData(multiData);
            consumed += layouter.fillVertical(lastChild, dy - consumed, recycler, s);
            int i = multiDataList.indexOf(multiData);
            Log.d(TAG, "scrollVerticalBy consumedLast=" + consumed + " i=" + i + " lastPosition=" + getPosition(lastChild));
            while (consumed < dy && i < multiDataList.size() - 1) {
                int bottom = layouter.getBottom();
                multiData = multiDataList.get(++i);
                layouter = ((LayouterMultiData) multiData).getLayouter();
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
            Layouter layouter = ((LayouterMultiData) data).getLayouter();
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
//                if (getDecoratedBottom(view) < 0 || getDecoratedTop(view) > getHeight() ) {
//                    recycleViews.add(view);
//                }
//                if (consumed == 0) {
//                    if (getDecoratedBottom(view) < 0 || getDecoratedTop(view) > getHeight() ) {
//                        recycleViews.add(view);
//                    }
//                } else if (consumed > 0) {
//                    // 从下往上滑动
//                    if (getDecoratedTop(view) > getHeight()) {
//                        recycleViews.add(view);
//                    }
//                } else {
//                    // 从上往下滑动
//                    if (getDecoratedBottom(view) < 0) {
//                        recycleViews.add(view);
//                    }
//                }
            }
        }
        for (View view : recycleViews) {
            detachAndScrapView(view, recycler);
        }
        recycleViews.clear();
        Log.d(TAG, "scrollVerticallyBy getChildCount=" + getChildCount());
        return consumed;
    }

    private float downX = -1;
    private float downY = -1;
    private int scrollDirection = DIRECTION_NONE; // > 0 h;  < 0, v

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    @Override
    public void onTouch(MotionEvent event) {
        int action = event.getAction();
//        Log.d(TAG, "onTouch action=" + MotionEvent.actionToString(action));
        if (MotionEvent.ACTION_DOWN == action) {
            scrollDirection = DIRECTION_NONE;
            downX = event.getRawX();
            downY = event.getRawY();
        } else if (MotionEvent.ACTION_MOVE == action) {
            if (scrollDirection == 0) {
                float deltaX = event.getRawX() - downX;
                float deltaY = event.getRawY() - downY;
                float radio = Math.abs(deltaX / deltaY);
                if (radio == 1f) {
                    return;
                }
                if (radio > 1f) {
                    scrollDirection = DIRECTION_HORIZONTAL;
                } else {
                    scrollDirection = DIRECTION_VERTICAL;
                }
            }
        } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {

        }
    }
}
