package com.zpj.recycler.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recyclerview.EasyAdapter;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.recyclerview.SelectableRecycler;
import com.zpj.recyclerview.footer.SimpleFooterViewHolder;
import com.zpj.recyclerview.refresh.IRefresher;

import java.util.List;

public class RecyclerActivity extends AppCompatActivity {

    private SelectableRecycler<Integer> mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        mRecycler = new SelectableRecycler<>((RecyclerView) findViewById(R.id.recycler_view));
        mRecycler.setEnableSelection(true)
                .setMaxSelectCount(3)
//                .setEnableLoadMore(true)
                .setEnableLoadMore(true)
                .setItemRes(R.layout.layout_text)
                .onRefresh(new IRefresher.OnRefreshListener() {
                    @Override
                    public void onRefresh(IRefresher refresher) {
                        mRecycler.clearDataSet();
                        mRecycler.notifyDataSetChanged();
                    }
                })
                .setOnSelectChangeListener(new IEasy.OnSelectChangeListener<Integer>() {
                    @Override
                    public void onSelectModeChange(boolean selectMode) {

                    }

                    @Override
                    public void onSelectChange(List<Integer> list, int position, boolean isChecked) {

                    }

                    @Override
                    public void onSelectAll() {

                    }

                    @Override
                    public void onUnSelectAll() {

                    }

                    @Override
                    public void onSelectOverMax(int maxSelectCount) {
                        Toast.makeText(RecyclerActivity.this, "最多只能选择" + maxSelectCount + "项", Toast.LENGTH_SHORT).show();
                    }

//                    @Override
//                    public boolean isSelected(Integer item, int position) {
//                        return false;
//                    }
                })

//                .setFooterViewBinder(new SimpleFooterViewHolder(R.layout.layout_loading_footer, R.layout.layout_text))
                .setFooterViewBinder(new SimpleFooterViewHolder(R.layout.layout_loading_footer, R.layout.layout_error_footer) {

                    @Override
                    public void onShowHasNoMore() {
                        super.onShowHasNoMore();
                        showInfo("没有更多了！");
                    }

                    @Override
                    public void onShowError(String msg) {
                        super.onShowError(msg);
                        showInfo("出错了！" + msg);
                    }

                    private void showInfo(String msg) {
                        TextView tvInfo = textView.findViewById(R.id.tv_info);
                        tvInfo.setText(msg);
                    }

                })
                .onLoadMore(new IEasy.OnLoadMoreListener() {
                    @Override
                    public boolean onLoadMore(final EasyAdapter.Enabled enabled, final int currentPage) {
                        if (mRecycler.getCount() >= 40) {
                            mRecycler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mRecycler.showErrorView("hhhhhhhhhh");
//                                    recyclerLayout.showNoNetworkView("dfghj");
//                                    recyclerLayout.showError();
                                }
                            }, 1000);
                            return true;
//                            return false;
                        }
                        // 模拟数据加载
                        mRecycler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = currentPage * 20; i < (currentPage + 1) * 20; i++) {
                                    mRecycler.addData(i);
                                }
                                mRecycler.notifyDataSetChanged();
                            }
                        }, 1000);
                        return true;
                    }
                })
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        holder.setText(R.id.tv_text, "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111第" + list.get(position) + "个");
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() {
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        startActivity(new Intent(RecyclerActivity.this, MultiDataActivity.class));
//                        startActivity(new Intent(MainActivity.this, StateActivity3.class));
                        Toast.makeText(RecyclerActivity.this, "第" + data + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .onItemLongClick(new IEasy.OnItemLongClickListener<Integer>() {
                    @Override
                    public boolean onLongClick(EasyViewHolder holder, View view, Integer data) {
                        mRecycler.addSelectedPosition(holder.getRealPosition());
                        mRecycler.enterSelectMode();
                        return true;
                    }
                })
                .build();
//        recyclerLayout.showLoading();
//        recyclerLayout.showErrorView("test");

    }

    @Override
    public void onBackPressed() {
        if (mRecycler.isSelectMode()) {
            mRecycler.exitSelectMode();
        } else {
            super.onBackPressed();
        }
    }
}
