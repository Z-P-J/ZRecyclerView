package com.zpj.recycler.demo.mutildata;

import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;

import java.util.List;

public class ExpandableMultiData extends BaseExpandMultiData<Integer> {

    public ExpandableMultiData(String title) {
        super(title);
    }

    public ExpandableMultiData(String title, List<Integer> list) {
        super(title, list);
    }

    @Override
    public boolean loadData() {
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
