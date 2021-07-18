package com.zpj.recyclerview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.statemanager.BaseViewHolder;
import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;
import static com.zpj.statemanager.State.STATE_EMPTY;
import static com.zpj.statemanager.State.STATE_ERROR;
import static com.zpj.statemanager.State.STATE_LOADING;
import static com.zpj.statemanager.State.STATE_LOGIN;
import static com.zpj.statemanager.State.STATE_NO_NETWORK;

public abstract class StateMultiData<T> extends MultiData<T> {

    protected State state = STATE_CONTENT;

    public StateMultiData() {
        super();
    }

    public StateMultiData(List<T> list) {
        super(list);
    }

    public State getState() {
        return state;
    }

    @Override
    void onBindViewHolder(EasyViewHolder holder, final int position, List<Object> payloads) {
        int viewType = getViewType(getRealPosition(position));
        if (viewType == STATE_EMPTY.hashCode() || viewType == STATE_LOADING.hashCode()
                || viewType == STATE_ERROR.hashCode() || viewType == STATE_LOGIN.hashCode()
                || viewType == STATE_NO_NETWORK.hashCode()) {
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (state == STATE_ERROR || state == STATE_NO_NETWORK) {
                        onRetry();
                    }
                }
            });
            return;
        }
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public int getCount() {
        if (state == STATE_CONTENT) {
            return super.getCount();
        }
        return 1;
    }

    @Override
    public int getColumnCount(int viewType) {
        if (state == STATE_CONTENT) {
            return super.getColumnCount(viewType);
        }
        return 1;
    }

    @Override
    public int getViewType(int position) {
        if (state == STATE_CONTENT) {
            return super.getViewType(position);
        }
        return state.hashCode();
    }

    @Override
    public boolean hasViewType(int viewType) {
        if (state != STATE_CONTENT && viewType == state.hashCode()) {
            return true;
        }
        return super.hasViewType(viewType);
    }

    @Override
     protected View onCreateView(Context context, ViewGroup container, int viewType) {
        if (viewType == STATE_EMPTY.hashCode() || viewType == STATE_LOADING.hashCode()
                || viewType == STATE_ERROR.hashCode() || viewType == STATE_LOGIN.hashCode()
                || viewType == STATE_NO_NETWORK.hashCode()) {
            IViewHolder viewHolder = getViewHolder(state);
            if (viewHolder != null) {
                if (viewHolder instanceof BaseViewHolder) {
                    ((BaseViewHolder) viewHolder).setOnRetry(new Runnable() {
                        @Override
                        public void run() {
                            onRetry();
                        }
                    });
                    ((BaseViewHolder) viewHolder).setOnLogin(new Runnable() {
                        @Override
                        public void run() {
                            onLogin();
                        }
                    });
                }
                return viewHolder.onCreateView(context);
            }
        }
        return super.onCreateView(context, container, viewType);
    }

    protected void showContent() {
        this.state = STATE_CONTENT;
        notifyDataSetChange();
    }

    protected void showLoading() {
        this.state = STATE_LOADING;
        this.mData.clear();
        notifyDataSetChange();
    }

    protected void showEmpty() {
        this.state = STATE_EMPTY;
        this.mData.clear();
        notifyDataSetChange();
    }

//    protected void showError(String msg) {
//        this.state = STATE_ERROR;
//    }

    protected void showError() {
//        notifyItemRangeRemoved();
//
//        int tempCount = getCount();
//        this.list.clear();
//        int currentCount = getCount();
//
//        if (tempCount == currentCount) {
//            notifyDataSetChange();
//        } else {
//            notifyItemRangeChanged(0, tempCount - currentCount);
//            notifyItemRangeRemoved();
//        }
//        this.state = STATE_ERROR;
//        int newCount = getCount();
//        if () {
//
//        }
        this.state = STATE_ERROR;
        this.mData.clear();
        notifyDataSetChange();
    }

    protected void showLogin() {
        this.state = STATE_LOGIN;
        this.mData.clear();
        notifyDataSetChange();
    }

    protected void showNoNetwork() {
        this.state = STATE_NO_NETWORK;
        this.mData.clear();
        notifyDataSetChange();
    }

    protected void onRetry() {
        showLoading();
        load(getAdapter());
    }

    protected void onLogin() {

    }






}
