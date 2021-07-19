package com.zpj.recyclerview;

public interface IDragAndSwipe {

    int getDragDirection(int position);

    int getSwipeDirection(int position);

    boolean onMove(int from, int to);

    void onSwiped(int position, int direction);

}
