package com.zpj.recyclerview;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.footer.AbsFooterViewHolder;
import com.zpj.recyclerview.footer.DefaultFooterViewHolder;
import com.zpj.recyclerview.footer.IFooterViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MultiRecyclerViewWrapper extends EasyStateConfig<MultiRecyclerViewWrapper> {

    protected final RecyclerView recyclerView;

    protected final List<MultiData<?>> mMultiDataList = new ArrayList<>();

    protected IRefresh mRefresh;

    protected MultiAdapter easyAdapter;

    protected ItemTouchHelper mItemTouchHelper;

    protected IEasy.AdapterInjector adapterInjector;

    protected RecyclerView.LayoutManager layoutManager;

    protected View headerView;
    protected IEasy.OnBindHeaderListener onBindHeaderListener;
    //    protected IEasy.OnBindFooterListener onBindFooterListener;
//    protected View footerView;
    protected IFooterViewHolder footerViewBinder;

    public static MultiRecyclerViewWrapper with(@NonNull RecyclerView recyclerView) {
        return new MultiRecyclerViewWrapper(recyclerView);
    }


    private MultiRecyclerViewWrapper(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public MultiRecyclerViewWrapper setHeaderView(View headerView) {
        this.headerView = headerView;
        return this;
    }

    public MultiRecyclerViewWrapper setMultiData(Collection<MultiData<?>> list) {
        this.mMultiDataList.clear();
        this.mMultiDataList.addAll(list);
        return this;
    }

    public MultiRecyclerViewWrapper setMultiData(int index, MultiData<?> data) {
        this.mMultiDataList.set(index, data);
        return this;
    }

    public MultiRecyclerViewWrapper setMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.clear();
        this.mMultiDataList.addAll(Arrays.asList(arr));
        return this;
    }

    public MultiRecyclerViewWrapper addMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.addAll(Arrays.asList(arr));
        return this;
    }

    public MultiRecyclerViewWrapper addMultiData(int index, MultiData<?> ... arr) {
        this.mMultiDataList.addAll(index, Arrays.asList(arr));
        return this;
    }

    public MultiRecyclerViewWrapper addMultiData(MultiData<?> data) {
        this.mMultiDataList.add(data);
        return this;
    }

    public MultiRecyclerViewWrapper addMultiData(int index, MultiData<?> data) {
        this.mMultiDataList.add(index, data);
        return this;
    }

    public MultiRecyclerViewWrapper removeMultiData(MultiData<?> data) {
        this.mMultiDataList.remove(data);
        return this;
    }

    public MultiRecyclerViewWrapper removeMultiData(int index) {
        this.mMultiDataList.remove(index);
        return this;
    }

    public MultiRecyclerViewWrapper removeAllMultiData(Collection<MultiData<?>> list) {
        this.mMultiDataList.removeAll(list);
        return this;
    }

    public MultiRecyclerViewWrapper removeAllMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.removeAll(Arrays.asList(arr));
        return this;
    }

    public List<MultiData<?>> getMultiDataList() {
        return mMultiDataList;
    }

    @SuppressLint("ResourceType")
    public MultiRecyclerViewWrapper setHeaderView(@LayoutRes int layoutRes, IEasy.OnBindHeaderListener l) {
        if (layoutRes > 0 && l != null) {
            this.headerView = LayoutInflater.from(recyclerView.getContext()).inflate(layoutRes, null, false);
            onBindHeaderListener = l;
        }
        return this;
    }

