package com.zpj.recyclerview;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;

public abstract class HeaderMultiData<T> extends StateMultiData<T> {

    public HeaderMultiData() {
        super();
    }

    public HeaderMultiData(List<T> list) {
        super(list);
    }

    @Override
    public int getItemCount() {
        if (getState() == STATE_CONTENT) {
            if (getChildCount() == 0 && hasMore) {
                return 0;
            }
            return getChildCount() + 1;
        }
        return 2;
    }

    @Override
    public boolean isStickyPosition(int position) {
        return position == 0;
    }

    @Override
    public final int getColumnCount(int viewType) {
        if (viewType == getHeaderViewType()) {
            return getHeaderColumnCount();
        }
        if (getState() != STATE_CONTENT) {
            return 1;
        }
        return getChildColumnCount(viewType);
    }

    @Override
    public final int getViewType(int position) {
        if (position == 0) {
            return getHeaderViewType();
        }
        if (getState() != STATE_CONTENT) {
            return getState().hashCode();
        }
        return getChildViewType(position);
    }

    @Override
    public final boolean hasViewType(int viewType) {
        if (getState() != STATE_CONTENT && viewType == getState().hashCode()) {
            return true;
        }
        return viewType == getHeaderViewType() || hasChildViewType(viewType);
    }

    @Override
    public final int getLayoutId(int viewType) {
        if (viewType == getHeaderViewType()) {
            return getHeaderLayoutId();
        }
        return getChildLayoutId(viewType);
    }

    @Override
    public final void onBindViewHolder(EasyViewHolder holder, List<T> list, int position, List<Object> payloads) {

        if (position == 0) {
            onBindHeader(holder, payloads);
        } else if (getState() == STATE_CONTENT) {
            onBindChild(holder, list, --position, payloads);
        }
    }

    public int getChildCount() {
        return mItems.size();
    }

    public @IntRange(from = 1) int getHeaderColumnCount() {
        return 1;
    }

    public @IntRange(from = 1) int getChildColumnCount(int viewType) {
        return 1;
    }

    @LayoutRes
    public abstract int getHeaderLayoutId();

    public int getHeaderViewType() {
        return getHeaderLayoutId();
    }

    public abstract int getChildViewType(int position);

    public abstract boolean hasChildViewType(int viewType);

    public abstract int getChildLayoutId(int viewType);

    public abstract void onBindHeader(EasyViewHolder holder, List<Object> payloads);

    public abstract void onBindChild(EasyViewHolder holder, List<T> list, int position, List<Object> payloads);


}
