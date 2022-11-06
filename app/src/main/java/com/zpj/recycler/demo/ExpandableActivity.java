package com.zpj.recycler.demo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.zpj.recycler.demo.mutildata.ExpandableLoadMoreMultiData;
import com.zpj.recycler.demo.mutildata.ExpandableMultiData;
import com.zpj.recycler.demo.mutildata.TestErrorStringMultiData;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.decoration.StickyHeaderItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ExpandableActivity extends AppCompatActivity {

    public static final int TYPE_TITLE = 111;
    public static final int TYPE_TEXT = 222;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        List<MultiData<?>> list = new ArrayList<>();
        list.add(new TestErrorStringMultiData("StringMultiData0"));

        List<Integer> list1 = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            list1.add(i);
        }

        List<Integer> list2 = new ArrayList<>();
        for (int i = 100; i < 120; i++) {
            list2.add(i);
        }

        list.add(new ExpandableMultiData("Expandable111", list1));
        ExpandableMultiData expandableMultiData = new ExpandableMultiData("Expandable222", list2);
        expandableMultiData.setExpand(false);
        list.add(expandableMultiData);

        list.add(new ExpandableLoadMoreMultiData("IntExpandableMultiData1111111111111111111111"));

        ExpandableLoadMoreMultiData expandableLoadMoreMultiData = new ExpandableLoadMoreMultiData("IntExpandableMultiData22222222222222222222");
        expandableLoadMoreMultiData.setExpand(false);
        list.add(expandableLoadMoreMultiData);


        MultiRecycler.with((RecyclerView) findViewById(R.id.recycler_view))
                .setItems(list)
                .addItemDecoration(new StickyHeaderItemDecoration())
                .build();

    }

}
