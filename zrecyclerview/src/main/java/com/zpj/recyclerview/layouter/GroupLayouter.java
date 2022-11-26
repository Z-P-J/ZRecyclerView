package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.view.View;

import com.zpj.recyclerview.MultiData;

import java.util.Arrays;
import java.util.List;

public class GroupLayouter extends AbsLayouter {

    @NonNull
    private final List<Layouter> mLayouters;

    public GroupLayouter(@NonNull List<Layouter> layouters) {
        mLayouters = layouters;
    }

    public GroupLayouter(@NonNull Layouter ... layouters) {
        mLayouters = Arrays.asList(layouters);
    }

    private Layouter mTouchLayouter;

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        for (Layouter layouter : mLayouters) {
            if (downY >= layouter.getTop() && downY <= layouter.getBottom()) {
                if (layouter.onTouchDown(multiData, downX, downY)) {
                    mTouchLayouter = layouter;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY) {
        return super.onTouchMove(multiData, x, y, downX, downY);
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        if (mTouchLayouter == null) {
            return false;
        }
        return mTouchLayouter.onTouchUp(multiData, velocityX, velocityY);
    }

    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        return 0;
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        return 0;
    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return 0;
    }
}
