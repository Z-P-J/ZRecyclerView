package com.zpj.recyclerview;

import java.util.List;

public interface ItemLoader<T> {

    void onLoad(List<T> items, MultiData.LoadCallback callback);

}
