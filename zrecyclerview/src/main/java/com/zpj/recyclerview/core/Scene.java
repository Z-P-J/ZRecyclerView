package com.zpj.recyclerview.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.HorizontalFlinger;
import com.zpj.recyclerview.layouter.Layouter;

public abstract class Scene<T extends Layouter> {

    private static final String TAG = "Scene";

    public final AnchorInfo mAnchorInfo = new AnchorInfo();

    protected BaseMultiLayoutManager mLayoutManager;
    protected final MultiData<?> mMultiData;
    protected final T mLayouter;

    protected LayoutHelper mHelper;

    protected Flinger mFlinger;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    private boolean mAttached = false;

    protected int mPositionOffset;

//    public Scene(MultiData<?> multiData) {
//        this(multiData, new VerticalLayouter());
//    }

    public Scene(MultiData<?> multiData, T layouter) {
        mMultiData = multiData;
        mLayouter = layouter;
    }

    public boolean canScrollHorizontally() {
        return mLayouter.canScrollHorizontally();
    }

    public boolean canScrollVertically() {
        return mLayouter.canScrollVertically();
    }

    public Context getContext() {
        return getRecycler().getContext();
    }

    public void postOnAnimation(Runnable action) {
        ViewCompat.postOnAnimation(getRecycler().getRecyclerView(), action);
    }

    public void removeCallbacks(Runnable action) {
        getRecycler().removeCallbacks(action);
    }

    public void attach(BaseMultiLayoutManager manager) {
        if (manager == null) {
            throw new IllegalArgumentException("attach error! LayoutManager must not be null!");
        }
        mLayoutManager = manager;
        if (mHelper != null && mHelper.getLayoutManager() != manager) {
            mHelper = null;
        }
        if (mHelper == null) {
            mHelper = createLayoutHelper();
        }
    }

    protected LayoutHelper createLayoutHelper() {
        return new LayoutHelper(this);
    }

    public void detach() {
        if (mFlinger != null) {
            mFlinger.stop();
            mFlinger = null;
        }
        mAnchorInfo.x = 0;
        mAnchorInfo.y = 0;
        mAnchorInfo.position = 0;
        mTop = 0;
        mLeft = 0;
        mRight = 0;
        mBottom = 0;
        mAttached = false;
    }

    public T getLayouter() {
        return mLayouter;
    }

    public MultiSceneRecycler getRecycler() {
        return mLayoutManager.getRecycler();
    }

    public Scene getScene(View child) {
        return mHelper.getScene(child);
    }

    public BaseMultiLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    public MultiData<?> getMultiData() {
        return mMultiData;
    }

    public LayoutHelper getLayoutHelper() {
        return mHelper;
    }

    public void setPositionOffset(int offset) {
        mPositionOffset = offset;
    }


    public int getPositionOffset() {
        return mPositionOffset;
    }

    public int getItemCount() {
        return mMultiData.getCount();
    }

    public void setLeft(int left) {
        mLeft = left;
    }

    public void setTop(int top) {
        mTop = top;
        checkAttach();
    }

    public void setRight(int right) {
        mRight = right;
    }

    public void setBottom(int bottom) {
        mBottom = bottom;
        checkAttach();
    }

    public int getLeft() {
        return mLeft;
    }

    public int getTop() {
        return mTop;
    }

    public int getRight() {
        return mRight;
    }

    public int getBottom() {
        return mBottom;
    }

    public void offsetLeftAndRight(int offset) {
        mLeft += offset;
        mRight += offset;
    }

    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        mBottom += offset;

