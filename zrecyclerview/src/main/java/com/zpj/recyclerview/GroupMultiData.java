package com.zpj.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

public class GroupMultiData extends MultiData<MultiData<?>> {

    private static final String TAG = "GroupMultiData";


//    public GroupMultiData() {
//        super();
//    }

    public GroupMultiData(List<MultiData<?>> datas) {
        super();
        mItems.addAll(datas);
    }

    public GroupMultiData(MultiData<?>...datas) {
        this(Arrays.asList(datas));
    }

//    public GroupMultiData(MultiData<?> data1, MultiData<?> data2, MultiData<?>...datas) {
//        super();
//        mData.add(data1);
//        mData.add(data2);
//        mData.addAll(Arrays.asList(datas));
//    }

    @Override
    public int getViewType(int position) {
        int count = 0;
        for (MultiData<?> data : mItems) {
            if (position >= count && position < count + data.getItemCount()) {
                return data.getViewType(position - count);
            }
            count  += data.getItemCount();
        }
        throw new IllegalArgumentException("getViewType illegal position=" + position);
    }

    @Override
    public boolean hasViewType(int viewType) {
        for (MultiData<?> data : mItems) {
            if (data.hasViewType(viewType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public View onCreateView(Context context, ViewGroup container, int viewType) {
        for (MultiData<?> data : mItems) {
            if (data.hasViewType(viewType)) {
                return data.onCreateView(context, container, viewType);
            }
        }
        return null;
    }



    @Override
    public boolean load(int start, int end, MultiSceneAdapter adapter) {
        if (getAdapter() == null) {
            setAdapter(adapter);
        }
        if (hasMore()) {

            int offset = 0;
            MultiData<?> loadingData = null;
            for (MultiData<?> data : mItems) {
                if (end < offset) {
                    break;
                }

                int max = offset + data.getItemCount();
                Log.d(TAG, "onLoadMore start=" + start + " end=" + end + " offset=" + offset + " max=" + max + " data=" + data);

                if (max <= start) {
                    offset = max;
                    continue;
                }

                if (data.hasMore() && data.load(Math.max(0, start - offset), end - offset, adapter)) {
                    loadingData = data;
                }
                offset = max;
            }

            Log.e(TAG, "onLoadMore loadingData=" + loadingData);
            return loadingData != null;
        }
        return false;
    }

    @Override
    protected final boolean loadData() {
        // TODO 动态加载MultiData
        return false;
    }

    @Override
    public void onItemSticky(EasyViewHolder holder, int position, boolean isSticky) {
        int count = 0;
        for (MultiData<?> data : mItems) {
            if (position >= count && position < count + data.getItemCount()) {
                data.onItemSticky(holder, position, isSticky);
                break;
            }
            count  += data.getItemCount();
        }
    }

    @Override
    public boolean isStickyPosition(int position) {
        int count = 0;
        for (MultiData<?> data : mItems) {
            if (position >= count && position < count + data.getItemCount()) {
                return data.isStickyPosition(position);
            }
            count  += data.getItemCount();
        }
        throw new IllegalArgumentException("isStickyPosition illegal position=" + position);
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<MultiData<?>> list, int position, List<Object> payloads) {
        int count = 0;
        for (MultiData<?> data : list) {
            if (position >= count && position < count + data.getItemCount()) {
                data.onBindViewHolder(holder, position - count, payloads);
                break;
            }
            count  += data.getItemCount();
        }
    }

    @Override
    public void setAdapter(MultiSceneAdapter adapter) {
        super.setAdapter(adapter);
        for (MultiData<?> data : mItems) {
            data.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount() {

        int count = 0;
        for (MultiData<?> data : mItems) {
            count += data.getItemCount();
        }

        return count;
    }




    @Override
    public final int getLayoutId(int viewType) {
        return 0;
    }

    @Override
    public final int getColumnCount(int viewType) {
        return super.getColumnCount(viewType);
    }

    @Override
    public final int getMaxColumnCount() {
        return super.getMaxColumnCount();
    }

}
