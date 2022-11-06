package com.zpj.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.footer.AbsFooterViewHolder;
import com.zpj.recyclerview.footer.DefaultFooterViewHolder;
import com.zpj.recyclerview.footer.IFooterViewHolder;
import com.zpj.recyclerview.refresh.DecorationRefresher;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.refresh.SwipeDecorationRefresher;
import com.zpj.statemanager.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BaseRecycler<T, R extends BaseRecycler<T, R>> extends EasyStateConfig<BaseRecycler<T, R>>
        implements IEasy.OnLoadMoreListener {

    protected final RecyclerView recyclerView;

    protected RecyclerView.LayoutManager layoutManager;

    protected EasyStateAdapter<T> easyAdapter;

    protected IRefresher mRefresher;

    protected IEasy.AdapterInjector adapterInjector;

    protected final List<T> mItems;

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

    public BaseRecycler(@NonNull RecyclerView recyclerView) {
        this(recyclerView, new ArrayList<T>(0));
    }

    public BaseRecycler(@NonNull RecyclerView recyclerView, @NonNull List<T> items) {
        this.recyclerView = recyclerView;
        this.mItems = items;
    }

    @Override
    public boolean onLoadMore(int currentPage) {
        if (onLoadMoreListener != null) {
            return onLoadMoreListener.onLoadMore(currentPage);
        }
        return false;
    }

    protected R self() {
        return (R) this;
    }

    public R setItemAnimator(RecyclerView.ItemAnimator animator) {
        recyclerView.setItemAnimator(animator);
        return self();
    }

    public R setItemRes(int res) {
        this.itemRes = res;
        return self();
    }

    public R setItems(Collection<T> list) {
        this.mItems.clear();
        this.mItems.addAll(list);
        return self();
    }

    public R setItems(T...items) {
        return setItems(Arrays.asList(items));
    }

    public R addItems(Collection<T> list) {
        this.mItems.addAll(list);
        return self();
    }

    public R addItems(T...items) {
        return addItems(Arrays.asList(items));
    }

    public R addItems(int index, Collection<T> list) {
        this.mItems.addAll(index, list);
        return self();
    }

    public R addItems(int index, T...items) {
        return addItems(index, Arrays.asList(items));
    }

    public R setItem(int index, T item) {
        if (index >= 0 && index < this.mItems.size()) {
            this.mItems.set(index, item);
        }
        return self();
    }

    public R addItem(int index, T item) {
        if (index >= 0 && index < this.mItems.size()) {
            this.mItems.add(index, item);
        }
        return self();
    }

    public R addItem(T item) {
        this.mItems.add(item);
        return self();
    }


    public R setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        return self();
    }

    public R addItemDecoration(RecyclerView.ItemDecoration decor) {
        this.recyclerView.addItemDecoration(decor);
        return self();
    }

    public R addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        this.recyclerView.addItemDecoration(decor, index);
        return self();
    }

    public R addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        this.recyclerView.addOnItemTouchListener(listener);
        return self();
    }

    public R addOnChildAttachStateChangeListener(RecyclerView.OnChildAttachStateChangeListener listener) {
        this.recyclerView.addOnChildAttachStateChangeListener(listener);
        return self();
    }

    public R setItemViewCacheSize(int size) {
        recyclerView.setItemViewCacheSize(size);
        return self();
    }

    public R setHasFixedSize(boolean hasFixedSize) {
        recyclerView.setHasFixedSize(hasFixedSize);
        return self();
    }

    public R setLayoutFrozen(boolean layoutFrozen) {
        recyclerView.setLayoutFrozen(layoutFrozen);
        return self();
    }

    public R setOnFlingListener(RecyclerView.OnFlingListener listener) {
        recyclerView.setOnFlingListener(listener);
        return self();
    }

    public R setRecyclerListener(RecyclerView.RecyclerListener listener) {
        recyclerView.setRecyclerListener(listener);
        return self();
    }

    public R setScrollingTouchSlop(int slop) {
        recyclerView.setScrollingTouchSlop(slop);
        return self();
    }

    public R setEdgeEffectFactory(RecyclerView.EdgeEffectFactory factory) {
        recyclerView.setEdgeEffectFactory(factory);
        return self();
    }

    public R setRecycledViewPool(RecyclerView.RecycledViewPool pool) {
        recyclerView.setRecycledViewPool(pool);
        return self();
    }

    public R setNestedScrollingEnabled(boolean enabled) {
        recyclerView.setNestedScrollingEnabled(enabled);
        return self();
    }

    public R setPreserveFocusAfterLayout(boolean preserveFocusAfterLayout) {
        recyclerView.setPreserveFocusAfterLayout(preserveFocusAfterLayout);
        return self();
    }

    public R setViewCacheExtension(RecyclerView.ViewCacheExtension extension) {
        recyclerView.setViewCacheExtension(extension);
        return self();
    }

    public R setChildDrawingOrderCallback(RecyclerView.ChildDrawingOrderCallback callback) {
        recyclerView.setChildDrawingOrderCallback(callback);
        return self();
    }

    public R setHeaderView(View headerView) {
        this.headerView = headerView;
        return self();
    }

    @SuppressLint("ResourceType")
    public R setHeaderView(@LayoutRes int layoutRes, IEasy.OnBindHeaderListener l) {
        if (layoutRes > 0 && l != null) {
            this.headerView = LayoutInflater.from(recyclerView.getContext()).inflate(layoutRes, null, false);
            onBindHeaderListener = l;
        }
        return self();
    }

    public R setFooterViewBinder(IFooterViewHolder footerViewBinder) {
        this.footerViewBinder = footerViewBinder;
        return self();
    }

    public R setFooterView(final View footerView) {
//        this.footerView = headerView;
        this.footerViewBinder = new AbsFooterViewHolder() {
            @Override
            public View onCreateFooterView(ViewGroup root) {
                return footerView;
            }
        };
        return self();
    }

    public R setFooterView(@LayoutRes final int layoutRes, final IEasy.OnBindFooterListener listener) {
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
        return self();
    }

    public R onLoadMore(IEasy.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        this.enableLoadMore = true;
        return self();
    }

    public R onGetChildViewType(IEasy.OnGetChildViewTypeListener<T> listener) {
        this.onGetChildViewTypeListener = listener;
        return self();
    }

    public R onGetChildLayoutId(IEasy.OnGetChildLayoutIdListener listener) {
        this.onGetChildLayoutIdListener = listener;
        return self();
    }

    public R onBindViewHolder(IEasy.OnBindViewHolderListener<T> callback) {
        this.onBindViewHolderListener = callback;
        return self();
    }

    public R onCreateViewHolder(IEasy.OnCreateViewHolderListener<T> callback) {
        this.onCreateViewHolder = callback;
        return self();
    }

    public IEasy.OnCreateViewHolderListener<T> getOnCreateViewHolder() {
        return onCreateViewHolder;
    }

    public R addOnScrollListener(final RecyclerView.OnScrollListener onScrollListener) {
        recyclerView.addOnScrollListener(onScrollListener);
        return self();
    }

    public R onViewClick(IEasy.OnClickListener<T> listener) {
        onClickListeners.put(View.NO_ID, listener);
        return self();
    }

    public R onViewClick(@IdRes int id, IEasy.OnClickListener<T> listener) {
        onClickListeners.put(id, listener);
        return self();
    }

    public R onViewClick(IEasy.OnClickListener<T> listener, int... ids) {
        for (int id : ids) {
            onClickListeners.put(id, listener);
        }
        return self();
    }

    public R onViewLongClick(@IdRes int id, IEasy.OnLongClickListener<T> listener) {
        onLongClickListeners.put(id, listener);
        return self();
    }

    public R onViewLongClick(IEasy.OnLongClickListener<T> listener, int... ids) {
        for (int id : ids) {
            onLongClickListeners.put(id, listener);
        }
        return self();
    }

    public R onItemClick(IEasy.OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
        return self();
    }

    public R onItemLongClick(IEasy.OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
        return self();
    }

    public R setLoadMoreEnabled(boolean enabled) {
        this.enableLoadMore = enabled;
        return self();
    }

    public R setAdapterInjector(IEasy.AdapterInjector adapterInjector) {
        this.adapterInjector = adapterInjector;
        if (easyAdapter != null) {
            easyAdapter.setAdapterInjector(adapterInjector);
        }
        return self();
    }

    public R onRefresh(IRefresher.OnRefreshListener listener) {
        return onRefresh(new SwipeDecorationRefresher(), listener);
    }

    public R onRefresh(IRefresher refresh) {
        mRefresher = refresh;
        if (refresh instanceof DecorationRefresher) {
            ((DecorationRefresher) refresh).bindRecyclerView(recyclerView);
        }
        return self();
    }

    public R onRefresh(IRefresher refresh, IRefresher.OnRefreshListener listener) {
        onRefresh(refresh);
        if (refresh != null) {
            refresh.setOnRefreshListener(listener);
        }
        return self();
    }

    public R build() {
        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(recyclerView.getContext());
        }

        easyAdapter = new EasyStateAdapter<>(
                recyclerView.getContext(), mItems,
                itemRes, onGetChildViewTypeListener,
                onGetChildLayoutIdListener, onCreateViewHolder,
                onBindViewHolderListener, onItemClickListener,
                onItemLongClickListener, onClickListeners,
                onLongClickListeners, mRefresher, this
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
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(easyAdapter);
        return self();
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
        showContent();
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
            if (last + 1 <= getItems().size()) {
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
        scrollToPosition(getItemCount() - 1);
    }

    public void smoothScrollToPosition(int position) {
        recyclerView.smoothScrollToPosition(position);
    }

    public void smoothScrollToFirstPosition() {
        smoothScrollToPosition(0);
    }

    public void smoothScrollToLastPosition() {
        smoothScrollToPosition(getItemCount() - 1);
    }

    public void scrollBy(int x, int y) {
        recyclerView.scrollBy(x, y);
    }

    public EasyStateAdapter<T> getAdapter() {
        return easyAdapter;
    }

    public Context getContext() {
        return recyclerView.getContext();
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

    public void removeCallbacks(Runnable runnable) {
        recyclerView.removeCallbacks(runnable);
    }

    public List<T> getItems() {
        return this.mItems;
    }

    public void removeItem(T item) {
        this.mItems.remove(item);
    }

    public void removeItem(int index) {
        this.mItems.remove(index);
    }

    public void removeItems(T...items) {
        this.mItems.removeAll(Arrays.asList(items));
    }

    public void removeItems(Collection<T> items) {
        this.mItems.removeAll(items);
    }

    public void removeItems(int fromIndex, int toIndex) {
        this.mItems.subList(fromIndex, toIndex).clear();
    }

    public int getItemCount() {
        return this.mItems.size();
    }

    public List<T> subItems(int fromIndex, int toIndex) {
        return this.mItems.subList(fromIndex, toIndex);
    }

    public void clearItems() {
        this.mItems.clear();
    }

    public boolean containsItem(T item) {
        return this.mItems.contains(item);
    }

    public T getItemAt(int index) {
        return this.mItems.get(index);
    }

    public T getFirstItem() {
        return getItemAt(0);
    }

    public T getLastItem() {
        return getItemAt(getItemCount() - 1);
    }

    public boolean isEmpty() {
        return this.mItems.isEmpty();
    }

    public State getState() {
        return getAdapter().getState();
    }

    public void setVisibility(int visibility) {
        recyclerView.setVisibility(visibility);
    }

    public void sortItems(Comparator<? super T> comparator) {
        Collections.sort(this.mItems, comparator);
    }

    public IFooterViewHolder getFooterViewHolder() {
        return easyAdapter.getFooterViewHolder();
    }

    public boolean isRefreshing() {
        if (mRefresher == null) {
            return false;
        }
        return mRefresher.isRefreshing();
    }

    public IRefresher getRefresher() {
        return mRefresher;
    }

    public void setRefreshing(boolean refreshing) {
        // TODO
//        refreshLayout.setRefreshing(refreshing);
    }

    public void startRefresh() {
        // TODO
//        if (!refreshLayout.isRefreshing()) {
//            refreshLayout.setRefreshing(true);
//        }
    }

    public void stopRefresh() {
        // TODO
//        if (refreshLayout.isRefreshing()) {
//            refreshLayout.setRefreshing(false);
//        }
    }

}
