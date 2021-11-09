package com.zpj.recyclerview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.zpj.recyclerview.footer.DefaultFooterViewHolder;
import com.zpj.recyclerview.manager.MultiLayoutManager;

import java.util.List;

public class MultiRecycler extends BaseRecycler<MultiData<?>, MultiRecycler> {

    protected ItemTouchHelper mItemTouchHelper;

    public MultiRecycler(@NonNull RecyclerView recyclerView) {
        super(recyclerView);
    }

    public MultiRecycler(@NonNull RecyclerView recyclerView, @NonNull List<MultiData<?>> dataSet) {
        super(recyclerView, dataSet);
    }

    public static MultiRecycler with(@NonNull RecyclerView recyclerView) {
        return new MultiRecycler(recyclerView);
    }

    public static MultiRecycler with(@NonNull RecyclerView recyclerView, @NonNull List<MultiData<?>> dataSet) {
        return new MultiRecycler(recyclerView, dataSet);
    }

    public MultiRecycler build() {
        MultiAdapter adapter = new MultiAdapter(recyclerView.getContext(), getDataSet(), this, mRefresher);
        easyAdapter = adapter;

        int maxSpan = 1;
        for (MultiData<?> data : getDataSet()) {
            maxSpan = lcm(data.getMaxColumnCount(), maxSpan);
            data.setAdapter(adapter);
            if (data instanceof IDragAndSwipe && mItemTouchHelper == null) {
                mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                        int position = easyAdapter.getRealPosition(viewHolder);
                        int count = 0;
                        for (MultiData<?> data : getDataSet()) {
                            if (data instanceof IDragAndSwipe && position >= count && position < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return makeMovementFlags(dragAndSwipeMultiData.getDragDirection(position),
                                        dragAndSwipeMultiData.getSwipeDirection(position));
                            }
                            count  += data.getCount();
                        }
                        return 0;
                    }

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                        final int pos = easyAdapter.getRealPosition(viewHolder);
                        final int pos1 = easyAdapter.getRealPosition(viewHolder1);
                        int count = 0;
                        for (MultiData<?> data : getDataSet()) {
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + data.getCount()
                                    && pos1 >= count && pos1 < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                return dragAndSwipeMultiData.onMove(pos - count, pos1 - count);
                            }
                            count  += data.getCount();
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
                        for (MultiData<?> data : getDataSet()) {
                            if (data instanceof IDragAndSwipe && pos >= count && pos < count + data.getCount()) {
                                IDragAndSwipe dragAndSwipeMultiData = (IDragAndSwipe) data;
                                dragAndSwipeMultiData.onSwiped(pos - count, i);
                                break;
                            }
                            count  += data.getCount();
                        }
                    }

                });
                mItemTouchHelper.attachToRecyclerView(recyclerView);
            }
        }
        if (layoutManager instanceof MultiLayoutManager) {
            ((MultiLayoutManager) layoutManager).attachRecycler(this);
        } else {
            layoutManager = new GridLayoutManager(recyclerView.getContext(), maxSpan);
        }
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
        easyAdapter.setLoadMoreEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(easyAdapter);
        easyAdapter.showContent();
        return this;
    }

    private int gcd(int x, int y) {
        return y == 0 ? x : gcd(y, x % y);
    }

    private int lcm(int x, int y) {
        return (x * y) / gcd(x, y);
    }


}
