package com.zpj.recycler.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.zpj.recycler.demo.mutildata.IntMultiData;
import com.zpj.recycler.demo.mutildata.RecyclerViewHeaderMultiData;
import com.zpj.recycler.demo.mutildata.StringMultiData;
import com.zpj.recycler.demo.mutildata.StringMultiData2;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData;
import com.zpj.recycler.demo.mutildata.StringSingleTypeMultiData2;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecyclerViewWrapper;

import java.util.ArrayList;
import java.util.List;

public class MultiDataActivity extends AppCompatActivity {

    public static final int TYPE_TITLE = 111;
    public static final int TYPE_TEXT = 222;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        MultiRecyclerView recyclerView = findViewById(R.id.recycler_view);
//
//        List<MultiData> list = new ArrayList<>();
//        list.add(new HeaderData("测试"));
//        list.add(new StringData());
//        list.add(new HeaderData("标题1"));
//        list.add(new RecyclerData());
//        list.add(new HeaderData("标题2"));
//        list.add(new IntegerData());
//        list.add(new HeaderData("标题3"));
//        list.add(new StringData());
//
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//
//        recyclerView.init(list);


        List<MultiData<?>> list = new ArrayList<>();
        list.add(new StringMultiData2("StringMultiData0"));

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

        MultiRecyclerViewWrapper.with((RecyclerView) findViewById(R.id.recycler_view))
                .setData(list)
                .setHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header, null, false))
                .build();


//        List<MultiData> list = new ArrayList<>();
//
//        list.add(new TitleBarData("sdfgthkjdsejbdg"));
//
//        list.add(new HeaderData("测试"));
//        list.add(new StringData());
//        list.add(new HeaderData("标题1"));
//        list.add(new RecyclerData());
//
//        list.add(new HeaderData("标题2"));
//        list.add(new IntegerData());
//        list.add(new HeaderData("标题3"));
//        list.add(new StringData());
//
//
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//        list.add(new IntegerData());
//
//        MultiRecyclerViewWrapper wrapper = new MultiRecyclerViewWrapper((RecyclerView) findViewById(R.id.recycler_view));
//        wrapper.setData(list)
//                .setHeaderView(LayoutInflater.from(this).inflate(R.layout.item_header, null, false))
//                .setMaxSpan(4)
//                .build();

    }

//    public static class HeaderData extends MultiData<String> {
//
//        private String title;
//
//        public HeaderData(String title) {
//            this.title = title;
//        }
//
//        @Override
//        public int getViewType(int position) {
//            return TYPE_TITLE;
//        }
//
//        @Override
//        public int getSpanCount(int viewType) {
//            return 4;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            return R.layout.item_header;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            data.add(title);
//            adapter.notifyDataSetChanged();
//            return true;
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
//            holder.setText(R.id.tv_text, list.get(position));
//            holder.setOnItemClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), "HeaderData title=" + title, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

