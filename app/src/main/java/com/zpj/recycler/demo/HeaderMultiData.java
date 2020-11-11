//package com.zpj.recycler.demo;
//
//import android.support.annotation.IntRange;
//import android.support.annotation.LayoutRes;
//
//import com.zpj.recyclerview.EasyViewHolder;
//import com.zpj.recyclerview.MultiData;
//
//import java.util.List;
//
//public abstract class HeaderMultiData<T> extends MultiData<T> {
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
//    public final int getSpanCount(int viewType) {
//        if (viewType == getHeaderLayoutId()) {
//            return getHeaderSpanCount();
//        }
//        return getChildSpanCount(viewType);
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
//        return getChildViewType(viewType);
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
////    @Override
////    public int getRealPosition(int position) {
////        if (position > 0) {
////            return --position;
////        }
////        return super.getRealPosition(position);
////    }
//
//    public int getChildCount() {
//        return list.size();
//    }
//
//    public abstract int getHeaderSpanCount();
//
//    @IntRange(from = 1)
//    @LayoutRes
//    public abstract  int getHeaderLayoutId();
//
//    public abstract int getChildSpanCount(int viewType);
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
