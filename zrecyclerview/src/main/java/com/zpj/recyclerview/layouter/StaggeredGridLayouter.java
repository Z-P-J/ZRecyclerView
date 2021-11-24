package com.zpj.recyclerview.layouter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.manager.MultiLayoutParams;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class StaggeredGridLayouter extends AbsLayouter {

    private static final String TAG = "StaggeredGridLayouter";

    private int[] mTops;
    private int[] mPositions;

    private int mSpanCount = 1;


    private class ItemInfo {

        int position;
        int top;
        int bottom;

    }

    private Column[] columns = null;
    private final SparseIntArray array = new SparseIntArray();

    private static class Column {

        final List<Integer> positions = new LinkedList<>();
        private final int index;

        public Column(int index) {
            this.index = index;
        }

        private int bottom = 0;

        public void addFirst(int pos) {
            positions.add(0, pos);
        }

        public void addLast(int pos) {
            positions.add(pos);
        }

        public int getFirst() {
            return positions.get(0);
        }

        public int getLast() {
            return positions.get(positions.size() - 1);
        }


    }

    public StaggeredGridLayouter(int mSpanCount) {
        this.mSpanCount = Math.max(mSpanCount, 1);
    }

    public void saveState() {
        int childWidth = getWidth() / mSpanCount;
        boolean[] places = new boolean[mSpanCount];

        if (this.mTops == null || this.mPositions == null) {
            this.mTops = new int[mSpanCount];
            this.mPositions = new int[mSpanCount];
        }

        int count = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) {
                continue;
            }

            MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
            if (params.getMultiData().getLayouter() != this) {
                continue;
            }

            int col = getDecoratedLeft(view) / childWidth;
            if (!places[col]) {
                places[col] = true;
                mTops[col] = getDecoratedTop(view);
                int index = columns[col].positions.indexOf(params.getViewLayoutPosition());
//                mPositions[col] = params.getViewLayoutPosition();
                if (index == 0) {
                    mPositions[col] = -1;
                } else if (index > 0) {
                    mPositions[col] = columns[col].positions.get(index - 1);
                } else {
                    mPositions[col] = columns[col].getLast();
                }
                count++;
            }

            if (count >= mSpanCount) {
                break;
            }
        }

        for (int i = 0; i < mSpanCount; i++) {
            Log.d(TAG, "fillVertical bottom=" + columns[i].bottom + " isPlace=" + places[i]);
            if (!places[i]) {
                Column column = columns[i];
                mPositions[i] = column.getLast();
            }
        }

        Log.d(TAG, "layoutChildren saveState mTops=" + Arrays.toString(mTops));
        Log.d(TAG, "layoutChildren saveState mPositions=" + Arrays.toString(mPositions));

    }

    @Override
    public boolean canScrollHorizontally() {
        return false;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void layoutChildren(MultiData<?> multiData, RecyclerView.Recycler recycler, int currentPosition) {
        if (getLayoutManager() == null || multiData.getCount() == 0 || mTop > getHeight()) {
            mBottom = mTop;
            return;
        }

        Log.d(TAG, "layoutChildren mTops=" + Arrays.toString(mTops) + " mPositions=" + Arrays.toString(mPositions));

        if (this.mTops == null || this.mPositions == null) {
            this.mTops = new int[mSpanCount];
            Arrays.fill(this.mTops, mTop);
            fillVertical(null, 1, recycler, multiData);
        } else {
            if (mTop > 0) {
                Arrays.fill(this.mTops, mTop);
            }
            int maxBottom = mBottom;
            for (int i = 0; i < mSpanCount; i++) {

                Column column = columns[i];
                int position = mPositions[i];

                int bottom = fillColumnBottom(recycler, multiData, column, getHeight(), position, mTops[i]);
                maxBottom = Math.max(bottom, maxBottom);
            }
            mBottom = maxBottom;
        }
    }

    @Override
    public int fillVertical(View anchorView, int dy, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        initColumns(multiData, recycler);

        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {


                int maxBottom = mTop;
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
//                    int position = column.positions.get(0);

                    int bottom = fillColumnBottom(recycler, multiData, column, dy, -1, mTop);
                    maxBottom = Math.max(bottom, maxBottom);
                }

                mBottom = maxBottom;
                return Math.min(dy, maxBottom - mTop);
            } else {


                int childWidth = getWidth() / mSpanCount;

                boolean[] places = new boolean[mSpanCount];
                int[] bottoms = new int[mSpanCount];
                int[] positions = new int[mSpanCount];

                int index = indexOfChild(anchorView);
                for (int i = index; i >= 0; i--) {
                    View view = getChildAt(i);
                    if (view == null) {
                        continue;
                    }

                    MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
                    if (params.getMultiData().getLayouter() != this) {
                        break;
                    }

                    int col = getDecoratedLeft(view) / childWidth;
                    if (!places[col]) {
                        places[col] = true;
                        bottoms[col] = getDecoratedBottom(view);
                        positions[col] = params.getViewLayoutPosition();
                    }

                    boolean flag = false;
                    for (int j = 0; j < mSpanCount; j++) {
                        if (!places[j]) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        break;
                    }

                }

                int maxBottom = mBottom;
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
                    int position = positions[i];

                    int bottom = fillColumnBottom(recycler, multiData, column, dy, position, bottoms[i]);
                    maxBottom = Math.max(bottom, maxBottom);
                }
                mBottom = maxBottom;
                return Math.min(dy, maxBottom - getHeight());
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {

                int minTop = mBottom;
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
//                    int position = column.getLast();

                    int top = fillColumnTop(recycler, multiData, column, dy, -1, mBottom + column.bottom);
                    minTop = Math.min(top, minTop);
                }

                mTop = minTop;
                return Math.min(-dy, mBottom - minTop);
            } else {

                int childWidth = getWidth() / mSpanCount;

                boolean[] places = new boolean[mSpanCount];
                int[] tops = new int[mSpanCount];
                int[] positions = new int[mSpanCount];
                Arrays.fill(positions, -1);

                int index = indexOfChild(anchorView);
                int count = 0;
                for (int i = index; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (view == null) {
                        continue;
                    }

                    MultiLayoutParams params = (MultiLayoutParams) view.getLayoutParams();
                    if (params.getMultiData().getLayouter() != this) {
                        break;
                    }

                    int col = getDecoratedLeft(view) / childWidth;
                    if (!places[col]) {
                        places[col] = true;
                        tops[col] = getDecoratedTop(view);
                        positions[col] = params.getViewLayoutPosition();
                        count++;
                    }

                    if (count >= mSpanCount) {
                        break;
                    }
                }


                int col = getDecoratedLeft(anchorView) / childWidth;
                Column anchorColumn = columns[col];
                int anchorBottom = getDecoratedBottom(anchorView) - anchorColumn.bottom;

                Log.d(TAG, "fillVertical bottom col=" + col);

                for (int i = 0; i < mSpanCount; i++) {
                    Log.d(TAG, "fillVertical bottom=" + columns[i].bottom + " isPlace=" + places[i]);
                    if (!places[i]) {
                        Column column = columns[i];
                        tops[i] = anchorBottom + column.bottom;
//                        positions[i] = column.getLast();
                    }
                }

                int minTop = mTop;
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
                    int position = positions[i];

                    Log.d(TAG, "fillVertical fillColumnTop tops[i]=" + tops[i] + " pos=" + position + " i=" + i);
                    int top = fillColumnTop(recycler, multiData, column, dy, position, tops[i]);
                    minTop = Math.min(top, minTop);
                }

                mTop = minTop;
                Log.d(TAG, "fillVertical anchorBottom=" + anchorBottom + " mTop=" + mTop);
                return Math.min(-dy, -minTop);
            }
        }

    }

    @Override
    protected int fillVerticalTop(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorTop) {
        return 0;
    }

    @Override
    protected int fillVerticalBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, int currentPosition, int availableSpace, int anchorBottom) {
        return 0;
    }

    private int fillColumnTop(RecyclerView.Recycler recycler, MultiData<?> multiData, Column column, int dy, int currentPosition, int anchorTop) {

        if (anchorTop - dy < 0) {
            return anchorTop;
        }

        int next;
        if (currentPosition < 0) {
            next = column.positions.size() - 1;
        } else {
            int index = column.positions.indexOf(currentPosition);
            if (index < 0) {
                throw new RuntimeException("fillColumnTop error");
            }
            next = index - 1;
        }

        int childWidth = getWidth() / mSpanCount;
        int childWidthUsed = getWidth() - childWidth;
        while (next >= 0) {
            int nextPosition = column.positions.get(next--);
            View view = getViewForPosition(nextPosition, recycler, multiData);
            addView(view, 0);
            measureChild(view, childWidthUsed, 0);

            int height = getDecoratedMeasuredHeight(view);

            int left = column.index * childWidth;
            int bottom = anchorTop;
            int top = bottom - height;
            int right = left + childWidth;


            anchorTop = top;

            layoutDecorated(view, left, top, right, bottom);

            if (anchorTop - dy < 0) {
                break;
            }
        }

        return anchorTop;

    }

    private int fillColumnBottom(RecyclerView.Recycler recycler, MultiData<?> multiData, Column column, int dy, int currentPosition, int anchorBottom) {

        if (anchorBottom - dy > getHeight()) {
            return anchorBottom;
        }

        int next;
        if (currentPosition < 0) {
            next = 0;
        } else {
            int index = column.positions.indexOf(currentPosition);
            if (index < 0) {
                throw new RuntimeException("fillColumnBottom error");
            }
            next = index + 1;
        }


        int childWidth = getWidth() / mSpanCount;
        int childWidthUsed = getWidth() - childWidth;
        while (next < column.positions.size()) {
            int nextPosition = column.positions.get(next++);
            View view = getViewForPosition(nextPosition, recycler, multiData);
            addView(view);
            measureChild(view, childWidthUsed, 0);

            int height = getDecoratedMeasuredHeight(view);

            int left = column.index * childWidth;
            int top = anchorBottom;
            int right = left + childWidth;
            int bottom = top + height;

            anchorBottom = bottom;

            layoutDecorated(view, left, top, right, bottom);

            if (anchorBottom - dy > getHeight()) {
                break;
            }
        }

        return anchorBottom;

    }

    @Override
    public int fillHorizontal(View anchorView, int dx, RecyclerView.Recycler recycler, MultiData<?> multiData) {
        return 0;
    }

    private void initColumns(MultiData<?> multiData, RecyclerView.Recycler recycler) {
        if (columns == null || array.size() != multiData.getCount()) {

            columns = new Column[mSpanCount];
            array.clear();

            for (int i = 0; i < mSpanCount; i++) {
                columns[i] = new Column(i);
            }

            int childWidth = getWidth() / mSpanCount;
            int childWidthUsed = getWidth() - childWidth;
            int minIndex = 0;
            int minBottom = 0;
            int maxBottom = 0;


            for (int i = mPositionOffset; i < mPositionOffset + multiData.getCount(); i++) {
                View view = getViewForPosition(i, recycler, multiData);
                measureChild(view, childWidthUsed, 0);

                int height = getDecoratedMeasuredHeight(view);

                Column column = columns[minIndex];
                column.bottom += height;
                Log.d(TAG, "initColumns i=" + i + " minIndex=" + minIndex + " height=" + height);
                column.addLast(i);
                array.put(i, minIndex);
                minBottom = column.bottom;
                maxBottom = Math.max(minBottom, maxBottom);

                for (int j = 0; j < mSpanCount; j++) {
                    int bottom = columns[j].bottom;
                    Log.d(TAG, "initColumns j=" + j + " bottom=" + bottom + " minBottom=" + minBottom + " maxBottom=" + maxBottom);
                    if (bottom == minBottom) {
                        if (j < minIndex) {
                            minIndex = j;
                        }
                        continue;
                    }
                    if (bottom < minBottom) {
                        minBottom = bottom;
                        minIndex = j;
                    }
//                    if (bottom > maxBottom) {
//                        maxBottom = bottom;
//                    }
                }
            }

            for (Column column : columns) {
                Log.d(TAG, "initColumns column.bottom pre=" + column.bottom + " maxBottom=" + maxBottom);
                column.bottom -= maxBottom;

                Log.d(TAG, "initColumns column.bottom=" + column.bottom);
            }

        }
    }

}
