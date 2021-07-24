package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;
import static com.zpj.statemanager.State.STATE_LOGIN;

public class MultiAdapter extends EasyStateAdapter<MultiData<?>> {

    private static final String TAG = "MultiAdapter";

    MultiAdapter(final Context context, List<MultiData<?>> list, final EasyStateConfig<?> config, IRefresh refresh) {
        super(context, list, 0, null, null,
                null, null, null,
                null, null, null, refresh, config);
    }

    @NonNull
    @Override
    public EasyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (state != STATE_CONTENT && (viewType == State.STATE_EMPTY.hashCode() || viewType == State.STATE_LOADING.hashCode()
                || viewType == State.STATE_ERROR.hashCode() || viewType == STATE_LOGIN.hashCode()
                || viewType == State.STATE_NO_NETWORK.hashCode())) {
            IViewHolder viewHolder = config.getViewHolder(state);
            if (viewHolder != null) {
                View view = viewHolder.onCreateView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                return new EasyViewHolder(view);
            }
        } else if (viewType == TYPE_REFRESH) {
            return new EasyViewHolder(mRefreshHeader.onCreateView(context, viewGroup));
        } else if (viewType == TYPE_HEADER) {
            return new EasyViewHolder(headerView);
        } else if (viewType == TYPE_FOOTER) {
            return footerViewHolder.onCreateViewHolder(viewGroup);
        }
        return new EasyViewHolder(onCreateView(viewGroup.getContext(), viewGroup, viewType));
    }

    @Override
    public int getItemCount() {
        if (state != State.STATE_CONTENT) {
            return 1;
        }
        int count = 0;
        for (MultiData<?> data : list) {
            count += data.getCount();
            if (data.hasMore()) {
                break;
            }
        }
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

    @Override
    public int getItemViewType(int position) {
        if (state != State.STATE_CONTENT) {
            return state.hashCode();
        } else if (isRefreshPosition(position)) {
            return TYPE_REFRESH;
        } else if (isHeaderPosition(position)) {
            return TYPE_HEADER;
        } else if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else {
            if (mRefreshHeader != null) {
                position--;
            }
            if (headerView != null) {
                position--;
            }
            return onGetViewType(list, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EasyViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (state != State.STATE_CONTENT) {
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
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    list.isEmpty() ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
            footerViewHolder.getView().setLayoutParams(params);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tryToLoadMore();
                }
            });
            footerViewHolder.onBindFooter(holder);
            tryToLoadMore();
            return;
        }


        position = getRealPosition(holder);
        int count = 0;
        for (MultiData<?> data : list) {
            if (position >= count && position < count + data.getCount()) {
                data.setAdapter(this);
                data.onBindViewHolder(holder, position - count, payloads);
                break;
            }
            count  += data.getCount();
        }
    }

    @Override
    protected void onLoadMore() {
        if (mIsLoading || !getLoadMoreEnabled() || !isBottom()) {
            return;
        }
        mIsLoading = true;
        Log.d(TAG, "onLoadMore");
        if (list.isEmpty() || currentPage < -1) {
            currentPage = -1;
        }

        MultiData<?> multiData = null;
        for (MultiData<?> data : list) {
            if (data.hasMore()) {
                multiData = data;
                break;
            }
        }
        if (multiData != null && multiData.load(this)) {
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
                    if (mRefreshHeader != null) {
                        position--;
                    }
                    if (headerView != null) {
                        position--;
                    }
                    int count = 0;
                    for (MultiData<?> data : list) {
                        if (position >= count && position < count + data.getCount()) {
                            int columnCount = data.getColumnCount(data.getViewType(position - count));
                            return gridManager.getSpanCount() / columnCount;
                        }
                        count  += data.getCount();
                    }
                    return gridManager.getSpanCount();
                }
            });
        }
    }

    public int onGetViewType(List<MultiData<?>> list, int position) {
        int count = 0;
        for (MultiData<?> data : list) {
            if (position >= count && position < count + data.getCount()) {
                return data.getViewType(position - count);
            }
            count  += data.getCount();
        }
        return TYPE_CHILD;
    }

//    public int onGetChildLayoutId(int viewType) {
//        for (MultiData<?> data : list) {
//            if (data.hasViewType(viewType)) {
//                return data.getLayoutId(viewType);
//            }
////            int id = data.getLayoutId(viewType);
////            if (id > 0) {
////                return id;
////            }
//        }
//        return 0;
//    }

    public View onCreateView(Context context, ViewGroup container, int viewType) {
        for (MultiData<?> data : list) {
            if (data.hasViewType(viewType)) {
                return data.onCreateView(context, container, viewType);
            }
        }
        return null;
    }

    public void notifyDataSetChange(MultiData<?> data) {
        int count = 0;
        for (MultiData<?> multiData : list) {
            if (multiData == data) {
                notifyItemRangeChanged(count, data.getCount());
            }
            count  += multiData.getCount();
        }
    }

    public void notifyItemRangeInserted(MultiData<?> data) {
        int count = 0;
        for (MultiData<?> multiData : list) {
            if (multiData == data) {
                notifyItemRangeInserted(count, data.getCount());
            }
            count  += multiData.getCount();
        }
    }

    public void notifyItemRangeInserted(MultiData<?> data, int positionStart, int count) {
        int num = 0;
        for (MultiData<?> multiData : list) {
            if (multiData == data) {
                if (positionStart >= data.getCount()) {
                    return;
                }
                if (positionStart + count > data.getCount()) {
                    notifyItemRangeInserted(num + positionStart, data.getCount() - positionStart);
                } else {
                    notifyItemRangeInserted(num + positionStart, count);
                }

//                if (positionStart + count <= data.getCount()) {
//                    notifyItemRangeInserted(num + positionStart, count);
//                } else {
//                    notifyItemRangeInserted(num + positionStart, count);
//                }
            }
            num  += multiData.getCount();
        }
    }


}