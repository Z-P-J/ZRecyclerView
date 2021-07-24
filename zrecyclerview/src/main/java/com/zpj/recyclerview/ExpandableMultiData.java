package com.zpj.recyclerview;

import android.view.View;

import java.util.List;

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
        final MultiAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        int num = getStartCount();
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                adapter.notifyItemRangeInserted(num + 1, mData.size());
                break;
            }
            num  += data.getCount();
        }
    }

    public void collapse() {
        isExpand = false;
//        tempData.clear();
//        tempData.addAll(list);
//        list.clear();
        MultiAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        int num = getStartCount();
        for (MultiData<?> data : adapter.getData()) {
            if (data == this) {
                adapter.notifyItemRangeRemoved(num + 1, mData.size());
                break;
            }
            num  += data.getCount();
        }
    }

//    protected abstract void onStateChange(EasyViewHolder headerHolder, boolean isExpand);


}
