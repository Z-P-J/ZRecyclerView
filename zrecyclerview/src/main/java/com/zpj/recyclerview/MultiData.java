package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiData<T> extends EasyStateConfig<MultiData<T>> { // extends BaseStateConfig<MultiData<T>>

    protected final List<T> list;

//    protected boolean isLoaded = false;
    protected boolean hasMore = true;

    private MultiAdapter adapter;

    public MultiData() {
        list = new ArrayList<>();
    }

    public MultiData(List<T> list) {
        this();
        this.list.addAll(list);
//        isLoaded = true;
        hasMore = false;
    }

    public void setAdapter(MultiAdapter adapter) {
        this.adapter = adapter;
    }

    public MultiAdapter getAdapter() {
        return adapter;
    }

    public List<T> getDataSet() {
        return list;
    }

    public int getCount() {
        return list.size();
    }

    public @IntRange(from = 1) int getMaxColumnCount() {
        return 1;
    }

    public @IntRange(from = 1) int getColumnCount(int viewType) {
        return 1;
    }

    public int getViewType(int position) {
        return getClass().getName().hashCode();
    }

    public abstract int getLayoutId(int viewType);

    public boolean hasViewType(int viewType) {
        return viewType == getClass().getName().hashCode();
    }

//    public boolean isLoaded() {
//        return isLoaded;
//    }

    public boolean hasMore() {
        return hasMore;
    }

    public abstract boolean loadData();

    boolean load(MultiAdapter adapter) {
        if (this.adapter == null) {
            this.adapter = adapter;
        }
        hasMore = loadData();
//        isLoaded = true;
        return !hasMore;
    }

    void onBindViewHolder(EasyViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, list, getRealPosition(position), payloads);
    }

    protected View onCreateView(Context context, ViewGroup container, int viewType) {
        return LayoutInflater.from(context).inflate(getLayoutId(viewType), container, false);
    }

//    @Override
//    public IViewHolder getLoadingViewHolder() {
//        if (loadingViewHolder == null) {
//            return StateManager.config().getLoadingViewHolder();
//        }
//        return super.getLoadingViewHolder();
//    }
//
//    @Override
//    public IViewHolder getEmptyViewHolder() {
//        if (emptyViewHolder == null) {
//            return StateManager.config().getEmptyViewHolder();
//        }
//        return super.getEmptyViewHolder();
//    }
//
//    @Override
//    public IViewHolder getErrorViewHolder() {
//        if (errorViewHolder == null) {
//            return StateManager.config().getErrorViewHolder();
//        }
//        return super.getErrorViewHolder();
//    }
//
//    @Override
//    public IViewHolder getLoginViewHolder() {
//        if (loginViewHolder == null) {
//            return StateManager.config().getLoginViewHolder();
//        }
//        return super.getLoginViewHolder();
//    }
//
//    @Override
//    public IViewHolder getNoNetworkViewHolder() {
//        if (noNetworkViewHolder == null) {
//            return StateManager.config().getNoNetworkViewHolder();
//        }
//        return super.getNoNetworkViewHolder();
//    }

    public abstract void onBindViewHolder(final EasyViewHolder holder, final List<T> list, final int position, final List<Object> payloads);

    public int getRealPosition(int position) {
        return position;
    }

