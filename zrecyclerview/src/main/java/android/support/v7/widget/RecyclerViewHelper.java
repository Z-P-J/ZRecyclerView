package android.support.v7.widget;

import android.view.View;
import android.view.animation.Interpolator;

import com.zpj.recyclerview.MultiRecycler;

public class RecyclerViewHelper {

    public static RecyclerView.Recycler getRecycler(MultiRecycler recycler) {
        return recycler.getRecyclerView().mRecycler;
    }

    public static RecyclerView.State getState(MultiRecycler recycler) {
        return recycler.getRecyclerView().mState;
    }

    public static Interpolator getInterpolator() {
        return RecyclerView.sQuinticInterpolator;
    }

    public static void scrapOrRecycleView(RecyclerView.LayoutManager manager, int index, View view) {
        RecyclerView.ViewHolder viewHolder = RecyclerView.getChildViewHolderInt(view);
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

}
