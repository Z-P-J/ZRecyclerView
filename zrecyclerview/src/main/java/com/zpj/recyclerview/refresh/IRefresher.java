package com.zpj.recyclerview.refresh;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface IRefresher {

    // 正常
    int STATE_NORMAL = 0;
    // 释放刷新
    int STATE_RELEASE_TO_REFRESH = 1;
    // 正在刷新
    int STATE_REFRESHING = 2;
    // 刷新完成
    int STATE_DONE = 3;

    void setOnRefreshListener(OnRefreshListener listener);

    /**
     * 设置对应刷新状态
     */
    void setState(int state);

    /**
     * 返回当前状态
     */
    int getState();

    View onCreateView(Context context, ViewGroup parent);

    View getView();

    void onDown();

    /**
     * 根据拉动的距离处理刷新状态
     *
     * @param delta 向下拉动的高度
     */
    void onMove(float delta);

    float getDelta();

    /**
     * 手指释放时的状态处理
     */
    boolean onRelease();

    void stopRefresh();

    boolean isRefreshing();

    interface OnRefreshListener {
        void onRefresh(IRefresher refresher);
    }


}
