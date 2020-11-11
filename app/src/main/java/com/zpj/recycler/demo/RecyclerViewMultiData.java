//package com.zpj.recycler.demo;
//
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.widget.Toast;
//
//import com.zpj.recyclerview.EasyRecyclerView;
//import com.zpj.recyclerview.EasyViewHolder;
//import com.zpj.recyclerview.IEasy;
//import com.zpj.recyclerview.MultiAdapter;
//import com.zpj.recyclerview.MultiData;
//
//import java.util.List;
//
//public class RecyclerViewMultiData extends MultiData<Integer> {
//
//    private EasyRecyclerView<Integer> recyclerView;
//
//    @Override
//    public int getCount() {
//        return isLoaded() ? 1 : 0;
//    }
//
//    @Override
//    public int getSpanCount(int viewType) {
//        return 4;
//    }
//
//    @Override
//    public int getLayoutId(int viewType) {
//        return R.layout.item_recycler;
//    }
//
//    @Override
//    public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000);
//                    for (int i = 0; i < 16; i++) {
//                        list.add(i);
//                    }
//                    recyclerView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            adapter.notifyDataSetChanged();
//                        }
//                    });
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//        return false;
//    }
//
//    @Override
//    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
//        if (recyclerView == null) {
//            RecyclerView view = holder.getView(R.id.recycler_view);
//            recyclerView = new EasyRecyclerView<>(view);
//            recyclerView.setData(list)
//                    .setItemRes(R.layout.item_text_grid)
//                    .setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false))
//                    .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
//                        @Override
//                        public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
//                            holder.setText(R.id.tv_text, "RecyclerData position=" + position);
//                            holder.setOnItemClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Toast.makeText(v.getContext(), "RecyclerData position=" + position, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    })
//                    .build();
//            recyclerView.showContent();
//        }
//    }
//
//}
