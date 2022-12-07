package com.zpj.recyclerview;

import android.view.View;

import java.util.List;

import static com.zpj.statemanager.State.STATE_CONTENT;

import com.zpj.recyclerview.core.Scene;

public abstract class ExpandableMultiData<T> extends HeaderMultiData<T> {

    private boolean isExpand = true;

    public ExpandableMultiData() {
        super();
    }

    public ExpandableMultiData(List<T> list) {
        super(list);
    }

    @Override
    public boolean hasChildViewType(int viewType) {
        return getChildLayoutId(viewType) == viewType;
    }

    @Override
    public void onBindHeader(final EasyViewHolder holder, List<Object> payloads) {
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpand) {
                    collapse();
                } else {
                    expand();
                }
//                onStateChange(holder, !isExpand);
            }
        });
    }

    @Override
    public int getCount() {
        if (getState() == STATE_CONTENT) {
            if (getChildCount() == 0 && hasMore) {
                return 0;
            }
            return getChildCount() + 1;
        }
        return isExpand ? 2 : 1;
    }

    @Override
    public final int getChildCount() {
        if (isExpand) {
            return super.getChildCount();
        } else {
            return 0;
        }
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public void expand() {
        isExpand = true;
        final MultiSceneAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        int num = getStartCount();
        for (Scene scene : adapter.getData()) {
            MultiData<?> data = scene.getMultiData();
            if (data == this) {
                adapter.notifyItemRangeInserted(num + 1, getCount() - mLastCount); // mData.size()
                break;
            }
            num  += data.getCount();
        }
        mLastCount = getCount();
    }

    public void collapse() {
        isExpand = false;
        MultiSceneAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        int num = getStartCount();
        for (Scene scene : adapter.getData()) {
            MultiData<?> data = scene.getMultiData();
            if (data == this) {
                adapter.notifyItemRangeRemoved(num + 1, mLastCount - getCount()); // mData.size()
                break;
            }
            num  += data.getCount();
        }
        mLastCount = getCount();
    }

//    protected abstract void onStateChange(EasyViewHolder headerHolder, boolean isExpand);


}
