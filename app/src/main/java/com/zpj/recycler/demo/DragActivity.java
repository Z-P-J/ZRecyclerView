package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zpj.recycler.demo.mutildata.MyDragAndSwipeMultiData;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData;
import com.zpj.recyclerview.IRefresh;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecyclerViewWrapper;
import com.zpj.recyclerview.RefreshViewHolder;
import com.zpj.recyclerview.RefreshViewHolder2;
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
        list.add(new MyDragAndSwipeMultiData() {

            @Override
            public int getColumnCount(int viewType) {
                return getMaxColumnCount();
            }

            @Override
            public int getMaxColumnCount() {
                return 2;
            }
        });

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
//        recyclerView.setOnTouchListener(new View.OnTouchListener() {
//            private float downX;
//            private float downY;
//            private boolean isMoveDown;
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int action = event.getAction();
//                if (MotionEvent.ACTION_DOWN == action) {
//                    isMoveDown = false;
//                    downX = event.getRawX();
//                    downY = event.getRawY();
//                    return false;
//                } else if (MotionEvent.ACTION_MOVE == action) {
//                    float deltaX = event.getRawX() - downX;
//                    float deltaY = event.getRawY() - downY;
//                    if (isMoveDown || (deltaY > 0 && !recyclerView.canScrollVertically(-1))) {
//                        isMoveDown = true;
//                        View view = recyclerView.getChildAt(0);
//                        view.setTranslationY(deltaY / 2);
//                        event.setAction(MotionEvent.ACTION_DOWN);
////                        recyclerView.onTouchEvent(event);
//                        return false;
//                    }
//                } else if (MotionEvent.ACTION_UP == action) {
//                    if (isMoveDown) {
//                        isMoveDown = false;
//                        recyclerView.getChildAt(0).animate().translationY(0).setDuration(360).start();
////                        recyclerView.onTouchEvent(event);
//                        return false;
//                    }
//                }
//                return false;
//            }
//        });
        MultiRecyclerViewWrapper.with(recyclerView)
                .setMultiData(list)
                .onRefresh(new RefreshViewHolder(), new IRefresh.OnRefreshListener() {
                    @Override
                    public void onRefresh(IRefresh refresh) {
                        Toast.makeText(DragActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        }, 500);
                    }
                })
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
