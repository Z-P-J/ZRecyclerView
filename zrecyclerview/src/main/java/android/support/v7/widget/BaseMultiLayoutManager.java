package android.support.v7.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.core.SceneLayoutParams;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BaseMultiLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "MultiLayoutManager";

    public final Set<View> recycleViews = new LinkedHashSet<>();

    protected MultiSceneRecycler mRecycler;

    public void attachRecycler(MultiSceneRecycler recycler) {
        this.mRecycler = recycler;
    }

    @Override
    public boolean isAutoMeasureEnabled() {
        return true;
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof SceneLayoutParams;
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        return new SceneLayoutParams(lp);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new SceneLayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new SceneLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    public MultiSceneRecycler getRecycler() {
        return mRecycler;
    }

    public Scene getScene(View child) {
        if (child == null) {
            return null;
        }
        return ((SceneLayoutParams) child.getLayoutParams()).getScene();
    }

    public Layouter getLayouter(View child) {
        Scene scene = getScene(child);
        if (scene == null) {
            return null;
        }
        return scene.getLayouter();
    }

    public MultiData<?> getMultiData(View child) {
        Scene scene = getScene(child);
        if (scene == null) {
            return null;
        }
        return scene.getMultiData();
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
