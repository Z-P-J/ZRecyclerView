package com.zpj.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class EasyAdapter<T> extends RecyclerView.Adapter<EasyViewHolder> {

    private static final String TAG = "EasyAdapter";

    private static final int TYPE_HEADER = -1;
    private static final int TYPE_CHILD = 0;
    private static final int TYPE_FOOTER = -2;


    protected final List<T> list;

    private int itemRes;

    protected int currentPage = -1;

    protected View headerView;
    protected View footerView;

    private final IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener;
    private final IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener;
    private final IEasy.OnBindViewHolderListener<T> onBindViewHolderListener;
    private final IEasy.OnCreateViewHolderListener<T> onCreateViewHolder;
    private IEasy.OnBindHeaderListener onBindHeaderListener;
    private IEasy.OnBindFooterListener onBindFooterListener;
    private final IEasy.OnItemClickListener<T> onItemClickListener;
    private final IEasy.OnItemLongClickListener<T> onItemLongClickListener;
    private final SparseArray<IEasy.OnClickListener<T>> onClickListeners;
    private final SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners;

    EasyAdapter(List<T> list, int itemRes,
                IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener,
                IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener,
                IEasy.OnCreateViewHolderListener<T> onCreateViewHolder,
                IEasy.OnBindViewHolderListener<T> onBindViewHolderListener,
                IEasy.OnItemClickListener<T> onClickListener,
                IEasy.OnItemLongClickListener<T> onLongClickListener,
                SparseArray<IEasy.OnClickListener<T>> onClickListeners,
                SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners) {
        this.list = list;
        this.itemRes = itemRes;
        this.onGetChildViewTypeListener = onGetChildViewTypeListener;
        this.onGetChildLayoutIdListener = onGetChildLayoutIdListener;
        this.onBindViewHolderListener = onBindViewHolderListener;
        this.onCreateViewHolder = onCreateViewHolder;
        this.onItemClickListener = onClickListener;
        this.onItemLongClickListener = onLongClickListener;
        this.onClickListeners = onClickListeners;
        this.onLongClickListeners = onLongClickListeners;
        registerAdapterDataObserver(mObserver);
        mEnabled = new Enabled(mOnEnabledListener);
    }

    @NonNull
    @Override
    public EasyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new EasyViewHolder(headerView);
        } else if (viewType == TYPE_FOOTER) {
            return new EasyViewHolder(footerView);
        } else {
            int res;
            if (onGetChildLayoutIdListener != null) {
                res = onGetChildLayoutIdListener.onGetChildLayoutId(viewType);
                if (res <= 0) {
                    res = itemRes;
                }
            } else {
                res = itemRes;
            }
            View view;
            if (onCreateViewHolder != null) {
                view = onCreateViewHolder.onCreateViewHolder(viewGroup, res, viewType);
            } else {
                view = LayoutInflater.from(viewGroup.getContext()).inflate(res, viewGroup, false);
            }
            return new EasyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EasyViewHolder easyViewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull final EasyViewHolder holder, int position, @NonNull List<Object> payloads) {
//        if (payloads.isEmpty()) {
////            onBindViewHolder(holder, position);
//            if (isHeaderPosition(position)) return;
//            if (isFooterPosition(position)) {
//                if (!canScroll() && mOnLoadMoreListener != null && !mIsLoading) {
//                    mIsLoading = true;
//                    // fix Cannot call this method while RecyclerView is computing a layout or scrolling
//                    mRecyclerView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (mOnLoadMoreListener.onLoadMore(mEnabled, currentPage + 1)) {
//                                footerView.setVisibility(View.VISIBLE);
//                                currentPage++;
//                            } else if (footerView != null){
//                                footerView.setVisibility(View.GONE);
//                            }
//                        }
//                    });
//                }
//                return;
//            }
//            if (onBindViewHolderListener != null) {
//                onBindViewHolderListener.onBindViewHolder(holder, list, getRealPosition(holder));
//            }
//        } else {
//            if (onBindViewHolderListener != null) {
//                onBindViewHolderListener.onBindViewHolder(holder, list, getRealPosition(holder), payloads);
//            }
//        }
        holder.setRealPosition(getRealPosition(holder));
        holder.setViewType(getItemViewType(position));
        if (isHeaderPosition(position)) {
            if (onBindHeaderListener != null) {
                onBindHeaderListener.onBindHeader(holder);
            }
            return;
        }
        if (isFooterPosition(position)) {
            Log.d(TAG, "isFooterPosition");
            if (list.isEmpty()) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                footerView.setLayoutParams(params);
            } else {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                footerView.setLayoutParams(params);
            }
            if (!canScroll() && mOnLoadMoreListener != null && !mIsLoading && getLoadMoreEnabled()) {
                mIsLoading = true;
                Log.d(TAG, "isFooterPosition onLoadMore");
                // fix Cannot call this method while RecyclerView is computing a layout or scrolling
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoadMore();
                    }
                });
            }
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "isFooterPosition click canScroll=" + canScroll() + " mIsLoading=" + mIsLoading);
                    if (mOnLoadMoreListener != null && !mIsLoading && getLoadMoreEnabled()) {
                        mIsLoading = true;
                        Log.d(TAG, "isFooterPosition onLoadMore");
                        // fix Cannot call this method while RecyclerView is computing a layout or scrolling
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                onLoadMore();
                            }
                        });
                    }
                }
            });
            if (onBindFooterListener != null) {
                onBindFooterListener.onBindFooter(holder);
            }
            return;
        }

        final T data = list.get(getRealPosition(holder));
        holder.setTag(data);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(holder, v, (T) holder.getTag());
                }
            }
        });
        holder.setOnItemLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onLongClick(holder, v, (T) holder.getTag());
                }
                return false;
            }
        });


        for (int i = 0; i < onClickListeners.size(); i++) {
            int key = onClickListeners.keyAt(i);
            View view = holder.getView(key);
            if (view != null) {
                final IEasy.OnClickListener<T> listener = onClickListeners.get(key);
                if (listener != null) {
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onClick(holder, v, (T) holder.getTag());
                        }
                    });
                    continue;
                }
                view.setOnClickListener(null);
            }
        }

        for (int i = 0; i < onLongClickListeners.size(); i++) {
            int key = onLongClickListeners.keyAt(i);
            View view = holder.getView(key);
            if (view != null) {
                final IEasy.OnLongClickListener<T> listener = onLongClickListeners.get(key);
                if (listener != null) {
                    view.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return listener.onLongClick(holder, v, (T) holder.getTag());
                        }
                    });
                    continue;
                }
                view.setOnLongClickListener(null);
            }
        }

        if (onBindViewHolderListener != null) {
            onBindViewHolderListener.onBindViewHolder(holder, list, getRealPosition(holder), payloads);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mOnScrollListener);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isHeaderPosition(position) || isFooterPosition(position))
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(mOnScrollListener);
        unregisterAdapterDataObserver(mObserver);
        mRecyclerView = null;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull EasyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.getItemView().getLayoutParams();
        if (lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (isHeaderPosition(holder.getLayoutPosition()) || isFooterPosition(holder.getLayoutPosition()))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return TYPE_HEADER;
        } else if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else if (onGetChildViewTypeListener != null) {
            if (headerView != null) {
                position -= 1;
            }
            return onGetChildViewTypeListener.onGetViewType(list, position);
        }
        return TYPE_CHILD;
    }

    @Override
    public int getItemCount() {
        int count = list == null ? 0 : list.size();
        if (headerView != null) {
            count++;
        }
        if (footerView != null) {
            count++;
        }
        return count;
    }

    protected void onLoadMore() {
        Log.d(TAG, "onLoadMore");
        LinearLayout llContainerProgress = footerView.findViewById(R.id.ll_container_progress);
        TextView tvMsg = footerView.findViewById(R.id.tv_msg);
        if (list.isEmpty() || currentPage < -1) {
            currentPage = -1;
        }
        if (mOnLoadMoreListener.onLoadMore(mEnabled, currentPage + 1)) {
            if (llContainerProgress != null) {
                llContainerProgress.setVisibility(View.VISIBLE);
            }
            if (tvMsg != null) {
                tvMsg.setVisibility(View.GONE);
            }
            currentPage++;
        } else {
//            if (llContainerProgress != null) {
//                llContainerProgress.setVisibility(View.GONE);
//            }
//            if (tvMsg != null) {
//                tvMsg.setVisibility(View.VISIBLE);
//            }
            mIsLoading = false;
            showFooterMsg(mRecyclerView.getContext().getString(R.string.easy_has_no_more));
        }
    }

    protected void showFooterMsg(String msg) {
        LinearLayout llContainerProgress = footerView.findViewById(R.id.ll_container_progress);
        TextView tvMsg = getFooterView().findViewById(R.id.tv_msg);
        if (llContainerProgress != null) {
            llContainerProgress.setVisibility(View.GONE);
        }
        if (tvMsg != null) {
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText(msg);
        }
    }

    private boolean canScroll() {
        if (mRecyclerView == null) {
            throw new NullPointerException("mRecyclerView is null, you should setAdapter(recyclerAdapter);");
        }
        return mRecyclerView.canScrollVertically(-1);
//        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    protected boolean isHeaderPosition(int position) {
        return headerView != null && position == 0;
    }

    protected boolean isFooterPosition(int position) {
        return footerView != null && position == getItemCount() - 1;
    }

    private int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return headerView == null ? position : position - 1;
    }

    public void setHeaderView(@NonNull View headerView) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headerView.setLayoutParams(params);
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    public void setOnBindHeaderListener(IEasy.OnBindHeaderListener onBindHeaderListener) {
        this.onBindHeaderListener = onBindHeaderListener;
    }

    public void setOnBindFooterListener(IEasy.OnBindFooterListener onBindFooterListener) {
        this.onBindFooterListener = onBindFooterListener;
    }

    public View getHeaderView() {
        return headerView;
    }

    public void setFooterView(@NonNull View footerView) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerView.setLayoutParams(params);
        this.footerView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    public View getFooterView() {
        return footerView;
    }

    public void setOnLoadMoreListener(IEasy.OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
        setLoadMoreEnabled(true);
    }

    private RecyclerView mRecyclerView;
    private IEasy.OnLoadMoreListener mOnLoadMoreListener;

    protected Enabled mEnabled;
    protected boolean mIsLoading;
    private boolean mShouldRemove;
    private boolean mShowNoMoreEnabled;
    private boolean mIsLoadFailed;


    public boolean getLoadMoreEnabled() {
        return mEnabled.getLoadMoreEnabled() && getItemCount() >= 0;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        mEnabled.setLoadMoreEnabled(enabled);
    }

    private void notifyFooterHolderChanged() {
        if (getLoadMoreEnabled()) {
            notifyItemChanged(getItemCount());
        } else if (mShouldRemove) {
            mShouldRemove = false;

            /*
              fix IndexOutOfBoundsException when setLoadMoreEnabled(false) and then use onItemRangeInserted
              @see android.support.v7.widget.RecyclerView.Recycler#validateViewHolderForOffsetPosition(RecyclerView.ViewHolder)
             */
            int position = getItemCount();
            if (isFooterPosition(position)) {
                notifyItemRemoved(position);
            } else {
                notifyItemChanged(position);
            }
//            RecyclerView.ViewHolder viewHolder =
//                    mRecyclerView.findViewHolderForAdapterPosition(position);
//            if (viewHolder instanceof LoadMoreAdapter.FooterHolder) {
//                notifyItemRemoved(position);
//            } else {
//                notifyItemChanged(position);
//            }
        }
    }

    private interface OnEnabledListener {
        void notifyChanged();

        void notifyLoadFailed(boolean isLoadFailed);
    }

    public static class Enabled {
        private boolean mLoadMoreEnabled = false;
        private boolean mIsLoadFailed = false;
        private OnEnabledListener mListener;

        public Enabled(OnEnabledListener listener) {
            mListener = listener;
        }

        /**
         * 设置是否启用加载更多
         *
         * @param enabled 是否启用
         */
        public void setLoadMoreEnabled(boolean enabled) {
            final boolean canNotify = mLoadMoreEnabled;
            mLoadMoreEnabled = enabled;

            if (canNotify && !mLoadMoreEnabled) {
                mListener.notifyChanged();
            }
        }

        /**
         * 设置是否加载失败
         *
         * @param isLoadFailed 是否加载失败
         */
        public void setLoadFailed(boolean isLoadFailed) {
            if (mIsLoadFailed != isLoadFailed) {
                mIsLoadFailed = isLoadFailed;
                mListener.notifyLoadFailed(isLoadFailed);
                setLoadMoreEnabled(!mIsLoadFailed);
            }
        }

        /**
         * 获取是否启用了加载更多,默认是 true
         *
         * @return boolean
         */
        public boolean getLoadMoreEnabled() {
            return mLoadMoreEnabled;
        }
    }

    private OnEnabledListener mOnEnabledListener = new OnEnabledListener() {
        @Override
        public void notifyChanged() {
            mShouldRemove = true;
//            if (!getLoadMoreEnabled()) {
//                currentPage = -1;
//            }
//            mOnScrollListener.onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
        }

        @Override
        public void notifyLoadFailed(boolean isLoadFailed) {
            mIsLoadFailed = isLoadFailed;
//            notifyFooterHolderChanged();
            Log.d(TAG, "notifyLoadFailed isLoadFailed=" + isLoadFailed);
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            Log.d(TAG, "onScrollStateChanged getLoadMoreEnabled=" + getLoadMoreEnabled() + "  mIsLoading=" + mIsLoading);
            if (footerView == null || !getLoadMoreEnabled() || mIsLoading || mOnLoadMoreListener == null) {
                return;
            }
            Log.d(TAG, "onScrollStateChanged newState=" + newState);

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                boolean isBottom;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    isBottom = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition()
                            >= layoutManager.getItemCount() - 1;
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager sgLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                    int[] into = new int[sgLayoutManager.getSpanCount()];
                    sgLayoutManager.findLastVisibleItemPositions(into);

                    isBottom = last(into) >= layoutManager.getItemCount() - 1;
                } else {
//                    isBottom = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition()
//                            >= layoutManager.getItemCount() - 1;
                    isBottom = false;
                }
                Log.d(TAG, "onScrollStateChanged isBottom=" + isBottom);

                if (isBottom) {
                    mIsLoading = true;
                    onLoadMore();
//                    ProgressBar progressBar = footerView.findViewById(R.id.progress_bar);
//                    TextView tvMsg = footerView.findViewById(R.id.tv_msg);
//                    if (list.isEmpty() || currentPage < -1) {
//                        currentPage = -1;
//                    }
//                    if (mOnLoadMoreListener.onLoadMore(mEnabled, currentPage + 1)) {
//                        if (progressBar != null) {
//                            progressBar.setVisibility(View.VISIBLE);
//                        }
//                        if (tvMsg != null) {
//                            tvMsg.setVisibility(View.GONE);
//                        }
//                        currentPage++;
//                    } else if (footerView != null) {
//                        if (progressBar != null) {
//                            progressBar.setVisibility(View.GONE);
//                        }
//                        if (tvMsg != null) {
//                            tvMsg.setVisibility(View.VISIBLE);
//                        }
//                    }
                }
            }
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
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
    };

    private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (mShouldRemove) {
                mShouldRemove = false;
            }
