package com.zpj.recyclerview;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.footer.IFooterViewHolder;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.List;

public class EasyAdapter<T> extends RecyclerView.Adapter<EasyViewHolder> {

    private static final String TAG = "EasyAdapter";

    protected static final int TYPE_REFRESH = -3;
    protected static final int TYPE_HEADER = -1;
    protected static final int TYPE_CHILD = 0;
    protected static final int TYPE_FOOTER = -2;


    protected final List<T> list;

    protected int itemRes;

    protected int currentPage = -1;

    protected View headerView;
    protected IFooterViewHolder footerViewHolder;

    protected final IRefresher mRefreshHeader;

    protected final IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener;
    protected final IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener;
    protected final IEasy.OnBindViewHolderListener<T> onBindViewHolderListener;
    protected final IEasy.OnCreateViewHolderListener<T> onCreateViewHolder;
    protected IEasy.OnBindHeaderListener onBindHeaderListener;
    //    protected IEasy.OnBindFooterListener onBindFooterListener;
    protected final IEasy.OnItemClickListener<T> onItemClickListener;
    protected final IEasy.OnItemLongClickListener<T> onItemLongClickListener;
    protected final SparseArray<IEasy.OnClickListener<T>> onClickListeners;
    protected final SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners;

    protected IEasy.AdapterInjector adapterInjector;

    protected boolean mIsDraggingOrSwiping = false;

    EasyAdapter(List<T> list, int itemRes,
                IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener,
                IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener,
                IEasy.OnCreateViewHolderListener<T> onCreateViewHolder,
                IEasy.OnBindViewHolderListener<T> onBindViewHolderListener,
                IEasy.OnItemClickListener<T> onClickListener,
                IEasy.OnItemLongClickListener<T> onLongClickListener,
                SparseArray<IEasy.OnClickListener<T>> onClickListeners,
                SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners, IRefresher refresh) {
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
        mRefreshHeader = refresh;
    }

    public List<T> getData() {
        return list;
    }

