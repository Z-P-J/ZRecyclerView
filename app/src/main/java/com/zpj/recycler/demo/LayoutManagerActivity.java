package com.zpj.recycler.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.zpj.recycler.demo.layouter.GridLayouter;
import com.zpj.recycler.demo.layouter.HorizontalLayouter;
import com.zpj.recycler.demo.layouter.VerticalLayouter;
import com.zpj.recycler.demo.manager.LayouterMultiData;
import com.zpj.recycler.demo.manager.MultiLayoutManager;
import com.zpj.recycler.demo.manager.StackLayoutManager;
import com.zpj.recyclerview.EasyRecycler;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.IEasy;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.refresh.IRefresher;

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

        mRecycler = new MultiRecycler((RecyclerView) findViewById(R.id.recycler_view), multiDataList);
        mRecycler.setLayoutManager(new MultiLayoutManager(multiDataList))
                .build();
    }

}
