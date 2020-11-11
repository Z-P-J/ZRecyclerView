package com.zpj.recycler.demo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.HeaderMultiData;
import com.zpj.recyclerview.MultiAdapter;

import java.util.List;

public class StringMultiData extends BaseHeaderMultiData<String> {

    public StringMultiData(String title) {
        super(title);
    }

    @Override
    public int getChildSpanCount(int viewType) {
        return 1;
    }

    @Override
    public boolean loadData(final MultiAdapter adapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 8; i++) {
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
    public void onBindChild(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
        holder.setText(R.id.tv_text, "StringData position=" + position);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
