package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.zpj.recycler.demo.mutildata.MyDragAndSwipeMultiData;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecyclerViewWrapper;
import com.zpj.recyclerview.footer.SimpleFooterViewHolder;

import java.util.ArrayList;
import java.util.List;

public class DragActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        List<MultiData<?>> list = new ArrayList<>();

        list.add(new StringSingleTypeMultiData());
        list.add(new MyDragAndSwipeMultiData());
        list.add(new StringSingleTypeMultiData());

        MultiRecyclerViewWrapper.with((RecyclerView) findViewById(R.id.recycler_view))
                .setData(list)
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