        checkAttach();
    }

    private void checkAttach() {
        if (getBottom() < 0 || getTop() > mLayoutManager.getHeight()) {
            if (mAttached) {
                mAttached = false;
                onDetached();
            }
        } else {
            if (!mAttached) {
                mAttached = true;
                onAttached();
            }
        }
    }

    protected void onAttached() {
        // TODO
    }

    protected void onDetached() {
        if (isOverScrolling) {
            mAnchorInfo.position = mOverScrollPosition;
            mAnchorInfo.x = mOverScrollOffset;
            isOverScrolling = false;
        }

        if (mFlinger != null) {
            mFlinger.stop();
        }
    }

    public boolean isAttached() {
        return mAttached;
    }



    private float mLastX;
    private float mLastY;
    private boolean isTouchUp = true;

    public boolean onTouchDown(float downX, float downY, MotionEvent event) {
        if (!canHandleTouch(downX, downY)) {
            return false;
        }
        if (canScrollHorizontally()) {
            mLastX = downX;
            mLastY = downY;
            isTouchUp = false;
            if (mFlinger != null) {
                mFlinger.stop();
            } else {
                mFlinger = createFlinger();
            }
            return true;
        }
        return false;
    }

    protected Flinger createFlinger() {
        return new HorizontalFlinger(this);
    }

    protected boolean canHandleTouch(float downX, float downY) {
        return isAttached() && downY >= getTop() & downY <= getBottom();
    }

    public boolean onTouchMove(float x, float y, float downX, float downY, MotionEvent event) {
        if (canScrollHorizontally()) {
            int dx = (int) (mLastX - x);
            mLastX = x;
            if (dx != 0) {
                scrollHorizontallyBy(dx);
                return true;
            }
        }
        return false;
    }

    public boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        if (canScrollHorizontally()) {
            isTouchUp = true;
            if (tryToStopOverScroll()) {
                return true;
            } else if (mFlinger != null) {
                Log.e(TAG, "fling----------------vx=" + velocityX);
                mFlinger.fling(velocityX, velocityY);
            }
        }
        return false;
    }

    public void onFlingFinished() {
        tryToStopOverScroll();
    }

    public void onFlingStopped() {
        tryToStopOverScroll();
    }

    public void layoutChildren() {
        mLayouter.layoutChildren(this);
    }

    public boolean scrollToPositionWithOffset(int position, int offset) {
        if (position >= mPositionOffset && position < mPositionOffset + getItemCount()) {
            mAnchorInfo.position = position - mPositionOffset;
            mAnchorInfo.x = offset;
            mAnchorInfo.y = offset;
            return true;
        }
        return false;
    }

    public void saveState(View firstChild) {
        mAnchorInfo.position = Math.max(0, getPosition(firstChild) - mPositionOffset);
        mAnchorInfo.x = getDecoratedLeft(firstChild);
        mAnchorInfo.y = getDecoratedTop(firstChild);
    }

    private static final int OVER_SCROLL_DOWN = 1;
    private static final int OVER_SCROLL_UP = 2;
    private static final int OVER_SCROLL_LEFT = 3;
    private static final int OVER_SCROLL_RIGHT = 4;

    protected boolean isOverScrolling;
    private int overScrollDirection;
    private int overScrollDistance;

    protected int mOverScrollPosition = 0;
    protected int mOverScrollOffset;

    public int scrollHorizontallyBy(int dx) {
        if (dx == 0) {
            return 0;
        }

        if (isOverScrolling) {
            boolean hasResistance = true;
            if (overScrollDirection == OVER_SCROLL_LEFT && dx > 0) {
                hasResistance = false;
            } else if (overScrollDirection == OVER_SCROLL_RIGHT && dx < 0) {
                hasResistance = false;
            }

            float overScrollRadio;
            int overScroll;
            if (hasResistance) {
                overScrollDistance += dx;
                float maxWidth = getWidth();
                if (overScrollDistance > maxWidth) {
                    overScrollDistance = (int) maxWidth;
                } else if (overScrollDistance < -maxWidth) {
                    overScrollDistance = (int) -maxWidth;
                }
                overScrollRadio = Math.abs(overScrollDistance) / maxWidth;
                overScroll = (int) ((0.72f - overScrollRadio / 2f) * dx);
            } else {
                overScrollRadio = 0;
                overScroll = dx;
            }

            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                final Scene scene = getScene(view);
                if (scene == this) {
                    for (int j = i; j < getChildCount(); j++) {
                        View child = getChildAt(j);
                        if (getScene(child) != this) {
                            break;
                        }
                        mHelper.offsetChildLeftAndRight(child, -overScroll);
                    }
                    saveState(view);
                    break;
                }
            }

            checkOverScroll();


            if (overScrollRadio >= 1f) {
//                    Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScrollRadio=" + overScrollRadio + " overScroll=" + overScroll);
                Log.e(TAG, "scrollHorizontallyBy return 000");
                return 0;
            }

            Log.e(TAG, "scrollHorizontallyBy return dx=" + dx);
            return dx;
        }

        overScrollDistance = 0;

        int consumed = 0;
        View firstChild = getChildAt(0);
        View lastChild = getChildAt(getChildCount() - 1);
        if (firstChild == null || lastChild == null) {
            return 0;
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx);
        int index = -1;
        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                if (getScene(view) == this) {
                    index = i;
                    consumed += mLayouter.fillHorizontal(this, view, dx);
                    break;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getScene(view) == this) {
                    index = i;
                    consumed -= mLayouter.fillHorizontal(this, view, dx);
                    break;
                }
            }
        }

        if (index < 0) {
            onDetached();
            return 0;
        }

        if (dx > 0) {
            // 从右往左滑动
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View view = getChildAt(i);
                Scene scene = getScene(view);
                if (scene != this) {
                    continue;
                }

                if (mHelper.shouldRecycleChildViewHorizontally(view, consumed)) {
                    mHelper.addViewToRecycler(view);
                } else {
                    mHelper.offsetChildLeftAndRight(view, -consumed);
                    index = i;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                Scene scene = getScene(view);
                if (scene != this) {
                    continue;
                }

                if (mHelper.shouldRecycleChildViewHorizontally(view, consumed)) {
                    mHelper.addViewToRecycler(view);
                } else {
                    assert view != null;
                    mHelper.offsetChildLeftAndRight(view, -consumed);
                }
            }
        }

        mHelper.recycleViews();


        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (getScene(view) == this) {
                index = i;
                break;
            }
        }

        firstChild = getChildAt(index);

        if (dx != consumed) {
            if (firstChild != null) {
                int firstPosition = getPosition(firstChild);
                int firstOffset = getDecoratedLeft(firstChild);
                this.mOverScrollPosition = Math.max(0, firstPosition - mPositionOffset);
                this.mOverScrollOffset = Math.min(0, firstOffset);
            } else {
                this.mOverScrollPosition = 0;
                this.mOverScrollOffset = 0;
            }

            isOverScrolling = true;
            overScrollDirection = dx < 0 ? OVER_SCROLL_LEFT : OVER_SCROLL_RIGHT;
            overScrollDistance = dx - consumed;

            int overScroll = (int) (overScrollDistance * 0.1f);

            Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScroll=" + overScroll);

            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getScene(view) == this) {
                    mHelper.offsetChildLeftAndRight(view, -overScroll);
                }
            }
            consumed = dx;
        }


        if (firstChild != null) {
            saveState(firstChild);
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " consumed=" + consumed + " isOverScrolling=" + isOverScrolling);

        return consumed;
    }

    private boolean tryToStopOverScroll() {
        if (isTouchUp && isOverScrolling) {
            checkOverScroll();
            Log.e(TAG, "tryToStopOverScroll overScrollDirection=" + overScrollDirection
                    + " mOverScrollOffset=" + mOverScrollOffset
                    + " anchorX=" + mAnchorInfo.x
                    + " isOverScrolling=" + isOverScrolling);
            if (isOverScrolling) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (getScene(view) == this) {
                        onStopOverScroll();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void checkOverScroll() {
        if (overScrollDirection == OVER_SCROLL_LEFT && mAnchorInfo.x <= mOverScrollOffset) {
            isOverScrolling = false;
            overScrollDistance = 0;
        } else if (overScrollDirection == OVER_SCROLL_RIGHT && mAnchorInfo.x >= mOverScrollOffset) {
            isOverScrolling = false;
            overScrollDistance = 0;
        }
    }

    public void onStopOverScroll() {
        if (isOverScrolling) {
            if (overScrollDirection <= OVER_SCROLL_UP) {
                if (overScrollDirection == OVER_SCROLL_DOWN) {
                    final View firstChild = mHelper.getFirstChild();
                    if (firstChild != null) {
                        final int firstTop = getDecoratedTop(firstChild);
                        if (firstTop > 0) {
                            // TODO
                        }
                    }
                } else if (overScrollDirection == OVER_SCROLL_UP) {
                    View lastChild = mHelper.getLastChild();
                    if (lastChild != null) {
                        final int bottom = getDecoratedBottom(lastChild);
                        if (bottom < getHeight()) {
                            // TODO
                        }
                    }
                }
            } else {
                if (overScrollDirection == OVER_SCROLL_LEFT) {
                    for (int i = 0; i < getChildCount(); i++) {
                        View view = getChildAt(i);
                        if (getScene(view) == this) {
                            final int firstLeft = getDecoratedLeft(view);
                            Log.d(TAG, "onStopOverScroll firstLeft=" + firstLeft + " overScrollDistance=" + overScrollDistance);
                            if (firstLeft > 0) { //  && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset()
                                mFlinger.scroll(-firstLeft, 0, 500);
                            }
                            break;
                        }
                    }
                } else if (overScrollDirection == OVER_SCROLL_RIGHT) {
                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        View view = getChildAt(i);
                        if (getScene(view) == this) {
                            final int right = getDecoratedRight(view);
                            Log.d(TAG, "onStopOverScroll right=" + right + " overScrollDistance=" + overScrollDistance);
                            if (right < getWidth()) { //  && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset() + scrollgetCount(multiData) - 1
                                mFlinger.scroll(getWidth() - right, 0, 500);
                            }
                            break;
                        }
                    }
                }
            }
        }


    }













    public int getPosition(@NonNull View child) {
        return mHelper.getPosition(child);
    }

    public int getDecoratedLeft(@NonNull View child) {
        return mHelper.getDecoratedLeft(child);
    }

    public int getDecoratedTop(@NonNull View child) {
        return mHelper.getDecoratedTop(child);
    }

    public int getDecoratedRight(@NonNull View child) {
        return mHelper.getDecoratedRight(child);
    }

    public int getDecoratedBottom(@NonNull View child) {
        return mHelper.getDecoratedBottom(child);
    }

    public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        mHelper.layoutDecorated(child, left, top, right, bottom);
    }

    public View findViewByPosition(int position) {
        return mHelper.findViewByPosition(position);
    }

    public View getViewForPosition(int position) {
        return mHelper.getViewForPosition(position);
    }

    public View obtainViewForPosition(int position) {
        return mHelper.obtainViewForPosition(position);
    }

    public View addView(int position) {
        View view = obtainViewForPosition(position);
        addView(view);
        return view;
    }

    public View addView(int position, int index) {
        View view = obtainViewForPosition(position);
        addView(view, index);
        return view;
    }

    public void addView(View child) {
        mHelper.addView(child);
    }

    public void addView(View child, int index) {
        mHelper.addView(child, index);
    }

    public View addViewAndMeasure(int position) {
        View view = obtainViewForPosition(position);
        addView(view);
        measureChild(view, 0, 0);
        return view;
    }

    public View addViewAndMeasure(int position, int index) {
        View view = obtainViewForPosition(position);
        addView(view, index);
        measureChild(view, 0, 0);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
        mHelper.measureChild(child, widthUsed, heightUsed);
    }



    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return mHelper.getDecoratedMeasuredWidth(child);
    }

    public int getDecoratedMeasuredHeight(@NonNull View child) {
        return mHelper.getDecoratedMeasuredHeight(child);
    }

    @Px
    public int getWidth() {
        return mHelper.getWidth();
    }

    @Px
    public int getHeight() {
        return mHelper.getHeight();
    }

    @Px
    public int getPaddingLeft() {
        return mHelper.getPaddingLeft();
    }

    @Px
    public int getPaddingTop() {
        return mHelper.getPaddingTop();
    }

    @Px
    public int getPaddingRight() {
        return mHelper.getPaddingRight();
    }

    @Px
    public int getPaddingBottom() {
        return mHelper.getPaddingBottom();
    }

    @Px
    public int getPaddingStart() {
        return mHelper.getPaddingStart();
    }

    @Px
    public int getPaddingEnd() {
        return mHelper.getPaddingEnd();
    }

    public int getChildCount() {
        return mHelper.getChildCount();
    }

    @Nullable
    public View getChildAt(int index) {
        return mHelper.getChildAt(index);
    }

    public int indexOfChild(View child) {
        return mHelper.indexOfChild(child);
    }

}
