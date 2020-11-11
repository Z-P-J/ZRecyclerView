package com.zpj.recycler.demo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiAdapter;
import com.zpj.recyclerview.SingleTypeMultiData;

import java.util.List;

public class StringSingleTypeMultiData extends SingleTypeMultiData<String> {

    @Override
    public int getSpanCount() {
        return 4;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_text;
    }

    @Override
    public boolean loadData(final MultiAdapter adapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 16; i++) {
                        list.add("" + i);
                    }
                    adapter.postNotifyDataSetChanged();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return false;
    }

    @Override
    public void onBindViewHolder(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
        holder.setText(R.id.tv_text, "StringData position=" + position);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
