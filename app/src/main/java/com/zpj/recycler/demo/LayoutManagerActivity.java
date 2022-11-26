package com.zpj.recycler.demo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zpj.recycler.demo.mutildata.BaseHeaderMultiData;
import com.zpj.recycler.demo.mutildata.TestContainerMultiData;
import com.zpj.recyclerview.EasyViewHolder;
import com.zpj.recyclerview.MultiData;
import com.zpj.recyclerview.MultiRecycler;
import com.zpj.recyclerview.SingleTypeMultiData;
import com.zpj.recyclerview.layouter.BannerLayouter;
import com.zpj.recyclerview.layouter.FlowLayouter;
import com.zpj.recyclerview.layouter.GridLayouter;
import com.zpj.recyclerview.layouter.HorizontalLayouter;
import com.zpj.recyclerview.layouter.StaggeredGridLayouter;
import com.zpj.recyclerview.layouter.VerticalLayouter;
import com.zpj.recyclerview.layouter.ViewPagerLayouter;
import com.zpj.recyclerview.manager.MultiLayoutManager;
import com.zpj.recyclerview.refresh.IRefresher;
import com.zpj.recyclerview.refresh.SimpleRefresher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LayoutManagerActivity extends AppCompatActivity {

    private static final String TAG = "LayoutManagerActivity";

    private final Integer[] flowArray = {
            1000000000, 10, 1000000, 1000, 10000, 1, 10, 10000000, 100, 1000000000, 100
    };

    private MultiRecycler mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layouter);

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 57; i++) {
            list.add(i);
        }


        List<MultiData<?>> multiDataList = new ArrayList<>();

//        final BannerLayouter bannerLayouter = new BannerLayouter();
//        bannerLayouter.setAutoPlayDuration(5000);
//        multiDataList.add(new TestContainerMultiData(list, bannerLayouter) {
//            @Override
//            public int getLayoutId() {
//                return R.layout.item_banner;
//            }
//        });
//
//        multiDataList.add(new TestContainerMultiData(list, new HorizontalLayouter()));
//
//        final ViewPagerLayouter viewPagerLayouter = new ViewPagerLayouter();
//        viewPagerLayouter.setPageTransformer(new ViewPagerLayouter.PageTransformer() {
//            @Override
//            public void transformPage(@NonNull View page, float position) {
//                Log.d(TAG, "transformPage pos=" + viewPagerLayouter.getPosition(page) + " position=" + position);
////                page.setCameraDistance(page.getHeight() * 10f);
////
////                if (position < 0) {
////                    page.setPivotX(page.getWidth());
////                } else {
////                    page.setPivotX(0);
////                }
////                page.setPivotY(page.getHeight() / 2f);
////
////                if (position < -1) {
////                    page.setAlpha(0f);
////                } else if (position <= 1) {
////                    page.setAlpha(1f);
////                    page.setRotationY(90 * position);
////                } else {
////                    page.setAlpha(0f);
////                }
//
//
//                if (position < -1) {
//                    page.setAlpha(0f);
//                } else if (position <= 1) {
//                    page.setAlpha(1f);
//                    page.setPivotX(page.getWidth() / 2f);
//                    page.setPivotY(page.getHeight());
//                    page.setRotation(15 * position);
//                } else {
//                    page.setAlpha(0f);
//                }
//            }
//        });
//        multiDataList.add(new TestContainerMultiData(list, viewPagerLayouter) {
//            @Override
//            public int getLayoutId() {
//                return R.layout.item_banner;
//            }
//        });
//
//        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()));














        final BannerLayouter bannerLayouter = new BannerLayouter();
        bannerLayouter.setAutoPlayDuration(5000);
        multiDataList.add(new LayouterMultiData(list, bannerLayouter) {
            @Override
            public int getLayoutId() {
                return R.layout.item_banner;
            }
        });

        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter(true)) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });

        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });

        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()));

        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });

        final ViewPagerLayouter viewPagerLayouter = new ViewPagerLayouter();
        viewPagerLayouter.setPageTransformer(new ViewPagerLayouter.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                Log.d(TAG, "transformPage pos=" + viewPagerLayouter.getPosition(page) + " position=" + position);
                if (position < -1) {
                    page.setAlpha(0f);
                } else if (position <= 1) {
                    page.setAlpha(1f);
                    page.setPivotX(page.getWidth() / 2f);
                    page.setPivotY(page.getHeight());
                    page.setRotation(15 * position);
                } else {
                    page.setAlpha(0f);
                }
            }
        });
        viewPagerLayouter.addOnPageChangeListener(new ViewPagerLayouter.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                Log.d(TAG, "onPageScrolled position=" + position + " offset=" + offset + " offsetPixels=" + offsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected position=" + position);
            }

            @Override
            public void onPageEnterEnd(int position) {
                Log.d(TAG, "onPageEnterEnd position=" + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "onPageScrollStateChanged state=" + state);
            }
        });
        multiDataList.add(new LayouterMultiData(list, viewPagerLayouter) {
            @Override
            public int getLayoutId() {
                return R.layout.item_banner;
            }
        });

        List<Integer> staggeredList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            staggeredList.addAll(Arrays.asList(flowArray));
        }
        multiDataList.add(new LayouterMultiData(staggeredList, new StaggeredGridLayouter(4)) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_card;
            }
        });

        multiDataList.add(new StringMultiData("测试", new VerticalLayouter()) {
//            @Override
//            public boolean isStickyItem(int position) {
//                return false;
//            }
        });
        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()) {
            @Override
            public boolean isStickyPosition(int position) {
                return position == 5 || position == 12 || position == 20;
            }

            @Override
            public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                super.onBindViewHolder(holder, list, position, payloads);
                holder.getItemView().setBackgroundColor(Color.TRANSPARENT);
                ViewCompat.setElevation(holder.getItemView(), 0);
            }

            @Override
            public void onItemSticky(EasyViewHolder holder, int position, final boolean isSticky) {
                super.onItemSticky(holder, position, isSticky);
                holder.getItemView().setBackgroundColor(isSticky ? Color.WHITE : Color.TRANSPARENT);
                ViewCompat.setElevation(holder.getItemView(), isSticky ? 20 : 0);
            }
        });
        multiDataList.add(new TestErrorStringMultiData(new VerticalLayouter()));


        List<Integer> flowList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            flowList.addAll(Arrays.asList(flowArray));
        }
        multiDataList.add(new LayouterMultiData(flowList, new FlowLayouter(20)) {
            @Override
            public int getLayoutId() {
                return R.layout.item_flow;
            }

            @Override
            public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
                final int data = list.get(position);
                holder.setOnItemClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(holder.getContext(), "" + data, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        multiDataList.add(new TestErrorStringMultiData(new GridLayouter(2)));
        multiDataList.add(new LayouterMultiData(list, new HorizontalLayouter()) {
            @Override
            public int getLayoutId() {
                return R.layout.item_text_grid;
            }
        });
        multiDataList.add(new LayouterMultiData(list, new VerticalLayouter()) {
            @Override
            public boolean isStickyPosition(int position) {
                return position == 0;
            }
        });
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
        mRecycler.setLayoutManager(new MultiLayoutManager())
                .onRefresh(new SimpleRefresher(), new IRefresher.OnRefreshListener() {
                    @Override
                    public void onRefresh(IRefresher refresher) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(LayoutManagerActivity.this, "刷新成功！", Toast.LENGTH_SHORT).show();
                                        mRecycler.notifyDataSetChanged();
                                    }
                                });
                            }
                        }).start();
                    }
                })
                .build();

        Button btn1 = findViewById(R.id.btn_1);
        Button btn2 = findViewById(R.id.btn_2);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                bannerLayouter.toggleAutoPlay();