//    public boolean isStickPosition(int position) {
//        return false;
//    }

    public void onClick(EasyViewHolder holder, View view, T data) {

    }

    public boolean onLongClick(EasyViewHolder holder, View view, T data) {
        return false;
    }

    public int getMultiDataPosition() {
        if (adapter != null) {
            return adapter.list.indexOf(this);
        }
        return -1;
    }

    protected void scrollToPosition(final int position) {
        final MultiAdapter adapter = getAdapter();
        adapter.post(new Runnable() {
            @Override
            public void run() {
                int num = adapter.headerView == null ? 0 : 1;
                for (MultiData<?> data : adapter.getData()) {
                    if (data == MultiData.this) {
                        getAdapter().getRecyclerView().scrollToPosition(num + position);
                        break;
                    }
                    num  += data.getCount();
                }
            }
        });
    }

    protected void smoothScrollToPosition(final int position) {
        final MultiAdapter adapter = getAdapter();
        adapter.post(new Runnable() {
            @Override
            public void run() {
                int num = adapter.headerView == null ? 0 : 1;
                for (MultiData<?> data : adapter.getData()) {
                    if (data == MultiData.this) {
                        getAdapter().getRecyclerView().smoothScrollToPosition(num + position);
                        break;
                    }
                    num  += data.getCount();
                }
            }
        });
    }



    public void notifyDataSetChange() {
        if (adapter == null) {
            return;
        }
//        int count = 0;
//        for (MultiData<?> data : adapter.getData()) {
//            if (data == this) {
//                Log.d("notifyDataSetChange", "count=" + count + " getCount=" + getCount());
//                adapter.postNotifyItemRangeChanged(count, getCount());
//                break;
//            }
//            count  += data.getCount();
//        }
//
        adapter.postNotifyDataSetChanged();

    }

    public void notifyItemChanged(final int position) {
//        if (adapter == null) {
//            return;
//        }
//        int count = 0;
//        for (MultiData<?> data : adapter.getData()) {
//            if (data == this) {
//                Log.d("postNotifyItemChanged", "count=" + count + " position=" + position + " getCount=" + getCount());
//                adapter.postNotifyItemChanged(count + position);
//                break;
//            }
//            count  += data.getCount();
//        }
        notifyItemChanged(position, null);
    }

    public void notifyItemChanged(final int position, @Nullable final Object payload) {
        if (adapter == null) {
            return;
        }
        int count = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                Log.d("postNotifyItemChanged", "count=" + count + " position=" + position + " getCount=" + getCount());
                adapter.postNotifyItemChanged(count + position, payload);
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeChanged(int positionStart, int count) {
//        if (adapter == null) {
//            return;
//        }
//        int num = 0;
//        for (MultiData<?> data : adapter.getData()) {
//            if (data == this) {
//                if (positionStart >= getCount()) {
//                    return;
//                }
//                if (positionStart + count > getCount()) {
//                    adapter.postNotifyItemRangeChanged(num + positionStart, getCount() - positionStart);
//                } else {
//                    adapter.postNotifyItemRangeChanged(num + positionStart, count);
//                }
//            }
//            num  += data.getCount();
//        }
        notifyItemRangeChanged(positionStart, count, null);
    }

    public void notifyItemRangeChanged(final int positionStart, final int count, @Nullable final Object payload) {
        if (adapter == null) {
            return;
        }
        int num = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                if (positionStart + count > getCount()) {
                    adapter.postNotifyItemRangeChanged(num + positionStart, getCount() - positionStart, payload);
                } else {
                    adapter.postNotifyItemRangeChanged(num + positionStart, count, payload);
                }
                break;
            }
            num  += data.getCount();
        }
    }

    public void notifyItemRangeRemoved() {
        if (adapter == null) {
            return;
        }
        int count = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                Log.d("notifyItemRangeRemoved", "count=" + count + " getCount=" + getCount());
                adapter.postNotifyItemRangeRemoved(count, getCount());
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeRemoved(int positionStart, int count) {
        if (adapter == null) {
            return;
        }
        int num = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                if (positionStart + count > getCount()) {
                    adapter.postNotifyItemRangeRemoved(num + positionStart, getCount() - positionStart);
                } else {
                    adapter.postNotifyItemRangeRemoved(num + positionStart, count);
                }
                break;
            }
            num  += data.getCount();
        }
    }

    public void notifyItemRemoved(int position) {
        if (adapter == null) {
            return;
        }
        int count = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                Log.d("postNotifyItemRemoved", "count=" + count + " position=" + position + " getCount=" + getCount());
                adapter.postNotifyItemRemoved(count + position);
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeInserted() {
        if (adapter == null) {
            return;
        }
        int count = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                Log.d("notifyItemRangeInserted", "count=" + count + " getCount=" + getCount());
                adapter.postNotifyItemRangeInserted(count, getCount());
                adapter.post(new Runnable() {
                    @Override
                    public void run() {
//                        if (adapter.footerView != null) {
//                            adapter.footerView.performClick();
//                        }
                        if (adapter.footerViewHolder != null) {
                            adapter.footerViewHolder.getView().performClick();
                        }
                    }
                });
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeInserted(int positionStart, int count) {
        if (adapter == null) {
            return;
        }
        int num = adapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                if (positionStart + count > getCount()) {
                    adapter.postNotifyItemRangeInserted(num + positionStart, getCount() - positionStart);
                } else {
                    adapter.postNotifyItemRangeInserted(num + positionStart, count);
                }
                adapter.post(new Runnable() {
                    @Override
                    public void run() {
//                        if (adapter.footerView != null) {
//                            adapter.footerView.performClick();
//                        }
                        if (adapter.footerViewHolder != null) {
                            adapter.footerViewHolder.getView().performClick();
                        }
                    }
                });
                break;
            }
            num  += data.getCount();
        }
    }

}
