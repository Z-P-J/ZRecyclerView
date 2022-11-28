package com.zpj.recyclerview.layouter;

import android.support.v7.widget.BaseMultiLayoutManager;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public interface Layouter {

    void setLayoutManager(BaseMultiLayoutManager manager);

    LayoutHelper getLayoutHelper();

    void setLeft(int left);

    void setTop(int top);

    void setRight(int right);

    void setBottom(int bottom);

    int getLeft();

    int getTop();

    int getRight();

    int getBottom();

    void offsetLeftAndRight(int offset);

    void offsetTopAndBottom(int offset);

    void setPositionOffset(int offset);

    int getPositionOffset();

    void setChildOffset(int offset);

    int getChildOffset();

    boolean canScrollHorizontally();

    boolean canScrollVertically();

    void layoutChildren(MultiData<?> multiData, int currentPosition);

    int fillVertical(View anchorView, int dy, MultiData<?> multiData);

    int fillHorizontal(View anchorView, int dx, MultiData<?> multiData);

    int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData);

    void saveState(int firstPosition, int firstOffset);

    boolean onTouchDown(MultiData<?> multiData, float downX, float downY, MotionEvent event);

    boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY, MotionEvent event);

    boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY, MotionEvent event);

    boolean isAttached();

}
