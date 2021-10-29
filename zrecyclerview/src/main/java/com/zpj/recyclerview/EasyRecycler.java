package com.zpj.recyclerview;

import android.annotation.SuppressLint;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.footer.AbsFooterViewHolder;
import com.zpj.recyclerview.footer.DefaultFooterViewHolder;
import com.zpj.recyclerview.footer.IFooterViewHolder;
import com.zpj.recyclerview.refresh.DecorationRefresher;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.refresh.SimpleRefresher;
import com.zpj.recyclerview.refresh.SwipeDecorationRefresher;
import com.zpj.statemanager.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EasyRecycler<T> extends EasyStateConfig<EasyRecycler<T>>
        implements IEasy.OnLoadMoreListener {

    protected final RecyclerView recyclerView;

    protected RecyclerView.LayoutManager layoutManager;

    protected EasyStateAdapter<T> easyAdapter;

    protected IRefresher mRefresh;

    protected IEasy.AdapterInjector adapterInjector;

    protected final List<T> mDataSet;

    protected int itemRes = -1;

    protected View headerView;
    protected IEasy.OnBindHeaderListener onBindHeaderListener;
//    protected IEasy.OnBindFooterListener onBindFooterListener;
//    protected View footerView;
    protected IFooterViewHolder footerViewBinder;

    protected boolean enableLoadMore = false;

    protected IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener;
    protected IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener;
    protected IEasy.OnBindViewHolderListener<T> onBindViewHolderListener;
    protected IEasy.OnCreateViewHolderListener<T> onCreateViewHolder;
    protected IEasy.OnLoadMoreListener onLoadMoreListener;

    protected final SparseArray<IEasy.OnClickListener<T>> onClickListeners = new SparseArray<>();
    protected final SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners = new SparseArray<>();
    protected IEasy.OnItemClickListener<T> onItemClickListener;
    protected IEasy.OnItemLongClickListener<T> onItemLongClickListener;

    public EasyRecycler(@NonNull RecyclerView recyclerView) {
        this(recyclerView, new ArrayList<T>(0));
    }

    public EasyRecycler(@NonNull RecyclerView recyclerView, @NonNull List<T> dataSet) {
        this.recyclerView = recyclerView;
        this.mDataSet = dataSet;
    }

    public EasyRecycler<T> setItemAnimator(RecyclerView.ItemAnimator animator) {
        recyclerView.setItemAnimator(animator);
        return this;
    }

    public EasyRecycler<T> setItemRes(int res) {
        this.itemRes = res;
        return this;
    }

    public EasyRecycler<T> setData(Collection<T> list) {
        this.mDataSet.clear();
        this.mDataSet.addAll(list);
        return this;
    }

    public EasyRecycler<T> setData(T...dataSet) {
        return setData(Arrays.asList(dataSet));
    }

    public EasyRecycler<T> addData(Collection<T> list) {
        this.mDataSet.addAll(list);
        return this;
    }

    public EasyRecycler<T> addData(T...dataSet) {
        return addData(Arrays.asList(dataSet));
    }

    public EasyRecycler<T> addData(int index, Collection<T> list) {
        this.mDataSet.addAll(index, list);
        return this;
    }

    public EasyRecycler<T> addData(int index, T...dataSet) {
        return addData(index, Arrays.asList(dataSet));
    }

    public EasyRecycler<T> setData(int index, T data) {
        if (index >= 0 && index < this.mDataSet.size()) {
            this.mDataSet.set(index, data);
        }
        return this;
    }

    public EasyRecycler<T> addData(int index, T data) {
        if (index >= 0 && index < this.mDataSet.size()) {
            this.mDataSet.add(index, data);
        }
        return this;
    }

    public EasyRecycler<T> addData(T data) {
        this.mDataSet.add(data);
        return this;
    }


    public EasyRecycler<T> setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return this;
    }

    public EasyRecycler<T> addItemDecoration(RecyclerView.ItemDecoration decor) {
        this.recyclerView.addItemDecoration(decor);
        return this;
    }

    public EasyRecycler<T> addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        this.recyclerView.addItemDecoration(decor, index);
        return this;
    }

    public EasyRecycler<T> setItemViewCacheSize(int size) {
        recyclerView.setItemViewCacheSize(size);
        return this;
    }

    public EasyRecycler<T> setHasFixedSize(boolean hasFixedSize) {
        recyclerView.setHasFixedSize(hasFixedSize);
        return this;
    }

    public EasyRecycler<T> setLayoutFrozen(boolean layoutFrozen) {
        recyclerView.setLayoutFrozen(layoutFrozen);
        return this;
    }

    public EasyRecycler<T> setOnFlingListener(RecyclerView.OnFlingListener listener) {
        recyclerView.setOnFlingListener(listener);
        return this;
    }

    public EasyRecycler<T> setRecyclerListener(RecyclerView.RecyclerListener listener) {
        recyclerView.setRecyclerListener(listener);
        return this;
    }

    public EasyRecycler<T> setScrollingTouchSlop(int slop) {
        recyclerView.setScrollingTouchSlop(slop);
        return this;
    }

    public EasyRecycler<T> setEdgeEffectFactory(RecyclerView.EdgeEffectFactory factory) {
        recyclerView.setEdgeEffectFactory(factory);
        return this;
    }

    public EasyRecycler<T> setRecycledViewPool(RecyclerView.RecycledViewPool pool) {
        recyclerView.setRecycledViewPool(pool);
        return this;
    }

    public EasyRecycler<T> setNestedScrollingEnabled(boolean enabled) {
        recyclerView.setNestedScrollingEnabled(enabled);
        return this;
    }

    public EasyRecycler<T> setPreserveFocusAfterLayout(boolean preserveFocusAfterLayout) {
        recyclerView.setPreserveFocusAfterLayout(preserveFocusAfterLayout);
        return this;
    }

    public EasyRecycler<T> setViewCacheExtension(RecyclerView.ViewCacheExtension extension) {
        recyclerView.setViewCacheExtension(extension);
        return this;
    }

    public EasyRecycler<T> setChildDrawingOrderCallback(RecyclerView.ChildDrawingOrderCallback callback) {
        recyclerView.setChildDrawingOrderCallback(callback);
        return this;
    }

    public EasyRecycler<T> setHeaderView(View headerView) {
        this.headerView = headerView;
        return this;
    }

    @SuppressLint("ResourceType")
    public EasyRecycler<T> setHeaderView(@LayoutRes int layoutRes, IEasy.OnBindHeaderListener l) {
        if (layoutRes > 0 && l != null) {
            this.headerView = LayoutInflater.from(recyclerView.getContext()).inflate(layoutRes, null, false);
            onBindHeaderListener = l;
        }
        return this;
    }

    public EasyRecycler<T> setFooterViewBinder(IFooterViewHolder footerViewBinder) {
        this.footerViewBinder = footerViewBinder;
        return this;
    }

    public EasyRecycler<T> setFooterView(final View footerView) {
//        this.footerView = headerView;
        this.footerViewBinder = new AbsFooterViewHolder() {
            @Override
            public View onCreateFooterView(ViewGroup root) {
                return footerView;
            }
        };
        return this;
    }

    public EasyRecycler<T> setFooterView(@LayoutRes final int layoutRes, final IEasy.OnBindFooterListener listener) {
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

    public EasyRecycler<T> onLoadMore(IEasy.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        this.enableLoadMore = true;
        return this;
    }

    public EasyRecycler<T> onGetChildViewType(IEasy.OnGetChildViewTypeListener<T> listener) {
        this.onGetChildViewTypeListener = listener;
        return this;
    }

    public EasyRecycler<T> onGetChildLayoutId(IEasy.OnGetChildLayoutIdListener listener) {
        this.onGetChildLayoutIdListener = listener;
        return this;
    }

    public EasyRecycler<T> onBindViewHolder(IEasy.OnBindViewHolderListener<T> callback) {
        this.onBindViewHolderListener = callback;
        return this;
    }

    public EasyRecycler<T> onCreateViewHolder(IEasy.OnCreateViewHolderListener<T> callback) {
        this.onCreateViewHolder = callback;
        return this;
    }

    public IEasy.OnCreateViewHolderListener<T> getOnCreateViewHolder() {
        return onCreateViewHolder;
    }

    public EasyRecycler<T> addOnScrollListener(final RecyclerView.OnScrollListener onScrollListener) {
        recyclerView.addOnScrollListener(onScrollListener);
        return this;
    }

    public EasyRecycler<T> onViewClick(IEasy.OnClickListener<T> listener) {
        onClickListeners.put(View.NO_ID, listener);
        return this;
    }

    public EasyRecycler<T> onViewClick(@IdRes int id, IEasy.OnClickListener<T> listener) {
        onClickListeners.put(id, listener);
        return this;
    }

    public EasyRecycler<T> onViewClick(IEasy.OnClickListener<T> listener, int... ids) {
        for (int id : ids) {
            onClickListeners.put(id, listener);
        }
        return this;
    }

    public EasyRecycler<T> onViewLongClick(@IdRes int id, IEasy.OnLongClickListener<T> listener) {
        onLongClickListeners.put(id, listener);
        return this;
    }

    public EasyRecycler<T> onViewLongClick(IEasy.OnLongClickListener<T> listener, int... ids) {
        for (int id : ids) {
            onLongClickListeners.put(id, listener);
        }
        return this;
    }

    public EasyRecycler<T> onItemClick(IEasy.OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public EasyRecycler<T> onItemLongClick(IEasy.OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
        return this;
    }

    public EasyRecycler<T> setLoadMoreEnabled(boolean enabled) {
        this.enableLoadMore = enabled;
        return this;
    }

    public EasyRecycler<T> setAdapterInjector(IEasy.AdapterInjector adapterInjector) {
        this.adapterInjector = adapterInjector;
        if (easyAdapter != null) {
            easyAdapter.setAdapterInjector(adapterInjector);
        }
        return this;
    }

    public EasyRecycler<T> onRefresh(IRefresher.OnRefreshListener listener) {
        return onRefresh(new SwipeDecorationRefresher(), listener);
    }

    public EasyRecycler<T> onRefresh(IRefresher refresh) {
        mRefresh = refresh;
        if (refresh instanceof DecorationRefresher) {
            ((DecorationRefresher) refresh).bindRecyclerView(recyclerView);
        }
        return this;
    }

    public EasyRecycler<T> onRefresh(IRefresher refresh, IRefresher.OnRefreshListener listener) {
        onRefresh(refresh);
        if (refresh != null) {
            refresh.setOnRefreshListener(listener);
        }
        return this;
    }

    public void build() {
        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(recyclerView.getContext());
        }

        easyAdapter = new EasyStateAdapter<>(
                recyclerView.getContext(), mDataSet,
                itemRes, onGetChildViewTypeListener,
                onGetChildLayoutIdListener, onCreateViewHolder,
                onBindViewHolderListener, onItemClickListener,
                onItemLongClickListener, onClickListeners,
                onLongClickListeners, mRefresh, this
        );
        easyAdapter.setAdapterInjector(adapterInjector);
        if (headerView != null) {
            easyAdapter.setHeaderView(headerView);
            easyAdapter.setOnBindHeaderListener(onBindHeaderListener);
        }
        if (footerViewBinder != null) {
            easyAdapter.setFooterViewHolder(footerViewBinder);
//            easyAdapter.setFooterView(footerView);
//            easyAdapter.setOnBindFooterListener(onBindFooterListener);
        } else if (onLoadMoreListener != null && enableLoadMore) {
//            footerView = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.easy_base_footer, null, false);
//            easyAdapter.setFooterView(footerView);
//            easyAdapter.setOnBindFooterListener(onBindFooterListener);
            easyAdapter.setFooterViewHolder(new DefaultFooterViewHolder());
        }
        easyAdapter.setOnLoadMoreListener(this);
        easyAdapter.setLoadMoreEnabled(onLoadMoreListener != null && enableLoadMore);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(easyAdapter);
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
//            if (payload == null) {
//                notifyItemRangeChanged(first, last - first + 1);
//            } else {
//                notifyItemRangeChanged(first, last - first + 1, payload);
//            }
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
            if (last + 1 <= getDataSet().size()) {
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

    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    public void scrollToFirstPosition() {
        scrollToPosition(0);
    }

    public void scrollToLastPosition() {
        scrollToPosition(getCount() - 1);
    }

    public void smoothScrollToPosition(int position) {
        recyclerView.smoothScrollToPosition(position);
    }

    public void smoothScrollToFirstPosition() {
        smoothScrollToPosition(0);
    }

    public void smoothScrollToLastPosition() {
        smoothScrollToPosition(getCount() - 1);
    }

    public void scrollBy(int x, int y) {
        recyclerView.scrollBy(x, y);
    }

    public EasyStateAdapter<T> getAdapter() {
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

    public void postDelayed(Runnable runnable, long delayMillis) {
        recyclerView.postDelayed(runnable, delayMillis);
    }

    public List<T> getDataSet() {
        return this.mDataSet;
    }

    public void removeData(T data) {
        this.mDataSet.remove(data);
    }

    public void removeData(T...dataSet) {
        this.mDataSet.removeAll(Arrays.asList(dataSet));
    }

    public void removeData(Collection<T> dataSet) {
        this.mDataSet.removeAll(dataSet);
    }

    public void removeData(int index) {
        this.mDataSet.remove(index);
    }

    public void removeData(int fromIndex, int toIndex) {
        this.mDataSet.subList(fromIndex, toIndex).clear();
    }

    public int getCount() {
        return this.mDataSet.size();
    }

    public List<T> subDataList(int fromIndex, int toIndex) {
        return this.mDataSet.subList(fromIndex, toIndex);
    }

    public void clearDataSet() {
        this.mDataSet.clear();
    }

    public boolean containsData(T data) {
        return this.mDataSet.contains(data);
    }

    public T getData(int index) {
        return this.mDataSet.get(index);
    }

    public T getFirstData() {
        return getData(0);
    }

    public T getLastData() {
        return getData(getCount() - 1);
    }

    public boolean isEmpty() {
        return this.mDataSet.isEmpty();
    }

    public State getState() {
        return getAdapter().getState();
    }

    public void setVisibility(int visibility) {
        recyclerView.setVisibility(visibility);
    }

    public void sortDataSet(Comparator<? super T> comparator) {
        Collections.sort(this.mDataSet, comparator);
    }

    @Override
    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
        if (onLoadMoreListener != null) {
            return onLoadMoreListener.onLoadMore(enabled, currentPage);
        }
        return false;
    }
}
