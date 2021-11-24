package android.support.v7.widget;

import com.zpj.recyclerview.MultiRecycler;

public class RecyclerViewHelper {

    public static RecyclerView.Recycler getRecycler(MultiRecycler recycler) {
        return recycler.getRecyclerView().mRecycler;
    }

    public static RecyclerView.State getState(MultiRecycler recycler) {
        return recycler.getRecyclerView().mState;
    }

}
