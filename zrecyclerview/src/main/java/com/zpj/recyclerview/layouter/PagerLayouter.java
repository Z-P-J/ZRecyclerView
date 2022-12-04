package com.zpj.recyclerview.layouter;

import android.support.annotation.IntRange;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.scene.PagerScene;

public class PagerLayouter extends AbsLayouter {

    private static final String TAG = "ViewPagerLayouter";

    protected boolean mIsInfinite;
    private int mOffscreenPageLimit = 2;
    
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

    public void setOffscreenPageLimit(@IntRange(from = 1) int limit) {
        mOffscreenPageLimit = limit;
    }

    public int getOffscreenPageLimit() {
        return mOffscreenPageLimit;
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
    public int fillVertical(Scene scene, View anchorView, int dy) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int currentPosition = scene.mAnchorInfo.position + scene.getPositionOffset();
                return fillVerticalBottom(scene, currentPosition, dy, scene.getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = scene.getDecoratedTop(anchorView);
                if (anchorBottom > scene.getHeight()) {
                    if (anchorBottom - dy > scene.getHeight()) {
                        return dy;
                    } else {
                        return anchorBottom - scene.getHeight();
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int currentPosition = scene.mAnchorInfo.position + scene.getPositionOffset();
                return fillVerticalTop(scene, currentPosition, dy, scene.getBottom());
            } else {
                // 如果占用两行则需要以下代码
                int anchorTop = scene.getDecoratedTop(anchorView);
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
    protected int fillVerticalTop(Scene scene, int currentPosition, int dy, int anchorTop) {

        int left = 0;
        int top = anchorTop;
        int right = 0;
        int bottom = anchorTop;

        int min = currentPosition - mOffscreenPageLimit;
        int max = currentPosition + mOffscreenPageLimit;
        int first = scene.getPositionOffset();
        int last = first + scene.getItemCount() - 1;
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
            View view = scene.addViewAndMeasure(position, index++);

            left = scene.mAnchorInfo.x + (i - currentPosition) * scene.getWidth();
            right = left + scene.getWidth();
            top = bottom - scene.getDecoratedMeasuredHeight(view);

            scene.layoutDecorated(view, left, top, right, bottom);
        }
        scene.setTop(top);
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, -top);
    }

    @Override
    protected int fillVerticalBottom(Scene scene, int currentPosition, int dy, int anchorBottom) {

        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int min = currentPosition - mOffscreenPageLimit;
        int max = currentPosition + mOffscreenPageLimit;
        int first = scene.getPositionOffset();
        int last = first + scene.getItemCount() - 1;

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
            View view = scene.addViewAndMeasure(position);

            left = scene.mAnchorInfo.x + (i - currentPosition) * scene.getWidth();
            right = left + scene.getWidth();
            bottom = top + scene.getDecoratedMeasuredHeight(view);

            scene.layoutDecorated(view, left, top, right, bottom);
        }
        scene.setBottom(bottom);
        return Math.min(dy, - anchorBottom);
    }

    @Override
    public int fillHorizontal(Scene scene, View anchorView, int dx) {
        if (anchorView == null) {
            return 0;
        }
        int centerPosition = scene.mAnchorInfo.position + scene.getPositionOffset();

        int min = centerPosition - mOffscreenPageLimit;
        int max = centerPosition + mOffscreenPageLimit;
        int first = scene.getPositionOffset();
        int last = first + scene.getItemCount() - 1;

        int anchorPosition = scene.getPosition(anchorView);

        if (anchorPosition < first || anchorPosition > last) {
            return 0;
        }
//        int anchorIndex = scene.indexOfChild(anchorView);
//        int centerIndex = scene.indexOfChild(scene.findViewByPosition(centerPosition));

        int anchorIndex = -1;
        int centerIndex = -1;

        for (int i = 0; i < scene.getChildCount(); i++) {
            View child = scene.getChildAt(i);
            if (child == null) {
                continue;
            } else if (child == anchorView) {
                anchorIndex = i;
            } else if (centerPosition == scene.getPosition(child)) {
                centerIndex = i;
            }
            if (anchorIndex >= 0 && centerIndex >= 0) {
                break;
            }
        }

        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = scene.getDecoratedRight(anchorView);

            int currentPosition = anchorPosition + 1;
            int left = anchorRight;
            int top = scene.getDecoratedTop(anchorView);
            int right = 0;
            int bottom = scene.getDecoratedBottom(anchorView);

            int i = anchorIndex + 1;
            while (i - centerIndex <= mOffscreenPageLimit) {
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
                View view = scene.addViewAndMeasure(position, i++);
                anchorView = view;

                right = left + scene.getWidth();
                scene.layoutDecorated(view, left, top, right, bottom);
                left = right;
            }

            anchorRight = scene.getDecoratedRight(anchorView);
            if (anchorRight - dx > scene.getWidth()) {
                return dx;
            } else {
                return anchorRight - scene.getWidth();
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = scene.getDecoratedLeft(anchorView);

            int currentPosition = anchorPosition - 1;
            int left = 0;
            int top = scene.getDecoratedTop(anchorView);
            int right = anchorLeft;
            int bottom = scene.getDecoratedBottom(anchorView);

            int i = anchorIndex - 1;
            while (centerIndex - i <= mOffscreenPageLimit) {
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

                View view = scene.addViewAndMeasure(position, anchorIndex);
                anchorView = view;

                left = right - scene.getWidth();
                scene.layoutDecorated(view, left, top, right, bottom);
                right = left;

                i--;
            }

            anchorLeft = scene.getDecoratedLeft(anchorView);
            if (anchorLeft - dx < 0) {
                return -dx;
            } else {
                return -anchorLeft;
            }
        }
    }

}
