package com.zpj.recyclerview.scene;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.PublicChildHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.AbsScene;
import com.zpj.recyclerview.core.AnchorInfo;
import com.zpj.recyclerview.core.LayoutHelper;
import com.zpj.recyclerview.core.SceneLayoutParams;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.layouter.Layouter;

public class ContainerScene extends AbsScene<Layouter> {

    private static final String TAG = "BannerScene";

    @NonNull
    private final Scene mDelegateScene;

    protected final BaseMultiLayoutManager mContainerLayoutManager = new ContainerLayoutManager(this);


    // TODO 代理的方式
    public static class ChildScene extends AbsScene<Layouter> {

        public ChildScene(MultiData<?> multiData, Layouter layouter) {
            super(multiData, layouter);
        }

        @Override
        public int getItemCount() {
            return super.getItemCount() - 1;
        }
    }

    // TODO 代理MultiData，不需要自定义ContainerMultiData
    public ContainerScene(MultiData<?> multiData, Layouter layouter) {
        this(new ChildScene(multiData, layouter));
    }

    public ContainerScene(@NonNull ChildScene scene) {
        super(scene.getMultiData(), new ContainerLayouter(scene));
        this.mDelegateScene = scene;
    }

    @Override
    public void attach(BaseMultiLayoutManager manager) {
        super.attach(manager);
        mContainerLayoutManager.attachRecycler(getRecycler());
        mDelegateScene.attach(mContainerLayoutManager);
    }

    @Override
    public void detach() {
        super.detach();
    }

    @Override
    protected ContainerLayoutHelper createLayoutHelper() {
        return new ContainerLayoutHelper(this);
    }

