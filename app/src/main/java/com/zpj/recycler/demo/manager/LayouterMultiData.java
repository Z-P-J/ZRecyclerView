package com.zpj.recycler.demo.manager;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.LayoutManagerActivity;
import com.zpj.recycler.demo.MultiDataActivity;
import com.zpj.recycler.demo.R;
import com.zpj.recycler.demo.layouter.Layouter;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.SingleTypeMultiData;

import java.util.List;

public class LayouterMultiData extends SingleTypeMultiData<Integer> {

    private final Layouter mLayouter;

    public LayouterMultiData(Layouter layouter) {
        super();
        this.mLayouter = layouter;
    }

    public LayouterMultiData(List<Integer> list, Layouter layouter) {
        super(list);
        this.mLayouter = layouter;
    }

    public Layouter getLayouter() {
        return mLayouter;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_text;
    }

    @Override
    public boolean loadData() {
        return false;
    }

    @Override
    public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
        holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
        final int data = list.get(position);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.getContext().startActivity(new Intent(holder.getContext(), MultiDataActivity.class));
//                        startActivity(new Intent(MainActivity.this, StateActivity3.class));
                Toast.makeText(holder.getContext(), "第" + data + "个", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
