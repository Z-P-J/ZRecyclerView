package com.zpj.recyclerview.scene;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.core.AbsScene;
import com.zpj.recyclerview.core.LayoutHelper;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.PagerFlinger;
import com.zpj.recyclerview.layouter.PagerLayouter;

import java.util.ArrayList;
import java.util.List;

public class PagerScene extends AbsScene<PagerLayouter> {

    private static final String TAG = "ViewPagerLayouter";

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_SETTLING = 2;

    private PageTransformer mTransformer;

    private int mScrollState = SCROLL_STATE_IDLE;
    private List<OnPageChangeListener> mOnPageChangeListeners;

    public PagerScene(MultiData<?> multiData) {
        this(multiData, new PagerLayouter());
    }

    public PagerScene(MultiData<?> multiData, boolean isInfinite) {
        this(multiData, new PagerLayouter());
        setIsInfinite(isInfinite);
    }

    public PagerScene(MultiData<?> multiData, PagerLayouter layouter) {
        super(multiData, layouter);
    }

    public void setIsInfinite(boolean isInfinite) {
        mLayouter.setIsInfinite(isInfinite);
    }

    public boolean isInfinite() {
        return mLayouter.isInfinite();
    }

    @Override
    protected PagerLayoutHelper createLayoutHelper() {
        return new PagerLayoutHelper(this);
    }

    @Override
    public void saveState(View firstChild) {
        View current = findViewByPosition(getCurrentPosition());
        super.saveState(current);
    }

    @Override
    public void layoutChildren() {
        super.layoutChildren();
        if (mFlinger == null) {
            mFlinger = createFlinger();
        }
    }

    @Override
    protected Flinger createFlinger() {
        return new PagerFlinger(this) {
            @Override
            protected void onItemSelected(int item) {
                mAnchorInfo.position = item;
                if (mOnPageChangeListeners != null) {
                    for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageSelected(item);
                        }
                    }
                }
            }

            @Override
            public void onFinished() {
                setScrollState(SCROLL_STATE_IDLE);
                if (mOnPageChangeListeners != null) {
                    for(int i = 0; i < mOnPageChangeListeners.size(); ++i) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageEnterEnd(mAnchorInfo.position);
                        }
                    }
                }
            }
        };
    }

    @Override
    public boolean onTouchDown(float downX, float downY, MotionEvent event) {
        boolean result = super.onTouchDown(downX, downY, event);
        if (result && mScrollState == SCROLL_STATE_SETTLING) {
            setScrollState(SCROLL_STATE_DRAGGING);
        }
        return result;
    }

    @Override
    public boolean onTouchMove(float x, float y, float downX, float downY, MotionEvent event) {
        if (this.mScrollState != SCROLL_STATE_DRAGGING) {
            setScrollState(SCROLL_STATE_DRAGGING);
        }
        return super.onTouchMove(x, y, downX, downY, event);
    }

    @Override
    public boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        setScrollState(SCROLL_STATE_SETTLING);
        return super.onTouchUp(velocityX, velocityY, event);
    }

    @Override
    public void onDetached() {
        if (mFlinger != null) {
            mFlinger.stop();
        }
        if (mAnchorInfo.x != 0) {
            mAnchorInfo.x = 0;
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

    public void setOffscreenPageLimit(@IntRange(from = 1) int limit) {
        if (limit < 1) {
            limit = 1;
        }
        if (getOffscreenPageLimit() != limit) {
            mLayouter.setOffscreenPageLimit(limit);
            if (getLayoutHelper() != null) {
                getLayoutHelper().requestLayout();
            }
        }
    }

    public int getOffscreenPageLimit() {
        return mLayouter.getOffscreenPageLimit();
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (smoothScroll) {
            if (mFlinger == null) {
                return;
            }
            int delta = mAnchorInfo.position - item;
            int dx = delta * getWidth() - mAnchorInfo.x;
            mFlinger.scroll(dx, 0);
        } else {
            if (mFlinger != null) {
                mFlinger.stop();
            }
            mAnchorInfo.position = item;
            mAnchorInfo.x = 0;
            mAnchorInfo.y = getTop();
            if (getLayoutHelper() != null) {
                getLayoutHelper().requestLayout();
            }
        }
    }

    public int getCurrentItem() {
        return mAnchorInfo.position;
    }

    public int getCurrentPosition() {
        return mAnchorInfo.position + mPositionOffset;
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

        protected final PagerScene mPagerScene;

        public PagerLayoutHelper(PagerScene scene) {
            super(scene);
            this.mPagerScene = scene;
        }

        @Override
        public int getDecoratedMeasuredWidth(@NonNull View child) {
            return getWidth();
        }

        @Override
        public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
            super.layoutDecorated(child, left, top, right, bottom);
            if (mPagerScene.mTransformer != null) {
                float position = (float) left / getWidth();
                mPagerScene.mTransformer.transformPage(child, position);
            }
        }

        @Override
        public void scrapOrRecycleView(int index, View view) {
            offsetChildLeftAndRight(view, Integer.MAX_VALUE);
            super.scrapOrRecycleView(index, view);
        }

        @Override
        public boolean shouldRecycleChildViewHorizontally(View view, int consumed) {
            if (mPagerScene.isInfinite()) {
                if (mPagerScene.getCurrentPosition() == getPosition(view)) {
                    return false;
                }
                View current = findViewByPosition(mPagerScene.getCurrentPosition());
                int index = indexOfChild(view);
                int center = indexOfChild(current);
                boolean result = Math.abs(index - center) > mPagerScene.getOffscreenPageLimit();
                Log.d(TAG, "shouldRecycleChildViewHorizontally currentPosition=" + mPagerScene.getCurrentPosition() + " position=" + getPosition(view) + " recycle=" + result);
                return result;
            } else {
                return Math.abs(mPagerScene.getCurrentPosition() - getPosition(view)) > mPagerScene.getOffscreenPageLimit();
            }
        }

        @Override
        public void offsetChildLeftAndRight(@NonNull View child, int offset) {
            super.offsetChildLeftAndRight(child, offset);
            if (mPagerScene.mOnPageChangeListeners != null && offset != 0
                    && getPosition(child) - mPagerScene.getPositionOffset() == mPagerScene.getCurrentItem()) {
                float left = getDecoratedLeft(child);
                float childOffset = left / getWidth();
                if (left < 0) {
                    childOffset = Math.abs(childOffset);
                } else {
                    childOffset = 1 - childOffset;
                }
                int childOffsetPixels = (int) (childOffset * getWidth());
                for(int i = 0; i < mPagerScene.mOnPageChangeListeners.size(); ++i) {
                    OnPageChangeListener listener = mPagerScene.mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrolled(mPagerScene.getCurrentItem(), childOffset, childOffsetPixels);
                    }
                }
            }

            if (mPagerScene.mTransformer != null) {
                if (offset == Integer.MAX_VALUE) {
                    mPagerScene.mTransformer.transformPage(child, 0);
                } else {
                    float left = getDecoratedLeft(child);
                    float position = left / getWidth();
                    mPagerScene.mTransformer.transformPage(child, position);
                }
            }
        }

    }


}
