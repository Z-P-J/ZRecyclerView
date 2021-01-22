package com.zpj.recycler.demo.mutildata;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.ExpandableMultiData;
import com.zpj.recyclerview.HeaderMultiData;

import java.util.List;

public abstract class BaseExpandMultiData<T> extends ExpandableMultiData<T> {

    protected final String title;

    public BaseExpandMultiData(String title) {
        super();
        this.title = title;
    }

    public BaseExpandMultiData(String title, List<T> list) {
        super(list);
        this.title = title;
    }

    @Override
    public int getHeaderLayoutId() {
        return R.layout.item_header;
    }

    @Override
    public int getHeaderViewType() {
        return BaseExpandMultiData.class.hashCode();
    }

    @Override
    public int getChildViewType(int position) {
        return R.layout.layout_text;
    }

    @Override
    public int getChildLayoutId(int viewType) {
        return R.layout.layout_text;
    }

    @Override
    public void onBindHeader(EasyViewHolder holder, List<Object> payloads) {
        final TextView tvText = holder.getView(R.id.tv_text);
        tvText.setText(title);
        setIcon(tvText);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpand()) {
                    collapse();
                } else {
                    expand();
                }
                setIcon(tvText);
            }
        });
    }

    private void setIcon(TextView textView) {
        int id = isExpand() ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_right_24;
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, id, 0);
    }

//    @Override
//    protected void onStateChange(EasyViewHolder headerHolder, boolean isExpand) {
//
//    }

}
