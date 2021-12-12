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

public class ViewPagerLayouter extends InfiniteHorizontalLayouter {

    private static final String TAG = "ViewPagerLayouter";

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private int mCurrentPosition;

    private PageTransformer transformer;

    @Override
    public void offsetChildLeftAndRight(@NonNull View child, int offset) {
        super.offsetChildLeftAndRight(child, offset);
        if (transformer != null) {
            if (offset == 0) {
                transformer.transformPage(child, 0);
            } else {
                float left = getDecoratedLeft(child);
                float position = left / getWidth();
                Log.d(TAG, "offsetChildLeftAndRight pos=" + (getPosition(child) - mPositionOffset) + " offset=" + offset + " position=" + position);
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
                if (listener != null) {
                    listener.onPageSelected(mCurrentPosition);
                }
            }
        };
    }

    @Override
    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return getWidth();
    }

    @Override
    protected void onDetached() {
        super.onDetached();
        if (mFirstOffset < 0) {
            mFirstOffset = 0;
            mFirstPosition++;
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

    private OnPageChangeListener listener;
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        this.listener = listener;
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
