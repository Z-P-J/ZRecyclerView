package com.zpj.recyclerview.flinger;

public interface Flinger {

    void fling(float velocityX, float velocityY);

    void scroll(int dx, int dy);

    void scroll(int dx, int dy, int duration);

    void postOnAnimation();

    boolean onComputeScroll(int dx, int dy);

    void stop();

    boolean isStop();

    void onFinished();

    void onStopped();

    interface ScrollListener {

        void onScroll(int dx, int dy);

        void onFinished();

        void onStopped();

    }

}
