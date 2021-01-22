package com.zpj.recycler.demo.mutildata;

import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.HeaderMultiData;

import java.util.List;

public abstract class BaseHeaderMultiData2<T> extends HeaderMultiData<T> {

    protected final String title;

    public BaseHeaderMultiData2(String title) {
        super();
        this.title = title;
    }

    public BaseHeaderMultiData2(String title, List<T> list) {
        super(list);
        this.title = title;
    }

    @Override
    public int getHeaderLayoutId() {
        return R.layout.item_header;
    }

    @Override
    public int getChildViewType(int position) {
        return R.layout.layout_text;
    }

    @Override
    public boolean hasChildViewType(int viewType) {
        return viewType == R.layout.layout_text;
    }

    @Override
    public int getChildLayoutId(int viewType) {
        return R.layout.layout_text;
    }

    @Override
    public void onBindHeader(EasyViewHolder holder, List<Object> payloads) {
        holder.setText(R.id.tv_text, title);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "HeaderData title=" + title, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