    @NonNull
    @Override
    public EasyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_REFRESH) {
            return new EasyViewHolder(mRefreshHeader.onCreateView(viewGroup.getContext(), viewGroup));
        } else if (viewType == TYPE_HEADER) {
            return new EasyViewHolder(headerView);
        } else if (viewType == TYPE_FOOTER) {
            return footerViewHolder.onCreateViewHolder(viewGroup);
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
        holder.setRealPosition(getRealPosition(holder));
        holder.setViewType(getItemViewType(position));
        if (isRefreshPosition(position)) {
            return;
        }
        if (isHeaderPosition(position)) {
            if (onBindHeaderListener != null) {
                onBindHeaderListener.onBindHeader(holder);
            }
            return;
        }
        if (isFooterPosition(position)) {
            Log.d(TAG, "isFooterPosition");
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    list.isEmpty() ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
            footerViewHolder.getView().setLayoutParams(params);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "isFooterPosition click canScroll=" + canScroll() + " mIsLoading=" + mIsLoading + " isBottom=" + isBottom());
                    tryToLoadMore();
                }
            });
            footerViewHolder.onBindFooter(holder);
            tryToLoadMore();
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


        holder.setOnViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IEasy.OnClickListener<T> listener = onClickListeners.get(View.NO_ID);
                if (listener != null) {
                    listener.onClick(holder, v, (T) holder.getTag());
                }
            }
        });
        holder.setOnViewLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                IEasy.OnLongClickListener<T> listener = onLongClickListeners.get(View.NO_ID);
                if (listener != null) {
                    return listener.onLongClick(holder, v, (T) holder.getTag());
                }
                return false;
            }
        });
        for (int i = 0; i < onClickListeners.size(); i++) {
            int key = onClickListeners.keyAt(i);
            if (key == View.NO_ID) {
                continue;
            }
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
    public void onViewRecycled(@NonNull EasyViewHolder holder) {
        super.onViewRecycled(holder);
        if (adapterInjector != null) {
            adapterInjector.onViewRecycled(holder);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                Log.d(TAG, "onInterceptTouchEvent action=" + MotionEvent.actionToString(motionEvent.getAction()));
                if (recyclerView.getLayoutManager() instanceof IMultiLayoutManager) {
                    ((IMultiLayoutManager) recyclerView.getLayoutManager()).onTouch(motionEvent);
                }
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
                Log.d(TAG, "onTouchEvent action=" + MotionEvent.actionToString(motionEvent.getAction()));
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            private float downX = -1;
            private float downY = -1;
            private float offset = 0;
            private boolean isMoveDown;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (recyclerView.getLayoutManager() instanceof IMultiLayoutManager) {
//                    ((IMultiLayoutManager) recyclerView.getLayoutManager()).onTouch(event);
//                }
                if (mRefreshHeader == null) {
                    return false;
                }
                if (mIsDraggingOrSwiping) {
                    return false;
                }

                int action = event.getAction();
                if (MotionEvent.ACTION_DOWN == action) {
                    isMoveDown = false;
                    if (mRefreshHeader.getView() != null
                            && mRefreshHeader.getView().getParent() != null
                            && mRefreshHeader.getState() == IRefresher.STATE_NORMAL) {
                        downX = event.getRawX();
                        downY = event.getRawY();
                        offset = mRefreshHeader.getDelta();
                    }
                    return false;
                } else if (MotionEvent.ACTION_MOVE == action) {
                    if (isMoveDown) {
                        float deltaY = event.getRawY() - downY + offset;
                        mRefreshHeader.onMove(deltaY);
                        event.setAction(MotionEvent.ACTION_DOWN);
                        return false;
                    } else if (mRefreshHeader.getView() != null
                            && mRefreshHeader.getView().getParent() != null
                            && mRefreshHeader.getState() == IRefresher.STATE_NORMAL) {
                        if (downY < 0) {
                            downY = event.getRawY();
                            offset = mRefreshHeader.getDelta();
                            mRefreshHeader.onDown();
                            event.setAction(MotionEvent.ACTION_DOWN);
                        } else {
                            float deltaY = event.getRawY() - downY + offset;
                            if (deltaY > 0) {
                                isMoveDown = true;
                                mRefreshHeader.onMove(deltaY);
                                event.setAction(MotionEvent.ACTION_DOWN);
                            }
                        }
                        return false;
                    }
                } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
                    if (isMoveDown) {
                        isMoveDown = false;
                        downX = -1;
                        downY = -1;
                        offset = 0;
                        mRefreshHeader.onRelease();
                        return false;
                    }
                }
                return false;
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mOnScrollListener);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        initLayoutManagerOnAttachedToRecyclerView(manager);
        if (adapterInjector != null) {
            adapterInjector.onAttachedToRecyclerView(recyclerView);
        }
    }

    protected void initLayoutManagerOnAttachedToRecyclerView(RecyclerView.LayoutManager manager) {
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
        if (adapterInjector != null) {
            adapterInjector.onDetachedFromRecyclerView(recyclerView);
        }
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
        if (adapterInjector != null) {
            adapterInjector.onViewAttachedToWindow(holder);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull EasyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (adapterInjector != null) {
            adapterInjector.onViewDetachedFromWindow(holder);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isRefreshPosition(position)) {
            return TYPE_REFRESH;
        } else if (isHeaderPosition(position)) {
            return TYPE_HEADER;
        } else if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else if (onGetChildViewTypeListener != null) {
            if (mRefreshHeader != null) {
                position--;
            }
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
        if (mRefreshHeader != null) {
            count++;
        }
        if (headerView != null) {
            count++;
        }
        if (footerViewHolder != null) {
            count++;
        }
        return count;
    }

    protected void tryToLoadMore() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                onLoadMore();
            }
        });
    }

    protected void onLoadMore() {
        if (mOnLoadMoreListener == null || mIsLoading || !getLoadMoreEnabled() || !isBottom()) {
            return;
        }
        mIsLoading = true;
        if (list.isEmpty() || currentPage < -1) {
            currentPage = -1;
        }
        if (mOnLoadMoreListener.onLoadMore(mEnabled, currentPage + 1)) {
            if (footerViewHolder != null) {
                footerViewHolder.onShowLoading();
            }
            currentPage++;
        } else {
            mIsLoading = false;
            if (footerViewHolder != null) {
                footerViewHolder.onShowHasNoMore();
            }
        }
    }

    protected void showFooterMsg(String msg) {
//        LinearLayout llContainerProgress = footerView.findViewById(R.id.ll_container_progress);
//        TextView tvMsg = getFooterView().findViewById(R.id.tv_msg);
//        if (llContainerProgress != null) {
//            llContainerProgress.setVisibility(View.GONE);
//        }
//        if (tvMsg != null) {
//            tvMsg.setVisibility(View.VISIBLE);
//            tvMsg.setText(msg);
//        }
        if (footerViewHolder != null) {
            footerViewHolder.onShowError(msg);
        }
    }

    protected boolean canScroll() {
        if (mRecyclerView == null) {
            throw new NullPointerException("mRecyclerView is null, you should setAdapter(recyclerAdapter);");
        }
        return mRecyclerView.canScrollVertically(-1);
//        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }

    protected boolean isRefreshPosition(int position) {
        return mRefreshHeader != null && position == 0;
    }

    protected boolean isHeaderPosition(int position) {
        return headerView != null && (mRefreshHeader == null ? position == 0 : position == 1);
    }

    protected boolean isFooterPosition(int position) {
        return footerViewHolder != null && position == getItemCount() - 1;
    }

    protected int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
//        return headerView == null ? position : position - 1;
        if (headerView != null) {
            position--;
        }
        if (mRefreshHeader != null) {
            position--;
        }
        return position;
    }

    public void setAdapterInjector(IEasy.AdapterInjector adapterInjector) {
        this.adapterInjector = adapterInjector;
    }

    public IEasy.AdapterInjector getAdapterInjector() {
        return adapterInjector;
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

//    public void setOnBindFooterListener(IEasy.OnBindFooterListener onBindFooterListener) {
//        this.onBindFooterListener = onBindFooterListener;
//    }

    public View getHeaderView() {
        return headerView;
    }

    public void setFooterView(@NonNull View footerView) {
//        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        footerView.setLayoutParams(params);
//        this.footerView = footerView;
//        notifyItemInserted(getItemCount() - 1);
    }

    public void setFooterViewHolder(IFooterViewHolder footerViewHolder) {
        this.footerViewHolder = footerViewHolder;
    }

    //    public View getFooterView() {
//        return footerView;
//    }

    public IFooterViewHolder getFooterViewHolder() {
        return footerViewHolder;
    }

    public void setOnLoadMoreListener(IEasy.OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
        setLoadMoreEnabled(true);
    }

    protected RecyclerView mRecyclerView;
    protected IEasy.OnLoadMoreListener mOnLoadMoreListener;

    protected Enabled mEnabled;
    protected boolean mIsLoading;
    private boolean mShouldRemove;
    private boolean mShowNoMoreEnabled;
    private boolean mIsLoadFailed;

    public IRefresher getRefresher() {
        return mRefreshHeader;
    }

    public boolean getLoadMoreEnabled() {
        return mEnabled.getLoadMoreEnabled() && getItemCount() >= 0;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        mEnabled.setLoadMoreEnabled(enabled);
    }

    private void notifyFooterHolderChanged() {
        Log.d(TAG, "notifyFooterHolderChanged getLoadMoreEnabled=" + getLoadMoreEnabled());
        if (getLoadMoreEnabled()) {
            mShouldRemove = false;
            postNotifyItemChanged(getItemCount() - 1);
//            notifyItemChanged(getItemCount() - 1);
        } else if (mShouldRemove) {
            mShouldRemove = false;

            /*
              fix IndexOutOfBoundsException when setLoadMoreEnabled(false) and then use onItemRangeInserted
              @see android.support.v7.widget.RecyclerView.Recycler#validateViewHolderForOffsetPosition(RecyclerView.ViewHolder)
             */
            int position = getItemCount() - 1;
            Log.d(TAG, "notifyFooterHolderChanged isFooterPosition(position)=" + isFooterPosition(position));
            if (isFooterPosition(position)) {
//                notifyItemRemoved(position);
                postNotifyItemRemoved(position);
            } else {
//                notifyItemChanged(position);
                postNotifyItemChanged(position);
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

    protected void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        Log.d(TAG, "onScrollStateChanged newState=" + newState);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onLoadMore();
        }
    }

    protected boolean isBottom() {
        boolean isBottom;
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        Log.d(TAG, "isBottom layoutManager=" + layoutManager);
        if (layoutManager instanceof LinearLayoutManager) {
            int last = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            Log.d(TAG, "isBottom last=" + last + " itemCount=" + getItemCount() + " count=" + layoutManager.getItemCount() + " isFirst=" + isFooterPosition(last));
            isBottom = isFooterPosition(last);
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager sgLayoutManager = (StaggeredGridLayoutManager) layoutManager;
            int[] into = new int[sgLayoutManager.getSpanCount()];
            sgLayoutManager.findLastVisibleItemPositions(into);

            isBottom = isFooterPosition(last(into));
        } else {
            isBottom = false;
        }
        return isBottom;
    }

    protected int last(int[] lastPositions) {
        int last = lastPositions[0];
        for (int value : lastPositions) {
            if (value > last) {
                last = value;
            }
        }
        return last;
    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            EasyAdapter.this.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }

    };

    private final RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            if (mShouldRemove) {
                mShouldRemove = false;
            }
//            notifyDataSetChanged();
            mIsLoading = false;
            Log.d(TAG, "onChanged getCount=" + getItemCount());
//            onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);

            if (mRefreshHeader != null) {
                mRefreshHeader.stopRefresh();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            if (mShouldRemove && positionStart == getItemCount() - 1) {
                mShouldRemove = false;
            }
//            notifyItemRangeChanged(positionStart, itemCount);
            mIsLoading = false;
//            onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
            Log.d(TAG, "onItemRangeChanged getCount=" + getItemCount() + " positionStart=" + positionStart + " itemCount=" + itemCount);
        }

//        @Override
//        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
//            if (mShouldRemove && positionStart == getItemCount() - 1) {
//                mShouldRemove = false;
//            }
////            notifyItemRangeChanged(positionStart, itemCount, payload);
//            mIsLoading = false;
//            Log.d(TAG, "onItemRangeChanged getCount=" + getItemCount() + " positionStart=" + positionStart + " itemCount=" + itemCount + " payload=" + payload);
//        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            // when no data is initialized (has loadMoreView)
            // should remove loadMoreView before notifyItemRangeInserted
//            if (mRecyclerView.getChildCount() == 1) {
//                notifyItemRemoved(0);
//            }
//            notifyItemRangeInserted(positionStart, itemCount);
            Log.d(TAG, "onItemRangeInserted positionStart=" + positionStart + " itemCount=" + itemCount + " getCount=" + getItemCount());
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    notifyFooterHolderChanged();
//                }
//            }, 1000);
            mIsLoading = false;
//            onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            Log.d(TAG, "onItemRangeRemoved positionStart=" + positionStart + " itemCount=" + itemCount + " getCount=" + getItemCount());
            if (mShouldRemove && positionStart == getItemCount() - 1) {
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
//            onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (mShouldRemove && (fromPosition == getItemCount() - 1 || toPosition == getItemCount() - 1)) {
                throw new IllegalArgumentException("can not move last position after setLoadMoreEnabled(false)");
            }
//            notifyItemMoved(fromPosition, toPosition);
            mIsLoading = false;
//            onScrollStateChanged(mRecyclerView, RecyclerView.SCROLL_STATE_IDLE);
        }
    };

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void post(Runnable runnable) {
        if (mRecyclerView != null) {
            mRecyclerView.post(runnable);
        }
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        if (mRecyclerView != null) {
            mRecyclerView.postDelayed(runnable, delayMillis);
        }
    }


    public void postNotifyDataSetChanged() {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
    }

    public void postNotifyItemChanged(final int position) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position);
                }
            });
        }
    }

    public void postNotifyItemChanged(final int position, @Nullable final Object payload) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemChanged(position, payload);
                }
            });
        }
    }

    public void postNotifyItemRangeChanged(final int positionStart, final int itemCount) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeChanged(positionStart, itemCount);
                }
            });
        }
    }

    public void postNotifyItemRangeChanged(final int positionStart, final int itemCount, @Nullable final Object payload) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeChanged(positionStart, itemCount, payload);
                }
            });
        }
    }

    public void postNotifyItemInserted(final int position) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemInserted(position);
                }
            });
        }
    }

    public void postNotifyItemMoved(final int fromPosition, final int toPosition) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemMoved(fromPosition, toPosition);
                }
            });
        }
    }

    public void postNotifyItemRangeInserted(final int positionStart, final int itemCount) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeInserted(positionStart, itemCount);
                }
            });
        }
    }

    public void postNotifyItemRemoved(final int position) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRemoved(position);
                }
            });
        }
    }

    public void postNotifyItemRangeRemoved(final int positionStart, final int itemCount) {
        if (mRecyclerView != null) {
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
            });
        }
    }

}
