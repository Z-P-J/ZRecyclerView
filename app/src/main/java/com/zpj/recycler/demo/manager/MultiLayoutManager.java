package com.zpj.recycler.demo.manager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.zpj.recycler.demo.layouter.Layouter;
import com.zpj.recyclerview.IMultiLayoutManager;
import com.zpj.recyclerview.MultiData;

import java.util.List;

public class MultiLayoutManager extends RecyclerView.LayoutManager implements IMultiLayoutManager {

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
//        Layouter last = null;
//        for (Layouter layouter : list) {
//            layouter.setLayoutManager(this);
//            if (last != null) {
//                layouter.setTop(last.getBottom());
//            }
//            layouter.onLayoutChildren(recycler, state);
//            last = layouter;
//        }

        int positionOffset = 0;
//        int childOffset = 0;
        Layouter last = null;
        for (MultiData<?> multiData : multiDataList) {
            if (multiData instanceof LayouterMultiData) {
                Layouter layouter = ((LayouterMultiData) multiData).getLayouter();
                layouter.setLayoutManager(this);
                if (last != null) {
                    layouter.setTop(last.getBottom());
                }
                layouter.setPositionOffset(positionOffset);
                layouter.onLayoutChildren(multiData, recycler, state);

                positionOffset += multiData.getCount();
                last = layouter;
            }
        }

    }

//    @Override
//    public boolean canScrollHorizontally() {
//        return scrollDirection == DIRECTION_HORIZONTAL;
//    }
//
//    @Override
//    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        for (Layouter layouter : list) {
//            layouter.setLayoutManager(this);
//            if (layouter.canScrollHorizontally()) {
//                layouter.scrollHorizontallyBy(dx, recycler, state);
//            }
//        }
//        return dx;
//    }
//
//    @Override
//    public boolean canScrollVertically() {
//        return scrollDirection == DIRECTION_VERTICAL;
//    }
//
//    @Override
//    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        for (Layouter layouter : list) {
//            layouter.setLayoutManager(this);
//            if (layouter.canScrollVertically()) {
//                layouter.scrollVerticallyBy(dy, recycler, state);
//            }
//        }
//        return dy;
//    }

    private float downX = -1;
    private float downY = -1;
    private int scrollDirection = DIRECTION_NONE; // > 0 h;  < 0, v

    private static final int DIRECTION_NONE = 0;
    private static final int DIRECTION_HORIZONTAL = 1;
    private static final int DIRECTION_VERTICAL = 2;

    @Override
    public void onTouch(MotionEvent event) {
        int action = event.getAction();
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
