package com.zpj.recyclerview.flinger;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.core.LayoutHelper;
import com.zpj.recyclerview.layouter.PagerLayouter;
import com.zpj.recyclerview.scene.PagerScene;

public abstract class PagerFlinger extends HorizontalFlinger {

    public PagerFlinger(@NonNull PagerScene scene) {
        super(scene);
    }

    @Override
    public void fling(float velocityX, float velocityY) {
        if (mScene instanceof PagerScene) {
            stop();

            PagerScene pagerScene = (PagerScene) mScene;
            LayoutHelper helper = pagerScene.getLayoutHelper();
            int tempCurrentItem = pagerScene.getCurrentItem();
            View current = helper.findViewByPosition(pagerScene.getCurrentPosition());
            int position = helper.getPosition(current);
            int item = position - pagerScene.getPositionOffset();

            int left = helper.getDecoratedLeft(current);
            int right = helper.getDecoratedRight(current);

            this.mScroller.fling(0, 0, (int) velocityX, 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

            int finalX = this.mScroller.getFinalX();

            int dx;
            int currentItem;
            if (velocityX > 0) {
                if (left + finalX > pagerScene.getWidth()) {
                    dx = pagerScene.getWidth() - left;
                    currentItem = item - 1;
                } else if (left < pagerScene.getWidth() / 2) {
                    dx = -left;
                    currentItem = item;
                } else {
                    dx = pagerScene.getWidth() - left;
                    currentItem = item - 1;
                }
                if (currentItem < 0) {
                    if (pagerScene.isInfinite()) {
                        currentItem = mScene.getItemCount() - 1;
                    } else {
                        dx = -left;
                        currentItem = 0;
                    }
                }
            } else {
                if (right + finalX < 0) {
                    dx = -right;
                    currentItem = item + 1;
                } else if (right > pagerScene.getWidth() / 2) {
                    dx = pagerScene.getWidth() - right;
                    currentItem = item;
                } else {
                    dx = -right;
                    currentItem = item - 1;
                }
                int itemCount = mScene.getItemCount();
                if (currentItem >= itemCount) {
                    if (pagerScene.isInfinite()) {
                        currentItem = 0;
                    } else {
                        dx = pagerScene.getWidth() - right;
                        currentItem = itemCount - 1;
                    }
                }
            }
            if (tempCurrentItem != currentItem) {
                onItemSelected(currentItem);
            }

            int count = 0;
            for (int i = 0; i < pagerScene.getChildCount(); i++) {
                View child = pagerScene.getChildAt(i);

                if (pagerScene.getMultiScene(child) == mScene) {
                    count++;
                }
            }
            Log.d("ViewPagerFlinger", "fling count=" + count);

            super.scroll(dx, 0, 500);
        }
    }

    protected abstract void onItemSelected(int item);

}
