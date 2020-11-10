package com.zpj.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiData<T> {

    protected final List<T> data;

    protected boolean isDataLoaded = false;

    public MultiData() {
        data = new ArrayList<>();
    }

    public List<T> getData() {
        return data;
    }

    public int getCount() {
        return data.size();
    }

    public int getSpanCount() {
        return 1;
    }

    public int getViewType(int position) {
        return hashCode();
    }

    public abstract int getLayoutId(int viewType);

    public boolean hasViewType(int viewType) {
        if (viewType == hashCode()) {
            return true;
        }
        return false;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public abstract boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter);

    boolean load(RecyclerView recyclerView, MultiAdapter adapter) {
        isDataLoaded = loadData(recyclerView, adapter);
        return isDataLoaded;
    }

    final void onBindViewHolder(EasyViewHolder holder, int position, List<Object> payloads) {
        onBindViewHolder(holder, data, position, payloads);
    }

    public abstract void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);

    public void onClick(EasyViewHolder holder, View view, T data) {

    }

    public boolean onLongClick(EasyViewHolder holder, View view, T data) {
        return false;
    }

}
