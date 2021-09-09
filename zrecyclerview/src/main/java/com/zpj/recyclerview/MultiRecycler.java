package com.zpj.recyclerview;

import android.annotation.SuppressLint;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.zpj.recyclerview.refresh.DecorationRefresher;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.refresh.SimpleRefresher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MultiRecycler extends EasyStateConfig<MultiRecycler> {

    protected final RecyclerView recyclerView;

    protected final List<MultiData<?>> mMultiDataList = new ArrayList<>();

    protected IRefresher mRefresh;

    protected MultiAdapter easyAdapter;

    protected ItemTouchHelper mItemTouchHelper;

    protected IEasy.AdapterInjector adapterInjector;

    protected RecyclerView.LayoutManager layoutManager;

    protected View headerView;
    protected IEasy.OnBindHeaderListener onBindHeaderListener;
    //    protected IEasy.OnBindFooterListener onBindFooterListener;
//    protected View footerView;
    protected IFooterViewHolder footerViewBinder;

    public static MultiRecycler with(@NonNull RecyclerView recyclerView) {
        return new MultiRecycler(recyclerView);
    }


    private MultiRecycler(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public MultiRecycler setHeaderView(View headerView) {
        this.headerView = headerView;
        return this;
    }

    public MultiRecycler setMultiData(Collection<MultiData<?>> list) {
        this.mMultiDataList.clear();
        this.mMultiDataList.addAll(list);
        return this;
    }

    public MultiRecycler setMultiData(int index, MultiData<?> data) {
        this.mMultiDataList.set(index, data);
        return this;
    }

    public MultiRecycler setMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.clear();
        this.mMultiDataList.addAll(Arrays.asList(arr));
        return this;
    }

    public MultiRecycler addMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.addAll(Arrays.asList(arr));
        return this;
    }

    public MultiRecycler addMultiData(int index, MultiData<?> ... arr) {
        this.mMultiDataList.addAll(index, Arrays.asList(arr));
        return this;
    }

    public MultiRecycler addMultiData(MultiData<?> data) {
        this.mMultiDataList.add(data);
        return this;
    }

    public MultiRecycler addMultiData(int index, MultiData<?> data) {
        this.mMultiDataList.add(index, data);
        return this;
    }

    public MultiRecycler removeMultiData(MultiData<?> data) {
        this.mMultiDataList.remove(data);
        return this;
    }

    public MultiRecycler removeMultiData(int index) {
        this.mMultiDataList.remove(index);
        return this;
    }

    public MultiRecycler removeAllMultiData(Collection<MultiData<?>> list) {
        this.mMultiDataList.removeAll(list);
        return this;
    }

    public MultiRecycler removeAllMultiData(MultiData<?> ... arr) {
        this.mMultiDataList.removeAll(Arrays.asList(arr));
        return this;
    }

    public List<MultiData<?>> getMultiDataList() {
        return mMultiDataList;
    }

    @SuppressLint("ResourceType")
    public MultiRecycler setHeaderView(@LayoutRes int layoutRes, IEasy.OnBindHeaderListener l) {
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

    public MultiRecycler setFooterViewBinder(IFooterViewHolder footerViewBinder) {
        this.footerViewBinder = footerViewBinder;
        return this;
    }

    public MultiRecycler setFooterView(final View footerView) {
//        this.footerView = headerView;
        this.footerViewBinder = new AbsFooterViewHolder() {
            @Override
            public View onCreateFooterView(ViewGroup root) {
                return footerView;
            }
        };
        return this;
    }

    public MultiRecycler setFooterView(@LayoutRes final int layoutRes, final IEasy.OnBindFooterListener listener) {
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

    public MultiRecycler addItemDecoration(RecyclerView.ItemDecoration decor) {
        this.recyclerView.addItemDecoration(decor);
        return this;
    }

    public MultiRecycler addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        this.recyclerView.addItemDecoration(decor, index);
        return this;
    }

    public MultiRecycler setAdapterInjector(IEasy.AdapterInjector adapterInjector) {
        this.adapterInjector = adapterInjector;
        if (easyAdapter != null) {
            easyAdapter.setAdapterInjector(adapterInjector);
        }
        return this;
    }

    public MultiRecycler onRefresh(IRefresher.OnRefreshListener listener) {
        mRefresh = new SimpleRefresher();
        mRefresh.setOnRefreshListener(listener);
        return this;
    }

    public MultiRecycler onRefresh(IRefresher refresh) {
        mRefresh = refresh;
        if (refresh instanceof DecorationRefresher) {
            ((DecorationRefresher) refresh).bindRecyclerView(recyclerView);
        }
        return this;
    }

    public MultiRecycler onRefresh(IRefresher refresh, IRefresher.OnRefreshListener listener) {
        onRefresh(refresh);
        if (refresh != null) {
            refresh.setOnRefreshListener(listener);
        }
        return this;
    }

    public MultiRecycler build() {
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


//                        int posFrom = easyAdapter.getRealPosition(viewHolder);
//                        int posTo = easyAdapter.getRealPosition(viewHolder1);
//
//                        final int realFrom = posFrom;
//                        final int realTo = posTo;
//                        MultiData from = null;
//                        MultiData to = null;
//                        int count = 0;
//                        for (MultiData<?> data : mMultiDataList) {
//                            if (data instanceof IDragAndSwipe) {
//
//                                if (from == null && posFrom >= count && posFrom < count + data.getCount()) {
//                                    posFrom -= count;
//                                    from = data;
//                                }
//                                if (to == null && posTo >= count && posTo < count + data.getCount()) {
//                                    posTo -= count;
//                                    to = data;
//                                }
//                            }
//                            if (from != null && to != null) {
//                                break;
//                            }
//                            count  += data.getCount();
//                        }
//                        if (from != null && to != null) {
//                            if (from == to) {
//                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) from;
//                                return dragAndSwipeMultiData.onMove(posFrom, posTo);
//                            } else {
//                                Object fromObj = from.getData().get(posFrom);
//                                Object toObj = to.getData().get(posTo);
//                                Log.d("MultiRecycler", "fromObj=" + fromObj + " toObj=" + toObj);
//                                if (fromObj != null && toObj != null
//                                        && fromObj.getClass().isAssignableFrom(toObj.getClass())
//                                        && toObj.getClass().isAssignableFrom(fromObj.getClass())) {
//                                    from.getData().set(posFrom, toObj);
//                                    to.getData().set(posTo, fromObj);
//                                    notifyItemMoved(realFrom, realTo);
//                                }
//                            }
//                        }
                        return false;
                    }

                    @Override
                    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                        super.onSelectedChanged(viewHolder, actionState);
                        easyAdapter.mIsDraggingOrSwiping = actionState != ItemTouchHelper.ACTION_STATE_IDLE;
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

    public void notifyItemMoved(int fromPosition, int toPosition) {
        if (easyAdapter == null) {
            return;
        }
        easyAdapter.notifyItemMoved(fromPosition, toPosition);
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
