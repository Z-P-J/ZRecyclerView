package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.ViewPagerFlinger;
import com.zpj.recyclerview.manager.MultiLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerLayouter extends InfiniteHorizontalLayouter {

    private static final String TAG = "ViewPagerLayouter";

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mCurrentPosition;

    private PageTransformer transformer;

    private int mScrollState = SCROLL_STATE_IDLE;
    private List<OnPageChangeListener> mOnPageChangeListeners;

    @Override
    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getWidth();
    }

    @Override
    public void offsetChildLeftAndRight(@NonNull View child, int offset) {
        super.offsetChildLeftAndRight(child, offset);

        if (mOnPageChangeListeners != null && offset != 0 && getPosition(child) - mPositionOffset == mCurrentPosition) {
            float left = getDecoratedLeft(child);
            float childOffset = left / getWidth();
            if (left < 0) {
                childOffset = Math.abs(childOffset);
            } else {
                childOffset = 1 - childOffset;
            }
            int childOffsetPixels = (int) (childOffset * getWidth());
            for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                if (listener != null) {
                    listener.onPageScrolled(mCurrentPosition, childOffset, childOffsetPixels);
                }
            }
        }

        if (transformer != null) {
            if (offset == 0) {
                transformer.transformPage(child, 0);
            } else {
                float left = getDecoratedLeft(child);
                float position = left / getWidth();
                transformer.transformPage(child, position);
            }
        }
    }

    @Override
    public void scrapOrRecycleView(MultiLayoutManager manager, int index, View view) {
        offsetChildLeftAndRight(view, 0);
        super.scrapOrRecycleView(manager, index, view);
    }

    @Override
    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        super.layoutDecorated(child, left, top, right, bottom);
        if (transformer != null) {
            float position = (float) left / getWidth();
            transformer.transformPage(child, position);
        }
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        super.layoutChildren(multiData, recycler, currentPosition);
        if (mFlinger == null) {
            mFlinger = createFlinger(multiData);
        }
    }

    @Override
    protected Flinger createFlinger(final MultiData<?> multiData) {
        return new ViewPagerFlinger(this, multiData) {
            @Override
            public void onFinished() {
                mCurrentPosition = mFirstOffset < 0 ? mFirstPosition + 1 : mFirstPosition;
                if (mCurrentPosition >= multiData.getCount()) {
                    mCurrentPosition = 0;
                }
                Log.d(TAG, "onFinishedScroll mCurrentPosition=" + mCurrentPosition + " mFirstPosition=" + mFirstPosition + " mFirstOffset=" + mFirstOffset);
                setScrollState(SCROLL_STATE_IDLE);
                if (mOnPageChangeListeners != null) {
                    for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageSelected(mCurrentPosition);
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
        super.onDetached();
        if (mFirstOffset < 0) {
            mFirstOffset = 0;
            mFirstPosition++;
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
        // TODO
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (smoothScroll) {
            if (mFlinger == null) {
                return;
            }
            int delta = mFirstPosition - item;
            int dx = delta * getWidth() - mFirstOffset;
            mFlinger.scroll(dx, 0);
        } else {
            if (mFlinger != null) {
                mFlinger.stop();
            }
            mCurrentPosition = item;
            mFirstPosition = item;
            mFirstOffset = 0;
            getLayoutManager().requestLayout();
        }
    }

    public int getCurrentItem() {
        return mCurrentPosition;
    }

    public void setPageTransformer(PageTransformer transformer) {
        this.transformer = transformer;
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

        void onPageScrollStateChanged(int state);
    }

}
