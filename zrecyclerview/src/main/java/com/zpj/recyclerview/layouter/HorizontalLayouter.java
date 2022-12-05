


package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.AnchorInfo;
import com.zpj.recyclerview.core.Scene;

public class HorizontalLayouter extends AbsLayouter {

    private static final String TAG = "HorizontalLayouter";

    protected boolean mIsInfinite;

    public HorizontalLayouter() {
        this(false);
    }

    public HorizontalLayouter(boolean isInfinite) {
        mIsInfinite = isInfinite;
    }

    public void setIsInfinite(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
    }

    public boolean isInfinite() {
        return mIsInfinite;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int fillVertical(Scene scene, AnchorInfo anchorInfo, int dy) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorInfo.anchorView == null) {
                return fillVerticalBottom(scene, anchorInfo, dy);
            } else {
                // 如果占用两行则需要以下代码
                if (anchorInfo.y - dy > scene.getHeight()) {
                    return dy;
                } else {
                    return anchorInfo.y - scene.getHeight();
                }
            }
        } else {
            // 从上往下滑动
            if (anchorInfo.anchorView == null) {
                return fillVerticalTop(scene, anchorInfo, dy);
            } else {
                // 如果占用两行则需要以下代码
                if (anchorInfo.y - dy < 0) {
                    return -dy;
                } else {
                    return -anchorInfo.y;
                }
            }
        }
    }

    @Override
    protected int fillVerticalTop(Scene scene, AnchorInfo anchorInfo, int dy) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int left = anchorInfo.x;
        int top = anchorInfo.y;
        int right = 0;
        int bottom = top;

        int availableSpace = scene.getWidth() - scene.getPaddingRight() - left;

        int i = 0;
        int itemCount = scene.getItemCount();
        while (availableSpace > 0) {
            if (currentPosition >= positionOffset + itemCount) {
                if (mIsInfinite) {
                    currentPosition = positionOffset;
                } else {
                    break;
                }
            }

            View view = scene.addViewAndMeasure(currentPosition++, i++);

            int measuredWidth = scene.getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            if (top == bottom) {
                top = bottom - scene.getDecoratedMeasuredHeight(view);
            }

            scene.layoutDecorated(view, left, top, right, bottom);
            left = right;
        }
        scene.setTop(top);
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, -top);
    }

    @Override
    protected int fillVerticalBottom(Scene scene, AnchorInfo anchorInfo, int dy) {
        int positionOffset = scene.getPositionOffset();
        int currentPosition = anchorInfo.position + positionOffset;
        int anchorBottom = anchorInfo.y;
        int left = anchorInfo.x;
        int top = anchorBottom;
        int right = 0;
        int bottom = top;

        int availableSpace = scene.getWidth() - scene.getPaddingRight() - left;


        int itemCount = scene.getItemCount();
        while (availableSpace > 0) {
            if (currentPosition >= positionOffset + itemCount) {
                if (mIsInfinite) {
                    currentPosition = positionOffset;
                } else {
                    break;
                }
            }
            View view = scene.addViewAndMeasure(currentPosition++);
            int measuredWidth = scene.getDecoratedMeasuredWidth(view);
            availableSpace -= measuredWidth;

            right = left + measuredWidth;
            bottom = top + scene.getDecoratedMeasuredHeight(view);

            scene.layoutDecorated(view, left, top, right, bottom);
            left += measuredWidth;
        }
        scene.setBottom(bottom);
        Log.d(TAG, "fillVerticalBottom dy=" + dy + " anchorBottom=" + anchorBottom + " bottom=" + bottom);
        return Math.min(dy, bottom - anchorBottom);
    }

    @Override
    public int fillHorizontal(Scene scene, AnchorInfo anchorInfo, int dx) {
        View anchorView = anchorInfo.anchorView;
        if (anchorView == null) {
            return 0;
        }
        int positionOffset = scene.getPositionOffset();
        int itemCount = scene.getItemCount();
        int anchorPosition = scene.getPosition(anchorView);
        if (anchorPosition < positionOffset || anchorPosition >= positionOffset + itemCount) {
            return 0;
        }
        int index = scene.indexOfChild(anchorView);
        Log.d(TAG, "fillHorizontal anchorPosition=" + anchorPosition + " index=" + index);
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = scene.getDecoratedRight(anchorView);
            if (anchorRight - dx > scene.getWidth()) {
                return dx;
            } else {

                if (!mIsInfinite && anchorPosition == positionOffset + itemCount - 1) {
                    return anchorRight - scene.getWidth();
                }

                int availableSpace = dx;
                int currentPosition = anchorPosition + 1;
                int left = anchorRight;
                int top = scene.getDecoratedTop(anchorView);
                int right = 0;
                int bottom = scene.getDecoratedBottom(anchorView);

                int i = index + 1;
                while (availableSpace > 0) {
                    if (currentPosition >= positionOffset + itemCount) {
                        if (mIsInfinite) {
                            currentPosition = positionOffset;
                        } else {
                            break;
                        }
                    }

                    View view = scene.addViewAndMeasure(currentPosition++, i++);

                    int measuredWidth = scene.getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    right = left + measuredWidth;
                    scene.layoutDecorated(view, left, top, right, bottom);
                    left = right;
                }
                return Math.min(dx, dx - availableSpace + (anchorRight - scene.getWidth()));
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = scene.getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {

                if (!mIsInfinite && anchorPosition == positionOffset) {
                    return -anchorLeft;
                }

                int availableSpace = -dx;
                int currentPosition = anchorPosition - 1;
                int left = 0;
                int top = scene.getDecoratedTop(anchorView);
                int right = anchorLeft;
                int bottom = scene.getDecoratedBottom(anchorView);

                while (availableSpace > 0) {
                    if (currentPosition < positionOffset) {
                        if (mIsInfinite) {
                            currentPosition = positionOffset + itemCount - 1;
                        } else {
                            break;
                        }
                    }
                    View view = scene.addViewAndMeasure(currentPosition--, index);

                    int measuredWidth = scene.getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    left = right - measuredWidth;
                    scene.layoutDecorated(view, left, top, right, bottom);
                    right = left;
                }
                return Math.min(-dx, -dx - availableSpace - anchorLeft);
            }
        }
    }

}
