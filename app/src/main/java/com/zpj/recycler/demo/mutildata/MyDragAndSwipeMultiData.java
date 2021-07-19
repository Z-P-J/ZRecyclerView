package com.zpj.recycler.demo.mutildata;

import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.DragAndSwipeMultiData;
import com.zpj.recyclerview.EasyViewHolder;

import java.util.List;

public class MyDragAndSwipeMultiData extends DragAndSwipeMultiData<String> {

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.layout_text;
    }

    @Override
    public boolean hasViewType(int viewType) {
        return super.hasViewType(viewType);
    }

    @Override
    public boolean loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 16; i++) {
                        mData.add("" + i);
                    }
                    showContent();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return false;
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, final List<String> list, final int position, List<Object> payloads) {
        final String data = list.get(position);
        holder.setText(R.id.tv_text, "DragAndSwipe position=" + data);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "DragAndSwipe position=" + data, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
