package com.zpj.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiData<T> {

    protected final List<T> list;

    private boolean isLoaded = false;
    protected boolean hasMore = true;

    public MultiData() {
        list = new ArrayList<>();
    }

    public List<T> getDataSet() {
        return list;
    }

    public int getCount() {
        return list.size();
    }

    public int getSpanCount(int viewType) {
        return 1;
    }

    public int getViewType(int position) {
        return getClass().getName().hashCode();
    }

    public abstract int getLayoutId(int viewType);

    public boolean hasViewType(int viewType) {
        return viewType == getClass().getName().hashCode();
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public abstract boolean loadData(final MultiAdapter adapter);

    boolean load(MultiAdapter adapter) {
        hasMore = loadData(adapter);
        isLoaded = true;
        return !hasMore;
    }

    final void onBindViewHolder(EasyViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, list, getRealPosition(position), payloads);
    }

    public abstract void onBindViewHolder(final EasyViewHolder holder, final List<T> list, final int position, final List<Object> payloads);

    public int getRealPosition(int position) {
        return position;
    }

    public void onClick(EasyViewHolder holder, View view, T data) {

    }

    public boolean onLongClick(EasyViewHolder holder, View view, T data) {
        return false;
    }

}
