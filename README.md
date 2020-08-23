# ZRecyclerView
 A RecyclerView which is implemented to make it easier to use.

## Usage

step 1:

```java
implementation 'com.zpj.widget:ZRecyclerView:1.0.8'
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
