package com.zpj.recycler.demo.mutildata;

import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;

import java.util.List;

public class TestErrorStringMultiData extends BaseHeaderMultiData<String> {

    private boolean isFirst = true;

    public TestErrorStringMultiData(String title) {
        super(title);
    }

    public TestErrorStringMultiData(String title, List<String> list) {
        super(title, list);
    }

    @Override
    public int getChildColumnCount(int viewType) {
        return getMaxColumnCount();
    }

    @Override
    public int getMaxColumnCount() {
        return 4;
    }

    @Override
    public boolean loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if (isFirst) {
                        isFirst = false;
                        showError();
//                        state = State.STATE_ERROR;
//                        notifyItemRangeInserted();
                        return;
                    }
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
