package com.zpj.recyclerview.flinger;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;

public class HorizontalFlinger extends AbsFlinger {

    private boolean stopOverScroll = false;

    public HorizontalFlinger(AbsLayouter layouter, MultiData<?> multiData) {
        super(layouter, multiData);
    }

    @Override
    public boolean onComputeScroll(int dx, int dy) {
        if (dx == 0) {
            return true;
        }
        int consumed = mLayouter.scrollHorizontallyBy(dx, mMultiData);

        if (consumed != dx) {
            stopOverScroll = true;
            return false;
        }
        return true;
    }

    @Override
    public void onFinished() {
        super.onFinished();
        if (stopOverScroll) {
            stopOverScroll = false;
            mLayouter.onStopOverScroll();
        }
    }
}
