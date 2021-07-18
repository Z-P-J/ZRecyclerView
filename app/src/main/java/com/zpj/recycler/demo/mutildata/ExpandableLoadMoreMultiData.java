package com.zpj.recycler.demo.mutildata;

import java.util.List;

public class ExpandableLoadMoreMultiData extends ExpandableMultiData {

    public ExpandableLoadMoreMultiData(String title) {
        super(title);
    }

    public ExpandableLoadMoreMultiData(String title, List<Integer> list) {
        super(title, list);
    }

    @Override
    public boolean loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < 10; i++) {
                        mData.add(i);
                    }
                    showContent();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    showError();
                }
            }
        }).start();
        return false;
    }

}
