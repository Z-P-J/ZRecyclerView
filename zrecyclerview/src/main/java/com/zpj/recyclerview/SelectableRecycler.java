package com.zpj.recyclerview;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zpj.recyclerview.refresh.SwipeDecorationRefresher;
import com.zpj.widget.checkbox.ZCheckBox;

import java.util.ArrayList;
import java.util.List;

public class SelectableRecycler<T> extends EasyRecycler<T>
        implements IEasy.OnLoadMoreListener,
        IEasy.OnLoadRetryListener,
        IEasy.OnItemClickListener<T>,
        IEasy.OnItemLongClickListener<T>,
        IEasy.OnGetChildViewTypeListener<T>,
        IEasy.OnGetChildLayoutIdListener,
        IEasy.OnCreateViewHolderListener<T>,
        IEasy.OnSelectChangeListener<T>,
        IEasy.OnBindViewHolderListener<T> {

    private static final String TAG = "SelectableRecycler";

    private static final String PAYLOAD_CHECK_BOX = "easy_refresh_check_box";

    private final List<Integer> selectedList = new ArrayList<>();

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

    private int maxSelectCount = Integer.MAX_VALUE;

    private boolean showCheckBox = false;

    private boolean selectMode = false;

    private boolean enableLoadMore = false;

    private boolean enableSelection = true;

    public SelectableRecycler(@NonNull RecyclerView recyclerView) {
        this(recyclerView, new ArrayList<T>(0));
    }

    public SelectableRecycler(@NonNull RecyclerView recyclerView, @NonNull List<T> dataSet) {
        super(recyclerView, dataSet);
        super.onCreateViewHolder(this);
        super.onGetChildViewType(this);
        super.onBindViewHolder(this);
        super.onItemLongClick(this);
        super.onItemClick(this);
        super.onLoadMore(this);
        super.setOnLoadRetryListener(this);
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.easy_item_recycler_layout, parent, false);
        FrameLayout container = view.findViewById(R.id.easy_container);

        int res = onGetChildLayoutId(viewType);

        View content;
        if (onCreateViewHolderListener != null) {
            content = onCreateViewHolderListener.onCreateViewHolder((ViewGroup) view, res, viewType);
        } else {
            content = LayoutInflater.from(parent.getContext()).inflate(res, null, false);
        }

        container.addView(content);
        return view;
    }

    @Override
    public void onBindViewHolder(final EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
        FrameLayout container = holder.getView(R.id.easy_container);
        View contentChild = container.getChildAt(0);

//        final View checkBoxContainer = holder.getView(R.id.easy_recycler_layout_check_box_container);
        final ZCheckBox checkBox = holder.getView(R.id.easy_recycler_layout_check_box);

        if (showCheckBox) {
            checkBox.setVisibility(enableSelection ? View.VISIBLE : View.GONE);
        } else {
            checkBox.setVisibility(selectMode ? View.VISIBLE : View.GONE);
        }

        checkBox.setChecked(selectedList.contains(position), false);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.performClick();
            }
        });

        contentChild.setPadding(
                contentChild.getPaddingStart(),
                contentChild.getPaddingTop(),
                checkBox.getVisibility() == View.VISIBLE ? 0 : contentChild.getPaddingStart(),
                contentChild.getPaddingBottom()
        );

        Log.d(TAG, "onBindViewHolder position=" + position + " selected=" + selectedList.contains(position));
        holder.setItemClickCallback(new IEasy.OnItemClickCallback() {
            @Override
            public boolean shouldIgnoreClick(View view) {
                Log.d(TAG, "shouldIgnoreClick selectMode=" + selectMode);
                if (selectMode) {
                    if (checkBox.isChecked()) {
                        if (unSelect(holder.getRealPosition())) {
                            checkBox.setChecked(false, true);
                        }
                    } else {
                        if (onSelected(holder.getRealPosition())) {
                            checkBox.setChecked(true, true);
                        }
                    }
//                            easyRecycler.notifyItemChanged(holder.getHolderPosition());
                    return true;
                }
                return false;
            }
        });

        if (payloads != null && !payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (PAYLOAD_CHECK_BOX.equals(payload)) {

                    return;
                }
            }
        }

        if (onBindViewHolderListener != null) {
            onBindViewHolderListener.onBindViewHolder(holder, list, position, payloads);
        }
    }

    @Override
    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
