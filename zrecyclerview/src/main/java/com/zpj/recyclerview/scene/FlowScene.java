package com.zpj.recyclerview.scene;

import android.support.annotation.NonNull;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.LayoutHelper;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.layouter.FlowLayouter;

public class FlowScene extends Scene<FlowLayouter> {

    private static final String TAG = "FlowLayouter";

    public FlowScene(MultiData<?> multiData) {
        this(multiData, 0);
    }

    public FlowScene(MultiData<?> multiData, int space) {
        this(multiData, space, space);
    }

    public FlowScene(MultiData<?> multiData, int spaceHorizontal, int spaceVertical) {
        this(multiData, spaceHorizontal, spaceVertical, spaceHorizontal, spaceVertical);
    }

    public FlowScene(MultiData<?> multiData, int spaceLeft, int spaceTop, int spaceRight, int spaceBottom) {
        this(multiData, new FlowLayouter(spaceLeft, spaceTop, spaceRight, spaceBottom));
    }

    public FlowScene(MultiData<?> multiData, FlowLayouter layouter) {
        super(multiData, layouter);
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
