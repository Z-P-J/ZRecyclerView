# ZRecyclerView
 A RecyclerView library which is implemented to make it easier to use.
 安卓RecyclerView框架，无侵入式设计，支持头布局、尾布局，状态切换、上拉加载更多等功能。独创MultiData，让你更容易实现复杂布局、多布局。

 <div>
     <img src="./demo1.gif" width="24%">
     <img src="./demo2.gif" width="24%">
     <img src="./demo3.gif" width="24%">
     <img src="./demo4.gif" width="24%">
 </div>

 ## 1. Gradle

 #### Latest Version：[![](https://jitpack.io/v/Z-P-J/ZRecyclerView.svg)](https://jitpack.io/#Z-P-J/ZRecyclerView)
 ```text
 implementation 'com.github.Z-P-J:ZRecyclerView:1.0.0'
 ```

## 2. EasyRecyclerView的使用（无侵入式）

step 1:

```xml
<android.support.v7.widget.RecyclerView
        android:id="@+id/recycler_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
```

step 2:
```java
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        EasyRecyclerView<Integer> easyRecyclerView = new EasyRecyclerView<>(recyclerView);
        easyRecyclerView.setData(list) // 设置数据
                .setItemRes(R.layout.layout_text) // 设置item
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() { // 绑定viewholder
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() { // item点击
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        
                    }
                })
                .onItemLongClick(new IEasy.OnItemLongClickListener<Integer>() { // item长按   
                    @Override
                    public boolean onLongClick(EasyViewHolder holder, View view, Integer data) {
                        return false;
                    }
                })
                .build();
        easyRecyclerView.showContent(); // 显示内容
        easyRecyclerView.showLoading(); // 显示加载中
        easyRecyclerView.showEmpty(); // 显示数据为空
        easyRecyclerView.showError(); // 显示加载出错
        easyRecyclerView.showNoNetwork(); // 显示无网络
```

## 3. EasyRecyclerLayout的使用（封装了SwipeRefreshLayout，侵入式）

step 1:

```xml
<com.zpj.recyclerview.EasyRecyclerLayout
        android:id="@+id/recycler_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
```

step 2:

```java
        recyclerLayout = findViewById(R.id.recycler_layout);
        recyclerLayout.setData(list)
                .setItemRes(R.layout.layout_text) // 布局layout
                .setEnableSelection(true) // 是否支持选择模式
                .setEnableSwipeRefresh(true) // 是否支持下拉刷新
                .setEnableLoadMore(true) // 是否支持加载更多
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() { // 下拉刷新回调
                    @Override
                    public void onRefresh() {
                        list.clear();
                        recyclerLayout.notifyDataSetChanged();
                    }
                })
                .setOnSelectChangeListener(new EasyRecyclerLayout.OnSelectChangeListener<Integer>() { // 选择模式回调
                    @Override
                    public void onSelectModeChange(boolean selectMode) {

                    }

                    @Override
                    public void onChange(List<Integer> list, int position, boolean isChecked) {

                    }

                    @Override
                    public void onSelectAll() {

                    }

                    @Override
                    public void onUnSelectAll() {

                    }
                })
                .onLoadMore(new IEasy.OnLoadMoreListener() { // 加载更多
                    @Override
                    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
                        for (int i = currentPage * 20; i < (currentPage + 1) * 20; i++) {
                            list.add(i);
                        }
                        recyclerLayout.notifyDataSetChanged();
                        return true;
                    }
                })
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() { // 绑定viewholder
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() { // item点击回调
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        Toast.makeText(MainActivity.this, "第" + data + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .onItemLongClick(new IEasy.OnItemLongClickListener<Integer>() { // item长按回调
                    @Override
                    public boolean onLongClick(EasyViewHolder holder, View view, Integer data) {
                        recyclerLayout.getSelectedSet().add(data);
                        recyclerLayout.enterSelectMode();
                        return true;
                    }
                })
                .build();

        recyclerLayout.showContent(); // 显示内容
        recyclerLayout.showLoading(); // 显示加载中
        recyclerLayout.showEmpty(); // 显示数据为空
        recyclerLayout.showError(); // 显示加载出错
        recyclerLayout.showNoNetwork(); // 显示无网络
```


## 4. MultiData的使用（TODO完善文档，可参考[MultiDataActivity](https://github.com/Z-P-J/ZRecyclerView/blob/master/app/src/main/java/com/zpj/recycler/demo/MultiDataActivity.java)）

#### 4.1 什么是MultiData?

为了使复杂布局的实现更简单，我自创了一种MultiData的方式来实现复杂布局，支持多种实体类，结合MultiRecyclerViewWrapper来方便我们使用。
MultiData你可以理解为比RecyclerView.Adapter更小的RecyclerView.Adapter，在MultiData中可以实现RecyclerView.Adapter的逻辑，比如onBindViewHolder、getViewType、getCount、notifyDataSetChange等

#### 4.1 创建MultiRecyclerViewWrapper对象
~~~java
List<MultiData<?>> list = new ArrayList<>();
MultiRecyclerViewWrapper wrapper = MultiRecyclerViewWrapper.with(recyclerView);
~~~
#### 4.2 自定义MultiData
MultiData是一个抽象类，我们需要继承它并实现一些方法
~~~java
/*
加载数据，一般在子线程中加载数据
return：true:表示还有更多数据，false:数据加载完成
*/
public boolean loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 16; i++) {
                        list.add("" + i);
                    }
                    showContent();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return false;
}
~~~
#### 4.3 todo完善文档