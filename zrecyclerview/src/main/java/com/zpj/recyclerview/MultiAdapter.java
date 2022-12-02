package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.core.MultiScene;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;
import static com.zpj.statemanager.State.STATE_LOGIN;

public class MultiAdapter extends EasyStateAdapter<MultiScene> {

    private static final String TAG = "MultiAdapter";

    MultiAdapter(final Context context, List<MultiScene> list, final EasyStateConfig<?> config, IRefresher refresher) {
        super(context, list, 0, null, null,
                null, null, null,
                null, null, null, refresher, config);
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
        } else if (viewType == TYPE_REFRESH && !(getRecyclerView().getLayoutManager() instanceof BaseMultiLayoutManager)) {
            View view = onCreateView(viewGroup.getContext(), viewGroup, viewType);
            if (view == null) {
                return new EasyViewHolder(mRefreshHeader.onCreateView(context, viewGroup));
            }
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
        for (MultiScene scene : list) {
            count += scene.getItemCount();
        }
        boolean isMultiManager = getRecyclerView().getLayoutManager() instanceof BaseMultiLayoutManager;
        if (!isMultiManager && mRefreshHeader != null) {
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
        boolean isMultiManager = getRecyclerView().getLayoutManager() instanceof BaseMultiLayoutManager;
        if (state != State.STATE_CONTENT) {
            return state.hashCode();
        } else if (!isMultiManager && isRefreshPosition(position)) {
            return TYPE_REFRESH;
        } else if (isHeaderPosition(position)) {
            return TYPE_HEADER;
        } else if (isFooterPosition(position)) {
            return TYPE_FOOTER;
        } else {
            if (!isMultiManager && mRefreshHeader != null) {
                position--;
            }
            if (headerView != null) {
                position--;
            }
            return onGetViewType(list, position);
        }
    }

    @Override
    protected int getRealPosition(RecyclerView.ViewHolder holder) {
        if (getRecyclerView().getLayoutManager() instanceof BaseMultiLayoutManager) {
            int position = holder.getLayoutPosition();
            if (headerView != null) {
                position--;
            }
            return position;
        }
        return super.getRealPosition(holder);
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
        for (MultiScene scene : list) {
            int itemCount = scene.getItemCount();
            MultiData<?> data = scene.getMultiData();
            if (position >= count && position < count + itemCount) {
                data.setAdapter(this);
                data.onBindViewHolder(holder, position - count, payloads);
                break;
            }
            count  += itemCount;
        }
    }

    protected int getRealPosition(int position) {
        if (headerView != null) {
            position--;
        }
        if (mRefreshHeader != null) {
            position--;
        }
        return position;
    }

    @Override
    protected void onLoadMore() {
        if (mIsLoading) { //  || !isBottom()
            return;
        }
        mIsLoading = true;

        Log.d(TAG, "onLoadMore");
        if (list.isEmpty() || currentPage < -1) {
            currentPage = -1;
        }

        View firstChild = mRecyclerView.getChildAt(0);
        if (firstChild == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        View lastChild = mRecyclerView.getChildAt(layoutManager.getChildCount() - 1);
        if (lastChild == null) {
            return;
        }
        int start = getRealPosition(mRecyclerView.getLayoutManager().getPosition(firstChild));
        int end = getRealPosition(mRecyclerView.getLayoutManager().getPosition(lastChild));
        Log.d(TAG, "onLoadMore start=" + start + " end=" + end);

        MultiScene multiScene = null;
        int offset = 0;
        for (int i = 0; i < list.size(); i++) {
            if (end < offset) {
                break;
            }
            MultiScene scene = list.get(i);
            MultiData<?> data = scene.getMultiData();
            int max = offset + data.getCount();

            if (max <= start) {
                offset = max;
                continue;
            }

            if (data.hasMore() && data.load(Math.max(0, start - offset), end - offset, this)) {
                multiScene = scene;
                Log.d(TAG, "onLoadMore scene=" + scene);
            }
            offset = max;
        }

        if (multiScene != null) { //  && multiData.load(this)
            if (footerViewHolder != null) {
                footerViewHolder.onShowLoading();
            }
            currentPage++;
        } else {
            mIsLoading = false;
            if (footerViewHolder != null && footerViewHolder.getView() != null) {
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
                    for (MultiScene scene : list) {
                        int itemCount = scene.getItemCount();
                        MultiData<?> data = scene.getMultiData();
                        if (position >= count && position < count + itemCount) {
                            int columnCount = data.getColumnCount(data.getViewType(position - count));
                            return gridManager.getSpanCount() / columnCount;
                        }
                        count  += itemCount;
                    }
                    return gridManager.getSpanCount();
                }
            });
        }
    }

    public int onGetViewType(List<MultiScene> list, int position) {
        int count = 0;
        for (MultiScene scene : list) {
            int itemCount = scene.getItemCount();
            MultiData<?> data = scene.getMultiData();
            if (position >= count && position < count + itemCount) {
                return data.getViewType(position - count);
            }
            count += itemCount;
        }
        return TYPE_CHILD;
    }

    public View onCreateView(Context context, ViewGroup container, int viewType) {
        for (MultiScene scene : list) {
            MultiData<?> data = scene.getMultiData();
            if (data.hasViewType(viewType)) {
                return data.onCreateView(context, container, viewType);
            }
        }
        return null;
    }

    public void notifyDataSetChange(MultiScene target) {
        int count = 0;
        for (MultiScene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == target) {
                notifyItemRangeChanged(count, itemCount);
            }
            count  += itemCount;
        }
    }

    public void notifyItemRangeInserted(MultiScene target) {
        int count = 0;
        for (MultiScene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == target) {
                notifyItemRangeInserted(count, itemCount);
            }
            count  += itemCount;
        }
    }

    public void notifyItemRangeInserted(MultiScene multiScene, int positionStart, int count) {
        int num = 0;
        for (MultiScene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == multiScene) {
                if (positionStart >= itemCount) {
                    return;
                }
                if (positionStart + count > itemCount) {
                    notifyItemRangeInserted(num + positionStart, itemCount - positionStart);
                } else {
                    notifyItemRangeInserted(num + positionStart, count);
                }
            }
            num  += itemCount;
        }
    }


}