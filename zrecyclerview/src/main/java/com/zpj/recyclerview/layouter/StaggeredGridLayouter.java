package com.zpj.recyclerview.layouter;

import android.support.annotation.IntRange;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.zpj.recyclerview.core.AbsLayouter;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.core.SceneLayoutParams;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StaggeredGridLayouter extends AbsLayouter {

    private static final String TAG = "StaggeredGridLayouter";

    private int[] mTops;
    private int[] mPositions;

    private final int mSpanCount;

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

    public StaggeredGridLayouter(@IntRange(from = 1) int spanCount) {
        mSpanCount = spanCount;
    }

    public void saveState(Scene scene) {
        int childWidth = scene.getWidth() / mSpanCount;
        boolean[] places = new boolean[mSpanCount];

        if (this.mTops == null || this.mPositions == null) {
            this.mTops = new int[mSpanCount];
            this.mPositions = new int[mSpanCount];
        }

        int count = 0;
        for (int i = 0; i < scene.getChildCount(); i++) {
            View view = scene.getChildAt(i);
            if (view == null) {
                continue;
            }

            SceneLayoutParams params = (SceneLayoutParams) view.getLayoutParams();
            if (params.getScene().getLayouter() != this) {
                continue;
            }

            int col = scene.getDecoratedLeft(view) / childWidth;
            if (!places[col]) {
                places[col] = true;
                mTops[col] = scene.getDecoratedTop(view);
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
    public void layoutChildren(Scene scene) {
        if (scene.getItemCount() == 0 || scene.getTop() > scene.getHeight()) {
            scene.setBottom(scene.getTop());
            return;
        }

        Log.d(TAG, "layoutChildren mTops=" + Arrays.toString(mTops) + " mPositions=" + Arrays.toString(mPositions));

        if (this.mTops == null || this.mPositions == null) {
            this.mTops = new int[mSpanCount];
            Arrays.fill(this.mTops, scene.getTop());
            fillVertical(scene, null, 1);
        } else {
            if (scene.getTop() > 0) {
                Arrays.fill(this.mTops, scene.getTop());
            }
            int maxBottom = scene.getBottom();
            for (int i = 0; i < mSpanCount; i++) {

                Column column = columns[i];
                int position = mPositions[i];

                int bottom = fillColumnBottom(scene, column, scene.getHeight(), position, mTops[i]);
                maxBottom = Math.max(bottom, maxBottom);
            }
            scene.setBottom(maxBottom);
        }
    }

    @Override
    public int fillVertical(Scene scene, View anchorView, int dy) {
        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        initColumns(scene);

        Log.e(TAG, "fillVertical anchorView is null=" + (anchorView == null) + " dy=" + dy);
        if (dy > 0) {
            // 从下往上滑动
            if (anchorView == null) {


                int maxBottom = scene.getTop();
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
//                    int position = column.positions.get(0);

                    int bottom = fillColumnBottom(scene, column, dy, -1, scene.getTop());
                    maxBottom = Math.max(bottom, maxBottom);
                }

                scene.setBottom(maxBottom);
                return Math.min(dy, maxBottom - scene.getTop());
            } else {


                int childWidth = scene.getWidth() / mSpanCount;

                boolean[] places = new boolean[mSpanCount];
                int[] bottoms = new int[mSpanCount];
                int[] positions = new int[mSpanCount];

                int index = scene.indexOfChild(anchorView);
                for (int i = index; i >= 0; i--) {
                    View view = scene.getChildAt(i);
                    if (view == null) {
                        continue;
                    }

                    SceneLayoutParams params = (SceneLayoutParams) view.getLayoutParams();
                    if (params.getScene().getLayouter() != this) {
                        break;
                    }

                    int col = scene.getDecoratedLeft(view) / childWidth;
                    if (!places[col]) {
                        places[col] = true;
                        bottoms[col] = scene.getDecoratedBottom(view);
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

                int maxBottom = scene.getBottom();
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
                    int position = positions[i];

                    int bottom = fillColumnBottom(scene, column, dy, position, bottoms[i]);
                    maxBottom = Math.max(bottom, maxBottom);
                }
                scene.setBottom(maxBottom);
                return Math.min(dy, maxBottom - scene.getHeight());
            }
        } else {
            // 从上往下滑动
            if (anchorView == null) {

                int minTop = scene.getBottom();
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
//                    int position = column.getLast();

                    int top = fillColumnTop(scene, column, dy, -1, scene.getBottom() + column.bottom);
                    minTop = Math.min(top, minTop);
                }

                scene.setTop(minTop);
                return Math.min(-dy, scene.getBottom() - minTop);
            } else {

                int childWidth = scene.getWidth() / mSpanCount;

                boolean[] places = new boolean[mSpanCount];
                int[] tops = new int[mSpanCount];
                int[] positions = new int[mSpanCount];
                Arrays.fill(positions, -1);

                int index = scene.indexOfChild(anchorView);
                int count = 0;
                for (int i = index; i < scene.getChildCount(); i++) {
                    View view = scene.getChildAt(i);
                    if (view == null) {
                        continue;
                    }

                    SceneLayoutParams params = (SceneLayoutParams) view.getLayoutParams();
                    if (params.getScene().getLayouter() != this) {
                        break;
                    }

                    int col = scene.getDecoratedLeft(view) / childWidth;
                    if (!places[col]) {
                        places[col] = true;
                        tops[col] = scene.getDecoratedTop(view);
                        positions[col] = params.getViewLayoutPosition();
                        count++;
                    }

                    if (count >= mSpanCount) {
                        break;
                    }
                }


                int col = scene.getDecoratedLeft(anchorView) / childWidth;
                Column anchorColumn = columns[col];
                int anchorBottom = scene.getDecoratedBottom(anchorView) - anchorColumn.bottom;

                Log.d(TAG, "fillVertical bottom col=" + col);

                for (int i = 0; i < mSpanCount; i++) {
                    Log.d(TAG, "fillVertical bottom=" + columns[i].bottom + " isPlace=" + places[i]);
                    if (!places[i]) {
                        Column column = columns[i];
                        tops[i] = anchorBottom + column.bottom;
//                        positions[i] = column.getLast();
                    }
                }

                int minTop = scene.getTop();
                for (int i = 0; i < mSpanCount; i++) {

                    Column column = columns[i];
                    int position = positions[i];

                    Log.d(TAG, "fillVertical fillColumnTop tops[i]=" + tops[i] + " pos=" + position + " i=" + i);
                    int top = fillColumnTop(scene, column, dy, position, tops[i]);
                    minTop = Math.min(top, minTop);
                }

                scene.setTop(minTop);
                Log.d(TAG, "fillVertical anchorBottom=" + anchorBottom + " top=" + minTop);
                return Math.min(-dy, -minTop);
            }
        }

    }

    @Override
    protected int fillVerticalTop(Scene scene, int currentPosition, int availableSpace, int anchorTop) {
        return 0;
    }

    @Override
    protected int fillVerticalBottom(Scene scene, int currentPosition, int availableSpace, int anchorBottom) {
        return 0;
    }

    private int fillColumnTop(Scene scene, Column column, int dy, int currentPosition, int anchorTop) {

        if (anchorTop - dy < 0) {
            return anchorTop;
        }

        int next;
        if (currentPosition < 0) {
            next = column.positions.size() - 1;
        } else {
            int index = column.positions.indexOf(currentPosition);
            if (index < 0) {
                throw new RuntimeException("fillColumnTop error! index=" + index + " currentPosition=" + currentPosition);
            }
            next = index - 1;
        }

        int childWidth = scene.getWidth() / mSpanCount;
        int childWidthUsed = scene.getWidth() - childWidth;
        while (next >= 0) {
            int nextPosition = column.positions.get(next--);
            View view = scene.obtainViewForPosition(nextPosition);
            scene.addView(view, 0);
            scene.measureChild(view, childWidthUsed, 0);

            int height = scene.getDecoratedMeasuredHeight(view);

            int left = column.index * childWidth;
            int bottom = anchorTop;
            int top = bottom - height;
            int right = left + childWidth;


            anchorTop = top;

            scene.layoutDecorated(view, left, top, right, bottom);

            if (anchorTop - dy < 0) {
                break;
            }
        }

        return anchorTop;

    }

    private int fillColumnBottom(Scene scene, Column column, int dy, int currentPosition, int anchorBottom) {

        if (anchorBottom - dy > scene.getHeight()) {
            return anchorBottom;
        }

        int next;
        if (currentPosition < 0) {
            next = 0;
        } else {
            int index = column.positions.indexOf(currentPosition);
            if (index < 0) {
                throw new RuntimeException("fillColumnBottom error! index=" + index + " currentPosition=" + currentPosition);
            }
            next = index + 1;
        }


        int childWidth = scene.getWidth() / mSpanCount;
        int childWidthUsed = scene.getWidth() - childWidth;
        while (next < column.positions.size()) {
            int nextPosition = column.positions.get(next++);
            View view = scene.obtainViewForPosition(nextPosition);
            scene.addView(view);
            scene.measureChild(view, childWidthUsed, 0);

            int height = scene.getDecoratedMeasuredHeight(view);

            int left = column.index * childWidth;
            int top = anchorBottom;
            int right = left + childWidth;
            int bottom = top + height;

            anchorBottom = bottom;

            scene.layoutDecorated(view, left, top, right, bottom);

            if (anchorBottom - dy > scene.getHeight()) {
                break;
            }
        }

        return anchorBottom;

    }
    
    private void initColumns(Scene scene) {
        if (columns == null || array.size() != scene.getItemCount()) {

            columns = new Column[mSpanCount];
            array.clear();

            for (int i = 0; i < mSpanCount; i++) {
                columns[i] = new Column(i);
            }

            int childWidth = scene.getWidth() / mSpanCount;
            int childWidthUsed = scene.getWidth() - childWidth;
            int minIndex = 0;
            int minBottom = 0;
            int maxBottom = 0;


            int positionOffset = scene.getPositionOffset();
            int itemCount = scene.getItemCount();
            for (int i = positionOffset; i < positionOffset + itemCount; i++) {
                View view = scene.obtainViewForPosition(i);
                scene.measureChild(view, childWidthUsed, 0);

                int height = scene.getDecoratedMeasuredHeight(view);

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
