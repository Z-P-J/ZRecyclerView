package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MultiData<T> extends EasyStateConfig<MultiData<T>> { // extends BaseStateConfig<MultiData<T>>

    protected final List<T> mData;

    protected boolean hasMore = true;

    private MultiAdapter mAdapter;

    private int mTempCount = 0;

    private Layouter mLayouter;

    public MultiData() {
        mData = new ArrayList<>();
    }

    public MultiData(List<T> mData) {
        this();
        this.mData.addAll(mData);
        hasMore = false;
    }

    public MultiData(Layouter layouter) {
        mData = new ArrayList<>();
        this.mLayouter = layouter;
    }

    public MultiData(List<T> mData, Layouter layouter) {
        this(layouter);
        this.mData.addAll(mData);
        hasMore = false;
    }

    public Layouter getLayouter() {
        if (mLayouter == null) {
            mLayouter = createLayouter();
            if (mLayouter == null) {
                throw new RuntimeException("You must create a nonnull layouter for this multidata!");
            }
        }
        return mLayouter;
    }

    protected Layouter createLayouter() {
        return new VerticalLayouter();
    }

    public void setAdapter(MultiAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    public MultiAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 获取数据列表对象
     * @return 列表对象
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * item总数。注意：要返回该MultiData下所有类型item的数量之和，可能不仅仅包含mData列表的数量，
     * 如果有header、footer或其它类型应加上这些item的数量
     * @return 返回item数量
     */
    public int getCount() {
        return mData.size();
    }

    /**
     * 在重写@getColumnCount方法的同时要重写getMaxColumnCount方法
     * @return 最大列数量
     */
    public @IntRange(from = 1) int getMaxColumnCount() {
        return 1;
    }

    /**
     * 根据viewType获取列数量
     * @param viewType item类型
     * @return 列数量
     */
    public @IntRange(from = 1) int getColumnCount(int viewType) {
        return 1;
    }

    /**
     * 根据位置获取item类型
     * @param position item位置
     * @return item类型
     */
    public int getViewType(int position) {
        return getClass().getName().hashCode();
    }

    /**
     * 根据item类型获取布局id
     * @param viewType item类型
     * @return 布局id
     */
    public abstract int getLayoutId(int viewType);

    /**
     * 是否存在该类型的item。注意：当我们重写getViewType方法时，必须重写该方法
     * @param viewType item类型
     * @return 是否存在该类型
     */
    public boolean hasViewType(int viewType) {
        return viewType == getClass().getName().hashCode();
    }

    /**
     * 返回是否可以加载更多
     * @return true: 可以加载更多 false：没有更多数据
     */
    public boolean hasMore() {
        return hasMore;
    }

    public void onItemSticky(final EasyViewHolder holder, final int position, final boolean isSticky) {
        onBindViewHolder(holder, mData, getRealPosition(position), Collections.emptyList());
    }

    public boolean isStickyPosition(int position) {
        return false;
    }

    /**
     * 加载数据，建议在子线程中加载数据
     * @return 是否可以加载更多。true:可以加载更多 false:没有更多数据
     */
    public abstract boolean loadData();

    boolean load(MultiAdapter adapter) {
        if (this.mAdapter == null) {
            this.mAdapter = adapter;
        }
        hasMore = loadData();
        return !hasMore;
    }

    /**
     * 绑定ViewHolder
     * @param holder EasyViewHolder
     * @param position 位置
     * @param payloads 有效载荷
     */
    void onBindViewHolder(EasyViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, mData, getRealPosition(position), payloads);
    }

    public View onCreateView(Context context, ViewGroup container, int viewType) {
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
                int num = getStartCount();
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
                int num = getStartCount();
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
        int count = getStartCount();
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
        if (getCount() == mTempCount) {
            notifyItemRangeChanged(0, getCount());
        } else {
            if (getCount() > mTempCount) {
                int num = getStartCount();
                for (MultiData<?> data : mAdapter.getData()) {
                    if (data == this) {
                        mAdapter.postNotifyItemRangeInserted(num + mTempCount, getCount() - mTempCount);
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
                if (mAdapter.footerViewHolder != null && mAdapter.footerViewHolder.getView() != null) {
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
        int count = getStartCount();
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
        int num = getStartCount();
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
        int count = getStartCount();
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
        int num = getStartCount();
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
        int count = getStartCount();
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
        int count = getStartCount();
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
        int num = getStartCount();
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

    protected int getStartCount() {
        int num = mAdapter.headerView == null ? 0 : 1;
        if (mAdapter.mRefreshHeader != null) {
            num++;
        }
        return num;
    }

}
