package com.zpj.recyclerview;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zpj.recyclerview.layouter.AbsLayouter;
import com.zpj.recyclerview.layouter.ContainerLayouter;
import com.zpj.recyclerview.layouter.Layouter;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;

public abstract class ContainerMultiData<T> extends StateMultiData<T> {

    public ContainerMultiData() {
        super();
    }

    public ContainerMultiData(List<T> list) {
        super(list);
    }

    public ContainerMultiData(AbsLayouter layouter) {
        super(new ContainerLayouter(layouter));
    }

    public ContainerMultiData(List<T> list, AbsLayouter layouter) {
        super(list, new ContainerLayouter(layouter));
    }

    @Override
    public View onCreateView(Context context, ViewGroup container, int viewType) {
        if (viewType == ContainerLayouter.ContainerLayout.class.hashCode()) {
            return new ContainerLayouter.ContainerLayout(context);
        }
        Log.d("ContainerMultiData", "onCreateView addView i=");
        return LayoutInflater.from(context).inflate(getLayoutId(viewType), container, false);
    }

    @Override
    public final int getCount() {
        if (state == STATE_CONTENT) {
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
            return ContainerLayouter.ContainerLayout.class.hashCode();
        }
        if (state != STATE_CONTENT) {
            return state.hashCode();
        }
        return getLayoutId();
    }

    @Override
    public final boolean hasViewType(int viewType) {
        if (state != STATE_CONTENT && viewType == state.hashCode()) {
            return true;
        }
        return viewType == ContainerLayouter.ContainerLayout.class.hashCode()
                || viewType == getLayoutId();
    }

    @Override
    public final int getLayoutId(int viewType) {
        return getLayoutId();
    }

    @Override
    public final void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
        if (position != 0 && state == STATE_CONTENT) {
            onBindChild(holder, list, --position, payloads);
        }
    }

    public int getChildCount() {
        return mData.size();
    }

    public abstract int getLayoutId();

    public abstract void onBindChild(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);


}
