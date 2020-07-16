package com.zpj.recyclerview;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public interface IEasy {

    interface OnItemClickCallback {
        boolean shouldIgnoreClick(View view);
    }

    interface OnClickListener<T> {
        void onClick(EasyViewHolder holder, View view, T data);
    }

    interface OnLongClickListener<T> {
        boolean onLongClick(EasyViewHolder holder, View view, T data);
    }

    interface OnItemClickListener<T> {
        void onClick(EasyViewHolder holder, View view, T data);
    }

    interface OnItemLongClickListener<T> {
        boolean onLongClick(EasyViewHolder holder, View view, T data);
    }


    interface OnBindViewHolderListener<T>{
        void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);
    }

    interface OnCreateViewHolderListener<T>{
        View onCreateViewHolder(ViewGroup parent, int layoutRes, int viewType);
    }

    interface OnBindHeaderListener {
        void onBindHeader(EasyViewHolder holder);
    }

    interface OnCreateFooterListener {
        void onCreateFooterView(View view);
    }

    interface OnLoadMoreListener {
        boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage);
    }

    interface OnGetChildViewTypeListener<T> {
        int onGetViewType(List<T> list, int position);
    }

    interface OnGetChildLayoutIdListener {
        @LayoutRes int onGetChildLayoutId(int viewType);
    }

    interface OnSelectChangeListener<T> {
        void onSelectModeChange(boolean selectMode);
        void onSelectChange(List<T> list, int position, boolean isChecked);
        void onSelectAll();
        void onUnSelectAll();
    }

}
