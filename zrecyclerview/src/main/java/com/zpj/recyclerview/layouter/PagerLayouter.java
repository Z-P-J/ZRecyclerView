package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.ViewPagerFlinger;

import java.util.ArrayList;
import java.util.List;

public class PagerLayouter extends AbsLayouter {

    private static final String TAG = "ViewPagerLayouter";

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mCurrentItem;

    private PageTransformer mTransformer;

    private int mScrollState = SCROLL_STATE_IDLE;
    private List<OnPageChangeListener> mOnPageChangeListeners;

    private int mOffscreenPageLimit = 2;

    protected boolean mIsInfinite = false;

    public void setIsInfinite(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
    }

    public boolean isInfinite() {
        return mIsInfinite;
    }

    @Override
    protected PagerLayoutHelper createLayoutHelper(BaseMultiLayoutManager manager) {
        return new PagerLayoutHelper(this, manager);
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
    public void saveState(int firstPosition, int firstOffset) {
        View current = findViewByPosition(getCurrentPosition());
        this.mFirstOffset = getDecoratedLeft(current);
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, int currentPosition) {
        super.layoutChildren(multiData, getCurrentPosition());
        if (mFlinger == null) {
            mFlinger = createFlinger(multiData);
        }
    }

    @Override
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                return fillVerticalBottom(multiData, getCurrentPosition(), dy, getTop());
            } else {
                // 如果占用两行则需要以下代码
                int anchorBottom = getDecoratedTop(anchorView);
                if (anchorBottom > getHeight()) {
                    if (anchorBottom - dy > getHeight()) {
                        return dy;
                    } else {
                        return anchorBottom - getHeight();
                    }
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                return fillVerticalTop(multiData, getCurrentPosition(), dy, getBottom());
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

        int min = currentPosition - mOffscreenPageLimit;
        int max = currentPosition + mOffscreenPageLimit;
        int first = mPositionOffset;
        int last = first + getCount(multiData) - 1;
        int index = 0;

        for (int i = min; i <= max; i++) {
            int position = i;
            if (position < first) {
                if (isInfinite()) {
                    position = last + position - first + 1;
                } else {
                    continue;
                }
            } else if (position > last) {
                if (isInfinite()) {
                    position = first + position - last - 1;
                } else {
                    continue;
                }
            }
            View view = addViewAndMeasure(position, index++, multiData);

            left = mFirstOffset + (i - currentPosition) * getWidth();
            right = left + getWidth();
            top = bottom - getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
        }
        mTop = top;
        // TODO 如果有多行，需要减去anchorTop
        return Math.min(-dy, - mTop);
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int dy, int anchorBottom) {

        int left = 0;
        int top = anchorBottom;
        int right = 0;
        int bottom = anchorBottom;

        int min = currentPosition - mOffscreenPageLimit;
        int max = currentPosition + mOffscreenPageLimit;
        int first = mPositionOffset;
        int last = first + getCount(multiData) - 1;

        for (int i = min; i <= max; i++) {
            int position = i;
            if (position < first) {
                if (isInfinite()) {
                    position = last + position - first + 1;
                } else {
                    continue;
                }
            } else if (position > last) {
                if (isInfinite()) {
                    position = first + position - last - 1;
                } else {
                    continue;
                }
            }
            Log.d(TAG, "fillVerticalBottom position=" + position);
            View view = addViewAndMeasure(position, multiData);

            left = mFirstOffset + (i - currentPosition) * getWidth();
            right = left + getWidth();
            bottom = top + getDecoratedMeasuredHeight(view);

            layoutDecorated(view, left, top, right, bottom);
        }
        mBottom = bottom;
        return Math.min(dy, - anchorBottom);
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        if (anchorView == null) {
            return 0;
        }
        int centerPosition = getCurrentPosition();

        int min = centerPosition - mOffscreenPageLimit;
        int max = centerPosition + mOffscreenPageLimit;
        int first = mPositionOffset;
        int last = first + getCount(multiData) - 1;

        int anchorPosition = getPosition(anchorView);

        if (anchorPosition < first || anchorPosition > last) {
            return 0;
        }
        int index = indexOfChild(anchorView);
        int center = indexOfChild(findViewByPosition(getCurrentPosition()));
        if (dx > 0) {
            // 从右往左滑动，从右边填充view

            int anchorRight = getDecoratedRight(anchorView);

            int currentPosition = anchorPosition + 1;
            int left = anchorRight;
            int top = getDecoratedTop(anchorView);
            int right = 0;
            int bottom = getDecoratedBottom(anchorView);

            int i = index + 1;
            while (i - center <= mOffscreenPageLimit) {
                int position = currentPosition++;
                if (position < first) {
                    if (isInfinite()) {
                        position = last + position - first + 1;
                    } else {
                        break;
                    }
                } else if (position > last) {
                    if (isInfinite()) {
                        position = first + position - last - 1;
                    } else {
                        break;
                    }
                }
                View view = addViewAndMeasure(position, i++, multiData);
                anchorView = view;

                right = left + getWidth();
                layoutDecorated(view, left, top, right, bottom);
                left = right;
            }

            anchorRight = getDecoratedRight(anchorView);
            if (anchorRight - dx > getWidth()) {
                return dx;
            } else {
                return anchorRight - getWidth();
            }
        } else {
            // 从左往右滑动，从左边填充view

            int anchorLeft = getDecoratedLeft(anchorView);

            int currentPosition = anchorPosition - 1;
            int left = 0;
            int top = getDecoratedTop(anchorView);
            int right = anchorLeft;
            int bottom = getDecoratedBottom(anchorView);

            int i = index - 1;
            while (center - i <= mOffscreenPageLimit) {
                int position = currentPosition--;
                if (position < first) {
                    if (isInfinite()) {
                        position = last + position - first + 1;
                    } else {
                        break;
                    }
                } else if (position > last) {
                    if (isInfinite()) {
                        position = first + position - last - 1;
                    } else {
                        break;
                    }
                }

                View view = addViewAndMeasure(position, index, multiData);
                anchorView = view;

                left = right - getWidth();
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

    @Override
    protected Flinger createFlinger(final MultiData<?> multiData) {
        return new ViewPagerFlinger(this, multiData) {
            @Override
            protected void onItemSelected(int item) {
                mCurrentItem = item;
                if (mOnPageChangeListeners != null) {
                    for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageSelected(mCurrentItem);
                        }
                    }
                }
            }

            @Override
            public void onFinished() {
//                Log.d(TAG, "onFinishedScroll mCurrentPosition=" + mCurrentItem + " mFirstPosition=" + mFirstPosition + " mFirstOffset=" + mFirstOffset);
                setScrollState(SCROLL_STATE_IDLE);
                if (mOnPageChangeListeners != null) {
                    for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageEnterEnd(mCurrentItem);
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        boolean result = super.onTouchDown(multiData, downX, downY);
        if (result && mScrollState == SCROLL_STATE_SETTLING) {
            setScrollState(SCROLL_STATE_DRAGGING);
        }
        return result;
    }

    @Override
    public boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY) {
        if (this.mScrollState != SCROLL_STATE_DRAGGING) {
            setScrollState(SCROLL_STATE_DRAGGING);
        }
        return super.onTouchMove(multiData, x, y, downX, downY);
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        setScrollState(SCROLL_STATE_SETTLING);
        return super.onTouchUp(multiData, velocityX, velocityY);
    }

    @Override
    protected void onDetached() {
        if (mFlinger != null) {
            mFlinger.stop();
        }
        if (mFirstOffset != 0) {
            mFirstOffset = 0;
        }
    }

    private void setScrollState(int newState) {
        if (this.mScrollState != newState) {
            this.mScrollState = newState;
//            if (this.mPageTransformer != null) {
//                this.enableLayers(newState != 0);
//            }
            this.dispatchOnScrollStateChanged(newState);
        }
    }

    private void dispatchOnScrollStateChanged(int state) {
        if (this.mOnPageChangeListeners != null) {
            for(int i = 0; i < this.mOnPageChangeListeners.size(); ++i) {
                OnPageChangeListener listener = this.mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        }
    }

    public void setOffscreenPageLimit(int limit) {
        if (limit < 1) {
            limit = 1;
        }
        if (mOffscreenPageLimit != limit) {
            mOffscreenPageLimit = limit;
            if (getLayoutHelper() != null) {
                getLayoutHelper().requestLayout();
            }
        }
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (smoothScroll) {
            if (mFlinger == null) {
                return;
            }
            int delta = mCurrentItem - item;
            int dx = delta * getWidth() - mFirstOffset;
            mFlinger.scroll(dx, 0);
        } else {
            if (mFlinger != null) {
                mFlinger.stop();
            }
            mCurrentItem = item;
            mFirstPosition = item;
            mFirstOffset = 0;
            if (getLayoutHelper() != null) {
                getLayoutHelper().requestLayout();
            }
        }
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public int getCurrentPosition() {
        return mCurrentItem + mPositionOffset;
    }

    public void setPageTransformer(PageTransformer transformer) {
        this.mTransformer = transformer;
    }

    public void addOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        if (this.mOnPageChangeListeners == null) {
            this.mOnPageChangeListeners = new ArrayList<>();
        }

        this.mOnPageChangeListeners.add(listener);
    }

    public void removeOnPageChangeListener(@NonNull OnPageChangeListener listener) {
        if (this.mOnPageChangeListeners != null) {
            this.mOnPageChangeListeners.remove(listener);
        }

    }

    public void clearOnPageChangeListeners() {
        if (this.mOnPageChangeListeners != null) {
            this.mOnPageChangeListeners.clear();
            this.mOnPageChangeListeners = null;
        }

    }

    public interface PageTransformer {
        void transformPage(@NonNull View page, float position);
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int position, float offset, @Px int offsetPixels);

        void onPageSelected(int position);

        void onPageEnterEnd(int position);

        void onPageScrollStateChanged(int state);
    }

    public static class PagerLayoutHelper extends LayoutHelper {

        protected final PagerLayouter mLayouter;

        public PagerLayoutHelper(PagerLayouter layouter, BaseMultiLayoutManager layoutManager) {
            super(layoutManager);
            mLayouter = layouter;
        }

        @Override
        public int getDecoratedMeasuredWidth(@NonNull View child) {
            return getWidth();
        }

        @Override
        public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
            super.layoutDecorated(child, left, top, right, bottom);
            if (mLayouter.mTransformer != null) {
                float position = (float) left / getWidth();
                mLayouter.mTransformer.transformPage(child, position);
            }
        }

        @Override
        public void scrapOrRecycleView(int index, View view) {
            offsetChildLeftAndRight(view, Integer.MAX_VALUE);
            super.scrapOrRecycleView(index, view);
        }

        @Override
        public boolean shouldRecycleChildViewHorizontally(View view, int consumed) {
            if (mLayouter.isInfinite()) {
                if (mLayouter.getCurrentPosition() == getPosition(view)) {
                    return false;
                }
                View current = findViewByPosition(mLayouter.getCurrentPosition());
                int index = indexOfChild(view);
                int center = indexOfChild(current);
                boolean result = Math.abs(index - center) > mLayouter.mOffscreenPageLimit;
                Log.d(TAG, "shouldRecycleChildViewHorizontally currentPosition=" + mLayouter.getCurrentPosition() + " position=" + getPosition(view) + " recycle=" + result);
                return result;
            } else {
                return Math.abs(mLayouter.getCurrentPosition() - getPosition(view)) > mLayouter.mOffscreenPageLimit;
            }
        }

        @Override
        public void offsetChildLeftAndRight(@NonNull View child, int offset) {
            super.offsetChildLeftAndRight(child, offset);
            if (mLayouter.mOnPageChangeListeners != null && offset != 0
                    && getPosition(child) - mLayouter.getPositionOffset() == mLayouter.getCurrentItem()) {
                float left = getDecoratedLeft(child);
                float childOffset = left / getWidth();
                if (left < 0) {
                    childOffset = Math.abs(childOffset);
                } else {
                    childOffset = 1 - childOffset;
                }
                int childOffsetPixels = (int) (childOffset * getWidth());
                for(int i = 0; i < mLayouter.mOnPageChangeListeners.size(); ++i) {
                    OnPageChangeListener listener = mLayouter.mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrolled(mLayouter.getCurrentItem(), childOffset, childOffsetPixels);
                    }
                }
            }

            if (mLayouter.mTransformer != null) {
                if (offset == Integer.MAX_VALUE) {
                    mLayouter.mTransformer.transformPage(child, 0);
                } else {
                    float left = getDecoratedLeft(child);
                    float position = left / getWidth();
                    mLayouter.mTransformer.transformPage(child, position);
                }
            }
        }

    }


}
