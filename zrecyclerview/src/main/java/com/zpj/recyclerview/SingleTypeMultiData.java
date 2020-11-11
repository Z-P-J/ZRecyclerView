package com.zpj.recyclerview;

public abstract class SingleTypeMultiData<T> extends MultiData<T> {

    @Override
    public final int getSpanCount(int viewType) {
        return getSpanCount();
    }

    @Override
    public final int getViewType(int position) {
        return getViewType();
    }

    @Override
    public final int getLayoutId(int viewType) {
        return getLayoutId();
    }

    public int getViewType() {
        return getLayoutId();
    }

    public int getSpanCount() {
        return 1;
    }

    public abstract int getLayoutId();

}
