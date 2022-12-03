package com.zpj.recyclerview.layouter;

import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.scene.PagerScene;

public class PagerLayouter extends AbsLayouter<PagerScene> {

    private static final String TAG = "ViewPagerLayouter";

    protected boolean mIsInfinite;
    
    public PagerLayouter() {
        this(false);
    }

    public PagerLayouter(boolean isInfinite) {
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
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(multiData, mScene.getCurrentPosition(), dy, mScene.getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getDecoratedTop(anchorView);
                if (anchorBottom > getRecyclerHeight()) {
                    if (anchorBottom - dy > getRecyclerHeight()) {
                        return dy;
                    } else {
                        return anchorBottom - getRecyclerHeight();
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(multiData, mScene.getCurrentPosition(), dy, mScene.getBottom());
            } else {
                // 如果占用两行则需要以下代码
                int anchorTop = getDecoratedTop(anchorView);
                if (anchorTop < 0) {
                    if (anchorTop - dy < 0) {
                        return -dy;
                    } else {
                        return -anchorTop;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int dy, int anchorTop) {

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int min = currentPosition - mScene.getOffscreenPageLimit();
        int max = currentPosition + mScene.getOffscreenPageLimit();
        int first = mScene.getPositionOffset();
        int last = first + mScene.getItemCount() - 1;
        int index = 0;

        for (int i = min; i <= max; i++) {
            int position = i;
            if (position < first) {
                if (mIsInfinite) {
                    position = last + position - first + 1;
                } else {
                    continue;
                }
            } else if (position > last) {
                if (mIsInfinite) {
                    position = first + position - last - 1;
                } else {
                    continue;
                }
            }
            View view = mScene.addViewAndMeasure(position, index++);

            left = mScene.mAnchorInfo.x + (i - currentPosition) * getRecyclerWidth();
            right = left + getRecyclerWidth();
            top = bottom - getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
        }
        mScene.setTop(top);
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, -top);
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {

        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int min = currentPosition - mScene.getOffscreenPageLimit();
        int max = currentPosition + mScene.getOffscreenPageLimit();
        int first = mScene.getPositionOffset();
        int last = first + mScene.getItemCount() - 1;

        for (int i = min; i <= max; i++) {
            int position = i;
            if (position < first) {
                if (mIsInfinite) {
                    position = last + position - first + 1;
                } else {
                    continue;
                }
            } else if (position > last) {
                if (mIsInfinite) {
                    position = first + position - last - 1;
                } else {
                    continue;
                }
            }
            Log.d(TAG, "fillVerticalBottom position=" + position);
            View view = mScene.addViewAndMeasure(position);

            left = mScene.mAnchorInfo.x + (i - currentPosition) * getRecyclerWidth();
            right = left + getRecyclerWidth();
            bottom = top + getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
        }
        mScene.setBottom(bottom);
        return Math.min(dy, - anchorBottom);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        if (anchorView == null) {
            return 0;
        }
        int centerPosition = mScene.getCurrentPosition();

        int min = centerPosition - mScene.getOffscreenPageLimit();
        int max = centerPosition + mScene.getOffscreenPageLimit();
        int first = mScene.getPositionOffset();
        int last = first + mScene.getItemCount() - 1;

        int anchorPosition = getPosition(anchorView);

        if (anchorPosition < first || anchorPosition > last) {
            return 0;
        }
//        int anchorIndex = mScene.indexOfChild(anchorView);
//        int centerIndex = mScene.indexOfChild(mScene.findViewByPosition(centerPosition));

        int anchorIndex = -1;
        int centerIndex = -1;

        for (int i = 0; i < mScene.getChildCount(); i++) {
            View child = mScene.getChildAt(i);
            if (child == null) {
                continue;
            } else if (child == anchorView) {
                anchorIndex = i;
            } else if (centerPosition == getPosition(child)) {
                centerIndex = i;
            }
            if (anchorIndex >= 0 && centerIndex >= 0) {
                break;
            }
        }

        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getDecoratedRight(anchorView);

            int currentPosition = anchorPosition + 1;
            int left = anchorRight;
            int top = getDecoratedTop(anchorView);
            int right = 0;
            int bottom = getDecoratedBottom(anchorView);

            int i = anchorIndex + 1;
            while (i - centerIndex <= mScene.getOffscreenPageLimit()) {
                int position = currentPosition++;
                if (position < first) {
                    if (mIsInfinite) {
                        position = last + position - first + 1;
                    } else {
                        break;
                    }
                } else if (position > last) {
                    if (mIsInfinite) {
                        position = first + position - last - 1;
                    } else {
                        break;
                    }
                }
                View view = mScene.addViewAndMeasure(position, i++);
                anchorView = view;

                right = left + getRecyclerWidth();
                layoutDecorated(view, left, top, right, bottom);
                left = right;
            }

            anchorRight = getDecoratedRight(anchorView);
            if (anchorRight - dx > getRecyclerWidth()) {
                return dx;
            } else {
                return anchorRight - getRecyclerWidth();
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getDecoratedLeft(anchorView);

            int currentPosition = anchorPosition - 1;
            int left = 0;
            int top = getDecoratedTop(anchorView);
            int right = anchorLeft;
            int bottom = getDecoratedBottom(anchorView);

            int i = anchorIndex - 1;
            while (centerIndex - i <= mScene.getOffscreenPageLimit()) {
                int position = currentPosition--;
                if (position < first) {
                    if (mIsInfinite) {
                        position = last + position - first + 1;
                    } else {
                        break;
                    }
                } else if (position > last) {
                    if (mIsInfinite) {
                        position = first + position - last - 1;
                    } else {
                        break;
                    }
                }

                View view = mScene.addViewAndMeasure(position, anchorIndex);
                anchorView = view;

                left = right - getRecyclerWidth();
                layoutDecorated(view, left, top, right, bottom);
                right = left;

                i--;
            }

            anchorLeft = getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {
                return -anchorLeft;
            }
        }
    }

}
