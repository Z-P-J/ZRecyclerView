package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.core.MultiSceneLayoutManager;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.footer.IFooterViewHolder;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.scene.RefresherScene;
import com.zpj.recyclerview.scene.VerticalScene;
import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;
import static com.zpj.statemanager.State.STATE_LOGIN;

public class MultiSceneAdapter extends EasyStateAdapter<Scene> {

    private static final String TAG = "MultiSceneAdapter";

    private FooterScene mFooterScene;
    private RefresherScene mRefresherScene;

    MultiSceneAdapter(final Context context, List<Scene> list, final EasyStateConfig<?> config) {
        super(context, list, 0, null, null,
                null, null, null,
                null, null, null, config);
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
        }
        return new EasyViewHolder(onCreateView(viewGroup.getContext(), viewGroup, viewType));
    }

    @Override
    public int getItemCount() {
        if (state != State.STATE_CONTENT) {
            return 1;
        }
        int count = 0;
        for (Scene scene : list) {
            count += scene.getItemCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (state != State.STATE_CONTENT) {
            return state.hashCode();
        } else {
            return onGetViewType(list, position);
        }
    }

    @Override
    protected int getRealPosition(RecyclerView.ViewHolder holder) {
        return holder.getLayoutPosition();
    }

    @Override
    public void onBindViewHolder(@NonNull EasyViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (state != State.STATE_CONTENT) {
            return;
        }
        holder.setRealPosition(position);
        holder.setViewType(getItemViewType(position));
        int count = 0;
        for (Scene scene : list) {
            int itemCount = scene.getItemCount();
            MultiData<?> data = scene.getMultiData();
            if (position >= count && position < count + itemCount) {
                Log.e(TAG, "onBindViewHolder scene=" + scene);
                data.setAdapter(this);
                data.onBindViewHolder(holder, position - count, payloads);
                break;
            }
            count  += itemCount;
        }
    }

    protected int getRealPosition(int position) {
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

        Scene loadingScene = null;
        int offset = 0;
        for (int i = 0; i < list.size(); i++) {
            if (end < offset) {
                break;
            }
            Scene scene = list.get(i);
            MultiData<?> data = scene.getMultiData();
            int max = offset + data.getCount();

            if (max <= start) {
                offset = max;
                continue;
            }

            if (data.hasMore() && data.load(Math.max(0, start - offset), end - offset, this)) {
                loadingScene = scene;
                Log.d(TAG, "onLoadMore scene=" + scene);
            }
            offset = max;
        }

        if (loadingScene != null) { //  && multiData.load(this)
            if (mFooterScene != null) {
                mFooterScene.onShowLoading();
            }
            currentPage++;
        } else {
            mIsLoading = false;
            if (mFooterScene != null && mFooterScene.getView() != null) {
                mFooterScene.onShowHasNoMore();
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
                    int count = 0;
                    for (Scene scene : list) {
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

    @Override
    public void setRefreshHeader(IRefresher refresher) {
        mRefresherScene = new RefresherScene(refresher) {

            @Override
            public void stopInterceptRequestLayout() {
                // TODO 更优雅的实现
                if (mHelper == null) {
                    RecyclerViewHelper.stopInterceptRequestLayout(mRecyclerView.getLayoutManager());
                } else {
                    mHelper.stopInterceptRequestLayout();
                }
            }
        };
        list.add(0, mRefresherScene);
    }

    @Override
    protected boolean isRefreshPosition(int position) {
        return false;
    }

    @Override
    public void stopRefresh() {
        if (mRefresherScene != null) {
            mRefresherScene.getRefresher().stopRefresh();
        }
    }

    @Override
    protected void initRefresherTouchListener(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent event) {

                if (recyclerView.getLayoutManager() instanceof MultiSceneLayoutManager) {
                    return false;
                }

                if (mRefresherScene == null) {
                    return false;
                }

                if (mIsDraggingOrSwiping) {
                    return false;
                }

                try {
                    int action = event.getAction();
                    Log.e(TAG, "onInterceptTouchEvent event=" + event.getAction() + " index=" + list.indexOf(mRefresherScene));
                    if (MotionEvent.ACTION_DOWN == action) {
                        mRefresherScene.onTouchDown(event);
                    } else if (MotionEvent.ACTION_MOVE == action) {
                        mRefresherScene.onTouchMove(event);
                    } else if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
                        mRefresherScene.onTouchUp(event, 0, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
    }

    @Override
    public void setHeaderView(@NonNull final View header) {
        Scene scene = new VerticalScene(new MultiData<Object>() {

            @Override
            public View onCreateView(Context context, ViewGroup container, int viewType) {
                ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                header.setLayoutParams(params);
                return header;
            }

            @Override
            public int getViewType(int position) {
                return header.hashCode();
            }

            @Override
            public boolean hasViewType(int viewType) {
                return viewType == header.hashCode();
            }

            @Override
            public int getLayoutId(int viewType) {
                return 0;
            }

            @Override
            protected boolean loadData() {
                return false;
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public void onBindViewHolder(EasyViewHolder holder, List<Object> list, int position, List<Object> payloads) {
                if (onBindHeaderListener != null) {
                    onBindHeaderListener.onBindHeader(holder);
                }
            }
        });
        if (mRefresherScene == null) {
            list.add(0, scene);
        } else {
            list.add(1, scene);
        }
    }

    @Override
    protected boolean isHeaderPosition(int position) {
        return false;
    }

    @Override
    public void setFooterViewHolder(IFooterViewHolder footerViewHolder) {
        mFooterScene = new FooterScene(footerViewHolder);
        list.add(mFooterScene);
    }

    @Override
    protected boolean isFooterPosition(int position) {
        return false;
    }

    @Override
    protected void showFooterMsg(String msg) {
        if (mFooterScene != null) {
            mFooterScene.onShowError(msg);
        }
    }

    public int onGetViewType(List<Scene> list, int position) {
        int count = 0;
        for (Scene scene : list) {
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
        for (Scene scene : list) {
            MultiData<?> data = scene.getMultiData();
            if (data.hasViewType(viewType)) {
                return data.onCreateView(context, container, viewType);
            }
        }
        return null;
    }

    public void notifyDataSetChange(Scene target) {
        int count = 0;
        for (Scene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == target) {
                notifyItemRangeChanged(count, itemCount);
            }
            count  += itemCount;
        }
    }

    public void notifyItemRangeInserted(Scene target) {
        int count = 0;
        for (Scene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == target) {
                notifyItemRangeInserted(count, itemCount);
            }
            count  += itemCount;
        }
    }

    public void notifyItemRangeInserted(Scene targetScene, int positionStart, int count) {
        int num = 0;
        for (Scene scene : list) {
            int itemCount = scene.getItemCount();
            if (scene == targetScene) {
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

    private class FooterMultiData extends MultiData<Object> {

        private final IFooterViewHolder footerViewHolder;

        private FooterMultiData(IFooterViewHolder footerViewHolder) {
            this.footerViewHolder = footerViewHolder;
        }

        @Override
        public View onCreateView(Context context, ViewGroup container, int viewType) {
            return footerViewHolder.onCreateViewHolder(container).getItemView();
        }

        @Override
        public int getViewType(int position) {
            return hashCode();
        }

        @Override
        public boolean hasViewType(int viewType) {
            return viewType == hashCode();
        }

        @Override
        public int getLayoutId(int viewType) {
            return 0;
        }

        @Override
        protected boolean loadData() {
            return false;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public void onBindViewHolder(EasyViewHolder holder, List<Object> list, int position, List<Object> payloads) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getItemCount() == 1 ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
            footerViewHolder.getView().setLayoutParams(params);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tryToLoadMore();
                }
            });
            footerViewHolder.onBindFooter(holder);
            tryToLoadMore();
        }
    }

    private class FooterScene extends VerticalScene {

        private final IFooterViewHolder footerViewHolder;

        public FooterScene(IFooterViewHolder footerViewHolder) {
            super(new FooterMultiData(footerViewHolder));
            this.footerViewHolder = footerViewHolder;
        }

        View getView() {
            return footerViewHolder.getView();
        }

        void onShowLoading() {
            footerViewHolder.onShowLoading();
        }

        void onShowHasNoMore() {
            footerViewHolder.onShowHasNoMore();
        }

        void onShowError(String msg) {
            footerViewHolder.onShowError(msg);
        }

    }

}