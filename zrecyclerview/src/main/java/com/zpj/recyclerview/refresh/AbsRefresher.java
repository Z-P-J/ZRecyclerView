package com.zpj.recyclerview.refresh;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class AbsRefresher implements IRefresher {

    protected int mState = STATE_NORMAL;
    protected OnRefreshListener mListener;
    protected View mView;
    protected float mDelta;

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setState(int state) {
        this.mState = state;
        if (mState == STATE_REFRESHING) {
            if (mListener != null) {
                mListener.onRefresh(this);
            }
        }
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public final View onCreateView(Context context, ViewGroup parent) {
        mView = onCreateRefreshView(context, parent);
        return mView;
    }

    @Override
    public final View getView() {
        return mView;
    }

    @Override
    public void onDown() {

    }

    @Override
    public void onMove(float delta) {
        this.mDelta = delta;
    }

    @Override
    public float getDelta() {
        return mDelta;
    }

    public boolean isRefreshing() {
        return mState == STATE_REFRESHING;
    }

    public abstract View onCreateRefreshView(Context context, ViewGroup parent);

}
