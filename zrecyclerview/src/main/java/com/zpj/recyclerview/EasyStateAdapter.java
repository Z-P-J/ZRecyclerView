package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;
import static com.zpj.statemanager.State.STATE_EMPTY;
import static com.zpj.statemanager.State.STATE_ERROR;
import static com.zpj.statemanager.State.STATE_LOADING;
import static com.zpj.statemanager.State.STATE_LOGIN;
import static com.zpj.statemanager.State.STATE_NO_NETWORK;

public class EasyStateAdapter<T> extends EasyAdapter<T> {

    private static final String TAG = "EasyStateAdapter";

//    protected static final int TYPE_STATE = -3;

    protected final Context context;

    protected final EasyStateConfig<?> config;

    protected State state = STATE_CONTENT;

    EasyStateAdapter(final Context context, List<T> list, int itemRes,
                     IEasy.OnGetChildViewTypeListener<T> onGetChildViewTypeListener,
                     IEasy.OnGetChildLayoutIdListener onGetChildLayoutIdListener,
                     IEasy.OnCreateViewHolderListener<T> onCreateViewHolder,
                     IEasy.OnBindViewHolderListener<T> onBindViewHolderListener,
                     IEasy.OnItemClickListener<T> onClickListener,
                     IEasy.OnItemLongClickListener<T> onLongClickListener,
                     SparseArray<IEasy.OnClickListener<T>> onClickListeners,
                     SparseArray<IEasy.OnLongClickListener<T>> onLongClickListeners,
                     final EasyStateConfig<?> config) {
        super(list, itemRes, onGetChildViewTypeListener, onGetChildLayoutIdListener,
                onCreateViewHolder, onBindViewHolderListener, onClickListener,
                onLongClickListener, onClickListeners, onLongClickListeners);
        this.context = context;
        this.config = config;
    }

    @NonNull
    @Override
    public EasyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == STATE_EMPTY.hashCode() || viewType == STATE_LOADING.hashCode()
                || viewType == STATE_ERROR.hashCode() || viewType == STATE_LOGIN.hashCode()
                || viewType == STATE_NO_NETWORK.hashCode()) {
            IViewHolder viewHolder = config.getViewHolder(state);
            if (viewHolder != null) {
                View view = viewHolder.onCreateView(context);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
                return new EasyViewHolder(view);
            }
        }
        return super.onCreateViewHolder(viewGroup, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull EasyViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (state == STATE_CONTENT) {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (state != STATE_CONTENT) {
            return state.hashCode();
        }
        return super.getItemViewType(position);
    }

    @Override
    protected boolean isHeaderPosition(int position) {
        if (state != STATE_CONTENT) {
            return true;
        }
        return super.isHeaderPosition(position);
    }

    @Override
    protected boolean isFooterPosition(int position) {
        if (state != STATE_CONTENT) {
            return true;
        }
        return super.isFooterPosition(position);
    }

    @Override
    public int getItemCount() {
        if (state != STATE_CONTENT) {
            return 1;
        }
        return super.getItemCount();
    }

    /**
     * 显示空视图
     */
    public final void showEmpty() {
        changeState(STATE_EMPTY);
//        stateLayout.showEmptyView();
    }

    public void showEmptyView(int msgId) {
        changeState(STATE_EMPTY);
//        stateLayout.showEmptyView(msgId);
    }

    public void showEmptyView(String msg) {
        changeState(STATE_EMPTY);
//        stateLayout.showEmptyView(msg);
    }

    public void showEmptyView(int msgId, int imgId) {
        changeState(STATE_EMPTY);
//        stateLayout.showEmptyView(msgId, imgId);
    }

    public void showEmptyView(String msg, int imgId) {
        changeState(STATE_EMPTY);
//        stateLayout.showEmptyView(msg, imgId);
    }

    /**
     * 显示错误视图
     */

    private void showErrorFooter(String msg) {
//        String errorMsg = "~ 出错了 ~";
//        if (!TextUtils.isEmpty(msg)) {
//            errorMsg += ("\n错误信息：" + msg);
//        }
        showFooterMsg(msg);
        if (mIsLoading) {
            currentPage--;
            mIsLoading = false;
        }
    }

    public final void showError() {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showErrorFooter(null);
            return;
        }
        changeState(STATE_ERROR);
//        stateLayout.showErrorView();
    }

    public void showErrorView(int msgId) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showErrorFooter(context.getString(msgId));
            return;
        }
        changeState(STATE_ERROR);
//        stateLayout.showErrorView(msgId);
    }

    public void showErrorView(String msg) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showErrorFooter(msg);
            return;
        }
        changeState(STATE_ERROR);
//        stateLayout.showErrorView(msg);
    }

    public void showErrorView(int msgId, int imgId) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showErrorFooter(context.getString(msgId));
            return;
        }
        changeState(STATE_ERROR);
//        stateLayout.showErrorView(msgId, imgId);
    }

    public void showErrorView(String msg, int imgId) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showErrorFooter(msg);
            return;
        }
        changeState(STATE_ERROR);
//        stateLayout.showErrorView(msg, imgId);
    }

    /**
     * 显示加载中视图
     */
    public final void showLoading() {
        changeState(STATE_LOADING);
//        stateLayout.showLoadingView();
    }

    public void showLoadingView(View view) {
        changeState(STATE_LOADING);
//        stateLayout.showLoadingView(view);
    }

    public void showLoadingView(View view, boolean showTip) {
        changeState(STATE_LOADING);
//        stateLayout.showLoadingView(view, showTip);
    }

    public void showLoadingView(int msgId) {
        changeState(STATE_LOADING);
//        stateLayout.showLoadingView(msgId);
    }

    public void showLoadingView(String msg) {
        changeState(STATE_LOADING);
//        stateLayout.showLoadingView(msg);
    }

    /**
     * 显示无网络视图
     */

    private void showNoNetworkFooter(String msg) {
//        String errorMsg = "网络不可用！";
//        if (!TextUtils.isEmpty(msg)) {
//            errorMsg += ("\n错误信息：" + msg);
//        }
        showFooterMsg(msg);
        if (mIsLoading) {
            currentPage--;
            mIsLoading = false;
        }
    }

    public final void showNoNetwork() {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showNoNetworkFooter(null);
            return;
        }
        changeState(STATE_NO_NETWORK);
//        stateLayout.showNoNetworkView();
    }

    public void showNoNetworkView(int msgId) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showNoNetworkFooter(context.getString(msgId));
            return;
        }
        changeState(STATE_NO_NETWORK);
//        stateLayout.showNoNetworkView(msgId);
    }

    public void showNoNetworkView(String msg) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showNoNetworkFooter(msg);
            return;
        }
        changeState(STATE_NO_NETWORK);
//        stateLayout.showNoNetworkView(msg);
    }

    public void showNoNetworkView(int msgId, int imgId) {
        if (getFooterViewHolder() != null && !list.isEmpty()) {
            showNoNetworkFooter(context.getString(msgId));
            return;
        }
        changeState(STATE_NO_NETWORK);
//        stateLayout.showNoNetworkView(msgId, imgId);
    }


    /**
     * 显示内容视图
     */
    public final void showContent() {
        if (state == STATE_CONTENT) {
            return;
        }
        state = STATE_CONTENT;
        setLoadMoreEnabled(true);
        notifyDataSetChanged();
    }

    public State getState() {
        return state;
    }

    private void changeState(State state) {
        this.state = state;
        setLoadMoreEnabled(false);
        list.clear();
        notifyDataSetChanged();
    }

}
