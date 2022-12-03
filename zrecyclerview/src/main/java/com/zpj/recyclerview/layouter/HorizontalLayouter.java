


package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.scene.HorizontalScene;

public class HorizontalLayouter extends AbsLayouter<HorizontalScene> {

    private static final String TAG = "HorizontalLayouter";

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(multiData,
                        mScene.mAnchorInfo.position + mScene.getPositionOffset(),
                        dy, mScene.getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getDecoratedBottom(anchorView);
                if (anchorBottom - dy > getRecyclerHeight()) {
                    return dy;
                } else {
                    return anchorBottom - getRecyclerHeight();
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(multiData,
                        mScene.mAnchorInfo.position + mScene.getPositionOffset(),
                        dy, mScene.getBottom());
            } else {
                // 如果占用两行则需要以下代码
                int anchorTop = getDecoratedTop(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {
                    return -anchorTop;
                }
            }
        }
    }

    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {

        int left = mScene.mAnchorInfo.x;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int availableSpace = getRecyclerWidth() - mScene.getPaddingRight() - left;

        int i = 0;
        int positionOffset = mScene.getPositionOffset();
        int itemCount = mScene.getItemCount();
        while (availableSpace > 0) {
            if (currentPosition >= positionOffset + itemCount) {
                if (mScene.isInfinite()) {
                    currentPosition = positionOffset;
                } else {
                    break;
                }
            }

            View view = mScene.addViewAndMeasure(currentPosition++, i++);

            int measuredWidth = getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            if (top == bottom) {
                top = bottom - getDecoratedMeasuredHeight(view);
            }

            layoutDecorated(view, left, top, right, bottom);
            left = right;
        }
        mScene.setTop(top);
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, -top);
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {

        int left = mScene.mAnchorInfo.x;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int availableSpace = getRecyclerWidth() - mScene.getPaddingRight() - left;

        int positionOffset = mScene.getPositionOffset();
        int itemCount = mScene.getItemCount();
        while (availableSpace > 0) {
            if (currentPosition >= positionOffset + itemCount) {
                if (mScene.isInfinite()) {
                    currentPosition = positionOffset;
                } else {
                    break;
                }
            }
            View view = mScene.addViewAndMeasure(currentPosition++);
            int measuredWidth = getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            bottom = top + getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
            left += measuredWidth;
        }
        mScene.setBottom(bottom);
        Log.d(TAG, "fillVerticalBottom dy=" + dy + " anchorBottom=" + anchorBottom + " bottom=" + bottom);
        return Math.min(dy, bottom - anchorBottom);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        if (anchorView == null) {
            return 0;
        }
        int positionOffset = mScene.getPositionOffset();
        int itemCount = mScene.getItemCount();
        int anchorPosition = getPosition(anchorView);
        if (anchorPosition < positionOffset || anchorPosition >= positionOffset + itemCount) {
            return 0;
        }
        int index = mScene.indexOfChild(anchorView);
        Log.d(TAG, "fillHorizontal anchorPosition=" + anchorPosition + " index=" + index);
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getDecoratedRight(anchorView);
            if (anchorRight - dx > getRecyclerWidth()) {
                return dx;
            } else {

                if (!mScene.isInfinite() && anchorPosition == positionOffset + itemCount - 1) {
                    return anchorRight - getRecyclerWidth();
                }

                int availableSpace = dx;
                int currentPosition = anchorPosition + 1;
                int left = anchorRight;
                int top = getDecoratedTop(anchorView);
                int right = 0;
                int bottom = getDecoratedBottom(anchorView);

                int i = index + 1;
                while (availableSpace > 0) {
                    if (currentPosition >= positionOffset + itemCount) {
                        if (mScene.isInfinite()) {
                            currentPosition = positionOffset;
                        } else {
                            break;
                        }
                    }

                    View view = mScene.addViewAndMeasure(currentPosition++, i++);

                    int measuredWidth = getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    right = left + measuredWidth;
                    layoutDecorated(view, left, top, right, bottom);
                    left = right;
                }
                return Math.min(dx, dx - availableSpace + (anchorRight - getRecyclerWidth()));
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {

                if (!mScene.isInfinite() && anchorPosition == positionOffset) {
                    return -anchorLeft;
                }

                int availableSpace = -dx;
                int currentPosition = anchorPosition - 1;
                int left = 0;
                int top = getDecoratedTop(anchorView);
                int right = anchorLeft;
                int bottom = getDecoratedBottom(anchorView);

                while (availableSpace > 0) {
                    if (currentPosition < positionOffset) {
                        if (mScene.isInfinite()) {
                            currentPosition = positionOffset + itemCount - 1;
                        } else {
                            break;
                        }
                    }
                    View view = mScene.addViewAndMeasure(currentPosition--, index);

                    int measuredWidth = getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    left = right - measuredWidth;
                    layoutDecorated(view, left, top, right, bottom);
                    right = left;
                }
                return Math.min(-dx, -dx - availableSpace - anchorLeft);
            }
        }
    }

}
