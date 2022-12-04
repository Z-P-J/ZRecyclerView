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
    public void layoutChildren(Scene scene) {
        int availableSpace = scene.getHeight() - scene.getTop();
        if (scene.getItemCount() == 0 || availableSpace < 0) {
            scene.setBottom(scene.getTop());
            return;
        }
        fillVerticalBottom(scene, scene.mAnchorInfo.position + scene.getPositionOffset(), availableSpace, scene.getTop());
    }

    @Override
    public int fillVertical(Scene scene, View anchorView, int dy) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int result = fillVerticalBottom(scene, scene.getPositionOffset(), dy, scene.getTop());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                int anchorBottom = scene.getDecoratedBottom(anchorView);
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + scene.getHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > scene.getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = scene.getPosition(anchorView);
                    if (anchorPosition == scene.getPositionOffset() + scene.getItemCount() - 1) {
                        return Math.max(0, anchorBottom - scene.getHeight());
                    }
                    int availableSpace = dy + scene.getHeight() - anchorBottom;
                    int result = fillVerticalBottom(scene, anchorPosition + 1, availableSpace, anchorBottom);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int result = fillVerticalTop(scene, scene.getPositionOffset() + scene.getItemCount() - 1,
                        -dy, scene.getBottom());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorTop = scene.getDecoratedTop(anchorView);
                int anchorPosition = scene.getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == scene.getPositionOffset()) {
                        return -anchorTop;
                    }
                    int availableSpace = -dy + anchorTop;
                    int result = fillVerticalTop(scene, anchorPosition - 1, availableSpace, anchorTop);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
    }

    @Override
    public int fillHorizontal(Scene scene, View anchorView, int dx) {
        return 0;
    }

    protected abstract int fillVerticalTop(Scene scene, int currentPosition, int availableSpace, int anchorTop);

    protected abstract int fillVerticalBottom(Scene scene, int currentPosition, int availableSpace, int anchorBottom);

}
