package com.zpj.recycler.demo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.HeaderMultiData;
import com.zpj.recyclerview.MultiAdapter;

import java.util.List;

public class IntMultiData extends BaseHeaderMultiData<Integer> {

    public IntMultiData(String title) {
        super(title);
    }

    @Override
    public boolean loadData(final MultiAdapter adapter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 10; i++) {
                        list.add(i);
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
    public void onBindChild(EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
        holder.setText(R.id.tv_text, "IntegerData 第" + list.get(position) + "个");
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "IntegerData position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
