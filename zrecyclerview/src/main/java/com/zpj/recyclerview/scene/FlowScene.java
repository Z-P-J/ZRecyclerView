package com.zpj.recyclerview.scene;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.LayoutHelper;
import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.layouter.FlowLayouter;
import com.zpj.recyclerview.layouter.Layouter;

public class FlowScene extends MultiScene {

    private static final String TAG = "FlowLayouter";

    public FlowScene(MultiData<?> multiData) {
        this(multiData, new FlowLayouter());
    }

    public FlowScene(MultiData<?> multiData, FlowLayouter layouter) {
        super(multiData, layouter);
    }

    @Override
    public FlowLayouter getLayouter() {
        return (FlowLayouter) super.getLayouter();
    }

    @Override
    protected FlowLayoutHelper createLayoutHelper() {
        return new FlowLayoutHelper(this);
    }

    public static class FlowLayoutHelper extends LayoutHelper {

        protected final FlowScene mScene;

        public FlowLayoutHelper(FlowScene scene) {
            super(scene);
            mScene = scene;
        }

        @Override
        public int getDecoratedLeft(@NonNull View child) {
            return super.getDecoratedLeft(child) - mScene.getLayouter().getSpaceLeft();
        }

        @Override
        public int getDecoratedTop(@NonNull View child) {
            return super.getDecoratedTop(child) - mScene.getLayouter().getSpaceTop();
        }

        @Override
        public int getDecoratedRight(@NonNull View child) {
            return super.getDecoratedRight(child) + mScene.getLayouter().getSpaceRight();
        }

        @Override
        public int getDecoratedBottom(@NonNull View child) {
            return super.getDecoratedBottom(child) + mScene.getLayouter().getSpaceBottom();
        }

    }

}
