package com.zpj.recycler.demo.manager;

import android.view.View;
import android.widget.Toast;

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
    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
        holder.setText(R.id.tv_text, "StringData position=" + position);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
