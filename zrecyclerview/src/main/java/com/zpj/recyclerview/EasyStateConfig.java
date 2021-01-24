package com.zpj.recyclerview;

import android.content.Context;
import android.view.View;

import com.zpj.statemanager.BaseStateConfig;
import com.zpj.statemanager.BaseViewHolder;
import com.zpj.statemanager.IViewHolder;
import com.zpj.statemanager.State;
import com.zpj.statemanager.StateManager;

public class EasyStateConfig<T extends EasyStateConfig<T>> {

    protected IEasy.OnLoadRetryListener onLoadRetryListener;

    protected IViewHolder loadingViewHolder;

    protected IViewHolder emptyViewHolder;

    protected IViewHolder errorViewHolder;

    protected IViewHolder loginViewHolder;

    protected IViewHolder noNetworkViewHolder;

    public T setOnLoadRetryListener(IEasy.OnLoadRetryListener onLoadRetryListener) {
        this.onLoadRetryListener = onLoadRetryListener;
        return (T) this;
    }

    public T setLoadingViewHolder(IViewHolder loadingViewHolder) {
        this.loadingViewHolder = loadingViewHolder;
        return (T) this;
    }

    public T setEmptyViewHolder(IViewHolder emptyViewHolder) {
        this.emptyViewHolder = emptyViewHolder;
        return (T) this;
    }

    public T setErrorViewHolder(IViewHolder errorViewHolder) {
        this.errorViewHolder = errorViewHolder;
        return (T) this;
    }

    public T setLoginViewHolder(IViewHolder loginViewHolder) {
        this.loginViewHolder = loginViewHolder;
        return (T) this;
    }

    public T setNoNetworkViewHolder(IViewHolder noNetworkViewHolder) {
        this.noNetworkViewHolder = noNetworkViewHolder;
        return (T) this;
    }

    public T setErrorView(final int layoutId) {
        return setErrorViewHolder(new EmptyViewHolder(layoutId));
    }

    public T setEmptyView(final int layoutId) {
        return setEmptyViewHolder(new EmptyViewHolder(layoutId));
    }

    public T setLoadingView(int layoutId) {
        return setLoadingViewHolder(new EmptyViewHolder(layoutId));
    }

    public T setLoginView(int layoutId) {
        return setLoginViewHolder(new EmptyViewHolder(layoutId));
    }

    public T setNoNetworkView(int layoutId) {
        return setNoNetworkViewHolder(new EmptyViewHolder(layoutId));
    }

    public T setLoadingView(View view) {
        return setLoadingViewHolder(new ConstantViewHolder(view));
    }

    public T setEmptyView(View view) {
        return setEmptyViewHolder(new ConstantViewHolder(view));
    }

    public T setErrorView(View view) {
        return setErrorViewHolder(new ConstantViewHolder(view));
    }

    public T setLoginView(View view) {
        return setLoginViewHolder(new ConstantViewHolder(view));
    }

    public T setNoNetworkView(View view) {
        return setNoNetworkViewHolder(new ConstantViewHolder(view));
    }

    public IViewHolder getViewHolder(State state) {
        IViewHolder viewHolder = getViewHolderInner(state);
        if (viewHolder instanceof BaseViewHolder && onLoadRetryListener != null) {
            ((BaseViewHolder) viewHolder).setOnRetry(new Runnable() {
                @Override
                public void run() {
                    onLoadRetryListener.onLoadRetry();
                }
            });
        }
        return viewHolder;
    }

    private IViewHolder getViewHolderInner(State state) {
        switch (state) {
            case STATE_LOADING:
                return getLoadingViewHolder();
            case STATE_EMPTY:
                return getEmptyViewHolder();
            case STATE_ERROR:
                return getErrorViewHolder();
            case STATE_LOGIN:
                return getLoginViewHolder();
            case STATE_NO_NETWORK:
                return getNoNetworkViewHolder();
            case STATE_CONTENT:
            default:
                return null;
        }
    }

    public IViewHolder getLoadingViewHolder() {
        if (loadingViewHolder == null) {
            return StateManager.config().getLoadingViewHolder();
        }
        return loadingViewHolder;
    }

    public IViewHolder getEmptyViewHolder() {
        if (emptyViewHolder == null) {
            return StateManager.config().getEmptyViewHolder();
        }
        return emptyViewHolder;
    }

    public IViewHolder getErrorViewHolder() {
        if (errorViewHolder == null) {
            return StateManager.config().getErrorViewHolder();
        }
        return errorViewHolder;
    }

    public IViewHolder getLoginViewHolder() {
        if (loginViewHolder == null) {
            return StateManager.config().getLoginViewHolder();
        }
        return loginViewHolder;
    }

    public IViewHolder getNoNetworkViewHolder() {
        if (noNetworkViewHolder == null) {
            return StateManager.config().getNoNetworkViewHolder();
        }
        return noNetworkViewHolder;
    }

    private static class EmptyViewHolder extends BaseViewHolder {

        private EmptyViewHolder(int layoutId) {
            super(layoutId);
        }

        @Override
        public void onViewCreated(View view) {

        }
    }

    private static class ConstantViewHolder implements IViewHolder {

        private final View view;

        private ConstantViewHolder(View view) {
            this.view = view;
        }

        @Override
        public View getView() {
            return view;
        }

        @Override
        public int getLayoutId() {
            return 0;
        }

        @Override
        public View onCreateView(Context context) {
            return view;
        }

        @Override
        public void onViewCreated(View view) {

        }

        @Override
        public void onDestroyView() {

        }

    }

}
