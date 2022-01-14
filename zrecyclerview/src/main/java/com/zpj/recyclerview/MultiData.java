package com.zpj.recyclerview;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
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

    private static final String TAG = "MultiData";

    protected final List<T> mData;

    protected boolean hasMore = true;

    private MultiAdapter mAdapter;

    protected int mLastCount = 0;

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
        mLastCount = getCount();
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
    protected abstract boolean loadData();

    public boolean load(MultiAdapter adapter) {
        if (this.mAdapter == null) {
            setAdapter(adapter);
        }
        if (hasMore()) {
            hasMore = loadData();
        }
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


    public void notifyItemMove(final int from, final int to) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int count = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d("notifyItemMove", "count=" + count + " getCount=" + getCount());
                    mAdapter.notifyItemMoved(from + count, to + count);
                    break;
                }
                count  += data.getCount();
            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemMove(from, to);
                }
            });
        }
    }

    public void notifyDataSetChange() {
        Log.d(TAG, "notifyDataSetChanged mAdapter=" + mAdapter);
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }

        if (isInMainThread()) {
            Log.d(TAG, "notifyDataSetChanged getItemCount=" + mAdapter.getItemCount() + " count=" + getCount() + " tempCount=" + mLastCount);
            if (getCount() != mLastCount) {
                if (getCount() > mLastCount) {
                    int num = getStartCount();
                    for (MultiData<?> data : mAdapter.getData()) {
                        if (data == this) {
                            Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeInserted");
                            mAdapter.notifyItemRangeInserted(num + mLastCount, getCount() - mLastCount);
                            break;
                        }
                        num  += data.getCount();
                    }
                } else {
                    Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeRemoved");
                    notifyItemRangeRemoved(getCount(), mLastCount - getCount());
                }
            }
            Log.d("MultiData", "notifyDataSetChanged->notifyItemRangeChanged");
            notifyItemRangeChanged(0, getCount());

            mLastCount = getCount();
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter.footerViewHolder != null && mAdapter.footerViewHolder.getView() != null) {
                        mAdapter.footerViewHolder.getView().performClick();
                    }
                }
            });
            Log.d("MultiData", "notifyDataSetChanged->end");
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChange();
                }
            });
        }
    }

    public void notifyItemChanged(final int position) {
        notifyItemChanged(position, null);
    }

    public void notifyItemChanged(final int position, @Nullable final Object payload) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int count = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d("postNotifyItemChanged", "count=" + count + " position=" + position + " getCount=" + getCount());
                    mAdapter.notifyItemChanged(count + position, payload);
                    break;
                }
                count  += data.getCount();
            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position, payload);
                }
            });
        }

    }

    public void notifyItemRangeChanged(int positionStart, int count) {
        notifyItemRangeChanged(positionStart, count, null);
    }

    public void notifyItemRangeChanged(final int positionStart, final int count, @Nullable final Object payload) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int num = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d(TAG, "notifyItemRangeChanged positionStart=" + positionStart + " count=" + count + " getCount=" + getCount());
//                    if (positionStart >= getCount()) {
//                        return;
//                    }
//                    if (positionStart + count > getCount()) {
//                        mAdapter.notifyItemRangeChanged(num + positionStart, getCount() - positionStart, payload);
//                    } else {
//                        mAdapter.notifyItemRangeChanged(num + positionStart, count, payload);
//                    }
                    mAdapter.notifyItemRangeChanged(num + positionStart, count, payload);
                    break;
                }
                num  += data.getCount();
            }
            mLastCount = getCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeChanged(positionStart, count, payload);
                }
            });
        }

    }

    public void notifyItemRangeRemoved() {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int count = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d("notifyItemRangeRemoved", "count=" + count + " getCount=" + getCount());
                    mAdapter.notifyItemRangeRemoved(count, getCount());
                    break;
                }
                count  += data.getCount();
            }
            mLastCount = getCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved();
                }
            });
        }
    }

    public void notifyItemRangeRemoved(final int positionStart, final int count) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int num = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d(TAG, "notifyItemRangeRemoved positionStart=" + positionStart + " count=" + count + " num=" + num + " getCount=" + getCount());
//                    if (positionStart >= getCount()) {
//                        return;
//                    }
//                    if (positionStart + count > getCount()) {
//                        mAdapter.notifyItemRangeRemoved(num + positionStart, getCount() - positionStart);
//                    } else {
//                        mAdapter.notifyItemRangeRemoved(num + positionStart, count);
//                    }
                    mAdapter.notifyItemRangeRemoved(num + positionStart, count);
                    break;
                }
                num  += data.getCount();
            }
            mLastCount = getCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(positionStart, count);
                }
            });
        }

    }

    public void notifyItemRemoved(final int position) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int count = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d("postNotifyItemRemoved", "count=" + count + " position=" + position + " getCount=" + getCount());
                    mLastCount = getCount();
                    mAdapter.notifyItemRemoved(count + position);
                    break;
                }
                count  += data.getCount();
            }
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(position);
                }
            });
        }

    }

    public void notifyItemRangeInserted() {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int count = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
                    Log.d("notifyItemRangeInserted", "count=" + count + " getCount=" + getCount());
                    mLastCount = getCount();
                    mAdapter.notifyItemRangeInserted(count, getCount());
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
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeInserted();
                }
            });
        }

    }

    public void notifyItemRangeInserted(final int positionStart, final int count) {
        if (mAdapter == null) {
            mLastCount = getCount();
            return;
        }
        if (isInMainThread()) {
            int num = getStartCount();
            for (MultiData<?> data : mAdapter.getData()) {
                if (data == this) {
//                    if (positionStart >= getCount()) {
//                        return;
//                    }
//
//                    if (positionStart + count > getCount()) {
//                        mAdapter.notifyItemRangeInserted(num + positionStart, getCount() - positionStart);
//                    } else {
//                        mAdapter.notifyItemRangeInserted(num + positionStart, count);
//                    }
                    mAdapter.notifyItemRangeInserted(num + positionStart, count);
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
            mLastCount = getCount();
        } else {
            mAdapter.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeInserted(positionStart, count);
                }
            });
        }
    }

    protected int getStartCount() {
        int num = mAdapter.headerView == null ? 0 : 1;
        if (mAdapter.mRefreshHeader != null) {
            num++;
        }
        return num;
    }

    private boolean isInMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

}
