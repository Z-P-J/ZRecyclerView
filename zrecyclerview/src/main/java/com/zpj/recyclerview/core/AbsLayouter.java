package com.zpj.recyclerview.core;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.Layouter;

public abstract class AbsLayouter implements Layouter {

    private static final String TAG = "AbsLayouter";

    @Override
    public void layoutChildren(Scene scene, AnchorInfo anchorInfo) {
        int availableSpace = scene.getHeight() - scene.getTop();
        if (scene.getItemCount() == 0 || availableSpace < 0) {
            scene.setBottom(scene.getTop());
            return;
        }

        anchorInfo.y = scene.getTop();
        fillVerticalBottom(scene, anchorInfo, availableSpace);
    }

    @Override
    public int fillVertical(Scene scene, AnchorInfo anchorInfo, int dy) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorInfo.anchorView == null) {
                anchorInfo.position = 0;
                int result = fillVerticalBottom(scene, anchorInfo, dy);
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                if (anchorInfo.y - dy > scene.getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = anchorInfo.position;
                    if (anchorPosition == scene.getItemCount() - 1) {
                        return Math.max(0, anchorInfo.y - scene.getHeight());
                    }
                    int availableSpace = dy + scene.getHeight() - anchorInfo.y;

                    anchorInfo.position = anchorPosition + 1;
                    int result = fillVerticalBottom(scene, anchorInfo, availableSpace);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorInfo.anchorView == null) {
                anchorInfo.position = scene.getItemCount() - 1;
                int result = fillVerticalTop(scene, anchorInfo, -dy);
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorPosition = anchorInfo.position;
                if (anchorInfo.y - dy < 0) {
                    return -dy;
                } else {
                    if (anchorPosition == 0) {
                        return -anchorInfo.y;
                    }
                    int availableSpace = -dy + anchorInfo.y;
                    anchorInfo.position = anchorPosition - 1;
                    int result = fillVerticalTop(scene, anchorInfo, availableSpace);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
    }

    @Override
    public int fillHorizontal(Scene scene, AnchorInfo anchorInfo, int dx) {
        return 0;
    }

    protected abstract int fillVerticalTop(Scene scene, AnchorInfo anchor, int availableSpace);

    protected abstract int fillVerticalBottom(Scene scene, AnchorInfo anchor, int availableSpace);

}
