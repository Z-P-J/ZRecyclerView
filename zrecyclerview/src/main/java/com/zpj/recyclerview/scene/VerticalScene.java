package com.zpj.recyclerview.scene;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.layouter.VerticalLayouter;

public class VerticalScene extends MultiScene {

    public VerticalScene(MultiData<?> multiData) {
        this(multiData, new VerticalLayouter());
    }

    public VerticalScene(MultiData<?> multiData, VerticalLayouter layouter) {
        super(multiData, layouter);
    }

}