//    public static class TitleBarData extends MultiData<String> {
//
//
//        private String title;
//
//        public TitleBarData(String title) {
//            this.title = title;
//        }
//
//        @Override
//        public int getCount() {
//            if (isLoaded()) {
//                return data.size() + 1;
//            }
//            return super.getCount();
//        }
//
//        @Override
//        public int getSpanCount(int viewType) {
//            if (viewType == TYPE_TITLE) {
//                return 4;
//            }
//            return 1;
//        }
//
//        @Override
//        public int getViewType(int position) {
//            if (position == 0) {
//                return TYPE_TITLE;
//            }
//            return TYPE_TEXT;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            if (viewType == TYPE_TITLE) {
//                return R.layout.item_header;
//            } else if (viewType == TYPE_TEXT){
//                return R.layout.layout_text;
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean hasViewType(int viewType) {
//            return viewType == TYPE_TITLE || viewType == TYPE_TEXT;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        for (int i = 0; i < 64; i++) {
//                            data.add("" + i);
//                        }
//                        recyclerView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            return true;
//        }
//
//        @Override
//        public int getRealPosition(int position) {
//            if (position > 0) {
//                return --position;
//            }
//            return super.getRealPosition(position);
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
//            switch (holder.getViewType()) {
//                case TYPE_TITLE:
//                    holder.setText(R.id.tv_text, title);
//                    holder.setOnItemClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(v.getContext(), "HeaderData title=" + title, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    break;
//                case TYPE_TEXT:
//                    holder.setText(R.id.tv_text, list.get(position));
//                    holder.setOnItemClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    break;
//            }
//        }
//    }


//    public static class TitleBarIntegerData extends MultiData<Integer> {
//
//
//        private String title;
//
//        public TitleBarIntegerData(String title) {
//            this.title = title;
//        }
//
//        @Override
//        public int getCount() {
//            if (isLoaded()) {
//                return data.size() + 1;
//            }
//            return super.getCount();
//        }
//
//        @Override
//        public int getSpanCount(int viewType) {
//            if (viewType == TYPE_TITLE) {
//                return 4;
//            }
//            return 4;
//        }
//
//        @Override
//        public int getViewType(int position) {
//            if (position == 0) {
//                return TYPE_TITLE;
//            }
//            return TYPE_TEXT;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            if (viewType == TYPE_TITLE) {
//                return R.layout.item_header;
//            } else if (viewType == TYPE_TEXT){
//                return R.layout.layout_text;
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean hasViewType(int viewType) {
//            return viewType == TYPE_TITLE || viewType == TYPE_TEXT;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        for (int i = 0; i < 64; i++) {
//                            data.add(i);
//                        }
//                        recyclerView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            return true;
//        }
//
//        @Override
//        public int getRealPosition(int position) {
//            if (position > 0) {
//                return --position;
//            }
//            return super.getRealPosition(position);
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
//            switch (holder.getViewType()) {
//                case TYPE_TITLE:
//                    holder.setText(R.id.tv_text, title);
//                    holder.setOnItemClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(v.getContext(), "HeaderData title=" + title, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    break;
//                case TYPE_TEXT:
//                    holder.setText(R.id.tv_text, "IntegerData 第" + list.get(position) + "个");
//                    holder.setOnItemClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Toast.makeText(v.getContext(), "IntegerData position=" + position, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    break;
//            }
//        }
//    }

//    public static class IntegerData extends MultiData<Integer> {
//
//
//        @Override
//        public int getSpanCount(int viewType) {
//            return 4;
//        }
//
//        @Override
//        public int getViewType(int position) {
//            return TYPE_TEXT;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            return R.layout.layout_text;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        for (int i = 0; i < 10; i++) {
//                            data.add(i);
//                        }
//                        recyclerView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            return true;
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
//            holder.setText(R.id.tv_text, "IntegerData 第" + list.get(position) + "个");
//            holder.setOnItemClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), "IntegerData position=" + position, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

//    public static class StringData extends MultiData<String> {
//
//
//        @Override
//        public int getSpanCount(int viewType) {
//            return 1;
//        }
//
//        @Override
//        public int getViewType(int position) {
//            return TYPE_TEXT;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            return R.layout.layout_text;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        for (int i = 0; i < 8; i++) {
//                            data.add("" + i);
//                        }
//                        recyclerView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            return true;
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
//            holder.setText(R.id.tv_text, "StringData position=" + position);
//            holder.setOnItemClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

//    public static class RecyclerData extends MultiData<Integer> {
//
//        private EasyRecyclerView<Integer> recyclerView;
//
//        @Override
//        public int getCount() {
//            return isLoaded() ? 1 : 0;
//        }
//
//        @Override
//        public int getSpanCount(int viewType) {
//            return 4;
//        }
//
//        @Override
//        public int getLayoutId(int viewType) {
//            return R.layout.item_recycler;
//        }
//
//        @Override
//        public boolean loadData(final RecyclerView recyclerView, final MultiAdapter adapter) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(2000);
//                        for (int i = 0; i < 16; i++) {
//                            list.add(i);
//                        }
//                        recyclerView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//            return true;
//        }
//
//        @Override
//        public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
//            if (recyclerView == null) {
//                RecyclerView view = holder.getView(R.id.recycler_view);
//                recyclerView = new EasyRecyclerView<>(view);
//                recyclerView.setData(list)
//                        .setItemRes(R.layout.item_text_grid)
//                        .setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false))
//                        .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
//                            @Override
//                            public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
//                                holder.setText(R.id.tv_text, "RecyclerData position=" + position);
//                                holder.setOnItemClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Toast.makeText(v.getContext(), "RecyclerData position=" + position, Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                            }
//                        })
//                        .build();
//                recyclerView.showContent();
//            }
//        }
//    }

}
