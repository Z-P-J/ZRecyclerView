package com.zpj.recycler.demo.manager;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.zpj.recycler.demo.layouter.GridLayouter;
import com.zpj.recycler.demo.layouter.HorizontalLayouter;
import com.zpj.recycler.demo.layouter.Layouter;
import com.zpj.recycler.demo.layouter.VerticalLayouter;
import com.zpj.recyclerview.IMultiLayoutManager;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.ArrayList;
import java.util.List;

public class MultiLayoutManager extends RecyclerView.LayoutManager implements IMultiLayoutManager {

    private final List<Layouter> list = new ArrayList<>();

    public MultiLayoutManager() {
        list.add(new HorizontalLayouter());
        list.add(new VerticalLayouter());
        list.add(new GridLayouter());
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        Layouter last = null;
        for (Layouter layouter : list) {
            layouter.setLayoutManager(this);
            if (last != null) {
                layouter.setTop(last.getBottom());
            }
            layouter.onLayoutChildren(recycler, state);
            last = layouter;
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return scrollDirection == DIRECTION_HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (Layouter layouter : list) {
            layouter.setLayoutManager(this);
            if (layouter.canScrollHorizontally()) {
                layouter.scrollHorizontallyBy(dx, recycler, state);
            }
        }
        return dx;
    }

    @Override
    public boolean canScrollVertically() {
        return scrollDirection == DIRECTION_VERTICAL;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        for (Layouter layouter : list) {
            layouter.setLayoutManager(this);
            if (layouter.canScrollVertically()) {
                layouter.scrollVerticallyBy(dy, recycler, state);
            }
        }
        return dy;
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
