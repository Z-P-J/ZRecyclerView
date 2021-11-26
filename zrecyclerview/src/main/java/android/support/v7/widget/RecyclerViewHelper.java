package android.support.v7.widget;

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

}
