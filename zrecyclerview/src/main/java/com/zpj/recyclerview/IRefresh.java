package com.zpj.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface IRefresh {

    int STATE_NORMAL = 0;             // 正常
    int STATE_RELEASE_TO_REFRESH = 1; // 释放刷新
    int STATE_REFRESHING = 2;         // 正在刷新
    int STATE_DONE = 3;               // 刷新完成

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

    /**
     * 根据拉动的距离处理刷新状态
     *
     * @param delta 向下拉动的高度
     */
    void onMove(float delta);

    /**
     * 手指释放时的状态处理
     */
    boolean onRelease();

    void stopRefresh();

    interface OnRefreshListener {
        void onRefresh(IRefresh refresh);
    }


}