//        if (isSelectMode()) {
//            return false;
//        }
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
            onSelectChangeListener.onSelectChange(getDataSet(), position, isChecked);
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

    @Override
    public void onSelectOverMax(int maxSelectCount) {
        if (onSelectChangeListener != null) {
            onSelectChangeListener.onSelectOverMax(maxSelectCount);
        }
    }

    public SelectableRecycler<T> setEnableLoadMore(boolean enableLoadMore) {
        this.enableLoadMore = enableLoadMore;
        return this;
    }

    public SelectableRecycler<T> setEnableSelection(boolean enableSelection) {
        this.enableSelection = enableSelection;
        return this;
    }

    public SelectableRecycler<T> setMaxSelectCount(int maxSelectCount) {
        if (maxSelectCount >= 0) {
            this.maxSelectCount = maxSelectCount;
        }
        return this;
    }

    public SelectableRecycler<T> setShowCheckBox(boolean showCheckBox) {
        this.showCheckBox = showCheckBox;
        return this;
    }

    @Override
    public SelectableRecycler<T> onLoadMore(IEasy.OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        enableLoadMore = true;
        return this;
    }

    public SelectableRecycler<T> setOnLoadRetryListener(IEasy.OnLoadRetryListener listener) {
        this.onLoadRetryListener = listener;
        return this;
    }

    public SelectableRecycler<T> setOnSelectChangeListener(IEasy.OnSelectChangeListener<T> onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onItemClick(IEasy.OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onItemLongClick(IEasy.OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onGetChildViewType(IEasy.OnGetChildViewTypeListener<T> listener) {
        this.onGetChildViewTypeListener = listener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onGetChildLayoutId(IEasy.OnGetChildLayoutIdListener listener) {
        this.onGetChildLayoutIdListener = listener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onCreateViewHolder(IEasy.OnCreateViewHolderListener<T> listener) {
        this.onCreateViewHolderListener = listener;
        return this;
    }

    @Override
    public SelectableRecycler<T> onBindViewHolder(final IEasy.OnBindViewHolderListener<T> listener) {
        this.onBindViewHolderListener = listener;
        return this;
    }

    @Override
    public void build() {
        setLoadMoreEnabled(onLoadMoreListener != null && enableLoadMore);
        super.build();

        if (enableLoadMore) {
            Log.d(TAG, "build-->showContent1");
            showContent();
            return;
        }
        if (getDataSet().isEmpty()) {
            Log.d(TAG, "build-->showLoading");
            showLoading();
        } else {
            Log.d(TAG, "build-->showContent2");
            showContent();
        }
    }


    public void enterSelectMode() {
        if (selectMode) {
            return;
        }
        // TODO
//        if (mRefresh != null) {
//            mRefresh.setEnable(false);
//        }
        selectMode = true;
        notifyVisibleItemChanged(PAYLOAD_CHECK_BOX);
        onSelectModeChange(selectMode);
    }

    public void exitSelectMode() {
        if (!selectMode) {
            return;
        }
        // TODO
//        if (mRefresh != null) {
//            enableSwipeRefresh
//            mRefresh.
//        }
        selectMode = false;
        selectedList.clear();
        notifyVisibleItemChanged(PAYLOAD_CHECK_BOX);
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
        onSelectChange(getDataSet(), position, isChecked);
    }

    private boolean onSelected(int position) {
        if (selectedList.size() >= maxSelectCount) {
            if (maxSelectCount == 1) {
                int index = selectedList.get(0);
                unSelect(index);
                notifyItemChanged(index, PAYLOAD_CHECK_BOX);
                return onSelected(position);
            } else {
                onSelectOverMax(maxSelectCount);
                return false;
            }
        }
        if (!selectedList.contains(position)) {
            selectedList.add(position);
            onSelectChange(position, true);
            if (selectedList.size() == getCount()) {
                onSelectAll();
            }
            return true;
        }
        return false;
    }

    private boolean unSelect(int position) {
        if (selectedList.contains(position)) {
            selectedList.remove(Integer.valueOf(position));
            onSelectChange(position, false);
            if (selectedList.size() == 0) {
                onUnSelectAll();
            }
            return true;
        }
        return false;
    }

    public void selectAll() {
        if (!selectMode && showCheckBox) {
            selectMode = true;
        }
        if (maxSelectCount == Integer.MAX_VALUE) {
            selectedList.clear();
        }
        for (int i = 0; i < getCount(); i++) {
            if (selectedList.size() >= maxSelectCount) {
                break;
            }
            if (!selectedList.contains(i)) {
                selectedList.add(i);
                onSelectChange(i, true);
            }
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
//        easyRecycler.notifyDataSetChanged();
        notifyVisibleItemChanged();
        onUnSelectAll();
    }

    public List<Integer> getSelectedPositionList() {
        return new ArrayList<>(selectedList);
    }

    public void clearSelectedPosition() {
        selectedList.clear();
    }

    public void addSelectedPosition(int position) {
        if (!selectedList.contains(position)) {
            selectedList.add(position);
        }
    }

    public int getSelectedCount() {
        return selectedList.size();
    }

    public List<T> getSelectedItem() {
        List<T> selectedItems = new ArrayList<>();
        for (Integer i : selectedList) {
            if (i < getCount()) {
                selectedItems.add(getData(i));
            }
        }
        return selectedItems;
    }

    @Override
    public void notifyDataSetChanged() {
        if (isEmpty() && !enableLoadMore) {
            showEmpty();
        } else {
            super.notifyDataSetChanged();
            showContent();
        }
        stopRefresh();
    }

    @Override
    public void notifyItemRemoved(int position) {
        super.notifyItemRemoved(position);
        if (isEmpty()) {
            showEmpty();
        }
    }

    @Override
    public void notifyItemInserted(int position) {
        super.notifyItemInserted(position);
        if (!isEmpty()) {
            showContent();
        }
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public boolean isRefreshing() {
        if (mRefresh == null) {
            return false;
        }
        return mRefresh.isRefreshing();
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

    @Override
    public void onLoadRetry() {
        if (onLoadRetryListener != null) {
            onLoadRetryListener.onLoadRetry();
        } else if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }
}
