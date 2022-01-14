package com.zpj.recyclerview.layouter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.PublicChildHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.manager.MultiLayoutParams;

public class ContainerLayouter extends AbsLayouter {

    private static final String TAG = "ContainerLayouter";

    @NonNull
    private final AbsLayouter mLayouter;

    private final BaseMultiLayoutManager layoutManager = new ContainerLayoutManager(this);

    public ContainerLayouter(@NonNull AbsLayouter layouter) {
        this.mLayouter = layouter;
    }

    public AbsLayouter getLayouter() {
        return mLayouter;
    }

    @Override
    public void setLayoutManager(BaseMultiLayoutManager manager) {
        super.setLayoutManager(manager);
        layoutManager.attachRecycler(manager.getRecycler());
        mLayouter.setLayoutManager(layoutManager);
    }

    @Override
    public void setLeft(int left) {
        super.setLeft(left);
        mLayouter.setLeft(0); // add padding
    }

    @Override
    public void setTop(int top) {
        super.setTop(top);
        mLayouter.setTop(top);
    }

    @Override
    public void setRight(int right) {
        super.setRight(right);
        mLayouter.setRight(getWidth());
    }

    @Override
    public void setBottom(int bottom) {
        super.setBottom(bottom);
        mLayouter.setBottom(bottom);
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        super.offsetLeftAndRight(offset);
        mLayouter.offsetLeftAndRight(offset);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
        mLayouter.offsetTopAndBottom(offset);
    }

    @Override
    public void offsetChildLeftAndRight(@NonNull View child, int offset) {
        super.offsetChildLeftAndRight(child, offset);
    }

    @Override
    public void setPositionOffset(int offset) {
        super.setPositionOffset(offset);
        mLayouter.setPositionOffset(offset + 1);
    }

    @Override
    public void addView(View child) {
        ContainerLayout containerLayout = (ContainerLayout) findViewByPosition(mPositionOffset);
        containerLayout.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        ContainerLayout containerLayout = (ContainerLayout) findViewByPosition(mPositionOffset);
        containerLayout.addView(child, index);
    }

    @Override
    public void scrapOrRecycleView(BaseMultiLayoutManager manager, int index, View view) {

        for (int i = layoutManager.getChildCount() - 1; i >= 0; --i) {
            View child = layoutManager.getChildAt(i);
            mLayouter.scrapOrRecycleView(layoutManager, i, child);
        }
        Log.d(TAG, "scrapOrRecycleView index=" + index + " childCount=" + layoutManager.getChildCount());
        super.scrapOrRecycleView(manager, index, view);
    }

    @Override
    public void addViewToRecycler(View view) {
        Log.d(TAG, "addViewToRecycler pos=" + getPosition(view));
        offsetChildLeftAndRight(view, Integer.MAX_VALUE);
        getLayoutManager().recycleViews.add(view);


        for (int i = layoutManager.getChildCount() - 1; i >= 0; --i) {
            View child = layoutManager.getChildAt(i);
            mLayouter.scrapOrRecycleView(layoutManager, i, child);
        }

        Log.d(TAG, "addViewToRecycler childCount=" + layoutManager.getChildCount());
    }

    @Override
    public boolean canScrollHorizontally() {
        return mLayouter.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        return mLayouter.canScrollVertically();
    }

    @Override
    public boolean onTouchDown(MultiData<?> multiData, float downX, float downY) {
        return mLayouter.onTouchDown(multiData, downX, downY);
    }

    @Override
    public boolean onTouchMove(MultiData<?> multiData, float x, float y, float downX, float downY) {
        return mLayouter.onTouchMove(multiData, x, y, downX, downY);
    }

    @Override
    public boolean onTouchUp(MultiData<?> multiData, float velocityX, float velocityY) {
        return mLayouter.onTouchUp(multiData, velocityX, velocityY);
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, int currentPosition) {
        View container = getViewForPosition(mPositionOffset, multiData);
//        ContainerLayout containerLayout = (ContainerLayout) container;
//        containerLayout.removeAllViews();

        MultiLayoutParams params = (MultiLayoutParams) container.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        super.addView(container);
        mLayouter.layoutChildren(multiData, currentPosition + 1);
        setRight(getWidth());
        int height = mLayouter.getBottom() - mLayouter.getTop();
        mBottom = mTop + height;
        container.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        super.layoutDecorated(container, getLeft(), getTop(), getRight(), getBottom());
        Log.d(TAG, "layoutChildren childCount=" + ((ViewGroup) container).getChildCount());
    }

