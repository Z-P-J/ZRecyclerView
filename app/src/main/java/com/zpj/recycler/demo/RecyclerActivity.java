package com.zpj.recycler.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recyclerview.EasyAdapter;
import com.zpj.recyclerview.EasyRecyclerLayout;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.recyclerview.footer.SimpleFooterViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RecyclerActivity extends AppCompatActivity {

    private final List<Integer> list = new ArrayList<>();

    private EasyRecyclerLayout<Integer> recyclerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        recyclerLayout = findViewById(R.id.recycler_layout);
        recyclerLayout.setData(list)
                .setItemRes(R.layout.layout_text)
//                .onCreateViewHolder(new IEasy.OnCreateViewHolderListener<Integer>() {
//                    @Override
//                    public View onCreateViewHolder(ViewGroup parent, int layoutRes, int viewType) {
//                        Log.d("onCreateViewHolder", "onCreateViewHolder");
//                        return LayoutInflater.from(parent.getContext())
//                                .inflate(R.layout.layout_text, parent, false);
//                    }
//                })
                .setEnableSelection(true)
                .setMaxSelectCount(3)
                .setEnableSwipeRefresh(true)
//                .setEnableLoadMore(true)
                .setEnableLoadMore(true)
//                .setLayoutManager(new GridLayoutManager(this, 3))
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        list.clear();
                        recyclerLayout.notifyDataSetChanged();
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
                        if (list.size() >= 40) {
                            recyclerLayout.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    recyclerLayout.showErrorView("hhhhhhhhhh");
//                                    recyclerLayout.showNoNetworkView("dfghj");
//                                    recyclerLayout.showError();
                                }
                            }, 1000);
                            return true;
//                            return false;
                        }
                        // 模拟数据加载
                        recyclerLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = currentPage * 20; i < (currentPage + 1) * 20; i++) {
                                    list.add(i);
                                }
                                recyclerLayout.notifyDataSetChanged();
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
                        recyclerLayout.addSelectedPosition(holder.getRealPosition());
                        recyclerLayout.enterSelectMode();
                        return true;
                    }
                })
                .build();
//        recyclerLayout.showLoading();
//        recyclerLayout.showErrorView("test");
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
