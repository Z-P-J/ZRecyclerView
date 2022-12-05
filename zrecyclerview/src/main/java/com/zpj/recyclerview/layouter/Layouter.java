package com.zpj.recyclerview.layouter;

import com.zpj.recyclerview.core.AnchorInfo;
import com.zpj.recyclerview.core.Scene;

public interface Layouter {

    boolean canScrollHorizontally();

    boolean canScrollVertically();

    void layoutChildren(Scene scene, AnchorInfo anchorInfo);

    int fillVertical(Scene scene, AnchorInfo anchorInfo, int dy);

    int fillHorizontal(Scene scene, AnchorInfo anchorInfo, int dx);

}