    @Override
    public int fillVertical(View anchorView, int dy, MultiData<?> multiData) {
        View container = findViewByPosition(mPositionOffset);
        boolean isNewContainer = container == null;
        if (isNewContainer) {
            container = getViewForPosition(mPositionOffset, multiData);
            if (dy > 0) {
                // 从下往上滑动
                super.addView(container);
            } else {
                super.addView(container, 0);
            }
        }

        ContainerLayout containerLayout = (ContainerLayout) container;
//        containerLayout.removeAllViews();

        if (dy > 0) {
            anchorView = containerLayout.getChildAt(containerLayout.getChildCount() - 1);
        } else {
            anchorView = containerLayout.getChildAt(0);
        }
        int consumed = mLayouter.fillVertical(anchorView, dy, multiData);

        int height = mLayouter.getBottom() - mLayouter.getTop();

        if (dy > 0) {
            // 从下往上滑动
            mBottom = mTop + height;
        } else {
            mTop = mBottom - height;
        }

        containerLayout.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

        super.layoutDecorated(containerLayout, getLeft(), getTop(), getRight(), getBottom());

        if (isNewContainer) {
            return consumed;
        }

        if (dy > 0) {
            // 从下往上滑动
            if (mBottom > getHeight()) {
                if (mBottom - dy > getHeight()) {
                    return dy;
                } else {
                    return mBottom - getHeight();
                }
            }
        } else {
            if (mTop < 0) {
                if (mTop - dy < 0) {
                    return -dy;
                } else {
                    return -mTop;
                }
            }
        }
        return 0;
    }

    @Override
    protected int fillVerticalTop(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        return 0;
    }

    @Override
    protected int fillVerticalBottom(MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        return 0;
    }

    @Override
    public int fillHorizontal(View anchorView, int dx, MultiData<?> multiData) {
        return mLayouter.fillHorizontal(anchorView, dx, multiData);
    }

    @Override
    public int scrollHorizontallyBy(int dx, MultiData<?> scrollMultiData) {
        return mLayouter.scrollHorizontallyBy(dx, scrollMultiData);
    }

    @Override
    public MultiData<?> getMultiData(View child) {
        if (child instanceof ContainerLayout) {
            return null;
        }
        return super.getMultiData(child);
    }

    private static final class ContainerLayoutManager extends BaseMultiLayoutManager {

        @NonNull
        private final ContainerLayouter mLayouter;

        public ContainerLayoutManager(@NonNull ContainerLayouter layouter) {
            this.mLayouter = layouter;
        }

        private ContainerLayout getContainerLayout() {
            return (ContainerLayout) mLayouter.findViewByPosition(mLayouter.getPositionOffset());
        }

        @Override
        public void attachRecycler(MultiRecycler recycler) {
            super.attachRecycler(recycler);
            setRecyclerView(recycler.getRecyclerView(), new PublicChildHelper(new PublicChildHelper.PublicCallback() {
                @Override
                public int getChildCount() {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return 0;
                    }
                    return container.getChildCount();
                }

                @Override
                public void addView(View view, int i) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    Log.d(TAG, "addView i=" + i + " pos=" + getPosition(view));
                    container.addView(view, i);
                    dispatchChildAttached(view);
                }

                @Override
                public int indexOfChild(View view) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return - 1;
                    }
                    return container.indexOfChild(view);
                }

                @Override
                public void removeViewAt(int index) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    View child = container.getChildAt(index);
                    if (child != null) {
                        dispatchChildDetached(child);
                        child.clearAnimation();
                    }
                    container.removeViewAt(index);
                }

