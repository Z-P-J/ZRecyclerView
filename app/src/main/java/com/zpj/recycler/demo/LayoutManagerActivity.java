package com.zpj.recycler.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.manager.MultiLayoutManager;
import com.zpj.recycler.demo.manager.StackLayoutManager;
import com.zpj.recyclerview.EasyRecycler;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.ArrayList;
import java.util.List;

public class LayoutManagerActivity extends AppCompatActivity {

    private EasyRecycler<Integer> mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            list.add(i);
        }

        mRecycler = new EasyRecycler<>((RecyclerView) findViewById(R.id.recycler_view), list);
        mRecycler.setItemRes(R.layout.item_text)
                .setLayoutManager(new MultiLayoutManager())
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() {
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        startActivity(new Intent(LayoutManagerActivity.this, MultiDataActivity.class));
//                        startActivity(new Intent(MainActivity.this, StateActivity3.class));
                        Toast.makeText(LayoutManagerActivity.this, "第" + data + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }

}
