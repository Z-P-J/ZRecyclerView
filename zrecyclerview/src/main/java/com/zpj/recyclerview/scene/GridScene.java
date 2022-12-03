package com.zpj.recyclerview.scene;

import android.support.annotation.IntRange;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.layouter.GridLayouter;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;

public class GridScene extends MultiScene {

    private int mSpanCount;

    public GridScene(MultiData<?> multiData) {
        this(multiData, 2);
    }

    public GridScene(MultiData<?> multiData, @IntRange(from = 1)int spanCount) {
        this(multiData, new GridLayouter(), spanCount);
    }

    public GridScene(MultiData<?> multiData, GridLayouter layouter, @IntRange(from = 1)int spanCount) {
        super(multiData, layouter);
        mSpanCount = spanCount;
    }

    public int getSpanCount() {
        return mSpanCount;
    }

    public void setSpanCount(@IntRange(from = 1) int spanCount) {
        if (mSpanCount == spanCount) {
            return;
        }
        mSpanCount = spanCount;
        if (getLayoutHelper() != null) {
            getLayoutHelper().requestLayout();
        }
    }

}
