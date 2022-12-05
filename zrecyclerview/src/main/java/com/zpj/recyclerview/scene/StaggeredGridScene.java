package com.zpj.recyclerview.scene;

import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsScene;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;

public class StaggeredGridScene extends AbsScene<StaggeredGridLayouter> {

    public StaggeredGridScene(MultiData<?> multiData) {
        this(multiData, new StaggeredGridLayouter(2));
    }

    public StaggeredGridScene(MultiData<?> multiData, int spanCount) {
        this(multiData, new StaggeredGridLayouter(spanCount));
    }

    public StaggeredGridScene(MultiData<?> multiData, StaggeredGridLayouter layouter) {
        super(multiData, layouter);
    }

    @Override
    public void saveState(View firstChild) {
        mLayouter.saveState(this);
    }

}
