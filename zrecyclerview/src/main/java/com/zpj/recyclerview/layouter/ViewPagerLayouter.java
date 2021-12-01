package com.zpj.recyclerview.layouter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.ViewPagerFlinger;

public class ViewPagerLayouter extends InfiniteHorizontalLayouter {

    private static final String TAG = "ViewPagerLayouter";

    private int mCurrentPosition;

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

    public void setPageTransformer(ViewPager.PageTransformer transformer) {
        // TODO
    }

    private ViewPager.OnPageChangeListener listener;
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.listener = listener;
    }

}
