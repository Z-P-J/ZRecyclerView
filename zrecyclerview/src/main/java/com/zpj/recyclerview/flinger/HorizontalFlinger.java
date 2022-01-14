package com.zpj.recyclerview.flinger;

import android.support.v7.widget.RecyclerViewHelper;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;

public class HorizontalFlinger extends AbsFlinger {

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
            stop();
            mLayouter.onStopOverScroll(mMultiData);
            return false;
        }
        return true;
    }
}
