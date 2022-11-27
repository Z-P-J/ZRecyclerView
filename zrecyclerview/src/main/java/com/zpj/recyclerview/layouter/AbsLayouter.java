package com.zpj.recyclerview.layouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.flinger.Flinger;
import com.zpj.recyclerview.flinger.HorizontalFlinger;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public abstract class AbsLayouter implements Layouter {

    private static final String TAG = "AbsLayouter";

    private LayoutHelper mHelper;
    protected Flinger mFlinger;

    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    protected int mPositionOffset;
    protected int mChildOffset;

    private boolean mAttached = false;

    @Override
    public void layoutChildren(MultiData<?> multiData, int currentPosition) {
        if (getLayoutHelper() == null || getCount(multiData) == 0 || mTop > getHeight()) {
            mBottom = mTop;
            return;
        }
        fillVerticalBottom(multiData, currentPosition, getHeight() - mTop, getTop());
    }

    @Override
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {
                int result = fillVerticalBottom(multiData, mPositionOffset, dy, getTop());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(dy, dy - result));
                return Math.min(dy, dy - result);
            } else {
                int anchorBottom = getDecoratedBottom(anchorView);
                Log.e(TAG, "fillVertical222 anchorBottom=" + anchorBottom + " height=" + getHeight() + " anchorBottom - dy=" + (anchorBottom - dy));
                if (anchorBottom - dy > getHeight()) {
//                    Log.d(TAG, "fillVertical return dy=" + dy);
                    return dy;
                } else {
                    int anchorPosition = getPosition(anchorView);
                    if (anchorPosition == mPositionOffset + getCount(multiData) - 1) {
                        return Math.max(0, anchorBottom - getHeight());
                    }
                    int availableSpace = dy + getHeight() - anchorBottom;
                    int result = fillVerticalBottom(multiData, anchorPosition + 1, availableSpace, anchorBottom);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(dy, dy - result) + " availableSpace=" + availableSpace);
                    return Math.min(dy, dy - result);
                }
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {
                int result = fillVerticalTop(multiData, mPositionOffset + getCount(multiData) - 1,
                        -dy, getBottom());
                Log.e(TAG, "fillVertical111 result=" + result + " return=" + Math.min(-dy, -dy - result));
                return Math.min(-dy, -dy - result);
            } else {
                int anchorTop = getDecoratedTop(anchorView);
                int anchorPosition = getPosition(anchorView);
                if (anchorTop - dy < 0) {
                    return -dy;
                } else {

                    if (anchorPosition == mPositionOffset) {
                        return -anchorTop;
                    }
                    int availableSpace = -dy + anchorTop;
                    int result = fillVerticalTop(multiData, anchorPosition - 1, availableSpace, anchorTop);
                    Log.e(TAG, "fillVertical222 result=" + result + " return=" + Math.min(-dy, availableSpace - result) + " availableSpace=" + availableSpace);
                    return Math.min(-dy, -dy - result);
                }
            }
        }
