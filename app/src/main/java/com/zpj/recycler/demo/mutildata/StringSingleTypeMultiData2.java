package com.zpj.recycler.demo.mutildata;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.R;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.SingleTypeMultiData;

import java.util.List;

public class StringSingleTypeMultiData2 extends SingleTypeMultiData<String> {

    private boolean isFirst = true;

    public StringSingleTypeMultiData2() {
        super();
    }

    public StringSingleTypeMultiData2(List<String> list) {
        super(list);
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getLayoutId() {
        return R.layout.layout_text;
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
//                        showError();
                        Log.d("StringSingle", "showLogin");
                        showLogin();
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
