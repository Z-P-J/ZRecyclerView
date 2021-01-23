package com.zpj.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.sunfusheng.GroupRecyclerViewAdapter;
import com.sunfusheng.GroupViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sunfusheng on 2018/3/7.
 */
@SuppressWarnings("unchecked")
public class StickyHeaderItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "StickyHeaderItem1";

    private MultiAdapter groupAdapter;
    private EasyViewHolder viewHolder;
    private View currStickyView;
    private View nextStickyView;
    private int currGroupPosition;
    private final Rect stickyRect = new Rect();
    private GestureDetector gestureDetector;

    private final Map<Integer, WH> whMap = new HashMap<>();

    private static class WH {
        private int width;
        private int height;
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (layoutManager == null || adapter == null) {
            return;
        }

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        groupAdapter = (MultiAdapter) adapter;


        int currItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        if (!isStickHeader(currItemPosition)) {
            return;
        }
        if (groupAdapter.headerView != null) {
            currItemPosition--;
        }
        if (currItemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        int count = 0;
        HeaderMultiData<?> currentMultiData = null;
        HeaderMultiData<?> nextMultiData = null;

        int currStickyPosition = RecyclerView.NO_POSITION;
        int nextStickyPosition = RecyclerView.NO_POSITION;

        int nextGroupPosition = currGroupPosition;

        int size = groupAdapter.list.size();
        for (int i = 0; i < size; i++) {
            MultiData<?> data = groupAdapter.list.get(i);
            if (data.hasMore) {
                break;
            }
            int p = currItemPosition - count;
            if (p >= 0 && p < data.getCount()) {
                if (data instanceof HeaderMultiData) {
                    currGroupPosition = i;
                    nextGroupPosition = currGroupPosition + 1;
                    currentMultiData = (HeaderMultiData<?>) data;
                    currStickyPosition = count;

//                    for (int j = i + 1; j < size; j++) {
//                        MultiData<?> nextData = groupAdapter.list.get(j);
//                        if (nextData instanceof HeaderMultiData) {
//                            nextMultiData = (HeaderMultiData<?>) nextData;
//                            nextStickyPosition = count;
//                            break;
//                        }
//                        count += data.getCount();
//                    }
//
//                    if (nextMultiData == null) {
//                        nextMultiData = currentMultiData;
//                        nextStickyPosition = currStickyPosition;
//                    }
                } else {
                    currentMultiData = null;
                    break;
                }
//                break;
            } else if (currentMultiData != null && nextMultiData == null) {
                if (data instanceof HeaderMultiData) {
                    nextGroupPosition = i;
                    nextMultiData = (HeaderMultiData<?>) data;
                    nextStickyPosition = count;
                    break;
                }
            }
            count += data.getCount();
        }

        if (currentMultiData == null) {
            return;
        } else if (nextMultiData == null) {
            nextMultiData = currentMultiData;
            nextStickyPosition = currStickyPosition;
        }


        Log.d(TAG, "currStickyPosition=" + currStickyPosition + " nextStickyPosition=" + nextStickyPosition);
        Log.d(TAG, "currentMultiData=" + currentMultiData + " nextMultiData=" + nextMultiData);
        Log.d(TAG, "curr==next=" + (currentMultiData == nextMultiData));

        RecyclerView.ViewHolder currViewHolder = parent.findViewHolderForAdapterPosition(currStickyPosition);
//        if (currViewHolder != null) {
//            currStickyView = currViewHolder.itemView;
//            currStickyView.setTag(currGroupPosition);
//        }
//
//        if (currStickyView == null) {
//            return;
//        }

        if (currViewHolder == null && currStickyView == null) {
            return;
        }

        if (nextMultiData != null) {
            RecyclerView.ViewHolder nextViewHolder = parent.findViewHolderForLayoutPosition(nextStickyPosition);
            if (nextViewHolder != null) {
                nextStickyView = nextViewHolder.itemView;
                nextStickyView.setTag(nextGroupPosition);
            }
        }


        View view = currViewHolder == null ? currStickyView : currViewHolder.itemView;
        WH wh = whMap.get(currGroupPosition);
        if (wh == null) {
            wh = new WH();
            wh.width = view.getWidth();
            wh.height = view.getHeight();
            whMap.put(currGroupPosition, wh);
        }
        int stickyViewWidth = wh.width;
        int stickyViewHeight = wh.height;
//        int stickyViewWidth = view.getWidth();
//        int stickyViewHeight = view.getHeight();
        int nextStickyViewTop = -1;
        if (nextStickyView != null) {
            nextStickyViewTop = nextStickyView.getTop();
        }

//        Log.d(TAG, "currStickyView.getTag=" + currStickyView.getTag() + " currGroupPosition=" + currGroupPosition + " nextGroupPosition=" + nextGroupPosition);
        if (currStickyView == null || (int) currStickyView.getTag() != currGroupPosition) {
            int viewType = currentMultiData.getHeaderViewType();
            if (viewHolder == null || (viewHolder.getViewType() != viewType)) {

                viewHolder = new EasyViewHolder(currentMultiData.onCreateView(parent.getContext(), parent, viewType));
                viewHolder.setViewType(viewType);
//                viewHolder = groupAdapter.onCreateViewHolder(parent, currentMultiData.getViewType(0));
//                viewHolder = new EasyViewHolder(groupAdapter.inflater.inflate(groupAdapter.getHeaderLayoutId(GroupRecyclerViewAdapter.TYPE_HEADER), parent, false));
            }
//            groupAdapter.onBindViewHolder(viewHolder, currStickyPosition);
            View itemView = viewHolder.getItemView();

            itemView.setTag(currGroupPosition);
            currStickyView = itemView;
            currStickyView.setTag(currGroupPosition);
        }

        currentMultiData.onBindHeader(viewHolder, null);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(stickyViewWidth, stickyViewHeight);
        currStickyView.setLayoutParams(layoutParams);
        currStickyView.measure(View.MeasureSpec.makeMeasureSpec(stickyViewWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(stickyViewHeight, View.MeasureSpec.EXACTLY));
        currStickyView.layout(0, -stickyViewHeight, stickyViewWidth, 0);

//        int viewType = currentMultiData.getHeaderViewType();
//        if (viewHolder == null || (viewHolder.getViewType() != viewType)) {
//
//            viewHolder = new EasyViewHolder(currentMultiData.onCreateView(parent.getContext(), parent, viewType));
//            viewHolder.setViewType(viewType);
////                viewHolder = groupAdapter.onCreateViewHolder(parent, currentMultiData.getViewType(0));
////                viewHolder = new EasyViewHolder(groupAdapter.inflater.inflate(groupAdapter.getHeaderLayoutId(GroupRecyclerViewAdapter.TYPE_HEADER), parent, false));
//        }
////            groupAdapter.onBindViewHolder(viewHolder, currStickyPosition);
//
//        View itemView = viewHolder.getItemView();
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, stickyViewHeight);
//        itemView.setLayoutParams(layoutParams);
////        itemView.measure(View.MeasureSpec.makeMeasureSpec(stickyViewWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(stickyViewHeight, View.MeasureSpec.EXACTLY));
//        itemView.layout(0, -stickyViewHeight, stickyViewWidth, 0);
//        itemView.setTag(currGroupPosition);
//        currStickyView = itemView;
//        currStickyView.setTag(currGroupPosition);

        int translateY = 0;
        Log.e(TAG, "nextStickyViewTop=" + nextStickyViewTop + " stickyViewHeight=" + stickyViewHeight + " nextGroupPosition=" + nextGroupPosition + " size=" + size);
        if (nextStickyViewTop > 0 && nextStickyViewTop < stickyViewHeight && nextGroupPosition < size) {
            translateY = nextStickyViewTop - stickyViewHeight;
        }
        Log.e(TAG, "translateY=" + translateY);
        canvas.translate(0, translateY);
        currStickyView.draw(canvas);

//        currStickyView.setBackgroundResource(R.drawable.background_top_radius);
//        if (beforeStickyView != null && beforeStickyView != currStickyView) {
//            beforeStickyView.setBackgroundColor(Color.WHITE);
//        }
//        Log.d("StickyHeaderDecoration", "beforeStickyView=" + beforeStickyView + "  currStickyView=" + currStickyView + "  nextStickyView=" + nextStickyView);

        stickyRect.left = 0;
        stickyRect.top = 0;
        stickyRect.right = stickyViewWidth;
        stickyRect.bottom = stickyViewHeight + translateY;

        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(parent.getContext(), simpleOnGestureListener);
            parent.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    if (currStickyView != null && currStickyView.isPressed() && e.getAction() == MotionEvent.ACTION_UP) {
                        currStickyView.setPressed(false);
                    }
                    return gestureDetector.onTouchEvent(e);
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    super.onTouchEvent(rv, e);
                    if (currStickyView != null && currStickyView.isPressed() && e.getAction() == MotionEvent.ACTION_UP) {
                        currStickyView.setPressed(false);
                    }
                }
            });
        }
    }

    protected boolean isStickHeader(int position) {
        return true;
    }

    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            boolean isValidTouch = isValidTouch(e);
            if (isValidTouch) {
                currStickyView.setPressed(true);
            }
            return isValidTouch;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (isValidTouch(e) && viewHolder != null) {
                currStickyView.setPressed(false);
                viewHolder.onClick(viewHolder.getItemView());
                return true;
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
//            GroupRecyclerViewAdapter adapter = groupAdapter;
            if (isValidTouch(e) && viewHolder != null) {
                currStickyView.setPressed(false);
                viewHolder.onLongClick(viewHolder.getItemView());
//                adapter.onItemLongClickListener.onItemLongClick(adapter, viewHolder, adapter.getItem(currGroupPosition, 0), currGroupPosition, 0);
            }
        }

        private boolean isValidTouch(MotionEvent e) {
            Rect rect = stickyRect;
            float x = e.getX();
            float y = e.getY();
            return x > rect.left && x < rect.right && y > rect.top && y < rect.bottom;
        }
    };
}
