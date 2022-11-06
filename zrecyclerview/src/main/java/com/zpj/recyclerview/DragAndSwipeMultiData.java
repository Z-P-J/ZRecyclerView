package com.zpj.recyclerview;

import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.Collections;

public abstract class DragAndSwipeMultiData<T> extends StateMultiData<T> implements IDragAndSwipe {

    @Override
    public int getDragDirection(int position) {
        return ItemTouchHelper.UP | ItemTouchHelper.DOWN
                | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    }

    @Override
    public int getSwipeDirection(int position) {
        return ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    }

    @Override
    public boolean onMove(int from, int to) {
        Collections.swap(mData, from, to);
        notifyItemMove(from, to);
        return true;
    }

    @Override
    public void onSwiped(int position, int direction) {
        mData.remove(position);
        notifyItemRemoved(position);
    }

}
