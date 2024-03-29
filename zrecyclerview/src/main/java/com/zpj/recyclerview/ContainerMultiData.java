package com.zpj.recyclerview;

import static com.zpj.statemanager.State.STATE_CONTENT;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.scene.ContainerScene;

import java.util.List;

// TODO 采用代理MultiData的方式
public abstract class ContainerMultiData<T> extends StateMultiData<T> {

    public ContainerMultiData() {
        super();
    }

    public ContainerMultiData(List<T> list) {
        super(list);
    }

    @Override
    public View onCreateView(Context context, ViewGroup container, int viewType) {
        if (viewType == ContainerScene.ContainerLayout.class.hashCode()) {
            return new ContainerScene.ContainerLayout(context);
        }
        Log.d("ContainerMultiData", "onCreateView addView i=");
        return LayoutInflater.from(context).inflate(getLayoutId(viewType), container, false);
    }

    @Override
    public final int getCount() {
        if (getState() == STATE_CONTENT) {
            if (getChildCount() == 0 && hasMore) {
                return 0;
            }
            return getChildCount() + 1;
        }
        return 2;
    }
    @Override
    public final int getColumnCount(int viewType) {
        return 1;
    }

    @Override
    public final int getViewType(int position) {
        if (position == 0) {
            return ContainerScene.ContainerLayout.class.hashCode();
        }
        if (getState() != STATE_CONTENT) {
            return getState().hashCode();
        }
        return getLayoutId();
    }

    @Override
    public final boolean hasViewType(int viewType) {
        if (getState() != STATE_CONTENT && viewType == getState().hashCode()) {
            return true;
        }
        return viewType == ContainerScene.ContainerLayout.class.hashCode()
                || viewType == getLayoutId();
    }

    @Override
    public final int getLayoutId(int viewType) {
        return getLayoutId();
    }

    @Override
    public final void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
        if (position != 0 && getState() == STATE_CONTENT) {
            onBindChild(holder, list, --position, payloads);
        }
    }

    public int getChildCount() {
        return mData.size();
    }

    public abstract int getLayoutId();

    public abstract void onBindChild(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);


}