//                viewPagerLayouter.setCurrentItem(9);
//                mRecycler.scrollToPosition(9);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecycler.smoothScrollToPosition(100);
            }
        });

    }

    public static class LayouterMultiData extends SingleTypeMultiData<Integer> {

        public LayouterMultiData(com.zpj.recyclerview.layouter.Layouter layouter) {
            super(layouter);
        }

        public LayouterMultiData(List<Integer> list, com.zpj.recyclerview.layouter.Layouter layouter) {
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

    public static class TestErrorStringMultiData extends LayouterMultiData {

        public TestErrorStringMultiData(com.zpj.recyclerview.layouter.Layouter layouter) {
            super(layouter);
            hasMore = false;
            showError();
        }

        @Override
        public boolean loadData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        for (int i = 0; i < 16; i++) {
                            mData.add(i);
                        }
                        showContent();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return false;
        }

        @Override
        public void onBindViewHolder(final EasyViewHolder holder, List<Integer> list, final int position, List<Object> payloads) {
            holder.setText(R.id.tv_text, "StringData position=" + position);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private static class StringMultiData extends BaseHeaderMultiData<String> {

//        public StringMultiData(String title) {
//            super(title);
//        }
//
//        public StringMultiData(String title, List<String> list) {
//            super(title, list);
//        }
//
//        public StringMultiData(String title, Layouter layouter) {
//            super(title, layouter);
//        }

        public StringMultiData(String title, com.zpj.recyclerview.layouter.Layouter layouter) {
            super(title, layouter);
            hasMore = false;
            showError();
        }

        @Override
        public int getChildColumnCount(int viewType) {
            return getMaxColumnCount();
        }

        @Override
        public int getMaxColumnCount() {
            return 4;
        }

        @Override
        public boolean loadData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        for (int i = 0; i < 16; i++) {
                            mData.add("" + i);
                        }
                        showContent();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            return false;
        }

        @Override
        public void onBindChild(EasyViewHolder holder, List<String> list, final int position, List<Object> payloads) {
            holder.setText(R.id.tv_text, "StringData position=" + position);
            holder.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "StringData position=" + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