//    public MultiRecyclerViewWrapper setFooterView(View headerView) {
//        this.footerView = headerView;
//        return this;
//    }
//
//    public MultiRecyclerViewWrapper setFooterView(@LayoutRes int layoutRes, IEasy.OnBindFooterListener listener) {
//        this.footerView = LayoutInflater.from(recyclerView.getContext()).inflate(layoutRes, null, false);
//        onBindFooterListener = listener;
//        return this;
//    }

    public MultiRecyclerViewWrapper setFooterViewBinder(IFooterViewHolder footerViewBinder) {
        this.footerViewBinder = footerViewBinder;
        return this;
    }

    public MultiRecyclerViewWrapper setFooterView(final View footerView) {
//        this.footerView = headerView;
        this.footerViewBinder = new AbsFooterViewHolder() {
            @Override
            public View onCreateFooterView(ViewGroup root) {
                return footerView;
            }
        };
        return this;
    }

    public MultiRecyclerViewWrapper setFooterView(@LayoutRes final int layoutRes, final IEasy.OnBindFooterListener listener) {
//        this.footerView = LayoutInflater.from(recyclerView.getContext()).inflate(layoutRes, null, false);
//        onBindFooterListener = listener;
        this.footerViewBinder = new AbsFooterViewHolder() {

            @Override
            public View onCreateFooterView(ViewGroup root) {
                return LayoutInflater.from(root.getContext()).inflate(layoutRes, null, false);
            }

            @Override
            public void onBindFooter(EasyViewHolder holder) {
                if (listener != null) {
                    listener.onBindFooter(holder);
                }
            }
        };
        return this;
    }

    public MultiRecyclerViewWrapper addItemDecoration(RecyclerView.ItemDecoration decor) {
        this.recyclerView.addItemDecoration(decor);
        return this;
    }

    public MultiRecyclerViewWrapper addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        this.recyclerView.addItemDecoration(decor, index);
        return this;
    }

    public MultiRecyclerViewWrapper setAdapterInjector(IEasy.AdapterInjector adapterInjector) {
        this.adapterInjector = adapterInjector;
        if (easyAdapter != null) {
            easyAdapter.setAdapterInjector(adapterInjector);
        }
        return this;
    }

    public MultiRecyclerViewWrapper onRefresh(IRefresh.OnRefreshListener listener) {
        mRefresh = new RefreshViewHolder();
        mRefresh.setOnRefreshListener(listener);
        return this;
    }

    public MultiRecyclerViewWrapper onRefresh(IRefresh refresh) {
        mRefresh = refresh;
        return this;
    }

    public MultiRecyclerViewWrapper onRefresh(IRefresh refresh, IRefresh.OnRefreshListener listener) {
        mRefresh = refresh;
        if (refresh != null) {
            refresh.setOnRefreshListener(listener);
        }
        return this;
    }

    public MultiRecyclerViewWrapper build() {
        easyAdapter = new MultiAdapter(recyclerView.getContext(), mMultiDataList, this, mRefresh);

        int maxSpan = 1;
        for (MultiData<?> data : mMultiDataList) {
            maxSpan = lcm(data.getMaxColumnCount(), maxSpan);
            data.setAdapter(easyAdapter);
            if (data instanceof IDragAndSwipe && mItemTouchHelper == null) {
                mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = easyAdapter.getRealPosition(viewHolder);
                        int count = 0;
                        for (MultiData<?> data : mMultiDataList) {
                            if (data instanceof IDragAndSwipe && position >= count && position < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return makeMovementFlags(dragAndSwipeMultiData.getDragDirection(position),
                                        dragAndSwipeMultiData.getSwipeDirection(position));
                            }
                            count  += data.getCount();
                        }
                        return 0;
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                        final int pos = easyAdapter.getRealPosition(viewHolder);
                        final int pos1 = easyAdapter.getRealPosition(viewHolder1);
                        int count = 0;
                        for (MultiData<?> data : mMultiDataList) {
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + data.getCount()
                                    && pos1 >= count && pos1 < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return dragAndSwipeMultiData.onMove(pos - count, pos1 - count);
                            }
                            count  += data.getCount();
                        }
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
                        final int pos = easyAdapter.getRealPosition(viewHolder);
                        int count = 0;
                        for (MultiData<?> data : mMultiDataList) {
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                dragAndSwipeMultiData.onSwiped(pos - count, i);
                                break;
                            }
                            count  += data.getCount();
                        }
                    }

                    @Override
                    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }

                    @Override
                    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }

                });
                mItemTouchHelper.attachToRecyclerView(recyclerView);
            }
        }
        layoutManager = new GridLayoutManager(recyclerView.getContext(), maxSpan);
        easyAdapter.setAdapterInjector(adapterInjector);
        if (headerView != null) {
            easyAdapter.setHeaderView(headerView);
            easyAdapter.setOnBindHeaderListener(onBindHeaderListener);
        }
        if (footerViewBinder != null) {
            easyAdapter.setFooterViewHolder(footerViewBinder);
        } else {
            easyAdapter.setFooterViewHolder(new DefaultFooterViewHolder());
        }
        easyAdapter.setLoadMoreEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(easyAdapter);
        easyAdapter.showContent();
        return this;
    }

    /**
     * 显示空视图
     */
    public final void showEmpty() {
        easyAdapter.showEmpty();
    }

    public void showEmptyView(int msgId) {
        easyAdapter.showEmptyView(msgId);
    }

    public void showEmptyView(String msg) {
        easyAdapter.showEmptyView(msg);
    }

    public void showEmptyView(int msgId, int imgId) {
        easyAdapter.showEmptyView(msgId, imgId);
    }

    public void showEmptyView(String msg, int imgId) {
        easyAdapter.showEmptyView(msg, imgId);
    }

    /**
     * 显示错误视图
     */
    public final void showError() {
        easyAdapter.showError();
    }

    public void showErrorView(int msgId) {
        easyAdapter.showErrorView(msgId);
    }

    public void showErrorView(String msg) {
        easyAdapter.showErrorView(msg);
    }

    public void showErrorView(int msgId, int imgId) {
        easyAdapter.showErrorView(msgId, imgId);
    }

    public void showErrorView(String msg, int imgId) {
        easyAdapter.showErrorView(msg, imgId);
    }

    /**
     * 显示加载中视图
     */
    public final void showLoading() {
        easyAdapter.showLoading();
    }

    public void showLoadingView(View view) {
        easyAdapter.showLoadingView(view);
    }

    public void showLoadingView(View view, boolean showTip) {
        easyAdapter.showLoadingView(view, showTip);
    }

    public void showLoadingView(int msgId) {
        easyAdapter.showLoadingView(msgId);
    }

    public void showLoadingView(String msg) {
        easyAdapter.showLoadingView(msg);
    }

    /**
     * 显示无网络视图
     */
    public final void showNoNetwork() {
        easyAdapter.showNoNetwork();
    }

    public void showNoNetworkView(int msgId) {
        easyAdapter.showNoNetworkView(msgId);
    }

    public void showNoNetworkView(String msg) {
        easyAdapter.showNoNetworkView(msg);
    }

    public void showNoNetworkView(int msgId, int imgId) {
        easyAdapter.showNoNetworkView(msgId, imgId);
    }

    /**
     * 显示内容视图
     */
    public final void showContent() {
        easyAdapter.showContent();
    }

    public void notifyDataSetChanged() {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyDataSetChanged();
    }

    public void notifyItemChanged(int position) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemChanged(position);
    }

    public void notifyItemChanged(int position, Object payload) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemChanged(position, payload);
    }

    public void notifyVisibleItemChanged() {
        notifyVisibleItemChanged(null);
    }

    public void notifyVisibleItemChanged(Object payload) {
        RecyclerView.LayoutManager manager = getLayoutManager();
        int first = -1;
        int last = -1;
        if (manager instanceof LinearLayoutManager) {
            first = ((LinearLayoutManager) manager).findFirstVisibleItemPosition();
            last = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
        } else if (manager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) manager;
            int[] firsts = new int[layoutManager.getSpanCount()];
            int[] into = new int[layoutManager.getSpanCount()];
            layoutManager.findFirstVisibleItemPositions(firsts);
            layoutManager.findLastVisibleItemPositions(into);
            first = first(firsts);
            last = last(into);
        }

        if (getAdapter().getHeaderView() != null) {
            first += 1;
        }

        if (last > first) {
            int count = last - first;
            if (last + 1 <= getData().size()) {
                count += 1;
            }
            if (payload == null) {
                notifyItemRangeChanged(first, count);
            } else {
                notifyItemRangeChanged(first, count, payload);
            }
        }


    }

    private int first(int[] firstPositions) {
        int first = firstPositions[0];
        for (int value : firstPositions) {
            if (value < first) {
                first = value;
            }
        }
        return first;
    }

    private int last(int[] lastPositions) {
        int last = lastPositions[0];
        for (int value : lastPositions) {
            if (value > last) {
                last = value;
            }
        }
        return last;
    }

    public void notifyItemInserted(int position) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemInserted(position);
    }

    public void notifyItemRangeChanged(int start, int itemCount) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemRangeChanged(start, itemCount);
    }

    public void notifyItemRangeChanged(int start, int itemCount, Object payload) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemRangeChanged(start, itemCount, payload);
    }

    public void notifyItemRemoved(int position) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemRemoved(position);
    }

    public void smoothScrollToPosition(int position) {
        recyclerView.smoothScrollToPosition(position);
    }

    public EasyStateAdapter<MultiData<?>> getAdapter() {
        return easyAdapter;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void post(Runnable runnable) {
        recyclerView.post(runnable);
    }

    public List<MultiData<?>> getData() {
        return mMultiDataList;
    }


    private int gcd(int x, int y) {
        return y == 0 ? x : gcd(y, x % y);
    }

    private int lcm(int x, int y) {
        return (x * y) / gcd(x, y);
    }


}
