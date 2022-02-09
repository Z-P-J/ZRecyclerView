package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.manager.MultiLayoutParams;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BaseMultiLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "MultiLayoutManager";

    public final Set<View> recycleViews = new LinkedHashSet<>();

    protected MultiRecycler mRecycler;

    public void attachRecycler(MultiRecycler recycler) {
        this.mRecycler = recycler;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof MultiLayoutParams;
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new MultiLayoutParams(lp);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new MultiLayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new MultiLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void setRecyclerView(RecyclerView recyclerView) {
        super.setRecyclerView(recyclerView);
    }

    public boolean isOverScrolling() {
        return false;
    }

    public void setRecyclerView(RecyclerView recyclerView, ChildHelper childHelper) {
        super.setRecyclerView(recyclerView);
        mChildHelper = recyclerView == null ? null : childHelper;
    }

    public View getViewForPosition(int position) {
        return RecyclerViewHelper.getRecycler(mRecycler).getViewForPosition(position);
    }

    public void detachAndScrapView(@NonNull View child) {
        detachAndScrapView(child, RecyclerViewHelper.getRecycler(mRecycler));
    }

    public MultiRecycler getRecycler() {
        return mRecycler;
    }

    public MultiData<?> getMultiData(View child) {
        if (child == null) {
            return null;
        }
        return ((MultiLayoutParams) child.getLayoutParams()).getMultiData();
    }

    public Layouter getLayouter(View child) {
        MultiData<?> multiData = getMultiData(child);
        if (multiData == null) {
            return null;
        }
        return multiData.getLayouter();
    }

    public View getFirstChild() {
        return getChildAt(0);
    }

    public View getLastChild() {
        return getChildAt(getChildCount() - 1);
    }

    public int indexOfChild(View child) {
        return mChildHelper.indexOfChild(child);
    }

    public void recycleViews() {
        RecyclerView.Recycler recycler = RecyclerViewHelper.getRecycler(mRecycler);
        for (View view : recycleViews) {
            Log.d(TAG, "recycleViews pos=" + getPosition(view));
            detachAndScrapView(view, recycler);
        }
        recycleViews.clear();

        List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
        for (int i = 0; i < scrapList.size(); i++) {
            removeAndRecycleView(scrapList.get(i).itemView, recycler);
        }
    }

    public void recycleView(View view) {
        RecyclerViewHelper.getRecycler(mRecycler).recycleView(view);
    }

    public void offsetChildLeftAndRight(@NonNull View child, int offset) {
        if (offset != 0) {
            child.offsetLeftAndRight(offset);
        }
    }

    public int getCount(MultiData<?> multiData) {
        return multiData.getCount();
    }

    public RecyclerView getRecycleView() {
        return mRecyclerView;
    }

    public void dispatchChildAttached(View child) {
        mRecyclerView.dispatchChildAttached(child);
    }

    public void dispatchChildDetached(View child) {
        mRecyclerView.dispatchChildDetached(child);
    }

}
