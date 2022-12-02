package com.zpj.recyclerview;

import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.statemanager.State;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;

public abstract class SingleTypeMultiData<T> extends StateMultiData<T> {

    public SingleTypeMultiData() {
        super();
    }

    public SingleTypeMultiData(List<T> list) {
        super(list);
    }

    @Override
    public final int getMaxColumnCount() {
        return getColumnCount();
    }

    @Override
    public final int getColumnCount(int viewType) {
        if (getState() != State.STATE_CONTENT) {
            return 1;
        }
        return getColumnCount();
    }

    @Override
    public final int getViewType(int position) {
        if (getState() == STATE_CONTENT) {
            return getViewType();
        }
        return getState().hashCode();
    }

    @Override
    public final boolean hasViewType(int viewType) {
        if (getState() != STATE_CONTENT && viewType == getState().hashCode()) {
            return true;
        }
        return viewType == getViewType();
    }

    @Override
    public final int getLayoutId(int viewType) {
        return getLayoutId();
    }

    public int getViewType() {
        return getLayoutId();
    }

    public int getColumnCount() {
        return 1;
    }

    public abstract int getLayoutId();

}
