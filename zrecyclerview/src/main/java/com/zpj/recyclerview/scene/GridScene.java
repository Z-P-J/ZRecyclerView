package com.zpj.recyclerview.scene;

import android.support.annotation.IntRange;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.layouter.GridLayouter;

public class GridScene extends Scene<GridLayouter> {

    public GridScene(MultiData<?> multiData) {
        this(multiData, new GridLayouter());
    }

    public GridScene(MultiData<?> multiData, @IntRange(from = 1)int spanCount) {
        this(multiData, new GridLayouter(spanCount));
    }

    public GridScene(MultiData<?> multiData, GridLayouter layouter) {
        super(multiData, layouter);
    }

    public int getSpanCount() {
        return mLayouter.getSpanCount();
    }

    public void setSpanCount(@IntRange(from = 1) int spanCount) {
        if (getSpanCount() == spanCount) {
            return;
        }
        mLayouter.setSpanCount(spanCount);
        if (getLayoutHelper() != null) {
            getLayoutHelper().requestLayout();
        }
    }

}
