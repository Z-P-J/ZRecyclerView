package com.zpj.recycler.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.SingleTypeMultiData;
import com.zpj.recyclerview.layouter.GridLayouter;
import com.zpj.recyclerview.layouter.HorizontalLayouter;
import com.zpj.recyclerview.layouter.Layouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;
import com.zpj.recyclerview.manager.MultiLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class LayoutManagerActivity extends AppCompatActivity {

    private MultiRecycler mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 57; i++) {
            list.add(i);
        }

        List<MultiData<?>> multiDataList = new ArrayList<>();
        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });
        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()));
        multiDataList.add(new LayouterMultiData(list, new GridLayouter(2)));
        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });
        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()));
        multiDataList.add(new LayouterMultiData(list, new GridLayouter(3)));

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mRecycler = new MultiRecycler(recyclerView, multiDataList);
        mRecycler.setLayoutManager(new MultiLayoutManager()).build();
    }

    public static class LayouterMultiData extends SingleTypeMultiData<Integer> {

        public LayouterMultiData(Layouter layouter) {
            super(layouter);
        }

        public LayouterMultiData(List<Integer> list, Layouter layouter) {
            super(list, layouter);
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public int getLayoutId() {
            return R.layout.item_text;
        }

        @Override
        public boolean loadData() {
            return false;
        }

        @Override
        public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
            holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
            final int data = list.get(position);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.getContext().startActivity(new Intent(holder.getContext(), MultiDataActivity.class));
//                        startActivity(new Intent(MainActivity.this, StateActivity3.class));
                    Toast.makeText(holder.getContext(), "第" + data + "个", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
