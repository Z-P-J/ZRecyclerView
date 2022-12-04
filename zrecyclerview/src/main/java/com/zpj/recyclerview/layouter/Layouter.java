package com.zpj.recyclerview.layouter;

import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.Scene;

public interface Layouter {

    boolean canScrollHorizontally();

    boolean canScrollVertically();

    void layoutChildren(Scene scene);

    int fillVertical(Scene scene, View anchorView, int dy);

    int fillHorizontal(Scene scene, View anchorView, int dx);

}
