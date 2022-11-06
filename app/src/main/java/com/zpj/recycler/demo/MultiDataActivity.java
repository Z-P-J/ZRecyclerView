package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.zpj.recycler.demo.mutildata.IntMultiData;
import com.zpj.recycler.demo.mutildata.RecyclerViewHeaderMultiData;
import com.zpj.recycler.demo.mutildata.StringMultiData;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData2;
import com.zpj.recycler.demo.mutildata.TestErrorStringMultiData;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.footer.SimpleFooterViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MultiDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        List<MultiData<?>> list = new ArrayList<>();
        list.add(new TestErrorStringMultiData("StringMultiData0"));

        list.add(new StringSingleTypeMultiData2());

        list.add(new StringMultiData("StringMultiData1"));
        list.add(new RecyclerViewHeaderMultiData("Recycler1"));

        list.add(new IntMultiData("IntMultiData"));
        list.add(new StringMultiData("StringMultiData2"));

        list.add(new RecyclerViewHeaderMultiData("Recycler2"));

        list.add(new StringSingleTypeMultiData());
        list.add(new RecyclerViewHeaderMultiData("Recycler3"));
        list.add(new StringSingleTypeMultiData());
        list.add(new RecyclerViewHeaderMultiData("Recycler4"));
        list.add(new StringSingleTypeMultiData());
        list.add(new RecyclerViewHeaderMultiData("Recycler5"));
        list.add(new StringSingleTypeMultiData());
        list.add(new StringSingleTypeMultiData());
        list.add(new StringSingleTypeMultiData());
        list.add(new StringSingleTypeMultiData());
        list.add(new StringSingleTypeMultiData());
        list.add(new StringSingleTypeMultiData());

        MultiRecycler.with((RecyclerView) findViewById(R.id.recycler_view))
                .setItems(list)
                .setHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header, null, false))
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
                .build();

    }

}
