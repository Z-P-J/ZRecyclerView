package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.AnchorInfo;
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
    protected int fillVerticalTop(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y;
        int right = scene.getWidth();
        int bottom = top;

        while (availableSpace > 0 && currentPosition >= positionOffset) {
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
    protected int fillVerticalBottom(Scene scene, AnchorInfo anchorInfo, int availableSpace) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = 0;
        int top = anchorInfo.y;
        int right = scene.getWidth();
        int bottom = top;

        while (availableSpace > 0 && currentPosition < positionOffset + scene.getItemCount()) {
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
