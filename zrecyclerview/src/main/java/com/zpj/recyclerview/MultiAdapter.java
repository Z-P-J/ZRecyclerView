package com.zpj.recyclerview;

import android.content.Context;
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
import android.widget.TextView;

import com.zpj.widget.statelayout.StateLayout;

import java.util.List;

import static com.zpj.recyclerview.EasyState.STATE_CONTENT;
import static com.zpj.recyclerview.EasyState.STATE_EMPTY;
import static com.zpj.recyclerview.EasyState.STATE_ERROR;
import static com.zpj.recyclerview.EasyState.STATE_LOADING;
import static com.zpj.recyclerview.EasyState.STATE_NO_NETWORK;

public class MultiAdapter extends EasyStateAdapter<MultiData> {

    private static final String TAG = "MultiAdapter";

//    private final IEasy.OnGetChildViewTypeListener<MultiData> onGetChildViewTypeListener;
//    private final IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener;
//    private final IEasy.OnBindViewHolderListener<MultiData> onBindViewHolderListener;
//    private final IEasy.OnCreateViewHolderListener<MultiData> onCreateViewHolder;
//    private final IEasy.OnItemClickListener<MultiData> onItemClickListener;
//    private final IEasy.OnItemLongClickListener<MultiData> onItemLongClickListener;

    MultiAdapter(final Context context, List<MultiData> list, final IEasy.OnLoadRetryListener onLoadRetryListener) {
        super(context, list, 0, null, null,
                null, null, null,
                null, null, null, onLoadRetryListener);
//        this.onGetChildViewTypeListener = onGetChildViewTypeListener;
//        this.onGetChildLayoutIdListener = onGetChildLayoutIdListener;
//        this.onBindViewHolderListener = onBindViewHolderListener;
//        this.onCreateViewHolder = onCreateViewHolder;
//        this.onItemClickListener = onClickListener;
//        this.onItemLongClickListener = onLongClickListener;
    }

    @NonNull
    @Override
    public EasyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_STATE) {
            Log.d(TAG, "onCreateViewHolder TYPE_STATE");
            return new EasyViewHolder(stateLayout);
        } else if (viewType == TYPE_HEADER) {
            return new EasyViewHolder(headerView);
        } else if (viewType == TYPE_FOOTER) {
            return new EasyViewHolder(footerView);
        } else {
            int res = onGetChildLayoutId(viewType);
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(res, viewGroup, false);
            return new EasyViewHolder(view);
        }
    }

    @Override
    public int getItemCount() {
        if (state != STATE_CONTENT) {
            return 1;
        }
        int count = 0;
        for (MultiData data : list) {
            count += data.getCount();
        }
        if (headerView != null) {
            count++;
        }
        if (footerView != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (state != STATE_CONTENT) {
            return TYPE_STATE;
        } else if (isHeaderPosition(position)) {
            return TYPE_HEADER;
        } else if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else {
            if (headerView != null) {
                position -= 1;
            }
            return onGetViewType(list, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EasyViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (state != STATE_CONTENT) {
            return;
        }
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
            if (!canScroll() && !mIsLoading && getLoadMoreEnabled()) {
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
                    if (!mIsLoading && getLoadMoreEnabled()) {
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


        position = getRealPosition(holder);
        int count = 0;
        for (MultiData data : list) {
            if (position >= count && position < count + data.getCount()) {
                data.onBindViewHolder(holder, position - count, payloads);
                break;
            }
            count  += data.getCount();
        }
    }

    @Override
    protected void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        Log.d(TAG, "onScrollStateChanged getLoadMoreEnabled=" + getLoadMoreEnabled() + "  mIsLoading=" + mIsLoading);
        if (footerView == null || !getLoadMoreEnabled() || mIsLoading) {
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
                isBottom = false;
            }
            Log.d(TAG, "onScrollStateChanged isBottom=" + isBottom);

            if (isBottom) {
                mIsLoading = true;
                onLoadMore();
            }
        }
    }

    @Override
    protected void onLoadMore() {
        Log.d(TAG, "onLoadMore");
        LinearLayout llContainerProgress = footerView.findViewById(R.id.ll_container_progress);
        TextView tvMsg = footerView.findViewById(R.id.tv_msg);
        if (list.isEmpty() || currentPage < -1) {
            currentPage = -1;
        }

        MultiData multiData = null;
        for (int i = list.size() - 1; i >= 0; i--) {
            MultiData data = list.get(i);
            if (!data.isDataLoaded()) {
                multiData = data;
            }
        }
//        for (MultiData data : list) {
//            if (!data.isDataLoaded()) {
//                multiData = data;
//            }
//        }
        if (multiData != null && multiData.load(mRecyclerView, this)) {
            if (llContainerProgress != null) {
                llContainerProgress.setVisibility(View.VISIBLE);
            }
            if (tvMsg != null) {
                tvMsg.setVisibility(View.GONE);
            }
            currentPage++;
        } else {
            mIsLoading = false;
            showFooterMsg(mRecyclerView.getContext().getString(R.string.easy_has_no_more));
        }
    }

    @Override
    protected void initLayoutManagerOnAttachedToRecyclerView(RecyclerView.LayoutManager manager) {
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isHeaderPosition(position) || isFooterPosition(position)) {
                        return gridManager.getSpanCount();
                    }
                    int count = 0;
                    for (MultiData data : list) {
                        if (position >= count && position < count + data.getCount()) {
                            return data.getSpanCount();
                        }
                        count  += data.getCount();
                    }
                    return gridManager.getSpanCount();
                }
            });
        }
    }

    public int onGetViewType(List<MultiData> list, int position) {
        int count = 0;
        for (MultiData data : list) {
            if (position >= count && position < count + data.getCount()) {
                return data.getViewType(position - count);
            }
            count  += data.getCount();
        }
        return TYPE_CHILD;
    }

    public int onGetChildLayoutId(int viewType) {
        for (MultiData data : list) {
            if (data.hasViewType(viewType)) {
                return data.getLayoutId(viewType);
            }
        }
        return 0;
    }


}