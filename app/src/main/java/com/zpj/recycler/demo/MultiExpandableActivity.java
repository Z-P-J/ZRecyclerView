package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.zpj.recycler.demo.mutildata.ExpandableMultiData;
import com.zpj.recyclerview.MultiSceneRecycler;
import com.zpj.recyclerview.core.MultiSceneLayoutManager;
import com.zpj.recyclerview.core.Scene;
import com.zpj.recyclerview.scene.VerticalScene;
import com.zpj.statemanager.State;

import java.util.ArrayList;
import java.util.List;

public class MultiExpandableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        List<Scene> list = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            list.add(new VerticalScene(new MyExpandMultiData("title_" + i)));
        }


        MultiSceneRecycler.with((RecyclerView) findViewById(R.id.recycler_view))
                .setItems(list)
                .setLayoutManager(new MultiSceneLayoutManager())
//                .addItemDecoration(new StickyHeaderItemDecoration())
                .build();

    }

    private static class MyExpandMultiData extends ExpandableMultiData {

        public MyExpandMultiData(String title) {
            super(title);
            setState(State.STATE_LOADING);
        }

        @Override
        public boolean loadData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep((long) (5000 * Math.random()));
                        int size = Math.min(10, (int) (100 * Math.random()));
                        for (int i = 0; i < size; i++) {
                            mData.add(i);
                        }
                        showContent();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        showError();
                    }
                }
            }).start();
            return false;
        }
    }

}
