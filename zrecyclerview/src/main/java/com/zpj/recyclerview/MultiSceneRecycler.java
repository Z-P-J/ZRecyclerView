package com.zpj.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.BaseMultiLayoutManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.footer.DefaultFooterViewHolder;

import java.util.List;

public class MultiSceneRecycler extends BaseRecycler<Scene, MultiSceneRecycler> {

    protected ItemTouchHelper mItemTouchHelper;

    public MultiSceneRecycler(@NonNull RecyclerView recyclerView) {
        super(recyclerView);
    }

    public MultiSceneRecycler(@NonNull RecyclerView recyclerView, @NonNull List<Scene> dataSet) {
        super(recyclerView, dataSet);
    }

    public static MultiSceneRecycler with(@NonNull RecyclerView recyclerView) {
        return new MultiSceneRecycler(recyclerView);
    }

    public static MultiSceneRecycler with(@NonNull RecyclerView recyclerView, @NonNull List<Scene> dataSet) {
        return new MultiSceneRecycler(recyclerView, dataSet);
    }

    public MultiSceneRecycler build() {
        MultiSceneAdapter adapter = new MultiSceneAdapter(recyclerView.getContext(), getItems(), this);
        easyAdapter = adapter;

        int maxSpan = 1;
        for (Scene scene : getItems()) {
            MultiData<?> data = scene.getMultiData();
            maxSpan = lcm(data.getMaxColumnCount(), maxSpan);
            data.setAdapter(adapter);
            if (data instanceof IDragAndSwipe && mItemTouchHelper == null) {
                mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = easyAdapter.getRealPosition(viewHolder);
                        int count = 0;
                        for (Scene scene : getItems()) {
                            int itemCount = scene.getItemCount();
                            MultiData<?> data = scene.getMultiData();
                            if (data instanceof IDragAndSwipe && position >= count && position < count + itemCount) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return makeMovementFlags(dragAndSwipeMultiData.getDragDirection(position),
                                        dragAndSwipeMultiData.getSwipeDirection(position));
                            }
                            count  += itemCount;
                        }
                        return 0;
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                        final int pos = easyAdapter.getRealPosition(viewHolder);
                        final int pos1 = easyAdapter.getRealPosition(viewHolder1);
                        int count = 0;
                        for (Scene scene : getItems()) {
                            int itemCount = scene.getItemCount();
                            MultiData<?> data = scene.getMultiData();
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + itemCount
                                    && pos1 >= count && pos1 < count + itemCount) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return dragAndSwipeMultiData.onMove(pos - count, pos1 - count);
                            }
                            count  += itemCount;
                        }


//                        int posFrom = easyAdapter.getRealPosition(viewHolder);
//                        int posTo = easyAdapter.getRealPosition(viewHolder1);
//
//                        final int realFrom = posFrom;
//                        final int realTo = posTo;
//                        MultiData from = null;
//                        MultiData to = null;
//                        int count = 0;
//                        for (MultiData<?> data : mMultiDataList) {
//                            if (data instanceof IDragAndSwipe) {
//
//                                if (from == null && posFrom >= count && posFrom < count + data.getCount()) {
//                                    posFrom -= count;
//                                    from = data;
//                                }
//                                if (to == null && posTo >= count && posTo < count + data.getCount()) {
//                                    posTo -= count;
//                                    to = data;
//                                }
//                            }
//                            if (from != null && to != null) {
//                                break;
//                            }
//                            count  += data.getCount();
//                        }
//                        if (from != null && to != null) {
//                            if (from == to) {
//                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) from;
//                                return dragAndSwipeMultiData.onMove(posFrom, posTo);
//                            } else {
//                                Object fromObj = from.getData().get(posFrom);
//                                Object toObj = to.getData().get(posTo);
//                                Log.d("MultiRecycler", "fromObj=" + fromObj + " toObj=" + toObj);
//                                if (fromObj != null && toObj != null
//                                        && fromObj.getClass().isAssignableFrom(toObj.getClass())
//                                        && toObj.getClass().isAssignableFrom(fromObj.getClass())) {
//                                    from.getData().set(posFrom, toObj);
//                                    to.getData().set(posTo, fromObj);
//                                    notifyItemMoved(realFrom, realTo);
//                                }
//                            }
//                        }
                        return false;
                    }

                    @Override
                    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                        super.onSelectedChanged(viewHolder, actionState);
                        easyAdapter.mIsDraggingOrSwiping = actionState != ItemTouchHelper.ACTION_STATE_IDLE;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
                        final int pos = easyAdapter.getRealPosition(viewHolder);
                        int count = 0;
                        for (Scene scene : getItems()) {
                            int itemCount = scene.getItemCount();
                            MultiData<?> data = scene.getMultiData();
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + itemCount) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                dragAndSwipeMultiData.onSwiped(pos - count, i);
                                break;
                            }
                            count  += itemCount;
                        }
                    }

                });
                mItemTouchHelper.attachToRecyclerView(recyclerView);
            }
        }

        easyAdapter.setRefreshHeader(mRefresher);
        easyAdapter.setAdapterInjector(adapterInjector);
        if (headerView != null) {
            easyAdapter.setHeaderView(headerView);
            easyAdapter.setOnBindHeaderListener(onBindHeaderListener);
        }
        if (footerViewBinder != null) {
            easyAdapter.setFooterViewHolder(footerViewBinder);
        } else {
            easyAdapter.setFooterViewHolder(new DefaultFooterViewHolder());
        }

        if (layoutManager instanceof BaseMultiLayoutManager) {
            ((BaseMultiLayoutManager) layoutManager).attachRecycler(this);
        } else {
            layoutManager = new GridLayoutManager(recyclerView.getContext(), maxSpan);
        }
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(easyAdapter);
        easyAdapter.showContent();
        return this;
    }

    @Override
    public MultiSceneAdapter getAdapter() {
        return (MultiSceneAdapter) super.getAdapter();
    }

    private int gcd(int x, int y) {
        return y == 0 ? x : gcd(y, x % y);
    }

    private int lcm(int x, int y) {
        return (x * y) / gcd(x, y);
    }


}
