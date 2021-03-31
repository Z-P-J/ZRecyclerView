# ZRecyclerView
 A RecyclerView which is implemented to make it easier to use.

 <div>
     <img src="./demo1.gif" width="30%">
     <img src="./demo2.gif" width="30%">
     <img src="./demo3.gif" width="30%">
 </div>

## Usage

step 1:
#### Latest Version：[![Download](https://api.bintray.com/packages/z-p-j/maven/ZRecyclerView/images/download.svg?version-1.0.0)](https://bintray.com/z-p-j/maven/ZRecyclerView/1.0.0/link)
```java
implementation 'com.zpj.widget:ZRecyclerView:1.2.1'
```

step 2:

```java
<com.zpj.recyclerview.EasyRecyclerLayout
        android:id="@+id/recycler_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
```

step 3:

```java
        recyclerLayout = findViewById(R.id.recycler_layout);
        recyclerLayout.setData(list)
                .setItemRes(R.layout.layout_text)
                .setEnableSelection(true)
                .setEnableSwipeRefresh(true)
                .setEnableLoadMore(true)
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        list.clear();
                        recyclerLayout.notifyDataSetChanged();
                    }
                })
                .setOnSelectChangeListener(new EasyRecyclerLayout.OnSelectChangeListener<Integer>() {
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
                .onLoadMore(new IEasy.OnLoadMoreListener() {
                    @Override
                    public boolean onLoadMore(EasyAdapter.Enabled enabled, int currentPage) {
                        for (int i = currentPage * 20; i < (currentPage + 1) * 20; i++) {
                            list.add(i);
                        }
                        recyclerLayout.notifyDataSetChanged();
                        return true;
                    }
                })
                .onBindViewHolder(new IEasy.OnBindViewHolderListener<Integer>() {
                    @Override
                    public void onBindViewHolder(EasyViewHolder holder, List<Integer> list, int position, List<Object> payloads) {
                        holder.setText(R.id.tv_text, "第" + list.get(position) + "个");
                    }
                })
                .onItemClick(new IEasy.OnItemClickListener<Integer>() {
                    @Override
                    public void onClick(EasyViewHolder holder, View view, Integer data) {
                        Toast.makeText(MainActivity.this, "第" + data + "个", Toast.LENGTH_SHORT).show();
                    }
                })
                .onItemLongClick(new IEasy.OnItemLongClickListener<Integer>() {
                    @Override
                    public boolean onLongClick(EasyViewHolder holder, View view, Integer data) {
                        recyclerLayout.getSelectedSet().add(data);
                        recyclerLayout.enterSelectMode();
                        return true;
                    }
                })
                .build();
```


## MultiData的使用（TODO）
为了使复杂布局的实现更简单，我自创了一种MultiData的方式来实现复杂布局，支持多种实体类，结合MultiRecyclerViewWrapper来方便我们使用。
#### 1. 创建MultiRecyclerViewWrapper对象
~~~java
List<MultiData<?>> list = new ArrayList<>();
MultiRecyclerViewWrapper wrapper = MultiRecyclerViewWrapper.with(recyclerView);
~~~
#### 2. 自定义MultiData
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
