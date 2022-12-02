


package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;

public class HorizontalLayouter extends AbsLayouter {

    private static final String TAG = "HorizontalLayouter";

    protected boolean mIsInfinite = true;

    public HorizontalLayouter() {
        this(false);
    }

    public HorizontalLayouter(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
    }

    public void setIsInfinite(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
    }

    public boolean isInfinite() {
        return mIsInfinite;
    }

//    @Override
//    public void saveState(int firstPosition, int firstOffset) {
//        if (isInfinite()) {
//            this.mFirstPosition = Math.max(0, firstPosition - mPositionOffset);
//            this.mFirstOffset = Math.min(0, firstOffset);
//        } else {
//            Log.d(TAG, "saveState firstPosition=" + firstPosition + " firstOffset=" + firstOffset);
//            super.saveState(firstPosition, firstOffset);
//        }
//    }

    @Override
    public void saveState(View firstChild) {
        super.saveState(firstChild);
        if (isInfinite()) {
            mAnchorInfo.x = Math.min(0, mAnchorInfo.x);
        }
    }

    @Override
    public void onDetached() {
        if (isInfinite()) {
            if (mFlinger != null) {
                mFlinger.stop();
            }
        } else {
            super.onDetached();
        }
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
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(multiData, mAnchorInfo.position + mPositionOffset, dy, mScene.getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getDecoratedBottom(anchorView);
                if (anchorBottom - dy > getHeight()) {
                    return dy;
                } else {
                    return anchorBottom - getHeight();
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(multiData, mAnchorInfo.position + mPositionOffset, dy, mScene.getBottom());
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

        int left = mAnchorInfo.x;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int availableSpace = getWidth() - getPaddingRight() - left;

        int i = 0;
        while (availableSpace > 0) {
            if (currentPosition >= mPositionOffset + getCount(multiData)) {
                if (isInfinite()) {
                    currentPosition = mPositionOffset;
                } else {
                    break;
                }
            }

            View view = addViewAndMeasure(currentPosition++, i++, multiData);

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

        int left = mAnchorInfo.x;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int availableSpace = getWidth() - getPaddingRight() - left;

        while (availableSpace > 0) {
            if (currentPosition >= mPositionOffset + getCount(multiData)) {
                if (isInfinite()) {
                    currentPosition = mPositionOffset;
                } else {
                    break;
                }
            }
            View view = addViewAndMeasure(currentPosition++, multiData);
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
        int anchorPosition = getPosition(anchorView);
        if (anchorPosition < mPositionOffset || anchorPosition >= mPositionOffset + getCount(multiData)) {
            return 0;
        }
        int index = indexOfChild(anchorView);
        Log.d(TAG, "fillHorizontal anchorPosition=" + anchorPosition + " index=" + index);
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getDecoratedRight(anchorView);
            if (anchorRight - dx > getWidth()) {
                return dx;
            } else {

                if (!isInfinite() && anchorPosition == mPositionOffset + getCount(multiData) - 1) {
                    return anchorRight - getWidth();
                }

                int availableSpace = dx;
                int currentPosition = anchorPosition + 1;
                int left = anchorRight;
                int top = getDecoratedTop(anchorView);
                int right = 0;
                int bottom = getDecoratedBottom(anchorView);

                int i = index + 1;
                while (availableSpace > 0) {
                    if (currentPosition >= mPositionOffset + getCount(multiData)) {
                        if (isInfinite()) {
                            currentPosition = mPositionOffset;
                        } else {
                            break;
                        }
                    }

                    View view = addViewAndMeasure(currentPosition++, i++, multiData);

                    int measuredWidth = getDecoratedMeasuredWidth(view);
                    availableSpace -= measuredWidth;

                    right = left + measuredWidth;
                    layoutDecorated(view, left, top, right, bottom);
                    left = right;
                }
                return Math.min(dx, dx - availableSpace + (anchorRight - getWidth()));
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {

                if (!isInfinite() && anchorPosition == mPositionOffset) {
                    return -anchorLeft;
                }

                int availableSpace = -dx;
                int currentPosition = anchorPosition - 1;
                int left = 0;
                int top = getDecoratedTop(anchorView);
                int right = anchorLeft;
                int bottom = getDecoratedBottom(anchorView);

                while (availableSpace > 0) {
                    if (currentPosition < mPositionOffset) {
                        if (isInfinite()) {
                            currentPosition = mPositionOffset + getCount(multiData) - 1;
                        } else {
                            break;
                        }
                    }
                    View view = addViewAndMeasure(currentPosition--, index, multiData);

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

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY, MotionEvent event) {
        if (isInfinite()) {
            if (canScrollHorizontally() && mFlinger != null) {
                mFlinger.fling(velocityX, velocityY);
            }
            return false;
        }
        return super.onTouchUp(multiData, velocityX, velocityY, event);
    }
}
