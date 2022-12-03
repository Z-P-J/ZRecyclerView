package com.zpj.recyclerview.scene;

import android.support.annotation.IntRange;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.MultiLayoutParams;
import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StaggeredGridScene extends MultiScene {

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
        ((StaggeredGridLayouter) mLayouter).saveState();
    }

}
