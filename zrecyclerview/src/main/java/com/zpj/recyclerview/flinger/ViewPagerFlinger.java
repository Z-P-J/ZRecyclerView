package com.zpj.recyclerview.flinger;

import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.layouter.AbsLayouter;

public class ViewPagerFlinger extends HorizontalFlinger {


    public ViewPagerFlinger(AbsLayouter layouter, MultiData<?> multiData) {
        super(layouter, multiData);
    }

    @Override
    public void fling(float velocityX, float velocityY) {
        if (mMultiData == null) {
            return;
        }
        this.mScroller.fling(0, 0, (int) velocityX, 0,
                Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);

        int finalX = this.mScroller.getFinalX();

        int dx = 0;
        for (int i = 0; i < mLayouter.getChildCount(); i++) {
            View view = mLayouter.getChildAt(i);
            if (mLayouter.getMultiData(view) == mMultiData) {
                int left = mLayouter.getDecoratedLeft(view);
                int right = mLayouter.getDecoratedRight(view);
                if (velocityX > 0) {
                    if (left + finalX > 0) {
                        dx = -left;
                    } else {
                        if (right < mLayouter.getWidth() / 2) {
                            dx = -right;
                        } else {
                            dx = -left;
                        }
                    }
                } else {
                    if (right + finalX < 0) {
                        dx = -right;
                    } else if (right > mLayouter.getWidth() / 2) {
                        dx = mLayouter.getWidth() - right;
                    } else {
                        dx = -right;
                    }
                }
                break;
            }
        }
        scroll(dx, 0, 500);
    }

}
