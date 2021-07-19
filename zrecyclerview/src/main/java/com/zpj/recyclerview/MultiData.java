package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiData<T> extends EasyStateConfig<MultiData<T>> { // extends BaseStateConfig<MultiData<T>>

    protected final List<T> mData;

    protected boolean hasMore = true;

    private MultiAdapter mAdapter;

    private int mTempCount = 0;

    public MultiData() {
        mData = new ArrayList<>();
    }

    public MultiData(List<T> mData) {
        this();
        this.mData.addAll(mData);
        hasMore = false;
    }

    public void setAdapter(MultiAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public MultiAdapter getAdapter() {
        return mAdapter;
    }

    public List<T> getData() {
        return mData;
    }

    public int getCount() {
        return mData.size();
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

    public boolean hasMore() {
        return hasMore;
    }

    public abstract boolean loadData();

    boolean load(MultiAdapter adapter) {
        if (this.mAdapter == null) {
            this.mAdapter = adapter;
        }
        hasMore = loadData();
//        isLoaded = true;
        return !hasMore;
    }

    void onBindViewHolder(EasyViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, mData, getRealPosition(position), payloads);
    }

    protected View onCreateView(Context context, ViewGroup container, int viewType) {
        return LayoutInflater.from(context).inflate(getLayoutId(viewType), container, false);
    }

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
        if (mAdapter != null) {
            return mAdapter.list.indexOf(this);
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


    public void notifyItemMove(int from, int to) {
        if (mAdapter == null) {
            return;
        }
        int count = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                Log.d("notifyItemMove", "count=" + count + " getCount=" + getCount());
                mAdapter.postNotifyItemMoved(from + count, to + count);
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyDataSetChange() {
        if (mAdapter == null) {
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
        // TODO 优化MultiData的notifyDataSetChange：先看是否需要notifyItemInsert还是直接notifyItemRangeChanged
//        mAdapter.postNotifyDataSetChanged();

        if (getCount() == mTempCount) {
            notifyItemRangeChanged(0, getCount());
        } else {
            if (getCount() > mTempCount) {
//                notifyItemRangeInserted(mTempCount, getCount() - mTempCount);

                int num = mAdapter.headerView == null ? 0 : 1;
                for (MultiData<?> data : mAdapter.getData()) {
                    if (data == this) {
                        mAdapter.postNotifyItemRangeInserted(num + mTempCount, getCount() - mTempCount);
//                        mAdapter.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                mAdapter.onScrollStateChanged(mAdapter.getRecyclerView(), RecyclerView.SCROLL_STATE_IDLE);
//                            }
//                        });
                        break;
                    }
                    num  += data.getCount();
                }

            } else {
                notifyItemRangeRemoved(getCount(), mTempCount - getCount());
            }
            mTempCount = getCount();
            notifyItemRangeChanged(0, getCount());
        }
        mAdapter.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter.footerViewHolder != null) {
                    mAdapter.footerViewHolder.getView().performClick();
                }
            }
        });
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
        if (mAdapter == null) {
            return;
        }
        int count = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                Log.d("postNotifyItemChanged", "count=" + count + " position=" + position + " getCount=" + getCount());
                mAdapter.postNotifyItemChanged(count + position, payload);
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
        if (mAdapter == null) {
            return;
        }
        int num = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                mTempCount = getCount();
                if (positionStart + count > getCount()) {
                    mAdapter.postNotifyItemRangeChanged(num + positionStart, getCount() - positionStart, payload);
                } else {
                    mAdapter.postNotifyItemRangeChanged(num + positionStart, count, payload);
                }
                break;
            }
            num  += data.getCount();
        }
    }

    public void notifyItemRangeRemoved() {
        if (mAdapter == null) {
            return;
        }
        int count = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                mTempCount = getCount();
                Log.d("notifyItemRangeRemoved", "count=" + count + " getCount=" + getCount());
                mAdapter.postNotifyItemRangeRemoved(count, getCount());
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeRemoved(int positionStart, int count) {
        if (mAdapter == null) {
            return;
        }
        int num = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                mTempCount = getCount();
                if (positionStart + count > getCount()) {
                    mAdapter.postNotifyItemRangeRemoved(num + positionStart, getCount() - positionStart);
                } else {
                    mAdapter.postNotifyItemRangeRemoved(num + positionStart, count);
                }
                break;
            }
            num  += data.getCount();
        }
    }

    public void notifyItemRemoved(int position) {
        if (mAdapter == null) {
            return;
        }
        int count = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                Log.d("postNotifyItemRemoved", "count=" + count + " position=" + position + " getCount=" + getCount());
                mTempCount = getCount();
                mAdapter.postNotifyItemRemoved(count + position);
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeInserted() {
        if (mAdapter == null) {
            return;
        }
        int count = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                Log.d("notifyItemRangeInserted", "count=" + count + " getCount=" + getCount());
                mTempCount = getCount();
                mAdapter.postNotifyItemRangeInserted(count, getCount());
                mAdapter.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter.footerViewHolder != null) {
                            mAdapter.footerViewHolder.getView().performClick();
                        }
                    }
                });
                break;
            }
            count  += data.getCount();
        }
    }

    public void notifyItemRangeInserted(int positionStart, int count) {
        if (mAdapter == null) {
            return;
        }
        int num = mAdapter.headerView == null ? 0 : 1;
        for (MultiData<?> data : mAdapter.getData()) {
            if (data == this) {
                if (positionStart >= getCount()) {
                    return;
                }
                mTempCount = getCount();
                if (positionStart + count > getCount()) {
                    mAdapter.postNotifyItemRangeInserted(num + positionStart, getCount() - positionStart);
                } else {
                    mAdapter.postNotifyItemRangeInserted(num + positionStart, count);
                }
                mAdapter.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdapter.footerViewHolder != null) {
                            mAdapter.footerViewHolder.getView().performClick();
                        }
                    }
                });
                break;
            }
            num  += data.getCount();
        }
    }

}