                @Override
                public View getChildAt(int i) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return null;
                    }
                    return container.getChildAt(i);
                }

                @Override
                public void removeAllViews() {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    int count = this.getChildCount();

                    for(int i = 0; i < count; ++i) {
                        View child = this.getChildAt(i);
                        dispatchChildDetached(child);
                        child.clearAnimation();
                    }

                    container.removeAllViews();
                }

                @Override
                public RecyclerView.ViewHolder getChildViewHolder(View view) {
                    return RecyclerViewHelper.getChildViewHolderInt(view);
                }

                @Override
                public void attachViewToParent(View view, int i, ViewGroup.LayoutParams layoutParams) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    RecyclerViewHelper.attachViewToParent(ContainerLayoutManager.this, container, view, i, layoutParams);
                }

                @Override
                public void detachViewFromParent(int i) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    RecyclerViewHelper.detachViewFromParent(ContainerLayoutManager.this, container, i);
                }

                @Override
                public void onEnteredHiddenState(View view) {
                    RecyclerViewHelper.onEnteredHiddenState(ContainerLayoutManager.this, view);
                }

                @Override
                public void onLeftHiddenState(View view) {
                    RecyclerViewHelper.onLeftHiddenState(ContainerLayoutManager.this, view);
                }
            }));
        }

        @Override
        public void addView(View child, int index) {
            Log.d(TAG, "addView index=" + index + " position=" + getPosition(child));
            super.addView(child, index);
        }

        @Override
        public void addView(View child) {
            Log.d(TAG, "addView position=" + getPosition(child));
            super.addView(child);
        }

        //        @Override
//        public void addView(View child) {
//            mLayouter.addView(child);
//        }
//
//        @Override
//        public void addView(View child, int index) {
//            mLayouter.addView(child, index);
//        }
//
//        @Override
//        public int getChildCount() {
//            ContainerLayout container = getContainerLayout();
//            if (container == null) {
//                return 0;
//            }
//            return container.getChildCount();
//        }
//
//        @Nullable
//        @Override
//        public View getChildAt(int index) {
//            ContainerLayout container = getContainerLayout();
//            if (container == null) {
//                return null;
//            }
//            return container.getChildAt(index);
//        }
//
//        @Override
//        public int indexOfChild(View child) {
//            ContainerLayout container = getContainerLayout();
//            if (container == null) {
//                return -1;
//            }
//            return container.indexOfChild(child);
//        }

        @Override
        public void layoutDecorated(@NonNull View child, int left, int top, int right, int bottom) {
            super.layoutDecorated(child, left, 0, right, bottom - top);
        }

        @Nullable
        @Override
        public View findViewByPosition(int position) {
            return super.findViewByPosition(position);
        }

        @Override
        public void offsetChildLeftAndRight(@NonNull View child, int offset) {
            if (child instanceof ContainerLayout) {
                return;
            }
            super.offsetChildLeftAndRight(child, offset);
        }

//        @Override
//        public void recycleViews(RecyclerView.Recycler recycler) {
//            for (View view : recycleViews) {
//                Log.d(TAG, "recycleViews pos=" + getPosition(view));
//                detachAndScrapView(view, recycler);
//            }
//            recycleViews.clear();
//
//            List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
//            for (int i = 0; i < scrapList.size(); i++) {
//                removeAndRecycleView(scrapList.get(i).itemView, recycler);
//            }
//        }

//        @Override
//        public void recycleViews(RecyclerView.Recycler recycler) {
//            ContainerLayout container = getContainerLayout();
//            if (container == null) {
//                return;
//            }
//            for (View view : recycleViews) {
//                Log.d(TAG, "recycleViews pos=" + getPosition(view));
//                try {
//                    detachAndScrapView(view, recycler);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
////                    container.detachViewFromParent(view);
//                    container.removeView(view);
//                }
//
//            }
//            recycleViews.clear();
//
//            List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
//            for (int i = 0; i < scrapList.size(); i++) {
//                removeAndRecycleView(scrapList.get(i).itemView, recycler);
//            }
//        }

        @Override
        public int getCount(MultiData<?> multiData) {
            return super.getCount(multiData) - 1;
        }
    }


    public static class ContainerLayout extends ViewGroup {

        public ContainerLayout(Context context) {
            this(context, null);
        }

        public ContainerLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected boolean checkLayoutParams(LayoutParams lp) {
            return lp instanceof MultiLayoutParams;
        }

        @Override
        public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            return new MultiLayoutParams(lp);
        }

        @Override
        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new MultiLayoutParams(getContext(), attrs);
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new MultiLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public void detachViewFromParent(int index) {
            super.detachViewFromParent(index);
        }

        @Override
        public void detachViewFromParent(View child) {
            super.detachViewFromParent(child);
        }

        @Override
        public void attachViewToParent(View child, int index, LayoutParams params) {
            super.attachViewToParent(child, index, params);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {

        }


    }
}
