package com.zpj.recycler.demo.mutildata;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.ExpandableMultiData;

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
        Log.d("StickyHeaderItem1", "payloads=" + payloads);
        final TextView tvText = holder.getView(R.id.tv_text);
        final ImageView ivArrow = holder.getView(R.id.iv_arrow);
        ivArrow.setVisibility(View.VISIBLE);
        tvText.setText(title);
        setIcon(ivArrow);
        holder.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpand()) {
                    collapse();
                } else {
                    expand();
                }
                setIcon(ivArrow);
                scrollToPosition(0);
            }
        });
    }

    private void setIcon(ImageView ivArrow) {
        int id = isExpand() ? R.drawable.ic_baseline_keyboard_arrow_down_24 : R.drawable.ic_baseline_keyboard_arrow_right_24;
        ivArrow.setImageResource(id);
    }

//    @Override
//    protected void onStateChange(EasyViewHolder headerHolder, boolean isExpand) {
//
//    }

}
