package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.Scene;

public class VerticalLayouter extends AbsLayouter {

    private static final String TAG = "VerticalLayouter";

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    protected int fillVerticalTop(Scene scene, int currentPosition, int availableSpace, int anchorTop) {
        int left = 0;
        int top = anchorTop;
        int right = scene.getWidth();
        int bottom = anchorTop;

        while (availableSpace > 0 && currentPosition >= scene.getPositionOffset()) {
            Log.e(TAG, "scrollVerticallyBy decoratedTop currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = scene.addViewAndMeasure(currentPosition--, 0);

            int measuredHeight= scene.getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            top = bottom - measuredHeight;

            scene.layoutDecorated(view, left, top, right, bottom);

            bottom = top;
        }
        scene.setTop(top);
        return availableSpace;
    }

    @Override
    protected int fillVerticalBottom(Scene scene, int currentPosition, int availableSpace, int anchorBottom) {
        int left = 0;
        int top = anchorBottom;
        int right = scene.getWidth();
        int bottom = anchorBottom;
        Log.e(TAG, "fillVerticalBottom anchorBottom=" + anchorBottom + " height=" + scene.getHeight());

        while (availableSpace > 0 && currentPosition < scene.getPositionOffset() + scene.getItemCount()) {
            Log.e(TAG, "fillVerticalBottom currentPosition=" + currentPosition + " availableSpace=" + availableSpace);
            View view = scene.addViewAndMeasure(currentPosition++);
            int measuredHeight= scene.getDecoratedMeasuredHeight(view);
            availableSpace -= measuredHeight;

            bottom = top + measuredHeight;

            scene.layoutDecorated(view, left, top, right, bottom);

            top = bottom;
        }
        scene.setBottom(bottom);
        return availableSpace;
    }
    
}