//            notifyDataSetChanged();
            mIsLoading = false;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mShouldRemove && positionStart == getItemCount()) {
                mShouldRemove = false;
            }
//            notifyItemRangeChanged(positionStart, itemCount);
            mIsLoading = false;
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            if (mShouldRemove && positionStart == getItemCount()) {
                mShouldRemove = false;
            }
//            notifyItemRangeChanged(positionStart, itemCount, payload);
            mIsLoading = false;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            // when no data is initialized (has loadMoreView)
            // should remove loadMoreView before notifyItemRangeInserted
//            if (mRecyclerView.getChildCount() == 1) {
//                notifyItemRemoved(0);
//            }
//            notifyItemRangeInserted(positionStart, itemCount);
            notifyFooterHolderChanged();
            mIsLoading = false;
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (mShouldRemove && positionStart == getItemCount()) {
                mShouldRemove = false;
            }
            /*
               use notifyItemRangeRemoved after clear item, can throw IndexOutOfBoundsException
               @link RecyclerView#tryGetViewHolderForPositionByDeadline
               fix java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position
             */
            boolean shouldSync = false;
            if (mEnabled.getLoadMoreEnabled() && getItemCount() == 0) {
                setLoadMoreEnabled(false);
                shouldSync = true;
                // when use onItemRangeInserted(0, count) after clear item
                // recyclerView will auto scroll to bottom, because has one item(loadMoreView)
                // remove loadMoreView
                if (getItemCount() == 1) {
                    notifyItemRemoved(0);
                }
            }
//            notifyItemRangeRemoved(positionStart, itemCount);
            if (shouldSync) {
                setLoadMoreEnabled(true);
            }
            mIsLoading = false;
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mShouldRemove && (fromPosition == getItemCount() || toPosition == getItemCount())) {
                throw new IllegalArgumentException("can not move last position after setLoadMoreEnabled(false)");
            }
//            notifyItemMoved(fromPosition, toPosition);
            mIsLoading = false;
        }
    };

}