//        return 0;
    }

    @Override
    public void setPositionOffset(int offset) {
        this.mPositionOffset = offset;
    }

    @Override
    public int getPositionOffset() {
        return mPositionOffset;
    }

    @Override
    public void setChildOffset(int offset) {
        this.mChildOffset = offset;
    }

    @Override
    public int getChildOffset() {
        return this.mChildOffset;
    }

    @Override
    public void setLayoutManager(BaseMultiLayoutManager manager) {
        mHelper = createLayoutHelper(manager);
    }

    protected LayoutHelper createLayoutHelper(BaseMultiLayoutManager manager) {
        return new LayoutHelper(manager);
    }

    @Override
    public LayoutHelper getLayoutHelper() {
        return mHelper;
    }

    @Override
    public void setLeft(int left) {
        this.mLeft = left;
    }

    @Override
    public void setTop(int top) {
        this.mTop = top;
    }

    @Override
    public void setRight(int right) {
        this.mRight = right;
    }

    @Override
    public void setBottom(int bottom) {
        this.mBottom = bottom;
    }


    @Override
    public int getLeft() {
        return mLeft;
    }

    @Override
    public int getTop() {
        return mTop;
    }

    @Override
    public int getRight() {
        return mRight;
    }

    @Override
    public int getBottom() {
        return mBottom;
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        this.mLeft += offset;
        this.mRight += offset;
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        this.mTop += offset;
        this.mBottom += offset;
        if (mBottom < 0 || mTop > getHeight()) {
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

    }

    protected void onDetached() {
        if (isStopOverScrolling) {
            this.mFirstOffset = this.mOverScrollOffset;
            this.mFirstPosition = this.mOverScrollPosition;
        }

        if (mFlinger != null) {
            mFlinger.stop();
        }
    }
    
    protected int getPosition(@NonNull View child) {
        return mHelper.getPosition(child);
    }

    protected View findViewByPosition(int position) {
        return mHelper.findViewByPosition(position);
    }

    protected int getDecoratedLeft(@NonNull View child) {
        return mHelper.getDecoratedLeft(child);
    }

    protected int getDecoratedTop(@NonNull View child) {
        return mHelper.getDecoratedTop(child);
    }

    protected int getDecoratedRight(@NonNull View child) {
        return mHelper.getDecoratedRight(child);
    }

    protected int getDecoratedBottom(@NonNull View child) {
        return mHelper.getDecoratedBottom(child);
    }
    
    protected void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
        mHelper.layoutDecorated(child, left, top, right, bottom);
    }

    @Override
    public void saveState(int firstPosition, int firstOffset) {
        this.mFirstPosition = Math.max(0, firstPosition - mPositionOffset);
        this.mFirstOffset = firstOffset;
    }

    protected abstract int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop);

    protected abstract int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom);

    public View getViewForPosition(int position) {
        return  mHelper.getViewForPosition(position);
    }

    public View getViewForPosition(int position, MultiData<?> multiData) {
        View view = null;
        if (multiData.isStickyPosition(position - mPositionOffset)) {
            view  =  mHelper.findViewByPosition(position);
        }
        if (view == null) {
            view = getViewForPosition(position);
        } else {
             mHelper.detachAndScrapView(view);
        }
        MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
        params.setMultiData(multiData);
        return view;
    }

    public View addView(int position, MultiData<?> multiData) {
        View view = getViewForPosition(position, multiData);
        addView(view);
        return view;
    }

    public View addView(int position, int index, MultiData<?> multiData) {
        View view = getViewForPosition(position, multiData);
        addView(view, index);
        return view;
    }

    public View addViewAndMeasure(int position, MultiData<?> multiData) {
        View view = getViewForPosition(position, multiData);
        addView(view);
        measureChild(view, 0, 0);
        return view;
    }

    public View addViewAndMeasure(int position, int index, MultiData<?> multiData) {
        View view = getViewForPosition(position, multiData);
        addView(view, index);
        measureChild(view, 0, 0);
        return view;
    }

    public void measureChild(@NonNull View child, int widthUsed, int heightUsed) {
       mHelper.measureChild(child, widthUsed, heightUsed);
    }

    public void addView(View child) {
         mHelper.addView(child);
    }

    public void addView(View child, int index) {
         mHelper.addView(child, index);
    }

    public int getDecoratedMeasuredWidth(@NonNull View child) {
        return  mHelper.getDecoratedMeasuredWidth(child);
    }

    public int getDecoratedMeasuredHeight(@NonNull View child) {
        return  mHelper.getDecoratedMeasuredHeight(child);
    }

    @Px
    public int getWidth() {
        return  mHelper.getWidth();
    }

    @Px
    public int getHeight() {
        return  mHelper.getHeight();
    }

    @Px
    public int getPaddingLeft() {
        return  mHelper.getPaddingLeft();
    }

    @Px
    public int getPaddingTop() {
        return  mHelper.getPaddingTop();
    }

    @Px
    public int getPaddingRight() {
        return  mHelper.getPaddingRight();
    }

    @Px
    public int getPaddingBottom() {
        return  mHelper.getPaddingBottom();
    }

    @Px
    public int getPaddingStart() {
        return  mHelper.getPaddingStart();
    }

    @Px
    public int getPaddingEnd() {
        return  mHelper.getPaddingEnd();
    }

    public int getChildCount() {
        return  mHelper.getChildCount();
    }

    @Nullable
    public View getChildAt(int index) {
        return  mHelper.getChildAt(index);
    }

    public MultiData<?> getMultiData(View child) {
        return  mHelper.getMultiData(child);
    }

    public Layouter getLayouter(View child) {
        return  mHelper.getLayouter(child);
    }

    public int indexOfChild(View child) {
        return  mHelper.indexOfChild(child);
    }

    public MultiRecycler getRecycler() {
        return  mHelper.getRecycler();
    }

    public Context getContext() {
        return getRecycler().getContext();
    }

    public int getCount(MultiData<?> multiData) {
        return  mHelper.getCount(multiData);
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        if (canScrollHorizontally()) {
//            getRecycler().getRecyclerView().stopScroll();
            if (mFlinger != null) {
                mFlinger.stop();
            } else {
                mFlinger = createFlinger(multiData);
            }
            return true;
        }
        return false;
    }

    protected Flinger createFlinger(MultiData<?> multiData) {
        return new HorizontalFlinger(this, multiData);
    }

    @Override
    public boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY) {
        return false;
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        if (canScrollHorizontally()) {
            if (isOverScrolling) {
                onStopOverScroll(multiData);
            } else if (mFlinger != null) {
                mFlinger.fling(velocityX, velocityY);
            }
        }
        return false;
    }

    public void onFlingFinished() {
        isStopOverScrolling = false;
    }

    public void onFlingStopped() {
        isStopOverScrolling = false;
    }

    private static final int OVER_SCROLL_DOWN = 1;
    private static final int OVER_SCROLL_UP = 2;
    private static final int OVER_SCROLL_LEFT = 3;
    private static final int OVER_SCROLL_RIGHT = 4;

    protected boolean isOverScrolling;
    private int overScrollDirection;
    private int overScrollDistance;
    private boolean isStopOverScrolling;

    protected int mOverScrollPosition = 0;
    protected int mOverScrollOffset;

    protected int mFirstPosition = 0;
    protected int mFirstOffset;

    @Override
    public int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData) {
        if (scrollMultiData == null) {
            return 0;
        }

        if (isOverScrolling) {
            overScrollDistance += dx;
            if (overScrollDirection == OVER_SCROLL_LEFT) {
                if (overScrollDistance >= 0) {
                    isOverScrolling = false;
                }
            } else if (overScrollDirection == OVER_SCROLL_RIGHT) {
                if (overScrollDistance <= 0) {
                    isOverScrolling = false;
                }
            }

            if (isOverScrolling) {

                float maxWidth = getWidth() / 1.5f;
                float overScrollRadio = Math.min(Math.abs(overScrollDistance), maxWidth) / maxWidth;
                int overScroll = (int) ((0.68f - overScrollRadio / 2f) * dx);

                Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScrollRadio=" + overScrollRadio + " overScroll=" + overScroll);

                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    final MultiData<?> multiData = getMultiData(view);
                    if (multiData == scrollMultiData) {
                        for (int j = i; j < getChildCount(); j++) {
                            View child = getChildAt(j);
                            if (getMultiData(child) != scrollMultiData) {
                                break;
                            }
                            Log.d(TAG, "scrollHorizontallyBy i=" + i);
                            mHelper.offsetChildLeftAndRight(child, -overScroll);
                        }

                        int firstPosition = getPosition(view);
                        int firstOffset = getDecoratedLeft(view);
                        saveState(firstPosition, firstOffset);

                        break;
                    }
                }

                if (overScrollRadio >= 1f) {
                    Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " overScrollRadio=" + overScrollRadio + " overScroll=" + overScroll);
                    return 0;
                }

                if (Math.abs(overScrollDistance) > maxWidth) {
                    return 0;
                }

                return dx;

            }
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
                if (getMultiData(view) == scrollMultiData) {
                    index = i;
                    consumed += fillHorizontal(view, dx, scrollMultiData);
                    break;
                }
            }
        } else {
            // 从左往右滑动
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (getMultiData(view) == scrollMultiData) {
                    index = i;
                    consumed -= fillHorizontal(view, dx, scrollMultiData);
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
                MultiData<?> multiData = getMultiData(view);
                if (multiData != scrollMultiData) {
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
                MultiData<?> multiData = getMultiData(view);
                if (multiData != scrollMultiData) {
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
            if (getMultiData(view) == scrollMultiData) {
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
                this.mOverScrollOffset = firstOffset;
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
                if (getMultiData(view) == scrollMultiData) {
                    mHelper.offsetChildLeftAndRight(view, -overScroll);
                }
            }
            consumed = dx;
        }


        if (firstChild != null) {
            int firstPosition = getPosition(firstChild);
            int firstOffset = getDecoratedLeft(firstChild);
            saveState(firstPosition, firstOffset);
            Log.e(TAG, "scrollHorizontallyBy firstPosition=" + firstPosition + " firstOffset=" + firstOffset);
        }

        Log.d(TAG, "scrollHorizontallyBy dx=" + dx + " consumed=" + consumed + " isOverScrolling=" + isOverScrolling);

        return consumed;
    }






    public void onStopOverScroll(final MultiData<?> scrollMultiData) {
        if (scrollMultiData == null) {
            return;
        }
        isStopOverScrolling = false;
        if (isOverScrolling) {
            isOverScrolling = false;
            mFlinger.stop();
            if (overScrollDirection <= OVER_SCROLL_UP) {
                if (overScrollDirection == OVER_SCROLL_DOWN) {
                    final View firstChild =  mHelper.getFirstChild();
                    if (firstChild != null) {
                        final int firstTop = getDecoratedTop(firstChild);
                        if (firstTop > 0) {
                            // TODO
                        }
                    }
                } else if (overScrollDirection == OVER_SCROLL_UP) {
                    View lastChild =  mHelper.getLastChild();
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
                        if (getMultiData(view) == scrollMultiData) {
                            final int firstLeft = getDecoratedLeft(view);
                            Log.d(TAG, "onStopOverScroll firstLeft=" + firstLeft);
                            if (firstLeft > 0) { //  && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset()
                                mFlinger.scroll(-firstLeft, 0, 500);
                                isStopOverScrolling = true;
                            }
                            break;
                        }
                    }
                } else if (overScrollDirection == OVER_SCROLL_RIGHT) {
                    for (int i = getChildCount() - 1; i >= 0; i--) {
                        View view = getChildAt(i);
                        if (getMultiData(view) == scrollMultiData) {
                            final int right = getDecoratedRight(view);
                            Log.d(TAG, "onStopOverScroll right=" + right);
                            if (right < getWidth()) { //  && getPosition(view) == scrollMultiData.getLayouter().getPositionOffset() + scrollgetCount(multiData) - 1
                                mFlinger.scroll(getWidth() - right, 0, 500);
                                isStopOverScrolling = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

}