    @Override
    public void setPositionOffset(int offset) {
        super.setPositionOffset(offset);
        mDelegateScene.setPositionOffset(offset + 1);
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
    public boolean onTouchDown(float downX, float downY, MotionEvent event) {
        return mDelegateScene.onTouchDown(downX, downY, event);
    }

    @Override
    public boolean onTouchMove(float x, float y, float downX, float downY, MotionEvent event) {
        return mDelegateScene.onTouchMove(x, y, downX, downY, event);
    }

    @Override
    public boolean onTouchUp(float velocityX, float velocityY, MotionEvent event) {
        return mDelegateScene.onTouchUp(velocityX, velocityY, event);
    }

    @Override
    public void saveState(View firstChild) {
        mDelegateScene.saveState(mDelegateScene.getChildAt(0));
    }

    @Override
    public void layoutChildren() {
        View container = obtainViewForPosition(mPositionOffset);
//        ContainerLayout containerLayout = (ContainerLayout) container;
//        containerLayout.removeAllViews();

        SceneLayoutParams params = (SceneLayoutParams) container.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        super.addView(container);
        mDelegateScene.layoutChildren();
        mDelegateScene.setRight(getWidth());
        int height = mDelegateScene.getBottom() - mDelegateScene.getTop();
        mDelegateScene.setBottom(mDelegateScene.getTop() + height);
        container.measure(View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        super.layoutDecorated(container, mDelegateScene.getLeft(), mDelegateScene.getTop(), mDelegateScene.getRight(), mDelegateScene.getBottom());
        Log.d(TAG, "layoutChildren childCount=" + ((ViewGroup) container).getChildCount());
    }

    @Override
    public int scrollHorizontallyBy(int dx) {
        return mDelegateScene.scrollHorizontallyBy(dx);
    }

//    @Override
//    public MultiData<?> getMultiData(View child) {
//        if (child instanceof ContainerLayout) {
//            return null;
//        }
//        return super.getMultiData(child);
//    }

    private static final class ContainerLayoutManager extends BaseMultiLayoutManager {

        @NonNull
        private final ContainerScene mScene;

        public ContainerLayoutManager(@NonNull ContainerScene layouter) {
            this.mScene = layouter;
        }

        private ContainerLayout getContainerLayout() {
            return (ContainerLayout) mScene.findViewByPosition(mScene.getPositionOffset());
        }

        @Override
        public void attachRecycler(MultiSceneRecycler recycler) {
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
                    RecyclerViewHelper.attachViewToParent(ContainerScene.ContainerLayoutManager.this, container, view, i, layoutParams);
                }

                @Override
                public void detachViewFromParent(int i) {
                    ContainerLayout container = getContainerLayout();
                    if (container == null) {
                        return;
                    }
                    RecyclerViewHelper.detachViewFromParent(ContainerScene.ContainerLayoutManager.this, container, i);
                }

                @Override
                public void onEnteredHiddenState(View view) {
                    RecyclerViewHelper.onEnteredHiddenState(ContainerScene.ContainerLayoutManager.this, view);
                }

                @Override
                public void onLeftHiddenState(View view) {
                    RecyclerViewHelper.onLeftHiddenState(ContainerScene.ContainerLayoutManager.this, view);
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
            return lp instanceof SceneLayoutParams;
        }

        @Override
        public RecyclerView.LayoutParams generateLayoutParams(LayoutParams lp) {
            return new SceneLayoutParams(lp);
        }

        @Override
        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            return new SceneLayoutParams(getContext(), attrs);
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new SceneLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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

    public static class ContainerLayoutHelper extends LayoutHelper {

        public ContainerLayoutHelper(ContainerScene scene) {
            super(scene);
        }

        @Override
        public void scrapOrRecycleView(int index, View view) {
            scrapOrRecycleAllViews();
            super.scrapOrRecycleView(index, view);
        }

        @Override
        public void addViewToRecycler(View view) {
            super.addViewToRecycler(view);
            scrapOrRecycleAllViews();
        }

        protected void scrapOrRecycleAllViews() {
            LayoutHelper helper = mScene.getLayoutHelper();
            for (int i = helper.getChildCount() - 1; i >= 0; --i) {
                View child = helper.getChildAt(i);
                helper.scrapOrRecycleView(i, child);
            }
        }

        @Override
        public void offsetChildLeftAndRight(@NonNull View child, int offset) {
            super.offsetChildLeftAndRight(child, offset);
        }
    }

    private static class ContainerLayouter extends AbsLayouter {

        private final Scene mChildScene;

        private ContainerLayouter(Scene scene) {
            mChildScene = scene;
        }


        @Override
        public boolean canScrollHorizontally() {
            return mChildScene.canScrollHorizontally();
        }

        @Override
        public boolean canScrollVertically() {
            return mChildScene.canScrollVertically();
        }

        @Override
        public int fillVertical(Scene scene, AnchorInfo anchorInfo, int dy) {
            int positionOffset = scene.getPositionOffset();
            View container = scene.findViewByPosition(positionOffset);
            boolean isNewContainer = container == null;
            if (isNewContainer) {
                container = scene.obtainViewForPosition(positionOffset);
                if (dy > 0) {
                    // 从下往上滑动
                    scene.addView(container);
                } else {
                    scene.addView(container, 0);
                }
            }

            ContainerLayout containerLayout = (ContainerLayout) container;
//        containerLayout.removeAllViews();

            View anchorView;
            if (dy > 0) {
                anchorView = containerLayout.getChildAt(containerLayout.getChildCount() - 1);
            } else {
                anchorView = containerLayout.getChildAt(0);
            }
            int consumed = mChildScene.fillVertical(anchorView, dy);

            int height = mChildScene.getBottom() - mChildScene.getTop();

            if (dy > 0) {
                // 从下往上滑动
                scene.setBottom(scene.getTop() + height);
            } else {
                scene.setTop(scene.getBottom() - height);
            }

            containerLayout.measure(View.MeasureSpec.makeMeasureSpec(scene.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));

            scene.layoutDecorated(containerLayout, scene.getLeft(), scene.getTop(), scene.getRight(), scene.getBottom());

            if (isNewContainer) {
                return consumed;
            }

            if (dy > 0) {
                // 从下往上滑动
                int bottom = scene.getBottom();
                if (bottom > scene.getHeight()) {
                    if (bottom - dy > scene.getHeight()) {
                        return dy;
                    } else {
                        return bottom - scene.getHeight();
                    }
                }
            } else {
                int top = scene.getTop();
                if (top < 0) {
                    if (top - dy < 0) {
                        return -dy;
                    } else {
                        return -top;
                    }
                }
            }
            return 0;
        }

        @Override
        protected int fillVerticalTop(Scene scene, AnchorInfo anchor, int availableSpace) {
            return 0;
        }

        @Override
        protected int fillVerticalBottom(Scene scene, AnchorInfo anchor, int availableSpace) {
            return 0;
        }

        @Override
        public int fillHorizontal(Scene scene, AnchorInfo anchorInfo, int dx) {
            View anchorView = (ViewGroup) scene.findViewByPosition(scene.getPositionOffset());
            if (anchorView != null) {
                anchorView = ((ViewGroup) anchorView).getChildAt(0);
            }
            return mChildScene.fillHorizontal(anchorView, dx);
        }
    }
}
