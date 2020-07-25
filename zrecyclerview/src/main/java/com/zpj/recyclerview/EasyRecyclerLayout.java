package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.zpj.widget.checkbox.SmoothCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EasyRecyclerLayout<T> extends FrameLayout
        implements IEasy.OnLoadMoreListener,
        IEasy.OnLoadRetryListener,
        IEasy.OnItemClickListener<T>,
        IEasy.OnItemLongClickListener<T>,
        IEasy.OnGetChildViewTypeListener<T>,
        IEasy.OnGetChildLayoutIdListener,
        IEasy.OnCreateViewHolderListener<T>,
        IEasy.OnSelectChangeListener<T>,
        IEasy.OnBindViewHolderListener<T> {

    private static final String TAG = "EasyRecyclerLayout";

    private final Set<Integer> selectedList = new ArraySet<>();

    private IEasy.OnItemClickListener<T> onItemClickListener;
    private IEasy.OnItemLongClickListener<T> onItemLongClickListener;
    private IEasy.OnLoadMoreListener onLoadMoreListener;
    private IEasy.OnLoadRetryListener onLoadRetryListener;
    private IEasy.OnSelectChangeListener<T> onSelectChangeListener;
    private IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener;
    private IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener;
    private IEasy.OnCreateViewHolderListener<T> onCreateViewHolderListener;
    private IEasy.OnBindViewHolderListener<T> onBindViewHolderListener;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private EasyRecyclerView<T> easyRecyclerView;
    private EasyStateAdapter<T> adapter;
    private SwipeRefreshLayout refreshLayout;

    private int itemRes = -1;

    private boolean showCheckBox = false;

    private boolean selectMode = false;

    private boolean enableSwipeRefresh = false;

    private boolean enableLoadMore = false;

    private boolean enableSelection = true;

    public EasyRecyclerLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EasyRecyclerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EasyRecyclerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.easy_layout_recycler, this);
        refreshLayout = view.findViewById(R.id.layout_swipe_refresh);
        refreshLayout.setEnabled(false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                refreshLayout.setEnabled(!isSelectMode() && enableSwipeRefresh && topRowVerticalPosition >= 0);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        easyRecyclerView = new EasyRecyclerView<>(recyclerView);
        easyRecyclerView.setItemRes(R.layout.easy_item_recycler_layout)
                .onCreateViewHolder(this)
                .onGetChildViewType(this)
                .onBindViewHolder(this)
                .onItemLongClick(this)
                .onItemClick(this)
                .onLoadMore(this)
                .setOnLoadRetryListener(this);

    }

    @Override
    public int onGetViewType(List<T> list, int position) {
        if (onGetChildViewTypeListener != null) {
            return onGetChildViewTypeListener.onGetViewType(list, position);
        }
        return 0;
    }

    @Override
    public int onGetChildLayoutId(int viewType) {
        int res;
        if (onGetChildLayoutIdListener != null) {
            res = onGetChildLayoutIdListener.onGetChildLayoutId(viewType);
            if (res <= 0) {
                res = itemRes;
            }
        } else {
            res = itemRes;
        }
        return res;
    }

    @Override
    public View onCreateViewHolder(ViewGroup parent, int layoutRes, int viewType) {
        View view = LayoutInflater.from(getContext()).inflate(layoutRes, parent, false);
        FrameLayout container = view.findViewById(R.id.easy_container);

        int res = onGetChildLayoutId(viewType);

        View content;
        if (onCreateViewHolderListener != null) {
            content = onCreateViewHolderListener.onCreateViewHolder((ViewGroup) view, res, viewType);
        } else {
            content = LayoutInflater.from(getContext()).inflate(res, null, false);
        }

        container.addView(content);
        return view;
    }

    @Override
    public void onBindViewHolder(final EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
        FrameLayout container = holder.getView(R.id.easy_container);
        View contentChild = container.getChildAt(0);

        final RelativeLayout checkBoxContainer = holder.getView(R.id.easy_recycler_layout_check_box_container);
        final SmoothCheckBox checkBox = holder.getView(R.id.easy_recycler_layout_check_box);
        checkBox.setChecked(selectedList.contains(position), false);
        checkBox.setClickable(false);
        checkBox.setOnCheckedChangeListener(null);
        if (showCheckBox) {
            checkBoxContainer.setVisibility(enableSelection ? VISIBLE : GONE);
        } else {
            checkBoxContainer.setVisibility(selectMode ? VISIBLE : GONE);
        }
        contentChild.setPaddingRelative(
                contentChild.getPaddingStart(),
                contentChild.getPaddingTop(),
                checkBoxContainer.getVisibility() == VISIBLE ? 0 : contentChild.getPaddingStart(),
                contentChild.getPaddingBottom()
        );

        checkBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBoxContainer.performClick();
            }
        });
        checkBoxContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false, true);
                    unSelect(holder.getRealPosition());
                } else {
                    checkBox.setChecked(true, true);
                    onSelected(holder.getRealPosition());
                }
            }
        });

        Log.d(TAG, "onBindViewHolder position=" + position + " selected=" + selectedList.contains(position));
        holder.setItemClickCallback(new IEasy.OnItemClickCallback() {
            @Override
            public boolean shouldIgnoreClick(View view) {
                Log.d(TAG, "shouldIgnoreClick selectMode=" + selectMode);
                if (selectMode) {
                    if (checkBox.isChecked()) {
                        checkBox.setChecked(false, true);
                        unSelect(holder.getRealPosition());
                    } else {
                        checkBox.setChecked(true, true);
                        onSelected(holder.getRealPosition());
                    }
//                            easyRecyclerView.notifyItemChanged(holder.getHolderPosition());
                    return true;
                }
                return false;
            }
        });
        if (onBindViewHolderListener != null) {
            onBindViewHolderListener.onBindViewHolder(holder, list, position, payloads);
        }
    }

    @Override
    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
        if (isSelectMode()) {
            return false;
        }
        if (onLoadMoreListener != null) {
            return onLoadMoreListener.onLoadMore(enabled, currentPage);
        }
        return false;
    }

    @Override
    public boolean onLongClick(EasyViewHolder holder, View view, T data) {
        if (onItemLongClickListener != null) {
            return onItemLongClickListener.onLongClick(holder, view, data);
        }
        return false;
    }

    @Override
    public void onClick(EasyViewHolder holder, View view, T data) {
        if (onItemClickListener != null) {
            onItemClickListener.onClick(holder, view, data);
        }
    }

    @Override
    public void onSelectModeChange(boolean selectMode) {
        if (onSelectChangeListener != null) {
            onSelectChangeListener.onSelectAll();
        }
    }

    @Override
    public void onSelectChange(List<T> list, int position, boolean isChecked) {
        if (onSelectChangeListener != null) {
            onSelectChangeListener.onSelectChange(easyRecyclerView.getData(), position, isChecked);
        }
    }

    @Override
    public void onSelectAll() {
        if (onSelectChangeListener != null) {
            onSelectChangeListener.onSelectAll();
        }
    }

    @Override
    public void onUnSelectAll() {
        if (onSelectChangeListener != null) {
            onSelectChangeListener.onUnSelectAll();
        }
    }


    public EasyRecyclerLayout<T> setItemRes(@LayoutRes final int res) {
        this.itemRes = res;
        return this;
    }

    public EasyRecyclerLayout<T> setData(List<T> list) {
        easyRecyclerView.setData(list);
        return this;
    }

    public EasyRecyclerLayout<T> addItemDecoration(RecyclerView.ItemDecoration decor) {
        this.easyRecyclerView.addItemDecoration(decor);
        return this;
    }

    public EasyRecyclerLayout<T> addItemDecoration(RecyclerView.ItemDecoration decor, int index) {
        this.easyRecyclerView.addItemDecoration(decor, index);
        return this;
    }

    public EasyRecyclerLayout<T> setItemAnimator(RecyclerView.ItemAnimator animator) {
        easyRecyclerView.setItemAnimator(animator);
        return this;
    }

    public EasyRecyclerLayout<T> setItemViewCacheSize(int size) {
        easyRecyclerView.setItemViewCacheSize(size);
        return this;
    }

    public EasyRecyclerLayout<T> setHasFixedSize(boolean hasFixedSize) {
        easyRecyclerView.setHasFixedSize(hasFixedSize);
        return this;
    }

    public EasyRecyclerLayout<T> setLayoutFrozen(boolean layoutFrozen) {
        easyRecyclerView.setLayoutFrozen(layoutFrozen);
        return this;
    }

    public EasyRecyclerLayout<T> setOnFlingListener(RecyclerView.OnFlingListener listener) {
        easyRecyclerView.setOnFlingListener(listener);
        return this;
    }

    public EasyRecyclerLayout<T> setRecyclerListener(RecyclerView.RecyclerListener listener) {
        easyRecyclerView.setRecyclerListener(listener);
        return this;
    }

    public EasyRecyclerLayout<T> setScrollingTouchSlop(int slop) {
        easyRecyclerView.setScrollingTouchSlop(slop);
        return this;
    }

    public EasyRecyclerLayout<T> setEdgeEffectFactory(RecyclerView.EdgeEffectFactory factory) {
        easyRecyclerView.setEdgeEffectFactory(factory);
        return this;
    }

    public EasyRecyclerLayout<T> setRecycledViewPool(RecyclerView.RecycledViewPool pool) {
        easyRecyclerView.setRecycledViewPool(pool);
        return this;
    }

    public EasyRecyclerLayout<T> setPreserveFocusAfterLayout(boolean preserveFocusAfterLayout) {
        easyRecyclerView.setPreserveFocusAfterLayout(preserveFocusAfterLayout);
        return this;
    }

    public EasyRecyclerLayout<T> setViewCacheExtension(RecyclerView.ViewCacheExtension extension) {
        easyRecyclerView.setViewCacheExtension(extension);
        return this;
    }

    public EasyRecyclerLayout<T> setChildDrawingOrderCallback(RecyclerView.ChildDrawingOrderCallback callback) {
        easyRecyclerView.setChildDrawingOrderCallback(callback);
        return this;
    }

    public EasyRecyclerLayout<T> setEnableSwipeRefresh(boolean enableSwipeRefresh) {
        this.enableSwipeRefresh = enableSwipeRefresh;
        refreshLayout.setEnabled(enableSwipeRefresh);
        return this;
    }

    public EasyRecyclerLayout<T> setEnableLoadMore(boolean enableLoadMore) {
        this.enableLoadMore = enableLoadMore;
        return this;
    }

    public EasyRecyclerLayout<T> setEnableSelection(boolean enableSelection) {
        this.enableSelection = enableSelection;
        return this;
    }

    public EasyRecyclerLayout<T> addOnScrollListener(final RecyclerView.OnScrollListener onScrollListener) {
        easyRecyclerView.addOnScrollListener(onScrollListener);
        return this;
    }

    public EasyRecyclerLayout<T> setOnRefreshListener(final SwipeRefreshLayout.OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (enableSwipeRefresh) {
                    if (isSelectMode()) {
                        refreshLayout.setRefreshing(false);
                        return;
                    }
                    refreshLayout.setRefreshing(true);
                    if (onRefreshListener != null) {
                        onRefreshListener.onRefresh();
                    }
                    refreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            refreshLayout.setRefreshing(false);
                        }
                    }, 1000);
                }
            }
        });
        return this;
    }

    public EasyRecyclerLayout<T> setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
        return this;
    }

    public EasyRecyclerLayout<T> setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        easyRecyclerView.setLayoutManager(layoutManager);
        return this;
    }

    public EasyRecyclerLayout<T> setHeaderView(View headerView) {
        easyRecyclerView.setHeaderView(headerView);
        return this;
    }

    public EasyRecyclerLayout<T> setHeaderView(@LayoutRes int layoutRes, IEasy.OnBindHeaderListener callback) {
        easyRecyclerView.setHeaderView(layoutRes, callback);
        return this;
    }

    public EasyRecyclerLayout<T> setFooterView(View headerView) {
        easyRecyclerView.setFooterView(headerView);
        return this;
    }

    public EasyRecyclerLayout<T> setFooterView(@LayoutRes int layoutRes, IEasy.OnCreateFooterListener callback) {
        easyRecyclerView.setFooterView(layoutRes, callback);
        return this;
    }

    public EasyRecyclerLayout<T> onLoadMore(IEasy.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        enableLoadMore = true;
        return this;
    }

    public EasyRecyclerLayout<T> setOnLoadRetryListener(IEasy.OnLoadRetryListener listener) {
        this.onLoadRetryListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> setOnSelectChangeListener(IEasy.OnSelectChangeListener<T> onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
        return this;
    }

    public EasyRecyclerLayout<T> onViewClick(@IdRes int id, IEasy.OnClickListener<T> listener) {
        easyRecyclerView.onViewClick(id, listener);
        return this;
    }

    public EasyRecyclerLayout<T> onViewClick(IEasy.OnClickListener<T> listener, int...ids) {
        easyRecyclerView.onViewClick(listener, ids);
        return this;
    }

    public EasyRecyclerLayout<T> onViewLongClick(@IdRes int id, IEasy.OnLongClickListener<T> listener) {
        easyRecyclerView.onViewLongClick(id, listener);
        return this;
    }

    public EasyRecyclerLayout<T> onViewLongClick(IEasy.OnLongClickListener<T> listener, int...ids) {
        easyRecyclerView.onViewLongClick(listener, ids);
        return this;
    }

    public EasyRecyclerLayout<T> onItemClick(IEasy.OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> onItemLongClick(IEasy.OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> onGetChildViewType(IEasy.OnGetChildViewTypeListener<T> listener) {
        this.onGetChildViewTypeListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> onGetChildLayoutId(IEasy.OnGetChildLayoutIdListener listener) {
        this.onGetChildLayoutIdListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> onCreateViewHolder(IEasy.OnCreateViewHolderListener<T> listener) {
        this.onCreateViewHolderListener = listener;
        return this;
    }

    public EasyRecyclerLayout<T> onBindViewHolder(final IEasy.OnBindViewHolderListener<T> listener) {
        this.onBindViewHolderListener = listener;
        return this;
    }

    public void build() {
//        if (easyRecyclerView.getOnCreateViewHolder() == null) {
//            easyRecyclerView.onCreateViewHolder(onCreateViewHolderListener);
//        }

        easyRecyclerView.setLoadMoreEnabled(onLoadMoreListener != null && enableLoadMore);
        easyRecyclerView.build();
        adapter = easyRecyclerView.getAdapter();
        if (enableLoadMore) {
            Log.d(TAG, "build-->showContent1");
            showContent();
            return;
        }
        if (easyRecyclerView.getData().isEmpty()) {
            Log.d(TAG, "build-->showLoading");
            showLoading();
        } else {
            Log.d(TAG, "build-->showContent2");
            showContent();
        }
    }

    /**
     * 显示空视图
     */
    public final void showEmpty() {
        Log.d(TAG, "showEmpty");
        adapter.showEmpty();
    }

    public void showEmptyView(int msgId) {
        adapter.showEmptyView(msgId);
    }

    public void showEmptyView(String msg) {
        adapter.showEmptyView(msg);
    }

    public void showEmptyView(int msgId, int imgId) {
        adapter.showEmptyView(msgId, imgId);
    }

    public void showEmptyView(String msg, int imgId) {
        adapter.showEmptyView(msg, imgId);
    }

    /**
     * 显示错误视图
     */
    public final void showError() {
        Log.d(TAG, "showError");
        adapter.showError();
    }

    public void showErrorView(int msgId) {
        adapter.showErrorView(msgId);
    }

    public void showErrorView(String msg) {
        adapter.showErrorView(msg);
    }

    public void showErrorView(int msgId, int imgId) {
        adapter.showErrorView(msgId, imgId);
    }

    public void showErrorView(String msg, int imgId) {
        adapter.showErrorView(msg, imgId);
    }

    /**
     * 显示加载中视图
     */
    public final void showLoading() {
        Log.d(TAG, "showLoading");
        adapter.showLoading();
    }

    public void showLoadingView(View view) {
        adapter.showLoadingView(view);
    }

    public void showLoadingView(View view, boolean showTip) {
        adapter.showLoadingView(view, showTip);
    }

    public void showLoadingView(int msgId) {
        adapter.showLoadingView(msgId);
    }

    public void showLoadingView(String msg) {
        adapter.showLoadingView(msg);
    }

    /**
     * 显示无网络视图
     */
    public final void showNoNetwork() {
        adapter.showNoNetwork();
    }

    public void showNoNetworkView(int msgId) {
        adapter.showNoNetworkView(msgId);
    }

    public void showNoNetworkView(String msg) {
        adapter.showNoNetworkView(msg);
    }

    public void showNoNetworkView(int msgId, int imgId) {
        adapter.showNoNetworkView(msgId, imgId);
    }

    /**
     * 显示内容视图
     */
    public final void showContent() {
        Log.d(TAG, "showContent");
        adapter.showContent();
    }



    public void enterSelectMode() {
        if (selectMode) {
            return;
        }
        refreshLayout.setEnabled(false);
        easyRecyclerView.getAdapter().setLoadMoreEnabled(false);
        selectMode = true;
//        easyRecyclerView.notifyDataSetChanged();
        notifyVisibleItemChanged();
        onSelectModeChange(selectMode);
    }

    public void exitSelectMode() {
        if (!selectMode) {
            return;
        }
        refreshLayout.setEnabled(enableSwipeRefresh);
        easyRecyclerView.getAdapter().setLoadMoreEnabled(true);
        selectMode = false;
        selectedList.clear();
//        easyRecyclerView.notifyDataSetChanged();
        notifyVisibleItemChanged();
        onSelectModeChange(selectMode);
    }

    private void onSelectChange(int position, boolean isChecked) {
        if (showCheckBox) {
            if (selectMode && getSelectedCount() == 0) {
                selectMode = false;
            } else if (!selectMode && getSelectedCount() > 0) {
                selectMode = true;
            }
        }
        onSelectChange(easyRecyclerView.getData(), position, isChecked);
    }

    private void onSelected(int position) {
        if (!selectedList.contains(position)) {
            selectedList.add(position);
            onSelectChange(position, true);
            if (selectedList.size() == easyRecyclerView.getData().size()) {
                onSelectAll();
            }
        }
    }

    private void unSelect(int position) {
        if (selectedList.contains(position)) {
            selectedList.remove(Integer.valueOf(position));
            onSelectChange(position, false);
            if (selectedList.size() == 0) {
                onUnSelectAll();
            }
        }
    }

    public void selectAll() {
        if (!selectMode && showCheckBox) {
            selectMode = true;
        }
        selectedList.clear();
        for (int i = 0; i < easyRecyclerView.getAdapter().getItemCount(); i++) {
            selectedList.add(i);
            onSelectChange(i, true);
//            notifyItemChanged(i);
        }
        notifyVisibleItemChanged();
        onSelectAll();
    }

    public void unSelectAll() {
        for (int i : selectedList) {
            onSelectChange(i, false);
        }
        selectedList.clear();
//        easyRecyclerView.notifyDataSetChanged();
        notifyVisibleItemChanged();
        onUnSelectAll();
    }

    public Set<Integer> getSelectedSet() {
        return selectedList;
    }

    public int getSelectedCount() {
        return selectedList.size();
    }

    public List<T> getSelectedItem() {
        List<T> selectedItems = new ArrayList<>();
        for (Integer i : selectedList) {
            if (i < easyRecyclerView.getData().size()) {
                selectedItems.add(easyRecyclerView.getData().get(i));
            }
        }
        return selectedItems;
    }

    public void notifyDataSetChanged() {
        if ((easyRecyclerView.getData() == null || easyRecyclerView.getData().isEmpty()) && !enableLoadMore) {
            showEmpty();
        } else {
            easyRecyclerView.notifyDataSetChanged();
            showContent();
        }
        stopRefresh();
    }

    public void notifyVisibleItemChanged() {
        easyRecyclerView.notifyVisibleItemChanged();
    }

    public void notifyVisibleItemChanged(Object payload) {
        easyRecyclerView.notifyVisibleItemChanged(payload);
    }

    public void notifyItemChanged(int position) {
        easyRecyclerView.notifyItemChanged(position);
    }

    public void notifyItemChanged(int position, Object payload) {
        easyRecyclerView.notifyItemChanged(position, payload);
    }

    public void notifyItemRangeChanged(int start, int end) {
        easyRecyclerView.notifyItemRangeChanged(start, end);
    }

    public void notifyItemRangeChanged(int start, int end, Object payload) {
        easyRecyclerView.notifyItemRangeChanged(start, end, payload);
    }

    public void notifyItemRemoved(int position) {
        easyRecyclerView.getAdapter().notifyItemRemoved(position);
        if (easyRecyclerView.getData().isEmpty()) {
            showEmpty();
        }
    }

    public void notifyItemInserted(int position) {
        easyRecyclerView.getAdapter().notifyItemInserted(position);
        if (!easyRecyclerView.getData().isEmpty()) {
            showContent();
        }
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public boolean isRefreshing() {
        return refreshLayout.isRefreshing();
    }

    public void setRefreshing(boolean refreshing) {
        refreshLayout.setRefreshing(refreshing);
    }

    public void startRefresh() {
        if (!refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(true);
        }
    }

    public void stopRefresh() {
        if (refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(false);
        }
    }

    public void smoothScrollToPosition(int position) {
        easyRecyclerView.smoothScrollToPosition(position);
    }

    public List<T> getData() {
        return easyRecyclerView.getData();
    }

    public EasyRecyclerView<T> getEasyRecyclerView() {
        return easyRecyclerView;
    }

    public RecyclerView getRecyclerView() {
        return easyRecyclerView.getRecyclerView();
    }

    public EasyStateAdapter<T> getAdapter() {
        return easyRecyclerView.getAdapter();
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return easyRecyclerView.getLayoutManager();
    }

    @Override
    public void onLoadRetry() {
        if (onLoadRetryListener != null) {
            onLoadRetryListener.onLoadRetry();
        } else if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }
}
