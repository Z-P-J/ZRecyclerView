package com.zpj.recyclerview.core;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.EasyViewHolder;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class StickyItemManager {

    private static final String TAG = "StickyItemManager";

    private final Deque<StickyInfo> stickyInfoStack = new ArrayDeque<>();

    private StickyInfo stickyInfo;
    private int currentStickyOffset = 0;

    private boolean mEnable;
    private boolean handleSticky;

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public boolean hasStickyItem() {
        return mEnable && stickyInfo != null;
    }

    public boolean isStickyPosition(int position) {
        return mEnable && stickyInfo != null && position == stickyInfo.position;
    }

    public void setHandleSticky(boolean handleSticky) {
        this.handleSticky = handleSticky;
    }

    public void initStickyItems(List<Scene> sceneList, int topPosition) {
        Log.d(TAG, "stickyInfo111=" + stickyInfo + " topPosition=" + topPosition);
        if (stickyInfo == null || topPosition > stickyInfo.position) {
            initStickyInfoStack(sceneList, topPosition);
            stickyInfo = stickyInfoStack.poll();
        } else if (topPosition < stickyInfo.position) {
            if (stickyInfoStack.isEmpty()) {
                initStickyInfoStack(sceneList, topPosition);
            }
            while (stickyInfo != null && topPosition < stickyInfo.position) {
                stickyInfo = stickyInfoStack.poll();
            }
        }

        Log.d(TAG, "stickyInfo111222=" + stickyInfo + " stickyInfoStack.size=" + stickyInfoStack.size());

        if (stickyInfo != null) {
            Scene scene = stickyInfo.scene;
            View child = scene.findViewByPosition(stickyInfo.position);
            if (stickyInfoStack.isEmpty() && child != null && scene.getDecoratedTop(child) == 0) {
                stickyInfo = null;
            } else {
                if (child == null) {
                    child = scene.getViewForPosition(stickyInfo.position);
                } else {
                    scene.getLayoutHelper().detachAndScrapView(child);
                }
                SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                params.setScene(stickyInfo.scene);
                scene.addView(child, scene.getChildCount());
                scene.measureChild(child, 0, 0);
                scene.layoutDecorated(child, 0, currentStickyOffset, scene.getWidth(),
                        currentStickyOffset + scene.getDecoratedMeasuredHeight(child));

                scene.getMultiData().onItemSticky(new EasyViewHolder(child),
                        stickyInfo.position - scene.getPositionOffset(), true);
            }
        }
    }

    private void initStickyInfoStack(List<Scene> sceneList, int position) {
        stickyInfoStack.clear();
        int offset = 0;

        for (Scene scene : sceneList) {
            int count = scene.getItemCount();
            for (int pos = offset; pos < offset + count; pos++) {
                if (scene.getMultiData().isStickyPosition(pos - offset)) {
                    Log.d(TAG, "stickyInfo111 pos=" + pos);
                    stickyInfoStack.push(new StickyInfo(pos, scene));
                }
                if (pos >= position) {
                    return;
                }
            }
            offset += count;
        }
    }

    public void onScrolled() {
        if (stickyInfo != null) {
            Scene scene = stickyInfo.scene;
            View child = scene.findViewByPosition(stickyInfo.position);
            if (child == null) {
                child = scene.getViewForPosition(stickyInfo.position);
            } else {
                scene.getLayoutHelper().detachAndScrapView(child);
            }
            SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
            params.setScene(stickyInfo.scene);
            scene.addView(child, scene.getChildCount());
            scene.measureChild(child, 0, 0);
            Log.e(TAG, "scrollVerticallyBy currentStickyOffset=" + currentStickyOffset);
            scene.layoutDecorated(child, 0, currentStickyOffset, scene.getWidth(),
                    currentStickyOffset + scene.getDecoratedMeasuredHeight(child));

            stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(child),
                    stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                    true);
        }
    }

    public boolean handleSticky(Scene scene, View view, int i, int consumed) {
        int position = scene.getPosition(view);

        boolean isStickyPosition = scene.getMultiData().isStickyPosition(position - scene.getPositionOffset());
        if (!isStickyPosition) {
            return false;
        }

        Log.e(TAG, "scrollVerticallyBy");
        Log.e(TAG, "scrollVerticallyBy ================================start");
        Log.d(TAG, "scrollVerticallyBy i=" + i + " stickyPosition=" + position);
        int decoratedTop = scene.getDecoratedTop(view);
        int decoratedBottom = scene.getDecoratedBottom(view);
//                        Log.d(TAG, "scrollVerticallyBy decoratedTop=" + decoratedTop
//                                + " decoratedBottom=" + decoratedBottom + " height=" + getDecoratedMeasuredHeight(view)
//                                + " consumed=" + consumed + " dy=" + dy
//                                + " currentStickyPosition=" + currentStickyPosition + " position=" + position + " iii=" + i + " last=" + (getChildCount() - 1));

        Log.d(TAG, "scrollVerticallyBy isStickyChild i=" + i + " position=" + position + " stickyInfo=" + stickyInfo);

        if (stickyInfo != null && stickyInfo.position == position) {
            // 已是当前吸顶view

//                            Log.d(TAG, "scrollVerticallyBy decoratedTop=" + decoratedTop + " childPos=" + (getChildCount() - 1) + " i=" + i + " consumed=" + consumed);
            if (i != scene.getChildCount() - 1 && decoratedTop - consumed >= 0) { //  && decoratedTop - consumed >= 0
//                            currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight(view) - consumed;
                if (stickyInfo != null) {
                    stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(view),
                            stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                            false);
                }
                if (stickyInfoStack.isEmpty()) {
                    stickyInfo = null;
                    currentStickyOffset = 0;
                } else {
                    stickyInfo = stickyInfoStack.pop();
                }
                handleSticky = false;
                if (stickyInfo != null) {
                    View child = scene.findViewByPosition(stickyInfo.position);
                    if (child == null) {
                        child = scene.getViewForPosition(stickyInfo.position);
                        scene.measureChild(child, 0, 0);
                    }
                    SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                    params.setScene(stickyInfo.scene);

                    currentStickyOffset = decoratedTop - scene.getDecoratedMeasuredHeight(child) - consumed;
                }
                Log.d(TAG, "scrollVerticallyBy 吸顶==>下一个吸顶 stickyInfo=" + stickyInfo + " currentStickyOffset=" + currentStickyOffset);
            } else {
                return true;
            }
        } else if (handleSticky) {
            handleSticky = false;
            // 不是当前吸顶view
            if (stickyInfo == null) {
                // 无吸顶
                // dy > 0 &&
                if (decoratedTop - consumed < 0) {
                    currentStickyOffset = 0;
                    stickyInfo = new StickyInfo(position, scene);
                    scene.layoutDecorated(view, 0, 0, scene.getWidth(), scene.getDecoratedMeasuredHeight(view));
                    Log.d(TAG, "scrollVerticallyBy 无吸顶===》当前吸顶 currentStickyPosition==position : " + position);
                    return true;
                }
                Log.d(TAG, "scrollVerticallyBy 无吸顶");
            } else {
                // 说明已经有sticky吸顶item
                View child = scene.findViewByPosition(stickyInfo.position);
                if (child == null) {
                    child = scene.getViewForPosition(stickyInfo.position);
                    scene.measureChild(child, 0, 0);
                }
                SceneLayoutParams params = (SceneLayoutParams) child.getLayoutParams();
                params.setScene(stickyInfo.scene);
                Log.d(TAG, "scrollVerticallyBy 有吸顶 stickyInfo=" + stickyInfo + " position=" + position);

                if (position > stickyInfo.position) {
                    Log.d(TAG, "scrollVerticallyBy 有吸顶 decoratedTop - consumed="
                            + (decoratedTop - consumed) + " childHeight=" + scene.getDecoratedMeasuredHeight(view)
                            + " pos=" + position + " stickyPos=" + stickyInfo.position);
                    if (decoratedTop - consumed <= 0) {
                        stickyInfoStack.push(stickyInfo);
                        scene.getLayoutHelper().addViewToRecycler(child);
                        if (stickyInfo != null) {
                            stickyInfo.scene.getMultiData().onItemSticky(new EasyViewHolder(child), stickyInfo.position - stickyInfo.scene.getPositionOffset(), false);
                        }
                        stickyInfo = new StickyInfo(position, scene);
                        currentStickyOffset = 0;
                        scene.layoutDecorated(view, 0, 0, scene.getWidth(), scene.getDecoratedMeasuredHeight(view));
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue + position=" + position);
                        return true;
                    } else if (decoratedTop - consumed < scene.getDecoratedMeasuredHeight(child)) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        int top = scene.getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = scene.getDecoratedTop(child);
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue currentStickyOffset=" + currentStickyOffset + " position=" + position);
                    } else {
                        currentStickyOffset = 0;
                    }
                } else {
                    if (decoratedBottom - consumed >= 0) {
                        child.offsetTopAndBottom(-consumed);
//                                    currentStickyOffset = Math.min(layouter.getDecoratedTop(child), getDecoratedMeasuredHeight(child));
                        int top = scene.getDecoratedTop(child);
                        if (top > 0) {
                            child.offsetTopAndBottom(-top);
                        }
                        currentStickyOffset = scene.getDecoratedTop(child);

                    } else if (decoratedBottom - consumed > scene.getDecoratedMeasuredHeight(child)) {
//                                    currentStickyOffset = decoratedTop - getDecoratedMeasuredHeight() - consumed;
                        if (stickyInfo != null) {
                            stickyInfo.scene.getMultiData()
                                    .onItemSticky(new EasyViewHolder(child),
                                            stickyInfo.position - stickyInfo.scene.getPositionOffset(),
                                            false);
                        }
                        if (stickyInfoStack.isEmpty()) {
                            stickyInfo = null;
                            currentStickyOffset = 0;
                        } else {
                            stickyInfo = stickyInfoStack.pop();
                        }

                        if (stickyInfo != null) {
                            child = scene.findViewByPosition(stickyInfo.position);
                            if (child == null) {
                                child = scene.getViewForPosition(stickyInfo.position);
                                scene.measureChild(child, 0, 0);
                            }
                            params = (SceneLayoutParams) child.getLayoutParams();
                            params.setScene(stickyInfo.scene);

                            currentStickyOffset = Math.min(0, decoratedTop - scene.getDecoratedMeasuredHeight(child) - consumed);
                        }

                        scene.layoutDecorated(view, 0, 0, scene.getWidth(), scene.getDecoratedMeasuredHeight(view));
                        Log.d(TAG, "scrollVerticallyBy 更改吸顶 child continue");
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private static class StickyInfo {
        int position;
        Scene scene;

        public StickyInfo(int position, Scene scene) {
            this.position = position;
            this.scene = scene;
        }

        @Override
        public String toString() {
            return "StickyInfo{" +
                    "position=" + position +
                    ", scene=" + scene +
                    '}';
        }
    }
}
