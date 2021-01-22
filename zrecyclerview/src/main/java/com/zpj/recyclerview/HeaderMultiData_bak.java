//package com.zpj.recyclerview;
//
//import android.support.annotation.IntRange;
//import android.support.annotation.LayoutRes;
//
//import java.util.List;
//
//public abstract class HeaderMultiData_bak<T> extends MultiData<T> {
//
//    @Override
//    public final int getCount() {
//        if (isLoaded()) {
//            return getChildCount() + 1;
//        }
//        return super.getCount();
//    }
//
//    @Override
//    public final int getColumnCount(int viewType) {
//        if (viewType == getHeaderLayoutId()) {
//            return getHeaderColumnCount();
//        }
//        return getChildColumnCount(viewType);
//    }
//
//    @Override
//    public final int getViewType(int position) {
//        if (position == 0) {
//            return getHeaderLayoutId();
//        }
//        return getChildViewType(position);
//    }
//
//    @Override
//    public final boolean hasViewType(int viewType) {
//        return viewType == getHeaderLayoutId() || hasChildViewType(viewType);
//    }
//
//    @Override
//    public final int getLayoutId(int viewType) {
//        if (viewType == getHeaderLayoutId()) {
//            return getHeaderLayoutId();
//        }
//        return getChildLayoutId(viewType);
//    }
//
//    @Override
//    public final void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {
//        if (position == 0) {
//            onBindHeader(holder, payloads);
//        } else {
//            onBindChild(holder, list, --position, payloads);
//        }
//    }
//
//    public int getChildCount() {
//        return list.size();
//    }
//
//    public @IntRange(from = 1) int getHeaderColumnCount() {
//        return 1;
//    }
//
//    public @IntRange(from = 1) int getChildColumnCount(int viewType) {
//        return 1;
//    }
//
//    @LayoutRes
//    public abstract int getHeaderLayoutId();
//
//    public abstract int getChildViewType(int position);
//
//    public abstract boolean hasChildViewType(int viewType);
//
//    public abstract int getChildLayoutId(int viewType);
//
//    public abstract void onBindHeader(EasyViewHolder holder, List<Object> payloads);
//    public abstract void onBindChild(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);
//
//
//}
