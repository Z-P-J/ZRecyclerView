package android.support.v7.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.scene.ContainerScene;

public class RecyclerViewHelper {

    public static RecyclerView.Recycler getRecycler(MultiSceneRecycler recycler) {
        return getRecycler(recycler.getRecyclerView());
    }

    public static RecyclerView.Recycler getRecycler(RecyclerView recyclerView) {
        return recyclerView.mRecycler;
    }

    public static RecyclerView.State getState(MultiSceneRecycler recycler) {
        return recycler.getRecyclerView().mState;
    }

    public static Interpolator getInterpolator() {
        return RecyclerView.sQuinticInterpolator;
    }

    public static void startInterceptRequestLayout(RecyclerView.LayoutManager manager) {
        manager.mRecyclerView.startInterceptRequestLayout();
    }

    public static void stopInterceptRequestLayout(RecyclerView.LayoutManager manager) {
        manager.mRecyclerView.stopInterceptRequestLayout(false);
    }

    public static void dispatchLayout(BaseMultiLayoutManager manager) {

//        manager.onLayoutChildren(manager.mRecyclerView.mRecycler, manager.mRecyclerView.mState);

        manager.mRecyclerView.dispatchLayout();
    }

    public static void scrapOrRecycleView(RecyclerView.LayoutManager manager, int index, View view) {
        RecyclerView.ViewHolder viewHolder = getChildViewHolderInt(view);
        if (!viewHolder.shouldIgnore()) {
            if (viewHolder.isInvalid() && !viewHolder.isRemoved() && !manager.mRecyclerView.mAdapter.hasStableIds()) {
                manager.removeViewAt(index);
                manager.mRecyclerView.mRecycler.recycleViewHolderInternal(viewHolder);
            } else {
                manager.detachViewAt(index);
                manager.mRecyclerView.mRecycler.scrapView(view);
                manager.mRecyclerView.mViewInfoStore.onViewDetached(viewHolder);
            }
        }
    }

    public static RecyclerView.ViewHolder getChildViewHolderInt(View child) {
        return RecyclerView.getChildViewHolderInt(child);
    }

    public static void attachViewToParent(RecyclerView.LayoutManager manager,
                                          ContainerScene.ContainerLayout container,
                                          View child, int index, ViewGroup.LayoutParams layoutParams) {
        RecyclerView.ViewHolder vh = getChildViewHolderInt(child);
        if (vh != null) {
            if (!vh.isTmpDetached() && !vh.shouldIgnore()) {
                throw new IllegalArgumentException("Called attach on a child which is not detached: " + vh + manager.mRecyclerView.exceptionLabel());
            }

            vh.clearTmpDetachFlag();
        }
        container.attachViewToParent(child, index, layoutParams);
    }

    public static void detachViewFromParent(RecyclerView.LayoutManager manager,
                                            ContainerScene.ContainerLayout container,
                                            int offset) {
        View child = container.getChildAt(offset);
        if (child != null) {
            RecyclerView.ViewHolder vh = getChildViewHolderInt(child);
            if (vh != null) {
                if (vh.isTmpDetached() && !vh.shouldIgnore()) {
                    throw new IllegalArgumentException("called detach on an already detached child " + vh + manager.mRecyclerView.exceptionLabel());
                }

                vh.addFlags(256);
            }
        }
        container.detachViewFromParent(offset);
    }

    public static void onEnteredHiddenState(RecyclerView.LayoutManager manager, View child) {
        RecyclerView.ViewHolder vh = getChildViewHolderInt(child);
        if (vh != null) {
            vh.onEnteredHiddenState(manager.mRecyclerView);
        }
    }

    public static void onLeftHiddenState(RecyclerView.LayoutManager manager, View child) {
        RecyclerView.ViewHolder vh = getChildViewHolderInt(child);
        if (vh != null) {
            vh.onLeftHiddenState(manager.mRecyclerView);
        }
    }

}
