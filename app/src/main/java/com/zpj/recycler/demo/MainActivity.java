package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zpj.recyclerview.EasyAdapter;
import com.zpj.recyclerview.EasyRecyclerLayout;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final List<Integer> list = new ArrayList<>();

    private EasyRecyclerLayout<Integer> recyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerLayout = findViewById(R.id.recycler_layout);
        recyclerLayout.setData(list)
//                .setItemRes(R.layout.layout_text)
                .onCreateViewHolder(new IEasy.OnCreateViewHolderListener<Integer>() {
                    @Override
                    public View onCreateViewHolder(ViewGroup parent, int layoutRes, int viewType) {
                        Log.d("onCreateViewHolder", "onCreateViewHolder");
                        return LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.layout_text, parent, false);
                    }
                })
                .setEnableSelection(true)
                .setEnableSwipeRefresh(true)
//                .setEnableLoadMore(true)
                .setEnableLoadMore(false)
                .setLayoutManager(new GridLayoutManager(this, 3))
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        list.clear();
                        recyclerLayout.notifyDataSetChanged();
                    }
                })
                .setOnSelectChangeListener(new EasyRecyclerLayout.OnSelectChangeListener<Integer>() {
                    @Override
                    public void onSelectModeChange(boolean selectMode) {

                    }

                    @Override
                    public void onChange(List<Integer> list, int position, boolean isChecked) {

                    }

                    @Override
                    public void onSelectAll() {

                    }

                    @Override
                    public void onUnSelectAll() {

                    }
                })
                .onLoadMore(new IEasy.OnLoadMoreListener() {
                    @Override
                    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
                        for (int i = currentPage * 20; i < (currentPage + 1) * 20; i++) {
                            list.add(i);
                        }
                        recyclerLayout.notifyDataSetChanged();
                        return true;
                    }
                })
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() {
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        Toast.makeText(MainActivity.this, "第" + data + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .onItemLongClick(new IEasy.OnItemLongClickListener<Integer>() {
                    @Override
                    public boolean onLongClick(EasyViewHolder holder, View view, Integer data) {
                        recyclerLayout.getSelectedSet().add(data);
                        recyclerLayout.enterSelectMode();
                        return true;
                    }
                })
                .build();
        recyclerLayout.showLoading();
    }

    @Override
    public void onBackPressed() {
        if (recyclerLayout.isSelectMode()) {
            recyclerLayout.exitSelectMode();
        } else {
            super.onBackPressed();
        }
    }
}
